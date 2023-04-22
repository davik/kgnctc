package com.quickml.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Attendance;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    // find latest attendance by studentId
    @Query(value = "{ 'studentId' : ?0 }", fields = "{ 'studentId' : 1, 'course' : 1, 'session' : 1, 'time' : 1, 'schoolName' : 1 }", sort = "{ 'time' : -1 }")
    List<Attendance> findLatest5ByStudentId(String studentId);

    // find latest attendance by studentId
    List<Attendance> findTop5ByStudentIdOrderByTimeDesc(String studentId);

}
