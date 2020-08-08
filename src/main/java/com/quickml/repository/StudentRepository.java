package com.quickml.repository;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Student;


@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
	List<Student> findByCourseAndSession(String course, String session);

	List<Student> findByMobile(String mobile);

	@Query(value = "{ 'payments.transactionDate' : {$gte : ?0, $lte: ?1 } }", fields = "{ 'course' : 1, 'payments.amount' : 1 , 'payments.transactionDate' : 1}")
	List<Student> findByPaymentsTransactionDateBetween(DateTime from, DateTime to);
}

