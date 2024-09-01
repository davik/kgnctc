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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Optional;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.opencsv.CSVWriter;
import com.quickml.pojos.Attendance;
import com.quickml.pojos.AttendanceDTO;
import com.quickml.pojos.ChangeHistory;
import com.quickml.pojos.Counter;
import com.quickml.pojos.Payment;
import com.quickml.pojos.SMSDTO;
import com.quickml.pojos.Student;
import com.quickml.pojos.StudentDTO;
import com.quickml.pojos.TeachingSchool;
import com.quickml.pojos.Notice;
import com.quickml.pojos.User;
import com.quickml.pojos.smstemplate.DueReminderBody;
import com.quickml.pojos.smstemplate.NoticeBody;
import com.quickml.pojos.smstemplate.PaymentBody;
import com.quickml.pojos.smstemplate.RegistrationBody;
import com.quickml.pojos.smstemplate.RootTemplate;
import com.quickml.repository.AttendanceRepository;
import com.quickml.repository.CounterRepository;
import com.quickml.repository.StudentRepository;
import com.quickml.repository.UserRepository;
import com.quickml.repository.TeachingSchoolRepository;
import com.quickml.repository.NoticeRepository;
import com.quickml.utils.Currency;
import com.quickml.utils.SMS;

@Controller
public class WelcomeController {

