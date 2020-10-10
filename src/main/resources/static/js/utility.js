
$.ajaxSetup({
    beforeSend: function(xhr) {
        var token = $("meta[name='_csrf']").attr("content");
        xhr.setRequestHeader('X-CSRF-TOKEN', token);
    }
});

function fetchPaymentDetail(id) {
    $('#nav-profile-tab').trigger('click');
    $('#id').val(id);
    $('#idSearch').click();
}

function fetchStudentDetail(id) {
    $('#nav-edit-tab').trigger('click');
    $('#idStudent').val(id);
    $('#idSearchEdit').click();
}

function call(e, path) {
    e.preventDefault();
    $.ajax({
        type: "GET",
        url: path,
        success: function(data) {
            $("#main_portion").html(data)
        },
        dataType: "text",
        contentType: "text/plain"
    });
}

function showPaymentForm() {
    $('.alert').hide();
    $('#msgPayment').hide();
    $('#paymentForm').show();
    $('html,body').animate({
        scrollTop: $("#paymentForm").offset().top
    }, 'slow');
}

function downloadInvoice(paymentid) {
    $.ajax({
            type: "POST",
            url: '/invoice?' + $.param({
                id: $('#id').val(),
                paymentId: paymentid
            })
        });
}

function createPayment(event) {
	$('#paymentButton').prop('disabled', true);
    let payment = {
        transactionId: $("#transactionId").val(),
        amount: $("#amount").val(),
        mode: $("#mode").val(),
        purpose: $("#purpose").val()
    };
    console.log(payment);

    $.ajax({
        type: "POST",
        url: "/createPayment?" + $.param({
            id: $('#id').val()
        }),
        data: JSON.stringify(payment),
        success: function(data) {
            $("#msgPayment").show();
            $('#msgPayment').html(data);
            $('html,body').animate({
                scrollTop: $("#msgPayment").offset().top
            }, 'slow');
            setTimeout(function() {
                $("#msgPayment").hide();
                $('#idSearch').click();
            }, 3000);
            $("#transactionId").val(''),
                $("#amount").val('');
        },
        contentType: "application/json"
    });
}

function handleAddiNumChk() {
    if ($('#onlyAddiNumbers').is(':checked')) {
        $("#course").prop("disabled", true);
        $("#session").prop("disabled", true);
    } else {
        $("#course").prop("disabled", false);
        $("#session").prop("disabled", false);
    }
}

function sendSMS() {
    $('#smsButton').prop('disabled', true);
    let sms = {
        course: $("#course").val(),
        session: $("#session").val(),
        onlyAddiNumbers: $('#onlyAddiNumbers').is(':checked'),
        additionalNumbers: $("#additionalNumbers").val(),
        noticePrefix: $("#noticePrefix").val(),
        prefixLength: $('#noticePrefix option:selected').text().length,
        message: $("#smsBody").val()
    };
    console.log(sms);

    $.ajax({
        type: "POST",
        url: "/sendSMS",
        data: JSON.stringify(sms),
        success: function(data) {
            $("#msg").show();
            $('#msg').html(data);
            $('html,body').animate({
                scrollTop: $("#msg").offset().top
            }, 'slow');
            setTimeout(function() {
                $("#msg").hide();
            }, 3000);
        },
        contentType: "application/json"
    });
}

function changeAncAttSess() {
    let url = '/payDueReport?' + $.param({
                course: $('#course').val(),
                session: $('#session').val()
            });
    $('#paydue').attr({target: '_blank', href: url});
}

function changeAncAttSessStudentDetails() {
    let url = '/allStudents?' + $.param({
                course: $('#r2course').val(),
                session: $('#r2session').val()
            });
    $('#studentDetail').attr({target: '_blank', href: url});
}

function changeAncAttDailyCollection() {
    $('#to').attr({min: $('#from').val()});
    $('#to').attr({max: $('#from').attr('max')});
    if ($('#from').val() && $('#to').val()) {
        $('#collectionReport').removeClass("disabled");
    } else {
        $('#collectionReport').addClass("disabled");
    }
    let url = '/collectionReport?' + $.param({
                from: $('#from').val(),
                to: $('#to').val()
            });
    $('#collectionReport').attr({target: '_blank', href: url});
}

