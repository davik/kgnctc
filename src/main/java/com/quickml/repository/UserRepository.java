package com.quickml.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
	User findByUsername(String username);
}