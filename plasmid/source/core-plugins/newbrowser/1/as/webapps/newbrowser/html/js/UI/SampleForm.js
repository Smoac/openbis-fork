function SampleForm(containerId, profile, sampleTypeCode, isELNExperiment) {
	this.containerId = containerId;
	this.profile = profile;
	this.sampleTypeCode = sampleTypeCode;
	this.isELNExperiment = isELNExperiment;
	this.projects = null;
	this.sampleTypesLinksTables = {};
	
	this.init = function() {
			Util.blockUI();
			var localReference = this;
			openbisServer.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
				localReference.listSpacesWithProjectsAndRoleAssignmentsCallback(data);
				localReference.repaint();
				Util.unblockUI();
			});
	}
	
	this.listSpacesWithProjectsAndRoleAssignmentsCallback = function(data) {
		var projects = [];
		for(var i = 0; i < data.result.length; i++) {
			if(data.result[i].projects) {
				for(var j = 0; j < data.result[i].projects.length; j++) {
					projects[projects.length] = "/" + data.result[i].projects[j].spaceCode + "/" + data.result[i].projects[j].code;
				}
			}
		}
		this.projects = projects;
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		
		var component = "";
		
			component += "<div class='row-fluid'>";
			component += "<div class='span12'>";
		
			component += "<h2>Create Laboratory Experiment</h2>";
		
			component += "<form class='form-horizontal' action='javascript:void(0);' onsubmit='sampleForm.createSample();'>";
			component += "<fieldset>";
			component += "<legend>Description:</legend>";
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='inputSpace'>Project:</label>";
			component += "<div class='controls'>";
			component += "<select id='sampleSpaceProject' required>";
			
			for(var i = 0; i < this.projects.length; i++) {
				component += "<option value='"+this.projects[i]+"'>"+this.projects[i]+"</option>";
			}
			component += "</select> (Required)";
			component += "</div>";
			component += "</div>";
		
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='inputCode'>Code:</label>";
			component += "<div class='controls'>";
			component += "<input type='text' placeholder='Code' id='sampleCode' required> (Required)";
			component += "</div>";
			component += "</div>";
		
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='inputName'>Name:</label>";
			component += "<div class='controls'>";
			component += "<input type='text' placeholder='Name' id='sampleName'>";
			component += "</div>";
			component += "</div>";
		
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='notes'>Goals:</label>";
			component += "<div class='controls'>";
			component += "<textarea id='sampleGoals' style='height: 80px; width: 450px;'></textarea>";
			component += "</div>";
			component += "</div>";
			component += "</fieldset>";
		
			component += "<fieldset>";
			component += "<legend>Components:</legend>";
		
			//Print one drop down for each group
			component += "<center>";
			
			for(typeGroupCode in this.profile.typeGroups) {
				var sampleGroupTypeDisplayName = this.profile.typeGroups[typeGroupCode]["DISPLAY_NAME"];
				
				component += "<select onchange='sampleForm.showSamplesWithoutPage(event)'>";
				component += "<option value=''> -- "+sampleGroupTypeDisplayName+" --</a></option>";
				for(var i = 0; i < this.profile.typeGroups[typeGroupCode]["LIST"].length; i++) {
					var sampleType = this.profile.getTypeForTypeCode(this.profile.typeGroups[typeGroupCode]["LIST"][i]);
					component += "<option value='"+sampleType.code+"'>"+sampleType.description+"</a></option>";
				}
				component += "</select> ";
			}
			
			component += "</center>";
			
			component += "<div id='sampleSearchContainer'></div>"; //getSampleSearchTableComponent();
			
			this.sampleTypesLinksTables = {};
			
			for(typeGroupCode in this.profile.typeGroups) {
				var id = "sampleParents_" + typeGroupCode;
				var sampleGroupTypeDisplayName = this.profile.typeGroups[typeGroupCode]["DISPLAY_NAME"];
				
				component += "<div class='control-group'>";
				component += "<label class='control-label'>" + sampleGroupTypeDisplayName + ":</label>";
				component += "<div class='controls'>";
				component += "<div id='"+id+"'></div>";
				component += "</div>";
				component += "</div>";
				
				this.sampleTypesLinksTables[id] = new SampleLinksTable(id, this.profile);
			
				component += "<div class='control-group'>";
				component += "<label class='control-label'>" + sampleGroupTypeDisplayName + " Comments:</label>";
				component += "<div class='controls'>";
				component += "<textarea id='sampleParents_"+typeGroupCode+"_COMMENTS' style='height: 80px; width: 450px;'></textarea>";
				component += "</div>";
				component += "</div>";
			}
			
			component += "</fieldset>";
		
			component += "<fieldset>";
			component += "<legend>Results:</legend>";
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='notes'>Interpretation:</label>";
			component += "<div class='controls'>";
			component += "<textarea id='sampleResultInterpretation' style='height: 80px; width: 450px;'></textarea>";
			component += "</div>";
			component += "</div>";
		
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='notes'>Description of the Result Dataset:</label>";
			component += "<div class='controls'>";
			component += "<textarea id='sampleResultDatasetDescription' style='height: 80px; width: 450px;'></textarea>";
			component += "</div>";
			component += "</div>";
		
			component += "<div class='control-group'>";
			component += "<div class='controls'>";
			component += "<input type='submit' class='btn btn-primary' value='Create Experiment'>";
			component += "</div>";
			component += "</div>";
		
			component += "</fieldset>";
			component += "</form>";
		
			component += "</div>";
			component += "</div>";
		
		$("#"+this.containerId).append(component);
		$('#sampleSpaceProject').prop('selectedIndex', -1);
	}
	
	this.showSamplesWithoutPage = function(event) {
		var sampleTypeCode = event.target.value;
		var sampleType = this.profile.getTypeForTypeCode(sampleTypeCode);
		
		if(sampleType !== null) {
			sampleTable = new SampleTable("sampleSearchContainer", this.profile, sampleTypeCode, false, false, true, false, true);
			sampleTable.init();
		}
	}
	
	this.addLinkedSample = function(group, sample) {
		this.sampleTypesLinksTables["sampleParents_"+group].addSample(sample);
	}
	
	this.createSample = function() {
		Util.blockUI();
		var sampleSpace = $("#sampleSpaceProject")[0].value.split("/")[1];
		var sampleProject = $("#sampleSpaceProject")[0].value.split("/")[2];
		var sampleCode = $("#sampleCode")[0].value;
		var sampleName = $("#sampleName")[0].value;
		var sampleGoals = $("#sampleGoals")[0].value;
	
		var sampleParents_METHODS_COMMENTS = $("#sampleParents_METHODS_COMMENTS")[0].value;
		var sampleParents_MATERIALS_COMMENTS = $("#sampleParents_MATERIALS_COMMENTS")[0].value;
		var sampleParents_SAMPLES_COMMENTS = $("#sampleParents_SAMPLES_COMMENTS")[0].value;
		var sampleParents_SAMPLES_COMMENTS = $("#sampleParents_OTHERS_COMMENTS")[0].value;
		
		var sampleResultInterpretation = $("#sampleResultInterpretation")[0].value;
		var sampleResultDatasetDescription = $("#sampleResultDatasetDescription")[0].value;
		
		var sampleParentsFinal = new Array();
		
		for(sampleGroupCode in this.profile.typeGroups) {
			var samplesIdentifiers = this.sampleTypesLinksTables["sampleParents_"+sampleGroupCode].getSamplesIdentifiers();
			sampleParentsFinal = sampleParentsFinal.concat(samplesIdentifiers);
		}
		
		var parameters = {
				"method" : "insertSample",
				"sampleSpace" : sampleSpace,
				"sampleProject" : sampleProject,
				"sampleCode" : sampleCode,
				"sampleType" : this.profile.ELNExperiment,
				"sampleProperties" : {
										"SPACE": sampleSpace,
										"PROJECT": sampleProject,
										"NAME": sampleName,
										"GOALS": sampleGoals,
									
										"METHODS_COMMENTS" : sampleParents_METHODS_COMMENTS,
										"MATERIALS_COMMENTS" : sampleParents_MATERIALS_COMMENTS,
										"SAMPLES_COMMENTS" : sampleParents_SAMPLES_COMMENTS,
										"OTHERS_COMMENTS" : sampleParents_SAMPLES_COMMENTS,
										
										"RESULT_INTERPRETATION" : sampleResultInterpretation,
										"RESULT_DATASET_DESCRIPTION" : sampleResultDatasetDescription
									},
				"sampleParents": sampleParentsFinal,
				"sampleExperimentCreate": true,
				"sampleExperimentCode": sampleCode,
				"sampleExperimentType": this.profile.ELNExperiment,
				"sampleExperimentProject": sampleProject
		};
		
		var localReference = this;
		openbisServer.createReportFromAggregationService("DSS1", "newbrowserapi", parameters, function(response) {
			localReference.createSampleCallback(response);
		});
		
		return false;
	}

	this.createSampleCallback = function(response) {
		if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") {
			window.alert("Lab. Experiment Created");
			showCreateExperimentPage();
		} else if (response.result.columns[1].title === "Error") {
			window.alert("Error:" + response.result.rows[0][1].value);
			Util.unblockUI();
		} else {
			window.alert("Unknown Error");
			Util.unblockUI();
		}
	}
}