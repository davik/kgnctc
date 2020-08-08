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
	@Id
	public String id;
	public String course = "";
	public String name = "";
	public String father = "";
	public String mother = "";
	public DateTime dob;
	public String gender = "";
	public String religion = "";
	public String category = "";
	@Indexed
	public String mobile = "";
	public String email = "";
	public String guardianContact = "";
	public String blood = "";
	public String language = "";
	public String nationality = "";
	public String applicationType = "";
	public String aadhaar = "";
	public String address1 = "";
	public String address2 = "";
	public ArrayList<Academic> academics;
	public String lastRegNo = "";
	public String subject = "";
	public String lastSchoolName = "";
	public String session = "";
	public ArrayList<Payment> payments = new ArrayList<Payment>();
	public double courseFee;

	public String[] ToStringArray() {
		ArrayList<String> arrayList = new ArrayList<> (Arrays.asList(id, course, name, father, mother,
				dob.toString(WelcomeController.dtfOut), gender, religion, category, mobile,
				email, guardianContact, blood, language, nationality,
				applicationType, aadhaar, address1, address2,
				lastRegNo, subject, lastSchoolName, session, Double.toString(courseFee)));
		for (Academic ac : academics) {
			double percentage = 0;
			if (null != ac.marks && null != ac.total) {
				percentage = (ac.marks/ac.total)*100;
			}
			arrayList.addAll(Arrays.asList(
					ac.name, ac.board, ac.year,
					null != ac.total ? Double.toString(ac.total): "",
					null != ac.marks ? Double.toString(ac.marks): "",
					Double.toString(percentage)));
		}

		return arrayList.toArray(new String[arrayList.size()]);
	}
}