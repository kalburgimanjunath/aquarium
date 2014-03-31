$(document).ready( function(){
	$('#customerform').submit(function(e){
		e.preventDefault();
		var that = this;

		function inviteCustomer(email, project) {
			var form = { email: email, project: project }
			$.ajax({
	      type: 'POST',
	      async: false,
	      url: "/api/customerproject",
	      data: $.param(form, true),
	      success: function(data, status, xhr){
	      	// If data is "customer-exists" -> exists in db so no change.
	      	if (data == "customer-exists") {
	      		$('#cp-alert-div').append('<div class="alert fade in alert-danger"><button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i></button>Customer already exists, but will try to send registration email anyway.</div>');
	      	} else {
	      	 $('#cp-alert-div').append('<div class="alert fade in alert-success"><button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i></button>Customer ' + email + ' added to project database.</div>');
	         }
	        $.ajax({
			      type: 'POST',
			      async: false,
			      url: "/signup",
			      //data: form.serialize(),
			      data: $.param(form, true),
			      success: function(data, status, xhr){
			      	$('#cp-alert-div').append('<div class="alert fade in alert-success"><button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i></button>An e-mail with a sign-up link was sent to ' + email + '.</div>');
			      },
			      error: function(xhr, status, err) {
			      	$('#cp-alert-div').append('<div class="alert fade in alert-success"><button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i></button>Error: No invitation e-mail sent to ' + email + '.</div>');
			      }
			    });
	      },
	      error: function(xhr, status, err) {
	      	$('#cp-alert-div').append('<div class="alert fade in alert-danger"><button type="button" class="close" data-dismiss="alert"><i class="fa fa-times"></i></button>Error: ' + email + ' could not be added to project database.</div>');
	      }
	    }); 
		}

		// Get number of email fields to be added
		var numOfEmailFields = 0; 
		while(that.elements['emails['+numOfEmailFields+']']) { 
		  numOfEmailFields++; 
		}

		// Send invite to each email address and add to database
		for (i = 0 ; i < numOfEmailFields ; i++) {
			var emails = that.elements['emails['+i+']'];
			// Only accept non-empty email addresses
			if (emails.value != '') {
				inviteCustomer(emails.value, that.elements['project'].value);
				emails.value = '';
			}
		}

	  return false;
	});
	
	$('.close').click(function() {
  	$('.alert').hide();
	})

	// Adds an input field when clicking button
	$("#add-customer-btn").click(function() {
		// Get number of last field
		var $counter = $('dd').find('input[type=email]').length;
		// Create field for next entry in db
		$("#customer-form-row").append('<dl class=" " id="emails_' + $counter + '__field"><dd><input type="email" id="emails_' + $counter + '_" name="emails[' + $counter + ']" value="" class="form-control"></dd>')

	});

});