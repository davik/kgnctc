package com.quickml.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Student;


@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
	List<Student> findByCourseAndSession(String course, String session);
}

