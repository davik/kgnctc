package com.quickml;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.quickml.pojos.Numbers;
import com.quickml.pojos.Student;



@Controller
public class WelcomeController {

	public WelcomeController(StudentRepository studRepo) {
		super();
		this.studRepo = studRepo;
	}

	// inject via application.properties
	@Value("${app.welcome.message}")
	private String MESSAGE = "";

	@Value("${app.welcome.title}")
	private String TITLE = "";
	
	@Autowired
	public final StudentRepository studRepo;

	@RequestMapping("/")
	public String welcome(Map<String, Object> model) {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		
		return "welcome";
	}

	// test 5xx errors
	@RequestMapping("/5xx")
	public String ServiceUnavailable() {
		throw new RuntimeException("ABC");
	}
	
	
	@RequestMapping(value = "/create", method=RequestMethod.POST)
    String getDescStat(Map<String, Object> model,
    		@RequestBody Student student) throws IOException{
		System.out.println("Rest received");
		System.out.println(student.name);	

    	studRepo.save(student);
    	return "create";
    }
	
	@RequestMapping(value = "/registration", method=RequestMethod.GET)
    String registration(Map<String, Object> model) throws IOException{
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "registration";
    }
	
	
	@RequestMapping(value = "/contact", method=RequestMethod.GET)
	String getContactPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "contact";
	}
	
	@RequestMapping(value = "/linear_algebra", method=RequestMethod.GET)
	String getLinearAlgebraPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "linear_algebra";
	}
	
	@RequestMapping(value = "/machine_learning", method=RequestMethod.GET)
	String getMachineLearningPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "machine_learning";
	}
	
	@RequestMapping(value = "/team", method=RequestMethod.GET)
	String getTeamPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "team";
	}
	
	@RequestMapping(value = "/statistics", method=RequestMethod.GET)
	String getStatisticsPage(Map<String, Object> model) throws IOException {
		model.put("message", MESSAGE);
		return "statistics";
	}

}