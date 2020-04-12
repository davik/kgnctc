package com.quickml.pojos;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
	@Id
	public String id;
	public String username;
	public String password;
	public String fullname;
	public boolean enabled = false;
}
