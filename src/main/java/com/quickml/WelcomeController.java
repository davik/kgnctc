package com.quickml;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.quickml.pojos.Counter;
import com.quickml.pojos.Student;



@Controller
public class WelcomeController {

	public WelcomeController(StudentRepository studRepo,
			CounterRepo counterRepo) {
		super();
		this.studRepo = studRepo;
		this.counterRepo = counterRepo;
	}

	// inject via application.properties
	@Value("${app.welcome.message}")
	private String MESSAGE = "";

	@Value("${app.welcome.title}")
	private String TITLE = "";
	
	@Autowired
	public final StudentRepository studRepo;
	@Autowired
	public final CounterRepo counterRepo;

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
    String create(Map<String, Object> model,
    		@RequestBody Student student) throws IOException{
		System.out.println("Rest received");

		if (student.name.isEmpty() ||
				student.father.isEmpty() ||
				student.mother.isEmpty() ||
				student.mobile.isEmpty() ||
				student.email.isEmpty()	 ||
				student.aadhaar.isEmpty()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		System.out.println(student.name);
		String id = student.session.substring(0, 4);
		Counter ct = null;
		if (student.course.equalsIgnoreCase("b.ed")) {
			id = id + "01";
			ct = counterRepo.findOne("bed");
		} else {
			id = id + "02";
			ct = counterRepo.findOne("ded");
		}
		id = id + String.format("%05d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		student.id = id;
    	studRepo.save(student);
    	
    	model.put("alert", "alert alert-success");
    	model.put("result", "Student Registered Successfully!");
    	return "create";
    }
	
	@RequestMapping(value = "/registration", method=RequestMethod.GET)
    String registration(Map<String, Object> model) throws IOException{
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		model.put("result", "Result will be displayed here!");
		return "registration";
    }
	
	
	@RequestMapping(value = "/contact", method=RequestMethod.GET)
	String getContactPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "contact";
	}
	
	@RequestMapping(value = "/students", method=RequestMethod.GET)
	String getStudentsPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		List<Student> students = studRepo.findAll();
		model.put("students", students);
		return "students";
	}
}