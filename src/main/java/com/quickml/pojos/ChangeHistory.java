package com.quickml.pojos;

import org.joda.time.DateTime;

public class ChangeHistory {
	public enum Operation {
		CREATED,
		MODIFIED,
		DELETED
	}
	public DateTime time = DateTime.now();
	public String message = "";
	public String user = "";
	public Operation operationType = Operation.CREATED;
}
