package com.quickml.pojos;


import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.index.Indexed;

public class Payment {
	public String paymentId;
	@Indexed
	public DateTime transactionDate;
	public String transactionId;
	public double amount;
	public String mode; // Cash, Cheque, DD etc
	public String purpose; // Course Fee, Exam Fee
	public String acceptedBy = "admin";
}