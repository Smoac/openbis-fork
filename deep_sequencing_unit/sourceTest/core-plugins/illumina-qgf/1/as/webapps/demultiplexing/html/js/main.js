
function setValidators() {
	$('#MismatchesInIndexForm').bootstrapValidator({
		fields: {
			indexMismatch: {
				validators: {
					regexp: {
						regexp: /^(([0-1]))$/i,
						message: 'The value is not 0 or 1'
					}
				}
			}
		}
	});
	$('#laneRangeForm').bootstrapValidator({
		fields: {
			laneRange: {
				validators: {
					regexp: {
						regexp: /^(([1-8]+)|([1-8]+-[1-8]+))((,(\s*)(([1-8]+)|([1-8]+-[1-8]+)))*)$/i,
						message: 'The value is not list of valid lanes or intervals.'
					}
				}
			}
		}
	});
}

function createVis() {
	if (didCreateVis) return;
	vis = d3.select("#main").append("div").attr("id", "vis");
	didCreateVis = true;
}

function enableSubmission() {
	$('#submitBtn').prop('disabled', false);
}

function disableSubmission() {
	$('#submitBtn').prop('disabled', true);
}

function displayReturnedTable(data) {
	if (data.error) {
		console.log(data.error);
		vis.append("p").text("Could not retrieve data.");
		return;
	}

	var dataToShow = data.result;
	console.log(dataToShow.rows[0][0].value);

	d3.select("#progress").remove()
	d3.select("#button-group").remove()

	vis.append("p").text("");
	// Pick all div elements of the visualization
	vis.selectAll("div").attr("class", "alert")
	.data(dataToShow.rows)
	.enter()
	.append("div")
	.attr("class", function (row) {
		if (row[0].value == 0 || row[0].value > 99) {
			if(row[0].value == 100) 
				enableSubmission();
			if(row[0].value == 101)
				disableSubmission();
			return "alert alert-success";
		}
		return "alert alert-danger";
	})
	.html(function (row) {
		return row[1].value;
	})

	var button = d3.select("#container")
	.append("div")

	button.selectAll("button")
	.data(dataToShow.rows)
	.enter()
	.append("div")
	.append("button")
	.attr("id", function (row) {
		return row[0].value;
	})
	.attr("class", function (row) {
		if (row[0].value == 100) {
			return "btn btn-success";
		}
		return "btn btn-danger";
	})
	//.attr("onclick", function(row) { return "callIngestionSetInvoice('" + row + "');" })
	.text(function (row) {
		if (row[0].value == 0) {
			return "OK" ;
		}
		return "Fail";
	});

}

function hideButtons(data) {
	var buttonId = data.result.rows[0][0].value;
	d3.select("#setInvoice").remove()
	d3.select("#main").append("div").attr("id", "Done").append("p").text("Done " + buttonId);
	d3.select("button#" + String(buttonId)).remove();
}

function spinner(target) {
	var opts = {
			lines: 13, // The number of lines to draw
			length: 7, // The length of each line
			width: 4, // The line thickness
			radius: 10, // The radius of the inner circle
			corners: 1, // Corner roundness (0..1)
			rotate: 0, // The rotation offset
			color: '#000', // #rgb or #rrggbb
			speed: 1, // Rounds per second
			trail: 60, // Afterglow percentage
			shadow: false, // Whether to render a shadow
			hwaccel: false, // Whether to use hardware acceleration
			className: 'spinner', // The CSS class to assign to the spinner
			zIndex: 2e9, // The z-index (defaults to 2000000000)
			top: 250, // Top position relative to parent in px
			left: 'auto' // Left position relative to parent in px
	};
	var spinner = new Spinner(opts).spin(target);
}

function callIngestionService(method) {
	d3.select("#main").select("#progress").remove()
	d3.select("#main").select("#vis").remove()
	
	if(method == 'startJob') {
		$('#laneRangeForm').data('bootstrapValidator').validate() 
		if(!$('#laneRangeForm').data('bootstrapValidator').isValid()) {
			$('#laneRangeForm').focus();
			return;
		}
	}
	
	didCreateVis = false;
	dsu.server.useSession(context.getSessionId());
	createVis()
	
	
	var permIdentifier = context.getEntityIdentifier()
	myUserId = context.getSessionId().split("-")[0]; 

	var submissionParameters =
	{
			sampleId: permIdentifier,
			userId : myUserId, 
			method: method,
			type : context.getEntityKind()
	};
	addProcessingParameters(submissionParameters);
	
	console.log(submissionParameters)

	var target = document.getElementById('progress');
	spinner(target)
//	dsu.server.createReportFromAggregationService("DSS1", "triggerbee", submissionParameters, displayReturnedTable);
}

function addProcessingParameters(parameters) {
	if($('#allLanes').is(':checked'))
		parameters.laneRange = 'all';
	else
		parameters.laneRange = $('#laneRange').val();
	parameters.sampleSheet = $('#sampleSheet').is(':checked');
	parameters.failedReads = $('#failedReads').is(':checked');
	parameters.mismatch = $('#indexMismatch').val();
	parameters.email =$('#email').is(':checked');
}

function refresh() {
	callIngestionService("pollJob")
}