	public WelcomeController(StudentRepository studRepo, CounterRepository counterRepo, UserRepository userRepo,
			AttendanceRepository attendanceRepo, TeachingSchoolRepository teachingSchoolRepo,
			NoticeRepository noticeRepo) {
		super();
		this.studRepo = studRepo;
		this.counterRepo = counterRepo;
		this.userRepo = userRepo;
		this.attendanceRepo = attendanceRepo;
		this.teachingSchoolRepo = teachingSchoolRepo;
		this.noticeRepo = noticeRepo;
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

	@Value("${app.college.sms}")
	private String smsEnabled = "";

	@Value("${app.college.smscount}")
	private int smsProvisionCount = 0;

	@Autowired
	public final StudentRepository studRepo;
	@Autowired
	public final CounterRepository counterRepo;
	@Autowired
	public final UserRepository userRepo;
	@Autowired
	public final AttendanceRepository attendanceRepo;
	@Autowired
	public final TeachingSchoolRepository teachingSchoolRepo;
	@Autowired
	public final NoticeRepository noticeRepo;
	public static final DateTimeFormatter dtfOut = DateTimeFormat.forPattern("MM/dd/yyyy");

	SMS sms = new SMS();

	@RequestMapping("/")
	public String welcome(Map<String, Object> model, HttpServletRequest request) {
		populateCommonPageFields(model, request);
		DateTime from = DateTime.now().withTimeAtStartOfDay();
		DateTime to = DateTime.now().withTimeAtStartOfDay().plusHours(24);
		List<Student> students = studRepo.findByPaymentsTransactionDateBetween(from, to);

		int bpharmInvoiceCount = 0, dpharmInvoiceCount = 0, prospectusCount = 0;
		double propectusAmount = 0;
		double bpharmAmount = 0, dpharmAmount = 0;
		double cash = 0, cheque = 0, dd = 0, netBanking = 0, pos = 0, bankDeposit = 0;
		for (Student st : students) {
			boolean bpharm = false, dpharm = false;

			for (Payment pt : st.payments) {
				if (pt.transactionDate.isAfter(from) && pt.transactionDate.isBefore(to) && pt.isActive) {
					double amount = 0;
					if (pt.purpose.contains("Course Fee Refund")) {
						amount = -pt.amount;
					} else {
						amount = pt.amount;
					}

					if (pt.purpose.contains("Prospectus Fee")) {
						prospectusCount++;
						propectusAmount = propectusAmount + amount;
						continue;
					}

					if (st.course.equalsIgnoreCase("B.Pharm")) {
						bpharmInvoiceCount++;
						bpharm = true;
					} else if (st.course.equalsIgnoreCase("D.Pharm")) {
						dpharmInvoiceCount++;
						dpharm = true;
					}
					double lateFeeAmount = pt.lateFeeAmount != -1 ? pt.lateFeeAmount : 0;
					if (bpharm) {
						bpharmAmount = bpharmAmount + amount + lateFeeAmount;
					} else if (dpharm) {
						dpharmAmount = dpharmAmount + amount + lateFeeAmount;
					}

					switch (pt.mode) {
						case "Cash":
							cash = cash + amount + lateFeeAmount;
							break;
						case "Cheque":
							cheque = cheque + amount + lateFeeAmount;
							break;
						case "DD":
							dd = dd + amount + lateFeeAmount;
							break;
						case "Net Banking":
							netBanking = netBanking + amount + lateFeeAmount;
							break;
						case "POS":
							pos = pos + amount + lateFeeAmount;
							break;
						case "Bank Deposit":
							bankDeposit = bankDeposit + amount + lateFeeAmount;
							break;
					}
				}
			}
		}
		model.put("bpharmAmount", bpharmAmount);
		model.put("dpharmAmount", dpharmAmount);
		model.put("prospectusAmount", propectusAmount);
		model.put("totalAmount", bpharmAmount + dpharmAmount + propectusAmount);
		model.put("bpharmInvoiceCount", bpharmInvoiceCount);
		model.put("dpharmInvoiceCount", dpharmInvoiceCount);
		model.put("prospectusCount", prospectusCount);
		model.put("totalInvoiceCount", bpharmInvoiceCount + dpharmInvoiceCount + prospectusCount);

		model.put("cash", cash);
		model.put("cheque", cheque);
		model.put("dd", dd);
		model.put("netBanking", netBanking);
		model.put("pos", pos);
		model.put("bankDeposit", bankDeposit);

		return "welcome";
	}

	// test 5xx errors
	@RequestMapping("/5xx")
	public String ServiceUnavailable() {
		throw new RuntimeException("ABC");
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST)
	String create(Map<String, Object> model, @RequestBody Student student, HttpServletRequest request)
			throws IOException {
		populateCommonPageFields(model, request);

		if (student.name.isEmpty() || student.mobile.isEmpty() || student.subject.isEmpty() || student.courseFee == 0) {
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
		// Counter is used to get the next id to be assigned for a new student based on
		// session and course
		Counter ct = null;
		Optional<Counter> oct = null;
		if (student.course.equalsIgnoreCase("B.Pharm")) {
			id_prefix = id_prefix + "01";
			oct = counterRepo.findById(id_prefix);
		} else {
			id_prefix = id_prefix + "02";
			oct = counterRepo.findById(id_prefix);
		}
		// If counter config not exists, create one
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = id_prefix;
			ct.nextId++;
		} else {
			ct = oct.get();
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
		Student savedEntity = studRepo.save(student);
		if (null != savedEntity) {
			RegistrationBody rb = new RegistrationBody();
			rb.id = savedEntity.id;
			rb.name = savedEntity.name;
			rb.mobiles = "91" + savedEntity.mobile;
			rb.course = savedEntity.course;
			rb.session = savedEntity.session;
			rb.fee = Double.toString(savedEntity.courseFee);
			rb.college = " ";
			RootTemplate<RegistrationBody> template = new RootTemplate<RegistrationBody>();
			template.flowType = RootTemplate.FlowType.REGISTRATION;
			template.recipients = new ArrayList<RegistrationBody>();
			template.recipients.add(rb);
			sms.send(template, smsEnabled, collegeShortName, smsProvisionCount, counterRepo);
		}

		model.put("alert", "alert alert-success");
		model.put("result", "Student Registered Successfully!");
		return "create";
	}

	@RequestMapping(value = "/registration", method = RequestMethod.GET)
	String registration(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		model.put("result", "Result will be displayed here!");
		model.put("student", new StudentDTO());
		return "registration";
	}

	@RequestMapping(value = "/contact", method = RequestMethod.GET)
	String getContactPage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		return "contact";
	}

	@RequestMapping(value = "/students", method = RequestMethod.GET)
	String getStudentsPage(Map<String, Object> model, HttpServletRequest request,
			@RequestParam(value = "course", defaultValue = "B.Pharm") String course,
			@RequestParam(value = "session", defaultValue = "2023-25") String session) throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students = studRepo.findByStatus(Student.Status.ACTIVE);
		model.put("students", students);
		return "students";
	}

