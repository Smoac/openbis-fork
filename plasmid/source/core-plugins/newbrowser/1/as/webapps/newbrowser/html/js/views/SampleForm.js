/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

SampleFormMode = {
    CREATE : 0,
    EDIT : 1,
    VIEW : 2
}

/**
 * Creates an instance of SampleForm.
 *
 * @constructor
 * @this {SampleForm}
 * @param {ServerFacade} serverFacade Used to access all server side calls.
 * @param {Inspector} inspector Used to add selected samples to show them as notes.
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 * @param {string} sampleTypeCode The sample type code that will be used as template for the form.
 * @param {boolean} isELNExperiment If the for should treat the sample type as an ELN Experiment, linking during creation an experiment with the same type and code.
 * @param {SampleFormMode} mode The form accepts CREATE/EDIT/VIEW modes for common samples and ELNExperiment samples
 * @param {Sample} sample The sample that will be used to populate the form if the mode is EDIT/VIEW, null can be provided for CREATE since is ignored.
 */
function SampleForm(serverFacade, inspector, containerId, profile, sampleTypeCode, isELNExperiment, mode, sample) {
	this.serverFacade = serverFacade;
	this.inspector = inspector;
	this.containerId = containerId;
	this.profile = profile;
	this.sampleTypeCode = sampleTypeCode;
	this.isELNExperiment = isELNExperiment;
	this.projects = [];
	this.projectsObj = [];
	this.spaces = [];
	this.sampleLinksParents = null;
	this.sampleLinksChildren = null;
	this.mode = mode;
	this.sample = sample;
	this.storage = null;
	this.dataSetViewer = null;
	this.isFormDirty = false;
	this.isFormLoaded = false;
	
	this.isDirty = function() {
		return this.isFormDirty;
	}
	
	this.isLoaded = function() {
		return this.isFormLoaded;
	}
	
	this.init = function() {
			Util.blockUI();
			var localReference = this;
			
			this.storage = new Storage(this.serverFacade,'sampleStorage', this.profile, this.sampleTypeCode, this.sample, this.mode === SampleFormMode.VIEW);
			this.storage.init();
				
			this.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
				//Collection information
				localReference.listSpacesWithProjectsAndRoleAssignmentsCallback(data);
						
				//Init Form elements
				localReference.repaint();
						
				//Check Mode
				if(localReference.mode === SampleFormMode.CREATE) {
						//Set the default space or project if available
						$("#sampleSpaceProject").val();
						if(localReference.isELNExperiment) {
							//Check if default project is available
							var defaultProject = localReference.profile.displaySettings.projectCode;
							if(defaultProject !== null) {
								$("#sampleSpaceProject").val(defaultProject);
							}
						} else {
							//Check if default space is available
							var defaultSpace = localReference.profile.displaySettings.spaceCode;
							if(defaultSpace !== null) {
								$("#sampleSpaceProject").val(defaultSpace);
							}
							//Check if assigned space is available
							var spaceForSampleType = this.profile.getSpaceForSampleType(this.sampleTypeCode);
							if(spaceForSampleType !== null) {
								$("#sampleSpaceProject").val(spaceForSampleType);
							}
						}
				} else if(localReference.mode === SampleFormMode.EDIT || localReference.mode === SampleFormMode.VIEW) {
						var dataStoreURL = null;
						if(localReference.profile.allDataStores.length > 0) {
							dataStoreURL = localReference.profile.allDataStores[0].downloadUrl
						}
						this.dataSetViewer = new DataSetViewer("dataSetViewerContainer", localReference.profile, localReference.sample, localReference.serverFacade, dataStoreURL);
						this.dataSetViewer.init();
						
						var sample = localReference.sample;
						//Populate Project/Space and Code
						if(localReference.isELNExperiment) {
							var spaceCode = sample.experimentIdentifierOrNull.split("/")[1];
							var projectCode = sample.experimentIdentifierOrNull.split("/")[2];
							$("#sampleSpaceProject").val("/" + spaceCode + "/" + projectCode);
						} else {
							$("#sampleSpaceProject").val(sample.spaceCode);
						}
						$("#sampleSpaceProject").prop('disabled', true);
					
						$("#sampleCode").val(sample.code);
						$("#sampleCode").prop('disabled', true);
				
						//Populate fields
						var sampleType = localReference.profile.getTypeForTypeCode(localReference.sampleTypeCode);
						for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
							var propertyTypeGroup = sampleType.propertyTypeGroups[i];
							for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
								var propertyType = propertyTypeGroup.propertyTypes[j];
								if(propertyType.dataType === "BOOLEAN") {
									$("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')).prop('checked', sample.properties[propertyType.code] === "true");
								} else {
									var value = sample.properties[propertyType.code];
									if(!value && propertyType.code.charAt(0) === '$') {
										value = sample.properties[propertyType.code.substr(1)];
									}
									$("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')).val(value);
								}
							}
						}
						localReference._reloadPreviewImage();
				}
				
				//Disable managed and dinamic
				var sampleType = localReference.profile.getTypeForTypeCode(localReference.sampleTypeCode);
				for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
					var propertyTypeGroup = sampleType.propertyTypeGroups[i];
						for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
							var propertyType = propertyTypeGroup.propertyTypes[j];
							if (localReference.mode === SampleFormMode.VIEW || propertyType.managed || propertyType.dinamic) {
								$("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')).prop('disabled', true);
							}
						}
					}
				
				//Repaint parents and children after updating the property state to show the annotations
				localReference.sampleLinksParents.repaint();
				localReference.sampleLinksChildren.repaint();
				
				//Allow user input
				Util.unblockUI();
				localReference.isFormLoaded = true;
			});
	}
	
	this.listSpacesWithProjectsAndRoleAssignmentsCallback = function(data) {
		for(var i = 0; i < data.result.length; i++) {
			this.spaces.push(data.result[i].code);
			if(data.result[i].projects) {
				for(var j = 0; j < data.result[i].projects.length; j++) {
					this.projects.push("/" + data.result[i].projects[j].spaceCode + "/" + data.result[i].projects[j].code);
					this.projectsObj.push(data.result[i].projects[j]);
				}
			}
		}
	}

	this.getTextBox = function(id, alt, isRequired) {
		var component = "<textarea id='" + id + "' alt='" + alt + "' style='height: 80px; width: 450px;'";
		
		if (isRequired) {
			component += "required></textarea> (Required)";
		} else {
			component += "></textarea>";
		}
		
		return component;
	}
	
	this.getBooleanField = function(id, alt) {
		return "<input type='checkbox' id='" + id + "' alt='" + alt + "' >";
	}
	
	this.getInputField = function(type, id, alt, isRequired) {
		var component = "<input type='" + type + "' id='" + id + "' alt='" + alt + "'";
		
		if (isRequired) {
			component += "required> (Required)";
		} else {
			component += ">";
		}
		
		return component;
	}
	
	this.getNumberInputField = function(step, id, alt, isRequired) {
		var component = "<input type='number' id='" + id + "' alt='" + alt + "' step='" + step + "'";
		
		if (isRequired) {
			component += "required> (Required)";
		} else {
			component += ">";
		}
		
		return component;
	}
	
	this.getDropDownField = function(code, terms, isRequired) {
		var component = "<select id='" + code + "' ";
		
		if (isRequired) {
			component += "required>";
		} else {
			component += ">";
		}
		
		component += "<option value='' selected></option>";
		for(var i = 0; i < terms.length; i++) {
			component += "<option value='" + terms[i].code + "'>" + terms[i].label + "</option>";
		}
		component += "</select> ";
		
		if (isRequired) {
			component += " (Required)";
		}
		
		return component;
	}
	
	this.getDatePickerField = function(id, alt, isRequired) {	
		var component  = "<div class='well' style='width: 250px;'>";
			component += "<div id='datetimepicker_" + id + "' class='input-append date'>";
			
			component += "<input id='" + id + "' data-format='yyyy-MM-dd HH:mm:ss' type='text' ";
			
			if (isRequired) {
				component += "required></input>";
			} else {
				component += "></input>";
			}
			
			component += "<span class='add-on'>";
			component += "<i data-time-icon='icon-time' data-date-icon='icon-calendar'></i>";
			component += "</span>";
			
			component += "</div>";
			component += "</div>";
			
			component += "<script type='text/javascript'> $(function() { $('#datetimepicker_" + id + "').datetimepicker({ language: 'en' });  }); </script>";
			
			return component;
	}
	
	this.getHierarchyButton = function() {
		return "<a class='btn' href=\"javascript:mainController.changeView('showSampleHierarchyPage','"+this.sample.permId+"');\"><img src='./img/hierarchy-icon.png' style='width:16px; height:17px;' /></a>";
	}
	
	this.getEditButton = function() {
		return "<a id='editButton' class='btn'><i class='icon-edit'></i> Enable Editing</a>";
	}
	
	this.enableEditButtonEvent = function() {
		var localReference = this;
		$( "#editButton" ).click(function() {
			mainController.navigationBar.updateBreadCloseActual();
			mainController.changeView('showEditSamplePage',sample);
		});
	}
	
	this.getPINButton = function() {
		var inspectedClass = "";
		if(this.inspector.containsSample(this.sample) !== -1) {
			inspectedClass = "inspectorClicked";
		}
		return "<a id='pinButton' class='btn pinBtn " + inspectedClass + "'><img src='./img/pin-icon.png' style='width:16px; height:16px;' /></a>";
	}
	
	this.enablePINButtonEvent = function() {
		var localReference = this;
		$( "#pinButton" ).click(function() {
			var isInspected = localReference.inspector.toggleInspectSample(sample);
			if(isInspected) {
				$('#pinButton').addClass('inspectorClicked');
			} else {
				$('#pinButton').removeClass('inspectorClicked');
			}
		});
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		var sampleType = profile.getTypeForTypeCode(this.sampleTypeCode);
		var sampleTypeDisplayName = sampleType.description;
		
		if(!sampleTypeDisplayName) {
				sampleTypeDisplayName = this.sampleTypeCode;
		}

		var component = "";
		
			component += "<div class='row-fluid'>";
			component += "<div class='span12'>";
			
			var message = null;
			var pinButton = "";
			var editButton = "";
			var hierarchyButton = "";
			
			if (this.mode === SampleFormMode.CREATE) {
				message = "Create";
			} else if (this.mode === SampleFormMode.EDIT) {
				message = "Update";
				pinButton = this.getPINButton();
				hierarchyButton = this.getHierarchyButton();
				sampleTypeDisplayName = sample.code;
			} else if (this.mode === SampleFormMode.VIEW) {
				message = "View";
				pinButton = this.getPINButton();
				hierarchyButton = this.getHierarchyButton();
				editButton = this.getEditButton();
				sampleTypeDisplayName = sample.code;
			}
			
			component += "<h2>" + message + " " + sampleTypeDisplayName + " " + pinButton + " " + hierarchyButton + " " + editButton + "</h2>";
			
			component += "<form class='form-horizontal' action='javascript:void(0);' onsubmit='mainController.currentView.createSample();'>";
			
			//
			// SELECT PROJECT/SPACE AND CODE
			//
			if (this.mode !== SampleFormMode.CREATE) {
				component += "<img data-preview-loaded='false' class='zoomableImage' id='preview-image' src='./img/image_loading.gif' style='height:300px; margin-right:20px; float:right;'></img>"
			}
			component += "<fieldset>";
			component += "<legend>Identification Info</legend>";
			//Space/Project
			var spaceSelectEnabled = true;
			if(!this.isELNExperiment && 
					this.mode === SampleFormMode.CREATE && 
					this.profile.getSpaceForSampleType(this.sampleTypeCode) !== null) {
				spaceSelectEnabled = false;
			}
			
			if(spaceSelectEnabled) {
				component += "<div class='control-group'>";
				if(this.isELNExperiment) {
					component += "<label class='control-label' for='inputSpace'>Project:</label>";
				} else {
					component += "<label class='control-label' for='inputSpace'>Space:</label>";
				}
				
				component += "<div class='controls'>";
				if(this.isELNExperiment) {
					component += "<select id='sampleSpaceProject' required>";
					component += "<option disabled=\"disabled\" selected></option>";
					for(var i = 0; i < this.projects.length; i++) {
						component += "<option value='"+this.projects[i]+"'>"+this.projects[i]+"</option>";
					}
					component += "</select> (Required)";
				} else {
					component += "<select id='sampleSpaceProject' required>";
					component += "<option disabled=\"disabled\" selected></option>";
					for(var i = 0; i < this.spaces.length; i++) {
						component += "<option value='"+this.spaces[i]+"'>"+this.spaces[i]+"</option>";
					}
					component += "</select> (Required)";
				}
				component += "</div>";
				component += "</div>";
			}
			
			//Code
			component += "<div class='control-group'>";
			component += "<label class='control-label' for='inputCode'>Code:</label>";
			component += "<div class='controls'>";
			component += "<input type='text' placeholder='Code' id='sampleCode' pattern='[a-zA-Z0-9_\\-\\.]+' required> (Required)";
			if(this.mode === SampleFormMode.CREATE) {
				component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
			}
			component += "</div>";
			component += "</div>";
			
			
			component += "</fieldset>";
			
			//
			// LINKS TO PARENTS
			//
			var requiredParents = [];
			var sampleTypeDefinitionsExtension = this.profile.sampleTypeDefinitionsExtension[this.sampleTypeCode];
			if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"]) {
				requiredParents = sampleTypeDefinitionsExtension["SAMPLE_PARENTS_HINT"];
			}
			
			var sampleParentsWidgetId = "sampleParentsWidgetId";
			component += "<div id='" + sampleParentsWidgetId + "'></div>";
			var isDisabled = this.mode === SampleFormMode.VIEW;
			
			var sampleParentsLinks = (this.sample)?this.sample.parents:null;
			this.sampleLinksParents = new SampleLinksWidget(sampleParentsWidgetId, this.profile, this.serverFacade, "Parents", requiredParents, isDisabled, sampleParentsLinks);
			
			//
			// LINKS TO CHILDREN
			//
			var requiredChildren = [];
			if(sampleTypeDefinitionsExtension && sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"]) {
				requiredChildren = sampleTypeDefinitionsExtension["SAMPLE_CHILDREN_HINT"];
			}
			
			var sampleChildrenWidgetId = "sampleChildrenWidgetId";
			component += "<div id='" + sampleChildrenWidgetId + "'></div>";
			
			var sampleChildrenLinks = (this.sample)?this.sample.children:null;
			this.sampleLinksChildren = new SampleLinksWidget(sampleChildrenWidgetId, this.profile, this.serverFacade, "Children", requiredChildren, isDisabled, sampleChildrenLinks);
			
			//
			// GENERATE CHILDREN
			//
			if(!(this.mode === SampleFormMode.VIEW)) {
				component += "<fieldset>";
				component += "<div class='control-group'>";
				component += "<div class='controls'>";
				component += "<a class='btn' id='generate_children'>Generate Children</a>";
				component += "</div>";
				component += "</div>";
				component += "</fieldset>";
			}
			
			
			//
			// SAMPLE TYPE FIELDS
			//
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				component += "<fieldset>";
				
				if(propertyTypeGroup.name) {
					component += "<legend>" + propertyTypeGroup.name + "</legend>";
					if(this.storage.isPropertyGroupFromStorage(propertyTypeGroup.name)) {
						component += "<div id='sampleStorage'></div>"; // When a storage is used, the storage needs a container
					}
				} else {
					component += "<legend></legend>";
				}
				
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(this.storage.isPropertyFromStorage(propertyType.code)) { continue; } // When a storage is used, the storage controls the rendering of the properties
					
					component += "<div class='control-group'>";
					component += "<label class='control-label' for='inputCode'>" + propertyType.label + ":</label>";
					component += "<div class='controls'>";
					
					if (propertyType.dataType === "BOOLEAN") {
						component += this.getBooleanField(propertyType.code, propertyType.description);
					} else if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
						var vocabulary = null;
						if(isNaN(propertyType.vocabulary)) {
							vocabulary = this.profile.getVocabularyById(propertyType.vocabulary.id);
						} else {
							vocabulary = this.profile.getVocabularyById(propertyType.vocabulary);
						}
						component += this.getDropDownField(propertyType.code, vocabulary.terms, propertyType.mandatory);
					} else if (propertyType.dataType === "HYPERLINK") {
						component += this.getInputField("url", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "INTEGER") {
						component += this.getNumberInputField("1", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "MATERIAL") {
						component += this.getInputField("text", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "MULTILINE_VARCHAR") {
						component += this.getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "REAL") {
						component += this.getNumberInputField("any", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "TIMESTAMP") {
						component += this.getDatePickerField(propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "VARCHAR") {
						component += this.getInputField("text", propertyType.code, propertyType.description, propertyType.mandatory);
					} else if (propertyType.dataType === "XML") {
						component += this.getTextBox(propertyType.code, propertyType.description, propertyType.mandatory);
					}
					
					component += "</div>";
					component += "</div>";
					
				}
				component += "</fieldset>";
			}
			
			//
			// Extra component placeholder defined by Profile.sampleFormContentExtra(sampleTypeCode, sample)
			//
			component += "<div id='sample-form-content-extra'></div>";
			
			//
			// FORM SUBMIT
			//
			if(!(this.mode === SampleFormMode.VIEW)) {
				component += "<fieldset>";
				component += "<div class='control-group'>";
				component += "<div class='controls'>";
				component += "<input type='submit' class='btn btn-primary' value='" + message + " " + sampleTypeDisplayName + "'>";
				component += "</div>";
				component += "</div>";
				component += "</fieldset>";
			}
			
			//
			// DATASETS
			//
			component += "<div id='dataSetViewerContainer'></div>";
			
			component += "</form>";
			
			component += "</div>";
			component += "</div>";
			
			
		//Add form to layout
		$("#"+this.containerId).append(component);
		this.storage.repaint();
		
		//Enable Events
		$("#sampleCode").change(
			function() {
				$(this).val($(this).val().toUpperCase()); //Codes can only be upper case
			}
		);
		
		var localInstance = this;
		
		if (this.mode !== SampleFormMode.CREATE) {
			this.enablePINButtonEvent();
		}
		
		if (this.mode === SampleFormMode.VIEW) {
			this.enableEditButtonEvent();
		}
		
		if(!(this.mode === SampleFormMode.VIEW)) {
			$("#generate_children").click(function(event) {
				localInstance._generateChildren();
			});
		}
		
		//Events to take care of a dirty form
		$("#sampleSpaceProject").change(function(event) {
			localInstance.isFormDirty = true;
		});
		$("#sampleCode").change(function(event) {
			localInstance.isFormDirty = true;
		});
		
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				var $field = $("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.'));
				$field.change(function(event) {
					localInstance.isFormDirty = true;
				});
			}
		}
		
		//Extra components
		this.profile.sampleFormContentExtra(this.sampleTypeCode, this.sample, "sample-form-content-extra");
		
		//Make Preview Image zoomable
		$("#preview-image").click(function(){
			Util.showImage($("#preview-image").attr("src"));
		});
	}
	
	this.showSamplesWithoutPage = function(event) {
		var sampleTypeCode = event.target.value;
		var sampleType = this.profile.getTypeForTypeCode(sampleTypeCode);
		
		if(sampleType !== null) {
			sampleTable = new SampleTable(this.serverFacade,"sampleSearchContainer", this.profile, sampleTypeCode, false, false, true, false, true);
			sampleTable.init();
		}
	}
	
	this.createSample = function() {
		Util.blockUI();
		
		//Other properties
		var properties = {};
		
		var sampleType = profile.getTypeForTypeCode(this.sampleTypeCode);
		for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
			var propertyTypeGroup = sampleType.propertyTypeGroups[i];
			for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
				var propertyType = propertyTypeGroup.propertyTypes[j];
				var value = null;
				
				if (propertyType.dataType === "BOOLEAN") {
					value = $("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')+":checked").val() === "on";
				} else {
					value = Util.getEmptyIfNull($("#"+propertyType.code.replace('$','\\$').replace(/\./g,'\\.')).val());
				}
				
				properties[propertyType.code] = value;
			}
		}
		
		//Parent Links
		if(!this.sampleLinksParents.isValid()) {
			Util.showError("Missing Parents.");
			return;
		}
		var sampleParentsFinal = this.sampleLinksParents.getSamplesIdentifiers();
		
		if(!this.sampleLinksParents.isValid()) {
			Util.showError("Missing Children.");
			return;
		}
		var sampleChildrenFinal = this.sampleLinksChildren.getSamplesIdentifiers();
		var sampleChildrenRemovedFinal = this.sampleLinksChildren.getSamplesRemovedIdentifiers();
		
		
		var intersect_safe = function(a, b)
		{
		  var ai=0, bi=0;
		  var result = new Array();

		  while( ai < a.length && bi < b.length )
		  {
		     if      (a[ai] < b[bi] ){ ai++; }
		     else if (a[ai] > b[bi] ){ bi++; }
		     else /* they're equal */
		     {
		       result.push(a[ai]);
		       ai++;
		       bi++;
		     }
		  }

		  return result;
		}
		
		sampleParentsFinal.sort();
		sampleChildrenFinal.sort();
		var intersection = intersect_safe(sampleParentsFinal, sampleChildrenFinal);
		if(intersection.length > 0) {
			Util.showError("The same entity can't be a parent and a child, please check: " + intersection);
			return;
		}
		
		//Identification Info
		var sampleCode = $("#sampleCode")[0].value;
		
		var sampleSpace = null;
		var sampleProject = null;
		var sampleExperiment = null;
		if(this.isELNExperiment) {
			sampleSpace = $("#sampleSpaceProject")[0].value.split("/")[1];
			sampleProject = $("#sampleSpaceProject")[0].value.split("/")[2];
			sampleExperiment = sampleCode;
		} else {
			sampleSpace = $("#sampleSpaceProject").val();
			if(!sampleSpace) {
				sampleSpace = this.profile.getSpaceForSampleType(this.sampleTypeCode);
			}
			var experimentIdentifier = this.profile.getExperimentIdentifierForSample(this.sampleTypeCode, sampleCode, properties);
			if(experimentIdentifier) {
				sampleSpace = experimentIdentifier.split("/")[1];
				sampleProject = experimentIdentifier.split("/")[2];
				sampleExperiment = experimentIdentifier.split("/")[3];
			}
		}
		
		//Children to create
		var samplesToCreate = [];
		this.sampleLinksChildren.getSamples().forEach(function(child) {
			if(child.newSample) {
				samplesToCreate.push(child);
			}
		});
		
		//Method
		var method = "";
		if(this.mode === SampleFormMode.CREATE) {
			method = "insertSample";
		} else if(this.mode === SampleFormMode.EDIT) {
			method = "updateSample";
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"sampleSpace" : sampleSpace,
				"sampleProject" : sampleProject,
				"sampleCode" : sampleCode,
				"sampleType" : this.sampleTypeCode,
				//Other Properties
				"sampleProperties" : properties,
				//Parent links
				"sampleParents": sampleParentsFinal,
				//Children links
				"sampleChildren": sampleChildrenFinal,
				"sampleChildrenNew": samplesToCreate,
				"sampleChildrenRemoved": sampleChildrenRemovedFinal,
				//Experiment parameters
				"sampleExperimentProject": sampleProject,
				"sampleExperimentType": (isELNExperiment)?this.sampleTypeCode:"ELN_FOLDER",
				"sampleExperimentCode": sampleExperiment
		};
		
		var localReference = this;
		
		if(this.profile.allDataStores.length > 0) {
			this.serverFacade.createReportFromAggregationService(this.profile.allDataStores[0].code, parameters, function(response) {
				localReference.createSampleCallback(response, localReference);
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
		
		return false;
	}

	this.createSampleCallback = function(response, localReference) {
		if(response.error) { //Error Case 1
			Util.showError(response.error.message, function() {Util.unblockUI();});
		} else if (response.result.columns[1].title === "Error") { //Error Case 2
			var stacktrace = response.result.rows[0][1].value;
			var isUserFailureException = stacktrace.indexOf("ch.systemsx.cisd.common.exceptions.UserFailureException") === 0;
			var startIndex = null;
			var endIndex = null;
			if(isUserFailureException) {
				startIndex = "ch.systemsx.cisd.common.exceptions.UserFailureException".length + 2;
				endIndex = stacktrace.indexOf("at ch.systemsx");
			} else {
				startIndex = 0;
				endIndex = stacktrace.length;
			}
			var errorMessage = stacktrace.substring(startIndex, endIndex).trim();
			Util.showError(errorMessage, function() {Util.unblockUI();});
		} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
			var sampleType = profile.getTypeForTypeCode(this.sampleTypeCode);
			var sampleTypeDisplayName = sampleType.description;
			if(!sampleTypeDisplayName) {
				sampleTypeDisplayName = this.sampleTypeCode;
			}
			var message = "";
			if(this.mode === SampleFormMode.CREATE) {
				message = "Created.";
			} else if(this.mode === SampleFormMode.EDIT) {
				message = "Updated.";
			}
			
			var callbackOk = function() {
				mainController.changeView('showSamplesPage', localReference.sampleTypeCode);
//				TO-DO: The Sample is not necessarily searchable after creation since the index runs asynchronously
//				localReference.serverFacade.searchWithType(localReference.sampleTypeCode, $("#sampleCode")[0].value, function(data) {
//					mainController.navigationBar.updateBreadCloseActual();
//					mainController.changeView('showViewSamplePageFromPermId',data[0].permId);
//				});
			}
			
			Util.showSuccess(sampleTypeDisplayName + " " + message, callbackOk);
			this.isFormDirty = false;
		} else { //This should never happen
			Util.showError("Unknown Error.", function() {Util.unblockUI();});
		}
	}
	
	this._updateLoadingToNotAvailableImage = function() {
		var notLoadedImages = $("[data-preview-loaded='false']");
		notLoadedImages.attr('src', "./img/image_unavailable.png");
	}
	
	this._reloadPreviewImage = function() {
		var _this = this;
		var previewCallback = 
			function(data) {
				if (data.result.length == 0) {
					_this._updateLoadingToNotAvailableImage();
				} else {
					var x = "123";
					var listFilesForDataSetCallback = 
						function(dataFiles) {
							if(!dataFiles.result) {
								//DSS Is not running probably
							} else {
								var elementId = 'preview-image';
								var downloadUrl = _this.profile.allDataStores[0].downloadUrl + '/' + data.result[0].code + "/" + dataFiles.result[1].pathInDataSet + "?sessionID=" + _this.serverFacade.getSession();
								
								var img = $("#" + elementId);
								img.attr('src', downloadUrl);
								img.attr('data-preview-loaded', 'true');
							}
						};
					_this.serverFacade.listFilesForDataSet(data.result[0].code, "/", true, listFilesForDataSetCallback);
				}
			};
		
		this.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [this.sample.permId], previewCallback);
	}
	
	this._generateChildren = function() {
		// Buttons
		var getGeneratedChildrenCodes = function() {
			//Get selected parents
			var $parentsFields = $("#parentsToGenerateChildren").find("input:checked");
			//Group parents by type - this structure helps the create children algorithm
			var selectedParentsByType = {};
			for(var i = 0; i < $parentsFields.length; i++) {
				var parentIdentifier = $parentsFields[i].id;
				var parent = parentsByIdentifier[parentIdentifier];
				var typeList = selectedParentsByType[parent.sampleTypeCode];
				if(!typeList) {
					typeList = [];
					selectedParentsByType[parent.sampleTypeCode] = typeList;
				}
				typeList.push(parent);
			}
			//Generate Children from parents
			var generatedChildren = [];
			var parentSampleCode = $("#sampleCode").val();
			for(var sampleTypeCode in selectedParentsByType) {
				var parentsOfType = selectedParentsByType[sampleTypeCode];
				
				var newGeneratedChildren = [];
				
				for(var i = 0; i < parentsOfType.length; i++) {
					var parentOfType = parentsOfType[i];
					if(generatedChildren.length === 0) {
						newGeneratedChildren.push(parentSampleCode + "_" + parentOfType.code);
					} else {
						for(var k = 0; k < generatedChildren.length; k++) {
							newGeneratedChildren.push(generatedChildren[k] + "_" + parentOfType.code);
						}
					}
				}
				
				generatedChildren = newGeneratedChildren;
			}
			return generatedChildren;
		}
		
		var showPreview = function() {
			var generatedChildren = getGeneratedChildrenCodes();
			//Show generated children
			$("#previewChildrenGenerator").empty();
			for(var i = 0; i < generatedChildren.length; i++) {
				$("#previewChildrenGenerator").append(generatedChildren[i] + "<br />");
			}
		}
		
		var _this = this;
		var $generateButton = $("<a>", { "class" : "btn" }).append("Generate!");
		$generateButton.click(function(event) { 
			var generatedChildrenSpace = null;
			if(_this.isELNExperiment) {
				generatedChildrenSpace = $("#sampleSpaceProject")[0].value.split("/")[1];
			} else {
				generatedChildrenSpace = $("#sampleSpaceProject").val();
			}
			var generatedChildrenCodes = getGeneratedChildrenCodes();
			var generatedChildrenType = $("#childrenTypeSelector").val();
			if(generatedChildrenType === "") {
				Util.showError("Please select the children type.", function() {}, true);
			} else {
				for(var i = 0; i < generatedChildrenCodes.length; i++) {
					var virtualSample = new Object();
					virtualSample.newSample = true;
					virtualSample.code = generatedChildrenCodes[i];
					virtualSample.identifier = "/" + generatedChildrenSpace + "/" + virtualSample.code;
					virtualSample.sampleTypeCode = generatedChildrenType;
					_this.sampleLinksChildren.addSample(virtualSample);
				}
					
				Util.unblockUI();
			}
		});
		
		var $cancelButton = $("<a>", { "class" : "btn" }).append("<i class='icon-remove'></i>");
		$cancelButton.click(function(event) { 
			Util.unblockUI();
		});
		
		var $selectAllButton = $("<a>", { "class" : "btn" }).append("Enable/Disable All");
		$selectAllButton.click(function(event) { 
			var $parentsFields = $("#parentsToGenerateChildren").find("input");
			for(var i = 0; i < $parentsFields.length; i++) {
				var $parentField = $parentsFields[i];
				$parentField.checked = !$parentField.checked;
			}
			
			showPreview();
		});
		
		// Parents
		var $parents = $("<div>");
		var parentsIdentifiers = this.sampleLinksParents.getSamplesIdentifiers();
		var parentsByType = {};
		var parentsByIdentifier = {};
		for(var i = 0; i < parentsIdentifiers.length; i++) {
			var parent = this.sampleLinksParents.getSampleByIdentifier(parentsIdentifiers[i]);
			var typeList = parentsByType[parent.sampleTypeCode];
			if(!typeList) {
				typeList = [];
				parentsByType[parent.sampleTypeCode] = typeList;
			}
			typeList.push(parent);
			parentsByIdentifier[parent.identifier] = parent;
		}
		
		for(var parentTypeCode in parentsByType) {
			var $parentsTypeColumn = $("<div>" , {"class" : "span3"});
			
			//$parents.append(parentTypeCode + ":").append($("<br>"));
			$parentsTypeColumn.append(parentTypeCode + ":").append($("<br>"));
			
			var parentsOfType = parentsByType[parentTypeCode];
			
			for(var i = 0; i < parentsOfType.length; i++) {
				var parent = parentsOfType[i];
				var parentProperty = new Object();
				parentProperty.code = parent.identifier;
				parentProperty.description = parent.identifier;
				parentProperty.label = parent.code;
				parentProperty.dataType = "BOOLEAN";
				//$parents.append(FormUtil.getFieldForPropertyTypeWithLabel(parentProperty));
				
				var $field = FormUtil.getFieldForPropertyTypeWithLabel(parentProperty);
				var $checkBox = $($field[0].elements[0]);
				$checkBox.change(function() { 
					showPreview();
				});
				
				$parentsTypeColumn.append($field);
			}
			$parents.append($parentsTypeColumn);
		}
		
		var $parentsComponent = $("<fieldset>", { "id" : 'parentsToGenerateChildren' } );
		$parentsComponent.append($("<legend>").append("Parents ").append($selectAllButton))
		$parentsComponent.append($parents);
		
		// Children
		var $childrenTypeDropdown = FormUtil.getSampleTypeDropdown('childrenTypeSelector', true);
		var $childrenTypeDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenTypeDropdown, 'Type');
		var $childrenComponent = $("<fieldset>");
		$childrenComponent.append($("<legend>").text("Children"))
		$childrenComponent.append($childrenTypeDropdownWithLabel);
		
		// Preview
		var $previewComponent = $("<fieldset>");
		$previewComponent.append($("<legend>").append("Preview"));
		$previewComponent.append($("<div>", {"id" : "previewChildrenGenerator"}));
		
		// Mounting the widget with the components
		var $childrenGenerator = $("<div>");
		$childrenGenerator.append($("<div>", {"style" : "text-align:right;"}).append($cancelButton));
		$childrenGenerator.append($("<form>", { "class" : "form-horizontal" , "style" : "margin-left:20px; margin-right:20px;"})
									.append($("<h1>").append("Children Generator"))
									.append($parentsComponent)
									.append($childrenComponent)
									.append($previewComponent)
									.append($("<br>")).append($generateButton)
								);
		
		// Show Widget
		Util.blockUI($childrenGenerator, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%'});
	}
}