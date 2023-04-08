package com.quickml.pojos;

import java.util.ArrayList;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.quickml.controller.WelcomeController;

@Document(collection = "students")
public class Student {
	public enum Status {
		ACTIVE,
		GRADUATE,
		DROPOUT
	}
	@Id
	public String id;
	public String course = "";
	public String name = "";
	public String father = "";
	public String mother = "";
	@org.springframework.format.annotation.DateTimeFormat(pattern="yyyy-MM-dd")
	public DateTime dob;
	public String gender = "";
	public String religion = "";
	public String category = "";
	@Indexed
	public String mobile = "";
	public String email = "";
	public String guardianContact = "";
	public String blood = "";
	public String language = "Bengali";
	public String nationality = "Indian";
	public String applicationType = "";
	public String aadhaar = "";
	public String address1 = "";
	public String address2 = "";
	public ArrayList<Academic> academics;
	public String lastRegNo = "";
	public String subject = "";
	public String lastSchoolName = "";
	public String referredBy = "";
	public String session = "";
	public ArrayList<Payment> payments = new ArrayList<Payment>();
	public double courseFee;
	public double familyIncome;
	public Status status = Status.ACTIVE;
	public String teachingSchool = "";
	
	public ArrayList<ChangeHistory> changeHistory= new ArrayList<>();

	public String[] ToStringArray() {
		ArrayList<String> arrayList = new ArrayList<> (Arrays.asList(id, course, name, father, mother,
				dob != null ? dob.toString(WelcomeController.dtfOut) : "", gender, religion, category, mobile,
				email, guardianContact, blood, language, nationality,
				applicationType, aadhaar, address1, address2,
				lastRegNo, subject, lastSchoolName, referredBy, session, Double.toString(courseFee), status.toString()));
		for (Academic ac : academics) {
			double percentage = 0;
			double total = 0;
			double marks = 0;
			try {
				total = Double.parseDouble(ac.total);
				marks = Double.parseDouble(ac.marks);
				if (0 != marks && 0 != total) {
					percentage = (marks/total)*100;
				}
				arrayList.addAll(Arrays.asList(
						ac.name, ac.board, ac.year,
						Double.toString(total),
						Double.toString(marks),
					    String.format("%.2f", percentage)));
			} catch (Exception e) {
				arrayList.addAll(Arrays.asList(
						ac.name, ac.board, ac.year,
						"", "", ""));
			}
		}

		return arrayList.toArray(new String[arrayList.size()]);
	}
}
