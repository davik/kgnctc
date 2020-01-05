package com.quickml;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Student;


@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
}

