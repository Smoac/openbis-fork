
function showSamplesRecursively(top, samples, depth)
{
	if (depth > 2) return;
	
	var sampEnter = 
		top.selectAll("div.samp")
			.data(samples)
		.enter()
			.append("div")
				.attr("class", "samp")
				.style("left", depth > 0 ? "40px" : 0)
				.text(function(d) { return d.identifier});
	
	showSamplesRecursively(sampEnter, function(d) {return d.parents ? d.parents : [] }, depth + 1);
}

function showSamplesWithParents(samples) {
	var top = d3.select("#main");
	samples = samples.filter(function(d) {return d.parents != null});
	showSamplesRecursively(top, samples, 0);
}

function retrieveAndShowSamplesWithParents() {
	
	var samplesWithParentsSc = 
	{
		operator : "MATCH_ALL_CLAUSES",
		subCriterias: [
			{
				targetEntityKind: "SAMPLE_PARENT",
				criteria: {
					matchClauses : [ 
						{"@type":"AttributeMatchClause",
							attribute : "CODE",
							fieldType : "ATTRIBUTE",
							desiredValue : "FR*" 
						}]}
			}
		]
	};
	
	openbisServer.searchForSamplesWithFetchOptions(
		samplesWithParentsSc,
		["PROPERTIES", "ANCESTORS"],
		function(data) { showSamplesWithParents(data.result) });
	
}


function enterApp()
{
	$("#login-form-div").hide();
	$("#main").show();
	$('#openbis-logo').height(50);
	
	retrieveAndShowSamplesWithParents();
}

