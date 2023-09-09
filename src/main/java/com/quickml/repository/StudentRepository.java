package com.quickml.repository;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.quickml.pojos.Student;
import com.quickml.pojos.Student.Status;

@Repository
public interface StudentRepository extends MongoRepository<Student, String> {
	List<Student> findByCourseAndSession(String course, String session);

	List<Student> findByCourseAndSessionAndStatus(String course, String session, Status status);

	List<Student> findByStatus(Status status);

	List<Student> findByMobile(String mobile);

	Student findByEmail(String email);

	@Query(value = "{ 'payments.transactionDate' : {$gte : ?0, $lte: ?1 } }", fields = "{ 'name' : 1, 'mobile' : 1, 'session' : 1, 'course' : 1,"
			+ " 'payments.paymentId' : 1 , 'payments.transactionId' : 1,"
			+ " 'payments.mode' : 1 , 'payments.purpose' : 1," + " 'payments.acceptedBy' : 1 , 'payments.isActive' : 1,"
			+ " 'payments.amount' : 1 , 'payments.transactionDate' : 1," + " 'payments.lateFeeAmount' : 1}")
	List<Student> findByPaymentsTransactionDateBetween(DateTime from, DateTime to);
}
