package com.quickml.pojos;

import org.joda.time.format.DateTimeFormat;

public class StudentDTO {
	public String id = "";
	public String course = "";
	public String name = "";
	public String father = "";
	public String mother = "";
	public String dob = "";
	public String gender = "";
	public String religion = "";
	public String category = "";
	public String mobile = "";
	public String email = "";
	public String guardianContact = "";
	public String blood = "";
	public String language = "Bengali";
	public String nationality = "Indian";
	public String applicationType = "";
	public String aadhaar = "";
	public String address1 = "";
	public String address2 = "";
	public String lastRegNo = "";
	public String subject = "";
	public String lastSchoolName = "";
	public String referredBy = "";
	public String session = "";
	public double courseFee;
	public double convenienceFee;
	public double dayBoardingFee;
	public double familyIncome;

	public String mpboard = "";
	public String mpyear = "";
	public String mptotal = "";
	public String mpmarks = "";

	public String hsboard = "";
	public String hsyear = "";
	public String hstotal = "";
	public String hsmarks = "";

	public String gradboard = "";
	public String gradyear = "";
	public String gradtotal = "";
	public String gradmarks = "";

	public String pgboard = "";
	public String pgyear = "";
	public String pgtotal = "";
	public String pgmarks = "";

	public boolean isModification = false;

	public StudentDTO replicate(Student st) {
		this.id = st.id;
		this.course = st.course;
		this.name = st.name;
		this.father = st.father;
		this.mother = st.mother;
		if (st.dob != null) {
			this.dob = st.dob.toString(DateTimeFormat.forPattern("YYYY-MM-dd"));
		}
		this.gender = st.gender;
		this.religion = st.religion;
		this.category = st.category;
		this.mobile = st.mobile;
		this.email = st.email;
		this.guardianContact = st.guardianContact;
		this.blood = st.blood;
		this.language = st.language;
		this.nationality = st.nationality;
		this.applicationType = st.applicationType;
		this.aadhaar = st.aadhaar;
		this.address1 = st.address1;
		this.address2 = st.address2;
		this.lastRegNo = st.lastRegNo;
		this.lastSchoolName = st.lastSchoolName;
		this.referredBy = st.referredBy;
		this.session = st.session;
		this.courseFee = st.courseFee;
		this.convenienceFee = st.convenienceFee;
		this.dayBoardingFee = st.dayBoardingFee;
		this.familyIncome = st.familyIncome;

		if (null != st.academics) {
			if (st.academics.get(0).board != null) {
				this.mpboard = st.academics.get(0).board;
				this.mpyear = st.academics.get(0).year;
				this.mptotal = st.academics.get(0).total;
				this.mpmarks = st.academics.get(0).marks;
			}

			if (st.academics.get(1).board != null) {
				this.hsboard = st.academics.get(1).board;
				this.hsyear = st.academics.get(1).year;
				this.hstotal = st.academics.get(1).total;
				this.hsmarks = st.academics.get(1).marks;
			}

			if (st.academics.get(2).board != null) {
				this.gradboard = st.academics.get(2).board;
				this.gradyear = st.academics.get(2).year;
				this.gradtotal = st.academics.get(2).total;
				this.gradmarks = st.academics.get(2).marks;
			}

			if (st.academics.get(3).board != null) {
				this.pgboard = st.academics.get(3).board;
				this.pgyear = st.academics.get(3).year;
				this.pgtotal = st.academics.get(3).total;
				this.pgmarks = st.academics.get(3).marks;
			}
		}
		return this;
	}
}
