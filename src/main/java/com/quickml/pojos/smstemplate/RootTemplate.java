package com.quickml.pojos.smstemplate;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class RootTemplate<T> {
	public String flow_id;
	public String sender;
	public List<T> recipients;

	public enum FlowType {
		REGISTRATION,
		PAYMENT,
		NOTICE
	}
	@JsonIgnore
	public FlowType flowType;
}
