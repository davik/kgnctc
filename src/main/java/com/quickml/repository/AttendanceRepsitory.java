package com.quickml.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Attendance;

@Repository
public interface AttendanceRepsitory extends MongoRepository<Attendance, String> {

}
