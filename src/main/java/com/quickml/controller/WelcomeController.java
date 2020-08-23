package com.quickml.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.opencsv.CSVWriter;
import com.quickml.pojos.ChangeHistory;
import com.quickml.pojos.Counter;
import com.quickml.pojos.Payment;
import com.quickml.pojos.Student;
import com.quickml.pojos.StudentDTO;
import com.quickml.pojos.User;
import com.quickml.repository.CounterRepository;
import com.quickml.repository.StudentRepository;
import com.quickml.repository.UserRepository;
import com.quickml.utils.Currency;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;



@Controller
public class WelcomeController {

	public WelcomeController(StudentRepository studRepo,
			CounterRepository counterRepo,
			UserRepository userRepo) {
		super();
		this.studRepo = studRepo;
		this.counterRepo = counterRepo;
		this.userRepo = userRepo;
	}

	// inject via application.properties
	@Value("${app.college.longname}")
	private String collegeLongName = "";

	@Value("${app.college.shortname}")
	private String collegeShortName = "";
	
	@Value("${app.college.address1}")
	private String collegeAddress1 = "";
	
	@Value("${app.college.address2}")
	private String collegeAddress2 = "";
	
	@Value("${app.college.contact}")
	private String collegeContact = "";
	
	@Value("${app.college.email}")
	private String collegeEmail = "";
	
	@Autowired
	public final StudentRepository studRepo;
	@Autowired
	public final CounterRepository counterRepo;
	@Autowired
	public final UserRepository userRepo;
	public static final DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");

	@RequestMapping("/")
	public String welcome(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		// TODO : Delete the nest section
		DateTime from = DateTime.now().withTimeAtStartOfDay();
		DateTime to = DateTime.now().withTimeAtStartOfDay().plusHours(24);
		List<Student> students = studRepo.findByPaymentsTransactionDateBetween(from, to);

		int bedInvoiceCount = 0, dedInvoiceCount = 0;
		double bedAmount = 0, dedAmount = 0;
		for (Student st : students) {
			boolean bed = false, ded = false; 
			
			for (Payment pt : st.payments) {
				if (pt.transactionDate.getDayOfMonth() == from.getDayOfMonth() && pt.isActive) {
					if (st.course.equalsIgnoreCase("B.Ed")) {
						bedInvoiceCount++;
						bed = true;
					} else if (st.course.equalsIgnoreCase("D.El.Ed")){
						dedInvoiceCount++;
						ded = true;
					}
					if (bed) {
						bedAmount =  bedAmount + pt.amount;
					} else if (ded) {
						dedAmount =  dedAmount + pt.amount;
					}
				}
			}
		}
		model.put("bedAmount", bedAmount);
		model.put("dedAmount", dedAmount);
		model.put("totalAmount", bedAmount + dedAmount);
		model.put("bedInvoiceCount", bedInvoiceCount);
		model.put("dedInvoiceCount", dedInvoiceCount);
		model.put("totalInvoiceCount", bedInvoiceCount + dedInvoiceCount);
		
		return "welcome";
	}

	// test 5xx errors
	@RequestMapping("/5xx")
	public String ServiceUnavailable() {
		throw new RuntimeException("ABC");
	}
	
	
	@RequestMapping(value = "/create", method=RequestMethod.POST)
    String create(Map<String, Object> model,
    		@RequestBody Student student,
    		HttpServletRequest request) throws IOException{
		populateCommonPageFields(model, request);

		if (student.name.isEmpty() ||
				student.father.isEmpty() ||
				student.mother.isEmpty() ||
				student.mobile.isEmpty() ||
				student.email.isEmpty()	 ||
				student.aadhaar.isEmpty() ||
				student.courseFee == 0) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}