function fetchStudents() {
    let url = '/studentList?' + $.param({
                course: $('#course').val(),
                session: $('#session').val()
            });
    $.ajax({
        type: "GET",
        url: url,
        success: function(data) {
            $(".table").html(data);
        },
        contentType: "application/json"
    });
}

function onPaymentCh() {
    if ($('#purpose').val() == 'Concession') {
       $('.alert').show();
       $('.alert').addClass('show');
       $('#mode').attr("disabled", "disabled");
       $('#transactionId').attr("readonly", true);
    } else {
        $('.alert').hide();
        $('.alert').removeClass('show');
        $('#mode').removeAttr("disabled");
        $('#transactionId').removeAttr("readonly");
    }
}

function createStudent(e) {

    e.preventDefault();

    let coursen = $("#courseBed").is(":checked") ?
        $("#courseBed").val() : $("#courseDed").val();
    let academics = [];
    let mp = {
        name: "Madhyamik Pariksha/ 10th",
        board: $("#mpboard").val(),
        year: $("#mpyear").val(),
        total: $("#mptotal").val(),
        marks: $("#mpmarks").val()
    };
    academics[0] = mp;
    let hs = {
        name: "Higher Secondary/ 12th",
        board: $("#hsboard").val(),
        year: $("#hsyear").val(),
        total: $("#hstotal").val(),
        marks: $("#hsmarks").val()
    };
    academics[1] = hs;
    let gr = {
        name: "Graduation",
        board: $("#gradboard").val(),
        year: $("#gradyear").val(),
        total: $("#gradtotal").val(),
        marks: $("#gradmarks").val()
    };
    academics[2] = gr;
    let pg = {
        name: "Post Graduation",
        board: $("#pgboard").val(),
        year: $("#pgyear").val(),
        total: $("#pgtotal").val(),
        marks: $("#pgmarks").val()
    };
    academics[3] = pg;
    let student = {
        course: coursen,
        name: $("#name").val(),
        father: $("#father").val(),
        mother: $("#mother").val(),
        dob: $("#dob").val(),
        gender: $("#gender").val(),
        religion: $("#religion").val(),
        category: $("#category").val(),
        mobile: $("#mobile").val(),
        email: $("#email").val(),
        guardianContact: $("#guardianContact").val(),
        blood: $("#blood").val(),
        language: $("#language").val(),
        nationality: $("#nationality").val(),
        applicationType: $("#type").val(),
        aadhaar: $("#aadhaar").val(),
        address1: $("#address1").val(),
        address2: $("#address2").val(),
        academics: academics,
        session: $('#sessionReg').val(),
        lastRegNo: $('#regLast').val(),
        subject: $('#subject').val(),
        lastSchoolName: $('#schoolName').val(),
        courseFee: $('#courseFee').val(),
        familyIncome: $('#familyIncome').val()
    };
    console.log(student);

    let url;
    if ($('#button').data('id') == '') {
        url = "/create"
    } else {
        url = "modifyStudentDetails?" + $.param({
                id: $('#button').data('id')
            })
    }
    $.ajax({
        type: "POST",
        url: url,
        data: JSON.stringify(student),
        success: function(data) {
            $("#msg").show();
            $('#msg').html(data);
            $('html,body').animate({
                scrollTop: $("#msg").offset().top
            }, 'slow');
            $('.clearit').val('');
            setTimeout(function() {
                $("#msg").hide();
            }, 3000);
        },
        contentType: "application/json"
    });
}

