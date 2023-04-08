package com.quickml.pojos;

import org.joda.time.DateTime;

@org.springframework.data.mongodb.core.mapping.Document(collection = "notices")
public class Notice {
    @org.springframework.data.annotation.Id
    public String id;
	public String course;
	public String session;
    public DateTime date;
	public String content;
}