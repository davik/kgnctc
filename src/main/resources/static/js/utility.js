function call(e, path) {
	e.preventDefault();
	  $.ajax({
		  type: "GET",
		  url: path,
		  success: function(data){$("#main_portion").html(data)},
		  dataType: "text",
		  contentType: "text/plain"
		});
}

$(document).ready(function () {
  // Listen to click event on the submit button
	console.log("heelo");
	$('#msg').hide();
  $('#button').click(function (e) {

    e.preventDefault();

    var coursen = $("#courseBed").is(":checked") ? 
    		$("#courseBed").val() : $("#courseDed").val();
    var academics = [];
    var mp = {
    	name : "Madhyamik Pariksha/ 10th",
		board : $("#mpboard").val(),
		year : $("#mpyear").val(),
		total : $("#mptotal").val(),
		marks : $("#mpmarks").val(),
		percentage : $("#mppercent").val()
    };
    academics[0] = mp;
    var hs = {
    	name : "Higher Secondary/ 12th",
		board : $("#hsboard").val(),
		year : $("#hsyear").val(),
		total : $("#hstotal").val(),
		marks : $("#hsmarks").val(),
		percentage : $("#hspercent").val()
    };
    academics[1] = hs;
    var gr = {
    	name : "Graduation",
		board : $("#gradboard").val(),
		year : $("#gradyear").val(),
		total : $("#gradtotal").val(),
		marks : $("#gradmarks").val(),
		percentage : $("#gradpercent").val()
    };
    academics[2] = gr;
    var pg = {
    	name : "Post Graduation",
		board : $("#pgboard").val(),
		year : $("#pgyear").val(),
		total : $("#pgtotal").val(),
		marks : $("#pgmarks").val(),
		percentage : $("#pgpercent").val()
    };
    academics[3] = pg;
    var student = {course: coursen,
    		name: 		$("#name").val(),
    		father: 	$("#father").val(),
    		mother: 	$("#mother").val(),
    		dob: 		$("#dob").val(),
    		gender: 	$("#gender").val(),
    		religion: 	$("#religion").val(),
    		category: 	$("#category").val(),
    		mobile: 	$("#mobile").val(),
    		email: 		$("#email").val(),
    		guardianContact: $("#guardianContact").val(),
    		blood: 		$("#blood").val(),
    		language: 	$("#language").val(),
    		nationality: $("#nationality").val(),
    		applicationType: $("#type").val(),
    		aadhaar: $("#aadhaar").val(),
    		address1: $("#address1").val(),
    		address2: $("#address2").val(),
    		academics: academics
    		};
    console.log(student);

   $.ajax({
		  type: "POST",
		  url: "/create",
		  data: JSON.stringify(student),
		  // success: function(rsp){$("#msg").append(rsp)},
		  success: $('#msg').show(),
		  dataType: "json",
		  contentType: "application/json"
		});
  }); 
});