$(document).ready(function() {
    $("body").tooltip({ selector: '[data-toggle=tooltip]' });
    $('[data-toggle="tooltip"]').tooltip({
        trigger : 'hover'
    });
    // Listen to click event on the submit button
    $('#button').click(createStudent);

    $('#idSearch').click(function(e) {
        e.preventDefault();
        $.ajax({
            type: "GET",
            url: '/paymentDetails?' + $.param({
                id: $('#id').val()
            }),
            success: function(data) {
                $('#paymentDetail').html(data);
                $(".convertTime").each(function() {
                    let utcDate = $(this).text();
                    let localDate = new Date(utcDate);
                    let x = localDate.toLocaleDateString('en-GB', {
                        day: '2-digit', month: 'short', year: 'numeric'
                      }).replace(/ /g, '-');
                    $(this).text(x);
                });
                $('#paymentForm').hide();
            },
            contentType: "application/json"
        });
    });

    $('#idSearchEdit').click(function(e) {
        e.preventDefault();
        $.ajax({
            type: "GET",
            url: '/studentDetails?' + $.param({
                id: $('#idStudent').val()
            }),
            success: function(data) {
                $('#studentDetail').html(data);
                // Select gender
                let findString = 'option[value="'+$('#gender').data('gender')+'"]';
                $('#gender').find(findString).attr("selected",true);
                // Select Religion
                findString = 'option[value="'+$('#religion').data('religion')+'"]';
                $('#religion').find(findString).attr("selected",true);
                // Select Category
                findString = 'option[value="'+$('#category').data('category')+'"]';
                $('#category').find(findString).attr("selected",true);
                // Select Blood
                findString = 'option[value="'+$('#blood').data('blood')+'"]';
                $('#blood').find(findString).attr("selected",true);
                // Select Application Type
                findString = 'option[value="'+$('#type').data('applicationtype')+'"]';
                $('#type').find(findString).attr("selected",true);
                // Select Session
                findString = 'option[value="'+$('#sessionReg').data('session')+'"]';
                $('#sessionReg').find(findString).attr("selected",true);
                // Select Course
                $('input[value="'+$("#course").data("course")+'"]').attr("checked",true);
                // Listen to click event on the submit button
                $('#button').click(createStudent);
            },
            contentType: "application/json"
        });
    });

    $("#studentSearch").on("keyup", function() {
        let value = $(this).val().toLowerCase();
        $("#studentTable tr").filter(function() {
          $(this).toggle($(this).text().toLowerCase().indexOf(value) > -1)
        });
      });

    $("body").on('show.bs.modal', "#exampleModal", function(event) {
        $('#reverseMsg').hide();
        let button = $(event.relatedTarget); // Button that triggered the modal
        let url = button.data('url'); // Extract info from data-* attributes
        let amount = button.data('amount');

        let modal = $(this);
        modal.find('.amount').text(amount);
        modal.find('.reverse').attr({href: url});
    });


    $("body").on('click', "#reverse", function(event) {
        event.preventDefault();
        let url = $(this).attr('href');
        $(this).addClass('disabled');
        $.ajax({
            type: "GET",
            url: url,
            success: function(data) {
                $("#reverseMsg").show();
                $('#reverseMsg').html(data);
                $('.clearit').val('');
                setTimeout(function() {
                    $("#reverseMsg").hide();
                    $('#exampleModal').modal('hide');
                    $('.modal-backdrop').remove();
                    $("#idSearch").click();
                }, 3000);
            }
        });
    });

    $("body").on('show.bs.modal', "#DueReminderModal", function(event) {
        $('#reverseMsg').hide();
        let button = $(event.relatedTarget); // Button that triggered the modal
        let url = button.data('url'); // Extract info from data-* attributes
        let amount = button.data('amount');

        let modal = $(this);
        modal.find('.amount').text(amount);
        modal.find('.reverse').attr({href: url});
    });

    $("#from").attr({max: function(){
        let today = new Date();
        let dd = today.getDate(); // Today is allowed
        let mm = today.getMonth()+1; //January is 0!
        let yyyy = today.getFullYear();
         if(dd<10){
                dd='0'+ dd
            }
            if(mm<10){
                mm='0'+ mm
            }

        today = yyyy+'-'+mm+'-'+dd;
        return today;
    }});

    
    $('#smsBody').keyup(function() {
        let characterCount = $('#noticePrefix option:selected').text().length + $(this).val().length,
          current = $('#current'),
          maximum = $('#maximum'),
          theCount = $('#the-count'),
          credit = $('#credit');

        current.text(characterCount);
        credit.text(Math.ceil(characterCount/160));

        if (characterCount > 160) {
            maximum.css('color', '#8f0001');
            current.css('color', '#8f0001');
            theCount.css('font-weight','bold');
        } else {
            maximum.css('color', '#666');
            current.css('color', '#666');
            theCount.css('font-weight','normal');
        }

          
    });
});