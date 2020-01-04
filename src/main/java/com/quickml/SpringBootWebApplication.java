package com.quickml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

@SpringBootApplication
public class SpringBootWebApplication {
	public static MongoDatabase database = null;

	public static void main(String[] args) throws Exception {

		MongoClientURI uri = new MongoClientURI(
    "mongodb+srv://avik:<password>@cluster0-z5mfp.mongodb.net/test?retryWrites=true&w=majority");

		MongoClient mongoClient = new MongoClient(uri);
		database = mongoClient.getDatabase("test");

		SpringApplication.run(SpringBootWebApplication.class, args);
	}

}