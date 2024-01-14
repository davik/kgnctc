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
	public String purpose; // Tuition Fee, Exam Fee
	public String acceptedBy = "admin";
	public boolean isActive = true; // Payments which are Reversed and the Reverse Payment are NOT Active
	public double lateFeeAmount = -1; // If amount == -1 then late fee is not incurred
}