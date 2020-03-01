package com.quickml;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.quickml.pojos.Counter;
import com.quickml.pojos.Payment;
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
		String id_prefix = student.session.substring(0, 4);
		// Counter is used to get the next id to be assigned for a new student based on session and course
		Counter ct = null;
		if (student.course.equalsIgnoreCase("b.ed")) {
			id_prefix = id_prefix + "01";
			ct = counterRepo.findOne(id_prefix);
		} else {
			id_prefix = id_prefix + "02";
			ct = counterRepo.findOne(id_prefix);
		}
		// If counter config not exists, create one
		if (ct == null) {
			ct = new Counter();
			ct.id = id_prefix;
			ct.nextId++;
		}
		// Increment and save the counter config
		id_prefix = id_prefix + String.format("%03d", ct.nextId);
		ct.nextId++;
		counterRepo.save(ct);

		student.id = id_prefix;
		// Temporary code here
		Payment payment = new Payment();
		payment.transactionDate = new Date();
		payment.amount = 20000;
		payment.mode = "Cash";
		payment.purpose = "Course Fee";
		student.payments = new ArrayList<Payment>();
		student.payments.add(payment);
		// Temporary ends
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
	
	@RequestMapping(value = "/payment", method=RequestMethod.GET)
	String getPaymentPage(Map<String, Object> model) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		return "payment";
	}

	@RequestMapping(value = "/paymentDetails", method=RequestMethod.POST)
	String getPaymentDetailsSec(Map<String, Object> model,
			@RequestParam(name = "id") String studentId) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
		} else {
			ArrayList<Payment> payments = student.payments;
			double paid = 0;
			for (Payment payment : payments) {
				paid += payment.amount;
			}
			model.put("due", student.courseFee - paid);
			model.put("student", student);
		}
		return "paymentDetails";
	}

	@RequestMapping(value = "/createPayment", method=RequestMethod.POST)
	String setPaymentDetails(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			@RequestBody Payment payment) throws IOException {
		model.put("title", TITLE);
		model.put("message", MESSAGE);
		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		} else {
			if (payment.amount == 0 ||
					payment.transactionId.isEmpty()) {
				model.put("alert", "alert alert-danger");
				model.put("result", "Please fill the mandatory fields!");
				return "create";
			}
			ArrayList<Payment> payments = student.payments;
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
			payment.transactionDate = calendar.getTime();
			payments.add(payment);
			studRepo.save(student);
		}
		model.put("alert", "alert alert-success");
    	model.put("result", "Payment Information Recorded Successfully!");
		return "paymentDetails";
	}
}