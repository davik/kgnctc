package com.quickml.pojos;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "students")
public class Student {
	public String firstName;
	public String lastName;
}