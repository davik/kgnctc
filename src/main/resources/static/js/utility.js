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
  $('#button').click(function (e) {

    e.preventDefault();

    var coursen = $("#courseBed").is(":checked") ? 
    		$("#courseBed").val() : $("#courseDed").val();
    var student = {course: coursen,
    		name: $("#name").val(),
    		father: $("#father").val(),
    		mother: $("#mother").val(),
    		dob: $("#dob").val(),
    		gender: $("#gender").val(),
    		religion: $("#religion").val(),
    		caste: $("#caste").val(),
    		mobile: $("#mobile").val(),
    		email: $("#email").val(),
    		guardianContact: $("#guardianContact").val(),
    		blood: $("#blood").val(),
    		language: $("#language").val(),
    		nationality: $("#nationality").val()};
    console.log(student);

//    $.ajax({
//		  type: "POST",
//		  url: "/desc",
//		  data: JSON.stringify(student),
//		  success: function(rsp){$("#desc").html(rsp)},
//		  dataType: "json",
//		  contentType: "application/json"
//		});
  }); 
});

