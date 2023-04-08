package com.quickml.pojos;


import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "teachingSchools")
public class TeachingSchool {
    @org.springframework.data.annotation.Id
    public String id;
    public String name = "";	
    public String address = "";
    // location
    public double latitude;
    public double longitude;
    // course
    public String course = "";
}