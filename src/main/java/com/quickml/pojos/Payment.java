package com.quickml.pojos;

import java.util.Date;

public class Payment {
	public Date transactionDate;
	public String transactionId;
	public double amount;
	public String mode; // Cash, Cheque, DD etc
	public String purpose; // Course Fee, Exam Fee
}