	@RequestMapping(value = "/studentList", method = RequestMethod.GET)
	String getStudentList(Map<String, Object> model, HttpServletRequest request,
			@RequestParam(value = "course") String course, @RequestParam(value = "session") String session)
			throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students;
		if (course.isEmpty() || session.isEmpty()) {
			students = studRepo.findAll();
		} else {
			students = studRepo.findByCourseAndSession(course, session);
		}
		model.put("students", students);
		return "studentList";
	}

	@RequestMapping(value = "/payment", method = RequestMethod.GET)
	String getPaymentPage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		return "payment";
	}

	@RequestMapping(value = "/report", method = RequestMethod.GET)
	String getReportPage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		return "report";
	}

	@RequestMapping(value = "/communication", method = RequestMethod.GET)
	String getCommPage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		Counter ct = null;
		Optional<Counter> oct = counterRepo.findById("SMS");
		if (!oct.isPresent()) {
			model.put("sentSMS", 0);
			ct = new Counter();
			ct.id = "SMS";
			ct.nextId = 0;
			counterRepo.save(ct);
		} else {
			ct = oct.get();
		}
		model.put("sentSMS", ct.nextId);
		model.put("balanceSMS", smsProvisionCount - ct.nextId);
		model.put("notices", noticeRepo.findAll().stream().sorted((n1, n2) -> n2.date.compareTo(n1.date))
				.collect(Collectors.toList()));
		return "communication";
	}

	@RequestMapping(value = "/teachingSchool", method = RequestMethod.GET)
	String getTeachingSchoolPage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		List<TeachingSchool> schools = teachingSchoolRepo.findAll();
		model.put("schools", schools);
		return "teachingSchool";
	}

	@RequestMapping(value = "/paymentDetails", method = RequestMethod.GET)
	String getPaymentDetailsSec(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			HttpServletRequest request) throws IOException, ParseException {
		populateCommonPageFields(model, request);

		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
		} else {
			Student student = oStudent.get();
			model.put("due", student.courseFee - GetPaid(student));
			model.put("student", student);
			model.put("schools", teachingSchoolRepo.findAll().stream().map(s -> s.name).collect(Collectors.toList()));
		}
		return "paymentDetails";
	}

	@RequestMapping(value = "/createPayment", method = RequestMethod.POST)
	String setPaymentDetails(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestBody Payment payment, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		} else {
			Student student = oStudent.get();
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
			Counter ct = null;
			if (payment.purpose.equals("Miscellaneous Fee")) {
				payment.purpose = payment.purpose + " (" + payment.transactionId + ")";
				Optional<Counter> oct = counterRepo
						.findById("MISC/" + year + "-" + String.valueOf(year + 1).substring(2));
				if (!oct.isPresent()) {
					ct = new Counter();
					ct.id = "MISC/" + year + "-" + String.valueOf(year + 1).substring(2);
					ct.nextId++;
				} else {
					ct = oct.get();
				}
				payment.transactionId = "";
			} else {
				Optional<Counter> oct = counterRepo.findById(year + "-" + String.valueOf(year + 1).substring(2));
				if (!oct.isPresent()) {
					ct = new Counter();
					ct.id = year + "-" + String.valueOf(year + 1).substring(2);
					ct.nextId++;
				} else {
					ct = oct.get();
				}
			}

			if (payment.purpose.equals("Course Fee Refund")) {
				// payment.amount = -payment.amount;
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
			Student savedEntity = studRepo.save(student);
			if (null != savedEntity) {
				// Evaluate Due amount
				double due = 0;
				double paid = 0;
				for (Payment pt : savedEntity.payments) {
					if (pt.purpose.equals("Concession")) {
						continue;
					}
					paid += pt.amount;
				}
				due = savedEntity.courseFee - GetPaid(savedEntity);

				PaymentBody pb = new PaymentBody();
				pb.mobiles = "91" + savedEntity.mobile;
				pb.paymentId = payment.paymentId;
				pb.purpose = payment.purpose;
				pb.amount = Double.toString(payment.amount);
				pb.mode = payment.mode;
				pb.due = Double.toString(due);
				pb.college = " ";
				RootTemplate<PaymentBody> template = new RootTemplate<PaymentBody>();
				template.flowType = RootTemplate.FlowType.PAYMENT;
				template.recipients = new ArrayList<PaymentBody>();
				template.recipients.add(pb);
				sms.send(template, smsEnabled, collegeShortName, smsProvisionCount, counterRepo, 2);
			}
		}
		model.put("alert", "alert alert-success");
		model.put("result", "Payment Information Recorded Successfully!");
		return "paymentDetails";
	}

	// Update Teaching School for a student
	@RequestMapping(value = "/updateTeachingSchool", method = RequestMethod.POST)
	@ResponseBody
	String updateTeachingSchool(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestParam(name = "schoolName") String schoolName) {
		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			return "Student not found!";
		} else {
			Student student = oStudent.get();
			student.teachingSchool = schoolName;
			Student savedEntity = studRepo.save(student);
		}
		return "Saved Successfully!";
	}

	@RequestMapping(value = "/invoice", method = RequestMethod.GET)
	String downloadInvoice(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestParam(name = "paymentId") String paymentId, HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return null;
		}
		Student student = oStudent.get();
		Payment pt = null;
		for (Payment p : student.payments) {
			if (paymentId.equals(p.paymentId)) {
				pt = p;
				break;
			}
		}
		if (null == pt) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Payment Info not found!");
			return null;
		}
		double paid = GetPaid(student);

		// model.put("logoFile", collegeShortName.toLowerCase() + "_logo.jpeg");
		model.put("collegeLongName", collegeLongName);
		model.put("collegeShortName", collegeShortName);
		model.put("collegeAddress1", collegeAddress1);
		model.put("collegeAddress2", collegeAddress2);
		model.put("collegeContact", collegeContact);
		model.put("collegeEmail", collegeEmail);
		model.put("name", student.name);
		model.put("id", student.id);
		model.put("course", student.course);
		model.put("session", student.session);
		model.put("paymentId", pt.paymentId);
		model.put("transactionMode", pt.mode);
		model.put("transactionId", pt.transactionId);
		model.put("due", student.courseFee - paid);
		model.put("purpose", pt.purpose);
		model.put("amount", pt.amount);
		model.put("total", pt.amount);
		model.put("inWords", Currency.convertToIndianCurrency(pt.amount));
		String date = pt.transactionDate.toString(dtfOut);
		model.put("date", date);
		User user = userRepo.findByUsername(pt.acceptedBy);
		model.put("user", user.fullname);
		model.put("isLateFee", false);
		// Populate Late fee details
		if (pt.lateFeeAmount != -1) {
			model.put("isLateFee", true);
			model.put("secondSerial", "2.");
			model.put("latefee", "Late Fee");
			model.put("latefeeAmount", pt.lateFeeAmount);
			model.replace("total", pt.amount + pt.lateFeeAmount);
			model.replace("inWords", Currency.convertToIndianCurrency(pt.amount + pt.lateFeeAmount));
		}

		// Download section
		return "invoice";
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
		if (request.getRemoteUser().equals("admin")) {
			model.put("admin", true);
		} else {
			model.put("admin", false);
		}
	}

	@RequestMapping(value = "/payDueReport", method = RequestMethod.GET)
	void generatePayDueReport(Map<String, Object> model, @RequestParam(name = "session") String session,
			@RequestParam(name = "course") String course, HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students = studRepo.findByCourseAndSessionAndStatus(course, session, Student.Status.ACTIVE);

		String outputFileName = "C:\\Users\\avik\\" + "paydue.csv";
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
				double paid = GetPaid(st);
				// Add Student Details
				data.add(new String[] { st.id, st.name, st.mobile, Double.toString(st.courseFee), Double.toString(paid),
						Double.toString(st.courseFee - paid) });
			}
			writer.writeAll(data);

			// closing writer connection
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "DueList" + "_" + course + "_" + session.substring(0, 4) + ".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + reportFileName + "\""));
		response.setContentLength((int) reportFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	@RequestMapping(value = "/allStudents", method = RequestMethod.GET)
	void downloadAllStudents(Map<String, Object> model, @RequestParam(name = "session") String session,
			@RequestParam(name = "course") String course, HttpServletResponse response, HttpServletRequest request)
			throws IOException {
		populateCommonPageFields(model, request);

		List<Student> students = studRepo.findByCourseAndSession(course, session);

		String outputFileName = "C:\\Users\\avik\\" + "allStudents.csv";
		File reportFile = new File(outputFileName);

		try {
			// create FileWriter object with file as parameter
			FileWriter outputfile = new FileWriter(reportFile);
			// create CSVWriter object filewriter object as parameter
			CSVWriter writer = new CSVWriter(outputfile);
			// create a List which contains String array
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "StudentID", "Course", "Name", "Father's Name", "Mother's Name", "Date of Birth",
					"Gender", "Religion", "Category", "Mobile", "Email Address", "Guardian Contact", "Blood Group",
					"Language", "Nationality", "Application Type", "Aadhaar Number", "Address", "Alternate Address",
					"Last Registration Number", "Subject", "Last School Name", "Session", "Course Fee", "Status",
					"Degree", "Board", "Year", "Total Marks", "Marks Obtained", "Percentage", "Degree", "Board", "Year",
					"Total Marks", "Marks Obtained", "Percentage", "Degree", "Board", "Year", "Total Marks",
					"Marks Obtained", "Percentage", "Degree", "Board", "Year", "Total Marks", "Marks Obtained",
					"Percentage" });
			for (Student st : students) {
				// Add Student Details
				data.add(st.ToStringArray());
			}
			writer.writeAll(data);

			// closing writer connection
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "All_Students" + "_" + course + "_" + session.substring(0, 4) + ".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + reportFileName + "\""));
		response.setContentLength((int) reportFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	@RequestMapping(value = "/reverse", method = RequestMethod.GET)
	public String revertTransaction(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestParam(name = "paymentId") String paymentId, HttpServletResponse response,
			HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		}
		Student student = oStudent.get();
		Payment pt = null;
		for (Payment p : student.payments) {
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
		Counter ct = null;
		Optional<Counter> oct = counterRepo.findById(year + "-" + String.valueOf(year + 1).substring(2));
		if (!oct.isPresent()) {
			ct = new Counter();
			ct.id = year + "-" + String.valueOf(year + 1).substring(2);
			ct.nextId++;
		} else {
			ct = oct.get();
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

	@RequestMapping(value = "/collectionReport", method = RequestMethod.GET)
	void generateCollectionReport(Map<String, Object> model,
			@RequestParam(name = "from") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") DateTime from,
			@RequestParam(name = "to") @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") DateTime to,
			HttpServletResponse response, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);

		from = from.withTimeAtStartOfDay();
		to = to.withTimeAtStartOfDay().plusHours(24);
		List<Student> students = studRepo.findByPaymentsTransactionDateBetween(from, to);

		String outputFileName = "C:\\Users\\avik\\" + "collection.csv";
		File reportFile = new File(outputFileName);

		try {
			// create FileWriter object with file as parameter
			FileWriter outputfile = new FileWriter(reportFile);

			// create CSVWriter object filewriter object as parameter
			CSVWriter writer = new CSVWriter(outputfile);

			// create a List which contains String array
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] { "StudentID", "Name", "Mobile", "Course", "Session", "Money Receipt No", "Date",
					"Transaction ID", "Mode", "Purpose", "Accepted By", "Amount", "Late Fee", "Total" });
			double total = 0;
			for (Student st : students) {
				ArrayList<Payment> payments = st.payments;
				if (null != payments) {
					for (Payment payment : payments) {
						if (payment.transactionDate.isAfter(from) && payment.transactionDate.isBefore(to)) {
							// Add Student Details
							double lateFeeAmount = payment.lateFeeAmount != -1 ? payment.lateFeeAmount : 0;
							data.add(new String[] { st.id, st.name, st.mobile, st.course, st.session, payment.paymentId,
									payment.transactionDate.toString(dtfOut), payment.transactionId, payment.mode,
									payment.purpose, payment.acceptedBy, Double.toString(payment.amount),
									Double.toString(lateFeeAmount), Double.toString(lateFeeAmount + payment.amount) });

							if (payment.purpose.contains("Course Fee Refund")) {
								total -= lateFeeAmount + payment.amount;
							} else {
								total += lateFeeAmount + payment.amount;
							}

						}
					}
				}
			}
			data.add(new String[] { "", "", "", "", "", "", "", "", "", "", "", "", "", "" });
			data.add(new String[] { "Total", "", "", "", "", "", "", "", "", "", "", "", "", Double.toString(total) });
			writer.writeAll(data);

			// closing writer connection
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Download section
		String mimeType = "text/csv";
		response.setContentType(mimeType);
		String reportFileName = "Collection" + "_" + from.getDayOfMonth() + "_" + from.getMonthOfYear() + "_"
				+ to.minusHours(24).getDayOfMonth() + "_" + to.getMonthOfYear() + ".csv";
		response.setHeader("Content-Disposition", String.format("attachment; filename=\"" + reportFileName + "\""));
		response.setContentLength((int) reportFile.length());
		InputStream inputStream = new BufferedInputStream(new FileInputStream(reportFile));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();

		model.put("alert", "alert alert-success");
		model.put("result", "Report Generated Successfully!");
	}

	@RequestMapping(value = "/studentDetails", method = RequestMethod.GET)
	String getStudentDetails(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			HttpServletRequest request) throws IOException, ParseException {
		populateCommonPageFields(model, request);

		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
		} else {
			Student student = oStudent.get();
			StudentDTO st = new StudentDTO().replicate(student);
			st.isModification = true;
			model.put("student", st);
		}
		return "layout/registrationForm";
	}

	@RequestMapping(value = "/modifyStudentDetails", method = RequestMethod.POST)
	String modifyStudentDetails(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestBody Student newStudent, HttpServletRequest request) throws IOException, ParseException {
		populateCommonPageFields(model, request);

		Optional<Student> oOldStudent = studRepo.findById(studentId);
		if (!oOldStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "create";
		}
		Student oldStudent = oOldStudent.get();
		if (newStudent.name.isEmpty() || newStudent.father.isEmpty() || newStudent.mother.isEmpty()
				|| newStudent.mobile.isEmpty() || newStudent.email.isEmpty() || newStudent.aadhaar.isEmpty()
				|| newStudent.courseFee == 0) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Please fill the mandatory fields!");
			return "create";
		}
		boolean idChanged = false;

		ChangeHistory ch = new ChangeHistory();
		ch.time = DateTime.now();
		ch.user = request.getRemoteUser();
		ch.operationType = ChangeHistory.Operation.MODIFIED;

		if (!newStudent.session.equals(oldStudent.session) || !newStudent.course.equals(oldStudent.course)) {

			if (!newStudent.session.equals(oldStudent.session)) {
				ch.message = "Session Modified from " + oldStudent.session + " to " + newStudent.session;
			}
			if (!newStudent.course.equals(oldStudent.course)) {
				ch.message = "Course Modified from " + oldStudent.course + " to " + newStudent.course;
			}
			// Change in ID
			String id_prefix = newStudent.session.substring(0, 4);
			// Counter is used to get the next id to be assigned for a new student based on
			// session and course
			Counter ct = null;
			Optional<Counter> oct = null;
			if (newStudent.course.equalsIgnoreCase("B.Pharm")) {
				id_prefix = id_prefix + "01";
				oct = counterRepo.findById(id_prefix);
			} else {
				id_prefix = id_prefix + "02";
				oct = counterRepo.findById(id_prefix);
			}
			// If counter config not exists, create one
			if (!oct.isPresent()) {
				ct = new Counter();
				ct.id = id_prefix;
				ct.nextId++;
			} else {
				ct = oct.get();
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
				model.put("result", "Student Information Modified, ID is changed to " + newStudent.id);
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

	@RequestMapping(value = "/sendSMS", method = RequestMethod.POST)
	String sendSMS(Map<String, Object> model, @RequestBody SMSDTO smsDTO, HttpServletRequest request)
			throws IOException {
		populateCommonPageFields(model, request);
		RootTemplate<NoticeBody> template = new RootTemplate<NoticeBody>();
		if (smsDTO.noticePrefix.equals("1")) {
			template.flowType = RootTemplate.FlowType.NOTICE_ENG;
		} else {
			template.flowType = RootTemplate.FlowType.NOTICE_BNG;
		}
		template.recipients = new ArrayList<NoticeBody>();

		if (!smsDTO.onlyAddiNumbers) {
			List<Student> students = studRepo.findByCourseAndSession(smsDTO.course, smsDTO.session);
			if (null == students) {
				model.put("alert", "alert alert-danger");
				model.put("result", "No Students found!");
				return "message";
			}
			for (Student st : students) {
				if (st.status == Student.Status.ACTIVE) {
					NoticeBody nb = new NoticeBody();
					nb.mobiles = "91" + st.mobile;
					nb.message = smsDTO.message;
					template.recipients.add(nb);
				}
			}
		}
		String[] additionalNumbers = smsDTO.additionalNumbers.split(",");
		for (String number : additionalNumbers) {
			NoticeBody nb = new NoticeBody();
			nb.mobiles = "91" + number.trim();
			nb.message = smsDTO.message;
			template.recipients.add(nb);
		}
		int perSMSCredit = ((smsDTO.prefixLength + smsDTO.message.length()) / 160) + 1;
		String msg = sms.send(template, smsEnabled, collegeShortName, smsProvisionCount, counterRepo, perSMSCredit);
		if (!msg.isEmpty()) {
			model.put("alert", "alert alert-danger");
			model.put("result", msg);
			return "message";
		}

		model.put("alert", "alert alert-success");
		model.put("result", "SMSs are being sent");
		return "message";
	}

	@RequestMapping(value = "/remindDue", method = RequestMethod.GET)
	String sendDueReminderSMS(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestParam(name = "amount") double amount, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "message";
		}
		Student student = oStudent.get();
		RootTemplate<DueReminderBody> template = new RootTemplate<DueReminderBody>();
		template.flowType = RootTemplate.FlowType.DUE_REMINDER;
		template.recipients = new ArrayList<DueReminderBody>();

		DueReminderBody drb = new DueReminderBody();
		drb.mobiles = "91" + student.mobile;
		drb.college = collegeShortName;
		drb.due = amount;
		drb.date = DateTime.now().toString("dd-MM-yyyy");
		template.recipients.add(drb);

		String msg = sms.send(template, smsEnabled, collegeShortName, smsProvisionCount, counterRepo);
		if (!msg.isEmpty()) {
			model.put("alert", "alert alert-danger");
			model.put("result", msg);
			return "message";
		}

		model.put("alert", "alert alert-success");
		model.put("result", "SMSs are being sent");
		return "message";
	}

	@RequestMapping(value = "/changeStatus", method = RequestMethod.GET)
	String changeStatus(Map<String, Object> model, @RequestParam(name = "id") String studentId,
			@RequestParam(name = "status") Student.Status status, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		Optional<Student> oStudent = studRepo.findById(studentId);
		if (!oStudent.isPresent()) {
			model.put("alert", "alert alert-danger");
			model.put("result", "Student not found!");
			return "message";
		}
		Student student = oStudent.get();
		student.status = status;
		studRepo.save(student);

		model.put("alert", "alert alert-success");
		model.put("result", "Status Changed");
		return "message";
	}

	public double GetPaid(Student student) {
		ArrayList<Payment> payments = student.payments;
		double paid = 0;
		if (null != payments) {
			for (Payment payment : payments) {
				if (!payment.isActive) {
					continue;
				}
				if (payment.purpose.equals("Examination Fee") || payment.purpose.equals("Registration Fee")
						|| payment.purpose.equals("Concession") || payment.purpose.equals("Online Application Fee")
						|| payment.purpose.equals("Late Fee") || payment.purpose.equals("Prospectus Fee")) {
					continue;
				}
				if (payment.purpose.contains("Miscellaneous Fee")) {
					continue;
				}
				if (payment.purpose.contains("Course Fee Refund")) {
					paid -= payment.amount;
					continue;
				}
				paid += payment.amount;
			}
		}
		return paid;
	}

	// Post Teaching School details
	@RequestMapping(value = "/postTeachingSchool", method = RequestMethod.POST)
	@ResponseBody
	public String postTeachingSchool(@RequestBody TeachingSchool teachingSchool) {
		// Check if name not empty and latitiude and longitude not 0
		if (teachingSchool.name.isEmpty() || teachingSchool.latitude == 0 || teachingSchool.longitude == 0) {
			return "Teaching School details not saved!";
		}
		// Check if Teaching School already exists
		TeachingSchool ts = teachingSchoolRepo.findByName(teachingSchool.name);
		if (null != ts) {
			return "Teaching School details already exist!";
		}

		teachingSchoolRepo.save(teachingSchool);
		return "Teaching School details saved successfully!";
	}

	// Get all Teaching School details
	@RequestMapping(value = "/getTeachingSchools", method = RequestMethod.GET)
	@ResponseBody
	public List<TeachingSchool> getTeachingSchools() {
		List<TeachingSchool> teachingSchools = teachingSchoolRepo.findAll();
		return teachingSchools;
	}

	// Get Teaching School details by name
	@RequestMapping(value = "/getTeachingSchool", method = RequestMethod.GET)
	@ResponseBody
	public TeachingSchool getTeachingSchool(@RequestParam(name = "name") String name) {
		TeachingSchool teachingSchool = teachingSchoolRepo.findByName(name);
		return teachingSchool;
	}

	// Post Notice details
	@RequestMapping(value = "/postNotice", method = RequestMethod.POST)
	@ResponseBody
	public String postNotice(@RequestBody Notice notice) {
		// Check if course, session and content is not empty
		if (notice.course.isEmpty() || notice.session.isEmpty() || notice.content.isEmpty()) {
			return "Notice details missing!";
		}
		notice.date = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));

		noticeRepo.save(notice);
		return "Notice details saved successfully!";
	}

	// Get Notice Page
	@RequestMapping(value = "/getNoticePage", method = RequestMethod.GET)
	String getNoticePage(Map<String, Object> model, HttpServletRequest request) throws IOException {
		populateCommonPageFields(model, request);
		List<Notice> notices = noticeRepo.findAll();
		model.put("notices", notices);
		return "notice";
	}

	// Get Notice by course and Session
	@RequestMapping(value = "/getNotices", method = RequestMethod.GET)
	@ResponseBody
	public List<Notice> getNotices(@RequestParam(name = "course") String course,
			@RequestParam(name = "session") String session) {
		List<Notice> notices = noticeRepo.findByCourseAndSession(course, session);
		return notices;
	}

	/* App related funtions starts here */
	// Get Student Details
	@RequestMapping(value = "/getStudentDetails", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getStudentDetails(@RequestParam(name = "id") String studentId) {
		Student student = studRepo.findByEmail(studentId);
		// Check if student exists
		if (student == null) {
			return null;
		}
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("student", student);
		model.put("due", student.courseFee - GetPaid(student));
		model.put("teachingSchool", teachingSchoolRepo.findByName(student.teachingSchool));
		// send last 5 notices based on course and session
		model.put("notices", noticeRepo.findTop5ByCourseAndSessionOrderByDateDesc(student.course, student.session));
		// Send last 5 attendance
		model.put("attendance", attendanceRepo.findTop5ByStudentIdOrderByTimeDesc(studentId));
		return model;
	}

	// Register Attendance with location
	@RequestMapping(value = "/registerAttendance", method = RequestMethod.POST)
	@ResponseBody
	public String registerAttendance(@RequestBody AttendanceDTO attendanceDTO) {
		Student student = studRepo.findByEmail(attendanceDTO.studentId);
		if (student == null) {
			return "Student not found!";
		}
		Attendance attendance = new Attendance();
		attendance.studentId = attendanceDTO.studentId;
		attendance.school = attendanceDTO.school;
		attendance.schoolName = attendanceDTO.schoolName;
		attendance.course = attendanceDTO.course;
		attendance.session = attendanceDTO.session;
		attendance.time = (DateTime) DateTime.now().withZone(DateTimeZone.forID("Asia/Kolkata"));
		attendance.latitude = attendanceDTO.latitude;
		attendance.longitude = attendanceDTO.longitude;
		attendanceRepo.save(attendance);
		return "Attendance Registered";
	}
}
