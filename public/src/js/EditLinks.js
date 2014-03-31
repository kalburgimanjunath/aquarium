// Adds an input field when clicking button
$(document).ready(function() {
	$("#add-link-btn").click(function() {
		// Get number of last field
		var $counter = $('div').find('input[type=text]').length;
		// Create field for next entry in db
		$("#links-form-table").append('<tr><td><dd><input type="text" id="labels_' + $counter + '_" name="labels[' + $counter + ']" value=""></dd></td><td><dd><input type="text" id="urls_' + $counter + '_" name="urls[' + $counter + ']" value=""></dd></td></tr>')
	});

});