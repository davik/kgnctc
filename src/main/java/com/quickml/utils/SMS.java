package com.quickml.utils;

import java.io.IOException;
import java.util.EnumMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickml.pojos.Counter;
import com.quickml.pojos.smstemplate.RootTemplate;
import com.quickml.repository.CounterRepository;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SMS {
	ObjectMapper mapper = new ObjectMapper();
	EnumMap<RootTemplate.FlowType, String> flowIdMap =
			new EnumMap<RootTemplate.FlowType, String>(RootTemplate.FlowType.class);

	public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	OkHttpClient client = new OkHttpClient();
	
	String url = "https://api.msg91.com/api/v5/flow/";
	String authKey = "338581A4nxx2hRj5f325806P1";

	public SMS() {
		flowIdMap.put(RootTemplate.FlowType.REGISTRATION, "5f545d1e2bb121244559a3bd");
		flowIdMap.put(RootTemplate.FlowType.PAYMENT, "5f546c470b0b813f3f3d7335");
		flowIdMap.put(RootTemplate.FlowType.NOTICE, "5f54c95a9723df6c7265715e");
	}
	
	String post(String json) throws IOException {
	  RequestBody body = RequestBody.create(json, JSON);
	  Request request = new Request.Builder()
			  .addHeader("authkey", authKey)
			  .url(url)
			  .post(body)
			  .build();
	  try (Response response = client.newCall(request).execute()) {
	    return response.body().string();
	  }
	}

	public String send(RootTemplate template,
			String smsEnabled,
			String senderId,
			int smsProvisionCount,
			CounterRepository counterRepo) {
		System.out.println("SMS enabled" + smsEnabled);
		if (!smsEnabled.equals("YES")) {
			return "SMS is not enabled";
		}
		Counter smsCount = counterRepo.findOne("SMS");
		if (null == smsCount) {
			smsCount = new Counter();
			smsCount.id = "SMS";
			smsCount.nextId = 0;
		}
		if (smsProvisionCount - smsCount.nextId <= 0) {
			return "SMS limit exhausted. Provisioned: " + smsProvisionCount
					+ ", Used: " + smsCount.nextId;
		}
		if (smsProvisionCount - smsCount.nextId < template.recipients.size()) {
			return "Number of students is greater than SMS balance!";
		}
		template.flow_id = flowIdMap.get(template.flowType);
		template.sender = senderId;
		try {
			String json = mapper.writeValueAsString(template);
			System.out.println(json);
			try {
				String rsp = post(json);
				System.out.println(rsp);
				smsCount.nextId = smsCount.nextId + template.recipients.size();
				counterRepo.save(smsCount);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}