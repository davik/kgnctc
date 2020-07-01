package com.quickml.pojos;


import org.joda.time.DateTime;

public class Payment {
	public String paymentId;
	public DateTime transactionDate;
	public String transactionId;
	public double amount;
	public String mode; // Cash, Cheque, DD etc
	public String purpose; // Course Fee, Exam Fee
}