		List<Student> existingStudents = studRepo.findByMobile(student.mobile);
		if (null != existingStudents && existingStudents.size() > 0) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student with same Mobile Number is already registered!");
			return "create";
		}

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
		ChangeHistory ch = new ChangeHistory();
		ch.time = DateTime.now();
		ch.user = request.getRemoteUser();
		ch.operationType = ChangeHistory.Operation.CREATED;
		
		student.changeHistory.add(ch);
    	studRepo.save(student);
    	
    	model.put("alert", "alert alert-success");
    	model.put("result", "Student Registered Successfully!");
    	return "create";
    }
	
	@RequestMapping(value = "/registration", method=RequestMethod.GET)
    String registration(Map<String, Object> model,
    		HttpServletRequest request) throws IOException{
		populateCommonPageFields(model, request);
		model.put("result", "Result will be displayed here!");
		model.put("student", new StudentDTO());
		return "registration";
    }
	
	
	@RequestMapping(value = "/contact", method=RequestMethod.GET)
	String getContactPage(Map<String, Object> model,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		return "contact";
	}
	
	@RequestMapping(value = "/students", method=RequestMethod.GET)
	String getStudentsPage(Map<String, Object> model,
			HttpServletRequest request,
			@RequestParam(value = "course", defaultValue = "B.Ed") String course,
			@RequestParam(value = "session", defaultValue = "2018-20") String session) throws IOException {
		populateCommonPageFields(model, request);
		
		List<Student> students = studRepo.findByCourseAndSession(course, session);
		model.put("students", students);
		return "students";
	}

	@RequestMapping(value = "/studentList", method=RequestMethod.GET)
	String getStudentList(Map<String, Object> model,
			HttpServletRequest request,
			@RequestParam(value = "course", defaultValue = "B.Ed") String course,
			@RequestParam(value = "session", defaultValue = "2018-20") String session) throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students = studRepo.findByCourseAndSession(course, session);
		model.put("students", students);
		return "studentList";
	}

	@RequestMapping(value = "/payment", method=RequestMethod.GET)
	String getPaymentPage(Map<String, Object> model,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		return "payment";
	}

	@RequestMapping(value = "/report", method=RequestMethod.GET)
	String getReportPage(Map<String, Object> model,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		return "report";
	}
	
	@RequestMapping(value = "/paymentDetails", method=RequestMethod.GET)
	String getPaymentDetailsSec(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			HttpServletRequest request) throws IOException, ParseException {
		populateCommonPageFields(model, request);

		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
		} else {
			ArrayList<Payment> payments = student.payments;
			double paid = 0;
			if (null != payments) {
				for (Payment payment : payments) {
					if (payment.purpose.equals("Examination Fee") ||
						payment.purpose.equals("Registration Fee")) {
						continue;
					}
					paid += payment.amount;
				}
			}
			model.put("due", student.courseFee - paid);
			model.put("student", student);
		}
		return "paymentDetails";
	}

	@RequestMapping(value = "/createPayment", method=RequestMethod.POST)
	String setPaymentDetails(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			@RequestBody Payment payment,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		
		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		} else {
			if (payment.amount == 0) {
				model.put("alert", "alert alert-danger");
				model.put("result", "Please fill the mandatory field Amount!");
				return "create";
			}
			if (!payment.mode.equals("Cash") && payment.transactionId.isEmpty()) {
				model.put("alert", "alert alert-danger");
				model.put("result", "Transaction ID is mandatory except Cash!");
				return "create";
			}
			ArrayList<Payment> payments = student.payments;
			if (null == payments) {
				payments = new ArrayList<Payment>();
			}
			Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
			// Fetch the Payment or Money Receipt ID counter
			int year = getFiscalYear(calendar);
			Counter ct = counterRepo.findOne(year + "-" + String.valueOf(year+1).substring(2));
			if (null == ct) {
				ct = new Counter();
				ct.id = year + "-" + String.valueOf(year+1).substring(2);
				ct.nextId++;
			}
			if (payment.purpose.equals("Concession")) {
				student.courseFee -= payment.amount;
				payment.isActive = false;
				ChangeHistory ch = new ChangeHistory();
				ch.time = DateTime.now();
				ch.user = request.getRemoteUser();
				ch.operationType = ChangeHistory.Operation.MODIFIED;
				ch.message = "Concession given. Amount: " + payment.amount;
				student.changeHistory.add(ch);
			}
			payment.paymentId = ct.id + "/" + String.format("%05d", ct.nextId);
			payment.transactionDate = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
			payment.acceptedBy = request.getRemoteUser();
			payments.add(payment);
			
			ct.nextId++;
			counterRepo.save(ct);
			studRepo.save(student);
		}
		model.put("alert", "alert alert-success");
    	model.put("result", "Payment Information Recorded Successfully!");
		return "paymentDetails";
	}
	
	@RequestMapping(value = "/invoice", method=RequestMethod.GET)
	void downloadInvoice(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			@RequestParam(name = "paymentId") String paymentId,
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return;
		}
		Payment pt = null;
		double paid = 0;
		for(Payment p : student.payments) {
			paid += p.amount;
			if (paymentId.equals(p.paymentId)) {
				pt = p;
				break;
			}
		}
		
		Resource resource = new ClassPathResource("InvoiceA4.jrxml");
		InputStream input = resource.getInputStream();
		JasperDesign design = null;
		try {
			design = JRXmlLoader.load(input);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JasperReport jasperReport = null;
		try {
			jasperReport = JasperCompileManager.compileReport(design);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String outputFile = "C:\\Users\\polaris2\\" + "JasperReportExample.pdf";
		OutputStream outputStream = new FileOutputStream(new File(outputFile));
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("logoFile", collegeShortName.toLowerCase() + "_logo.jpeg");
			paramMap.put("collegeLongName", collegeLongName);
			paramMap.put("collegeShortName", collegeShortName);
			paramMap.put("collegeAddress1", collegeAddress1);
			paramMap.put("collegeAddress2", collegeAddress2);
			paramMap.put("collegeContact", collegeContact);
			paramMap.put("collegeEmail", collegeEmail);
			paramMap.put("name", student.name);
			paramMap.put("id", student.id);
			paramMap.put("course", student.course);
			paramMap.put("session", student.session);
			paramMap.put("paymentId", pt.paymentId);
			paramMap.put("transactionMode", pt.mode);
			paramMap.put("transactionId", pt.transactionId);
			paramMap.put("due", student.courseFee - paid);
			paramMap.put("purpose", pt.purpose);
			paramMap.put("amount", pt.amount);
			paramMap.put("inWords", Currency.convertToIndianCurrency(pt.amount));
			String date = pt.transactionDate.toString(dtfOut);
			paramMap.put("date", date);
			User user = userRepo.findByUsername(pt.acceptedBy);
			paramMap.put("user", user.fullname);
			JasperPrint jasperPrint = JasperFillManager.fillReport(
					jasperReport,
					paramMap,
					new JREmptyDataSource());
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputStream.flush();
		outputStream.close();
		// Download section
		File invoiceFile = new File(outputFile);
		String mimeType = "application/pdf";
		response.setContentType(mimeType);
		String invoiceFileName = student.id+"_"+pt.paymentId.substring(8)+".pdf";
		response.setHeader("Content-Disposition", String.format("inline; filename=\""+invoiceFileName+"\""));
		response.setContentLength((int) invoiceFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(invoiceFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();
	}
	
	public int getFiscalYear(Calendar calendar) {
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        return (month > Calendar.MARCH) ? year : year - 1;
    }
	
	public void populateCommonPageFields(Map<String, Object> model, HttpServletRequest request) {
		model.put("title", collegeShortName);
		model.put("message", collegeLongName);
		model.put("collegeLongName", collegeLongName);
		model.put("user", request.getRemoteUser());
	}
	
	@RequestMapping(value = "/payDueReport", method=RequestMethod.GET)
	void generatePayDueReport(Map<String, Object> model,
			@RequestParam(name = "session") String session,
			@RequestParam(name = "course") String course,
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students = studRepo.findByCourseAndSession(course, session);
		
		String outputFileName = "C:\\Users\\polaris2\\" + "paydue.csv";
		File reportFile = new File(outputFileName);
		  
	    try {
	        // create FileWriter object with file as parameter
	        FileWriter outputfile = new FileWriter(reportFile);

	        // create CSVWriter object filewriter object as parameter
	        CSVWriter writer = new CSVWriter(outputfile);

	        // create a List which contains String array 
	        List<String[]> data = new ArrayList<String[]>(); 
	        data.add(new String[] { "StudentID", "Name", "Mobile", "CourseFee", "Paid", "Due" });
	        for (Student st : students) {
				ArrayList<Payment> payments = st.payments;
				double paid = 0;
				if (null != payments) {
					for (Payment payment : payments) {
						if (payment.purpose.equals("Examination Fee") ||
							payment.purpose.equals("Registration Fee")) {
							continue;
						}
						paid += payment.amount;
					}
				}
				// Add Student Details
				data.add(new String[] {st.id,
						st.name,
						st.mobile,
						Double.toString(st.courseFee),
						Double.toString(paid),
						Double.toString(st.courseFee-paid)});
			}
	        writer.writeAll(data); 
	  
	        // closing writer connection 
	        writer.close(); 
	    } 
	    catch (IOException e) { 
	        // TODO Auto-generated catch block 
	        e.printStackTrace(); 
	    } 
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "DueList"+"_"+course+"_"+session.substring(0, 4)+".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\""+reportFileName+"\""));
		response.setContentLength((int) reportFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		model.put("alert", "alert alert-success");
    	model.put("result", "Report Generated Successfully!");
	}

	@RequestMapping(value = "/allStudents", method=RequestMethod.GET)
	void downloadAllStudents(Map<String, Object> model,
			@RequestParam(name = "session") String session,
			@RequestParam(name = "course") String course,
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students = studRepo.findByCourseAndSession(course, session);

		String outputFileName = "C:\\Users\\polaris2\\" + "allStudents.csv";
		File reportFile = new File(outputFileName);

	    try {
	        // create FileWriter object with file as parameter
	        FileWriter outputfile = new FileWriter(reportFile);
	        // create CSVWriter object filewriter object as parameter
	        CSVWriter writer = new CSVWriter(outputfile);
	        // create a List which contains String array
	        List<String[]> data = new ArrayList<String[]>();
	        data.add(new String[] { "StudentID", "Course", "Name", "Father's Name",
	        		"Mother's Name", "Date of Birth", "Gender", "Religion", "Category",
	        		"Mobile", "Email Address", "Guardian Contact", "Blood Group", "Language",
	        		"Nationality", "Application Type", "Aadhaar Number", "Address",
	        		"Alternate Address", "Last Registration Number", "Subject",
	        		"Last School Name", "Session", "Course Fee", "Status",
	        		"Degree", "Board", "Year", "Total Marks", "Marks Obtained", "Percentage",
                    "Degree", "Board", "Year", "Total Marks", "Marks Obtained", "Percentage",
                    "Degree", "Board", "Year", "Total Marks", "Marks Obtained", "Percentage",
                    "Degree", "Board", "Year", "Total Marks", "Marks Obtained", "Percentage"});
	        for (Student st : students) {
				// Add Student Details
				data.add(st.ToStringArray());
			}
	        writer.writeAll(data);

	        // closing writer connection
	        writer.close();
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "All_Students"+"_"+course+"_"+session.substring(0, 4)+".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\""+reportFileName+"\""));
		response.setContentLength((int) reportFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	@RequestMapping(value = "/reverse", method=RequestMethod.GET)
	public String revertTransaction(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			@RequestParam(name = "paymentId") String paymentId,
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		}
		Payment pt = null;
		for(Payment p : student.payments) {
			if (paymentId.equals(p.paymentId)) {
				pt = p;
			}
		}
		if (pt == null) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Payment not found!");
			return "create";
		}
		// Make the actual payment not active
		pt.isActive = false;
		Payment reversePt = new Payment();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("IST"));
		// Fetch the Payment or Money Receipt ID counter
		int year = getFiscalYear(calendar);
		Counter ct = counterRepo.findOne(year + "-" + String.valueOf(year+1).substring(2));
		if (null == ct) {
			ct = new Counter();
			ct.id = year + "-" + String.valueOf(year+1).substring(2);
			ct.nextId++;
		}

		if (pt.purpose.equals("Concession")) {
			student.courseFee += pt.amount;
			ChangeHistory ch = new ChangeHistory();
			ch.time = DateTime.now();
			ch.user = request.getRemoteUser();
			ch.operationType = ChangeHistory.Operation.MODIFIED;
			ch.message = "Concession withdrawn. Amount: " + pt.amount;
			student.changeHistory.add(ch);
		}

		reversePt.paymentId = ct.id + "/" + String.format("%05d", ct.nextId);
		reversePt.transactionDate = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
		reversePt.acceptedBy = request.getRemoteUser();
		reversePt.isActive = false;
		reversePt.amount = -pt.amount;
		reversePt.transactionId = pt.transactionId;
		reversePt.mode = pt.mode;
		reversePt.purpose = "Reverse";
		student.payments.add(reversePt);

		ct.nextId++;
		counterRepo.save(ct);
		studRepo.save(student);

		model.put("alert", "alert alert-success");
		model.put("result", "Payment Reversed Successfully!");
		return "paymentDetails";
	}

	@RequestMapping(value = "/collectionReport", method=RequestMethod.GET)
	void generateCollectionReport(Map<String, Object> model,
			@RequestParam(name = "from") @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd") DateTime from,
			@RequestParam(name = "to") @org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd") DateTime to,
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		from = from.withTimeAtStartOfDay();
		to = to.withTimeAtStartOfDay().plusHours(24);
		List<Student> students = studRepo.findByPaymentsTransactionDateBetween(from, to);

		String outputFileName = "C:\\Users\\polaris2\\" + "collection.csv";
		File reportFile = new File(outputFileName);

	    try {
	        // create FileWriter object with file as parameter
	        FileWriter outputfile = new FileWriter(reportFile);

	        // create CSVWriter object filewriter object as parameter
	        CSVWriter writer = new CSVWriter(outputfile);

	        // create a List which contains String array
	        List<String[]> data = new ArrayList<String[]>();
	        data.add(new String[] { "StudentID", "Name", "Mobile", "Course",
	        		"Session", "Money Receipt No", "Date", "Transaction ID",
	        		"Mode", "Purpose", "Accepted By", "Amount"});
	        double total = 0;
	        for (Student st : students) {
				ArrayList<Payment> payments = st.payments;
				if (null != payments) {
					for (Payment payment : payments) {
						if (payment.transactionDate.isAfter(from) &&
								payment.transactionDate.isBefore(to)) {
							// Add Student Details
							data.add(new String[] {st.id,
									st.name,
									st.mobile,
									st.course,
									st.session,
									payment.paymentId,
									payment.transactionDate.toString(dtfOut),
									payment.transactionId,
									payment.mode,
									payment.purpose,
									payment.acceptedBy,
									Double.toString(payment.amount)});
							total += payment.amount;
						}
					}
				}
			}
	        data.add(new String[] {"", "", "", "", "", "", "", "", "", "", "", ""});
	        data.add(new String[] {"Total", "", "", "", "", "", "", "", "", "", "", Double.toString(total)});
	        writer.writeAll(data);

	        // closing writer connection
	        writer.close();
	    }
	    catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	    }
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "Collection"+"_"+
				from.getDayOfMonth() + "_"+ from.getMonthOfYear() + "_" +
				to.minusHours(24).getDayOfMonth()+"_"+to.getMonthOfYear()+".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\""+reportFileName+"\""));
		response.setContentLength((int) reportFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	@RequestMapping(value = "/studentDetails", method=RequestMethod.GET)
	String getStudentDetails(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			HttpServletRequest request) throws IOException, ParseException {
		populateCommonPageFields(model, request);

		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
		} else {
			StudentDTO st = new StudentDTO().replicate(student);
			st.isModification = true;
			model.put("student", st);
		}
		return "layout/registrationForm";
	}

	@RequestMapping(value = "/modifyStudentDetails", method=RequestMethod.POST)
	String modifyStudentDetails(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			@RequestBody Student newStudent,
			HttpServletRequest request) throws IOException, ParseException {
		populateCommonPageFields(model, request);

		Student oldStudent = studRepo.findOne(studentId);
		if (null == oldStudent) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		}
		if (newStudent.name.isEmpty() ||
				newStudent.father.isEmpty() ||
				newStudent.mother.isEmpty() ||
				newStudent.mobile.isEmpty() ||
				newStudent.email.isEmpty()	 ||
				newStudent.aadhaar.isEmpty() ||
				newStudent.courseFee == 0) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		boolean idChanged = false;

		ChangeHistory ch = new ChangeHistory();
		ch.time = DateTime.now();
		ch.user = request.getRemoteUser();
		ch.operationType = ChangeHistory.Operation.MODIFIED;

		if (!newStudent.session.equals(oldStudent.session) ||
				!newStudent.course.equals(oldStudent.course)) {

			if (!newStudent.session.equals(oldStudent.session)) {
				ch.message = "Session Modified from " + oldStudent.session + " to " + newStudent.session;
			}
			if (!newStudent.course.equals(oldStudent.course)) {
				ch.message = "Course Modified from " + oldStudent.course + " to " + newStudent.course;
			}
			// Change in ID
			String id_prefix = newStudent.session.substring(0, 4);
			// Counter is used to get the next id to be assigned for a new student based on session and course
			Counter ct = null;
			if (newStudent.course.equalsIgnoreCase("b.ed")) {
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

			newStudent.id = id_prefix;
			idChanged = true;
		} else {
			newStudent.id = oldStudent.id;
		}
		// Copy Payment Information
		newStudent.payments = new ArrayList<>(oldStudent.payments);
		if (newStudent.courseFee != oldStudent.courseFee) {
			model.put("alert", "alert alert-warning");
			model.put("result", "Change in Course Fee, Incident will be reported!");
			ch.message = "Course Fee Modified from " + oldStudent.courseFee + " to " + newStudent.courseFee;
		} else {
			if (idChanged) {
				model.put("alert", "alert alert-warning");
		    	model.put("result", "Student Information Modified, ID is changed to " + newStudent.id );
			} else {
				model.put("alert", "alert alert-success");
		    	model.put("result", "Student Information Modified Successfully!");
			}
		}
		newStudent.changeHistory = new ArrayList<>(oldStudent.changeHistory);
		newStudent.changeHistory.add(ch);
		studRepo.save(newStudent);

		if (idChanged) {
			studRepo.delete(oldStudent);
		}

		return "create";
	}

	@RequestMapping(value = "/icard", method=RequestMethod.GET)
	void downloadICard(Map<String, Object> model,
			@RequestParam(name = "id") String studentId,
			HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Student student = studRepo.findOne(studentId);
		if (null == student) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return;
		}
		
		Resource resource = new ClassPathResource("ICard.jrxml");
		InputStream input = resource.getInputStream();
		JasperDesign design = null;
		try {
			design = JRXmlLoader.load(input);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JasperReport jasperReport = null;
		try {
			jasperReport = JasperCompileManager.compileReport(design);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String outputFile = "C:\\Users\\polaris2\\" + "JasperReportExample.pdf";
		OutputStream outputStream = new FileOutputStream(new File(outputFile));
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("logoFile", collegeShortName.toLowerCase() + "_logo.jpeg");
			paramMap.put("collegeLongName", collegeLongName);
			paramMap.put("collegeShortName", collegeShortName);
			paramMap.put("collegeAddress1", collegeAddress1);
			paramMap.put("collegeAddress2", collegeAddress2);
			paramMap.put("collegeContact", collegeContact);
			paramMap.put("collegeEmail", collegeEmail);
			paramMap.put("name", student.name);
			paramMap.put("id", student.id);
			paramMap.put("course", student.course);
			paramMap.put("session", student.session);
			String dob = student.dob.toString(dtfOut);
			paramMap.put("dob", dob);
			paramMap.put("blood", student.blood);
			paramMap.put("address1", student.address1);
			JasperPrint jasperPrint = JasperFillManager.fillReport(
					jasperReport,
					paramMap,
					new JREmptyDataSource());
			JasperExportManager.exportReportToPdfStream(jasperPrint, outputStream);
		} catch (JRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		outputStream.flush();
		outputStream.close();
		// Download section
		File invoiceFile = new File(outputFile);
		String mimeType = "application/pdf";
		response.setContentType(mimeType);
		String icard = student.id+".pdf";
		response.setHeader("Content-Disposition", String.format("inline; filename=\""+icard+"\""));
		response.setContentLength((int) invoiceFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(invoiceFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();
	}
}
