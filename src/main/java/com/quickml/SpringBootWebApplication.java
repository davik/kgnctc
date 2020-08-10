package com.quickml;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;


@SpringBootApplication
public class SpringBootWebApplication {
	
	 @Autowired
	  private ObjectMapper objectMapper;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}

	@PostConstruct
	  public void setUp() {
	    objectMapper.registerModule(new JodaModule());
	}
}