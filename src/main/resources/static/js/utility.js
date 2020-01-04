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
  $('#button').click(function (e) {

    e.preventDefault();

    var student = {firstName: $("#firstName").val(),
    			   lastName: $("#lastName").val()};
    console.log(student);

    $.ajax({
		  type: "POST",
		  url: "/desc",
		  data: JSON.stringify(student),
		  success: function(rsp){$("#desc").html(rsp)},
		  dataType: "json",
		  contentType: "application/json"
		});
    $.ajax({
		  type: "GET",
		  url: "/summary",
		  success: function(data){$("#summary").html(data)},
		});
  }); 
});
