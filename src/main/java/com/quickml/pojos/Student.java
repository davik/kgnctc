package com.quickml.pojos;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "students")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {
	public String course;
	public String name;
	public String father;
	public String mother;
	public Date dob;
	public String gender;
	public String religion;
	public String caste;
	public String mobile;
	public String email;
	public String guardianContact;
	public String blood;
	public String language;
	public String nationality;
	public String address1;
	public String address2;
	public ArrayList<Academic> academics;
	public String lastRegNo;
	public String subject;
	public String lastSchoolName;
}