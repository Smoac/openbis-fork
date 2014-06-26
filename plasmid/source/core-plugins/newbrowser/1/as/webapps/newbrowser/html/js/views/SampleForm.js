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
 * @param {boolean} isELNSubExperiment If the for should treat the sample type as an ELN Experiment, linking during creation an experiment
 * @param {SampleFormMode} mode The form accepts CREATE/EDIT/VIEW modes for common samples and ELNExperiment samples
 * @param {Sample} sample The sample that will be used to populate the form if the mode is EDIT/VIEW, null can be provided for CREATE since is ignored.
 * @param {String} experimentIdentifier The experiment of a subexperiment, only used for creation
 */
function SampleForm(serverFacade, inspector, containerId, profile, sampleTypeCode, isELNSubExperiment, mode, sample, experimentIdentifier) {
	this.serverFacade = serverFacade;
	this.inspector = inspector;
	this.containerId = containerId;
	this.profile = profile;
	this.sampleTypeCode = sampleTypeCode;
	this.isELNSubExperiment = isELNSubExperiment;
	this.projects = [];
	this.projectsObj = [];
	this.spaces = [];
	this.sampleLinksParents = null;
	this.sampleLinksChildren = null;
	this.mode = mode;
	this.sample = sample;
	this.experimentIdentifier = experimentIdentifier;
	this.storages = [];
	this.dataSetViewer = null;
	this.isFormDirty = false;
	this.isFormLoaded = false;
	
	this.formColumnClass = 'col-md-12'
	this.labelColumnClass = 'col-md-2';
	this.controlColumnClass = 'col-md-5';
	
	this.isDirty = function() {
		return this.isFormDirty;
	}
	
	this.isLoaded = function() {
		return this.isFormLoaded;
	}
	
	this.init = function() {
			Util.blockUI();
			var localReference = this;
			this.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
				var sampleType = localReference.profile.getSampleTypeForSampleTypeCode(localReference.sampleTypeCode);
				
				//Collection information
				localReference.listSpacesWithProjectsAndRoleAssignmentsCallback(data);
						
				//Init Form elements
				localReference.repaint();
						
				//Check Mode
				if(localReference.mode === SampleFormMode.CREATE) {	
					//Set the default space or project if available
					$("#sampleSpaceProject").val();
					
					if(localReference.isELNSubExperiment) {
						$("#sampleSpaceProject").val(localReference.experimentIdentifier);
						$("#sampleSpaceProject").prop('disabled', true);
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
					
					localReference.serverFacade.generateCode(sampleType.codePrefix, function(data) {
						$("#sampleCode").val(data.result);
					});
				} else if(localReference.mode === SampleFormMode.EDIT || localReference.mode === SampleFormMode.VIEW) {
						var dataStoreURL = null;
						if(localReference.profile.allDataStores.length > 0) {
							dataStoreURL = localReference.profile.allDataStores[0].downloadUrl
						}
						this.dataSetViewer = new DataSetViewer("dataSetViewerContainer", localReference.profile, localReference.sample, localReference.serverFacade, dataStoreURL);
						this.dataSetViewer.init();
						
						var sample = localReference.sample;
						//Populate Project/Space and Code
						if(localReference.isELNSubExperiment) {
							$("#sampleSpaceProject").val(sample.experimentIdentifierOrNull);
						} else {
							$("#sampleSpaceProject").val(sample.spaceCode);
						}
						$("#sampleSpaceProject").prop('disabled', true);
					
						$("#sampleCode").val(sample.code);
						$("#sampleCode").prop('disabled', true);
				
						//Populate fields
						var sampleType = localReference.profile.getSampleTypeForSampleTypeCode(localReference.sampleTypeCode);
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
		var component = "<textarea class='form-control' id='" + id + "' alt='" + alt + "' style='height: 80px; width: 450px;'";
		
		if (isRequired) {
			component += "required></textarea>";
		} else {
			component += "></textarea>";
		}
		
		return component;
	}
	
	this.getBooleanField = function(id, alt) {
		return "<div class='checkbox'><input type='checkbox' id='" + id + "' alt='" + alt + "' ></div>";
	}
	
	this.getInputField = function(type, id, alt, isRequired) {
		var component = "<input class='form-control' type='" + type + "' id='" + id + "' alt='" + alt + "'";
		
		if (isRequired) {
			component += "required>";
		} else {
			component += ">";
		}
		
		return component;
	}
	
	this.getNumberInputField = function(step, id, alt, isRequired) {
		var component = "<input class='form-control' type='number' id='" + id + "' alt='" + alt + "' step='" + step + "'";
		
		if (isRequired) {
			component += "required>";
		} else {
			component += ">";
		}
		
		return component;
	}
	
	this.getDropDownField = function(code, terms, isRequired) {
		var component = "<select class='form-control' id='" + code + "' ";
		
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
		
		return component;
	}
	
	this.getDatePickerField = function(id, alt, isRequired) {	
		var component  = "<div class='form-group' style='margin-left: 0px;'>";
			component += "<div id='datetimepicker_" + id + "' class='input-group date'>";
			
			component += "<input class='form-control' id='" + id + "' data-format='yyyy-MM-dd HH:mm:ss' type='text' ";
			
			if (isRequired) {
				component += "required></input>";
			} else {
				component += "></input>";
			}
			
			component += "<span class='input-group-addon'><span class='glyphicon glyphicon-calendar'></span>";
			
			component += "</div>";
			component += "</div>";
			
			component += "<script type='text/javascript'> $(function() { $('#datetimepicker_" + id + "').datetimepicker({ language: 'en' });  }); </script>";
			
			return component;
	}
	
	this.getHierarchyButton = function() {
		return "<a class='btn btn-default' href=\"javascript:mainController.changeView('showSampleHierarchyPage','"+this.sample.permId+"');\"><img src='./img/hierarchy-icon.png' style='width:16px; height:17px;' /></a>";
	}
	
	this.getEditButton = function() {
		return "<a id='editButton' class='btn btn-default'><span class='glyphicon glyphicon-edit'></span> Enable Editing</a>";
	}
	
	this.enableEditButtonEvent = function() {
		var localReference = this;
		$( "#editButton" ).click(function() {
			mainController.changeView('showEditSamplePageFromPermId',sample.permId);
		});
	}
	
	this.getPINButton = function() {
		var inspectedClass = "";
		if(this.inspector.containsSample(this.sample) !== -1) {
			inspectedClass = "inspectorClicked";
		}
		return "<a id='pinButton' class='btn btn-default " + inspectedClass + "'><img src='./img/pin-icon.png' style='width:16px; height:16px;' /></a>";
	}
	
	this.getCopyButton = function() {
		return "<a id='copyButton' class='btn btn-default'><img src='./img/copy-icon.png' style='width:16px; height:16px;' /></a>";
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
	
	this.enableCopyButtonEvent = function() {
		var localReference = this;
		$( "#copyButton" ).click(function() {
			var component = "<div class='form-horizontal'>"
				component += "<legend>Duplicate Entity</legend>";
				component += "<span class='glyphicon glyphicon-warning-sign'></span> The duplicate will not have parents or children: <br><br>";
				component += "<div class='form-group col-md-9'>";
				component += "<label class='control-label  " + localReference.labelColumnClass+ "'>Code&nbsp;(*):</label>";
				component += "<div class='" + localReference.controlColumnClass + "'>";
				component += "<input type='text' class='form-control' placeholder='Code' id='newSampleCodeForCopy' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
				component += "</div>";
				component += "<div class='" + localReference.controlColumnClass + "'>";
				component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
				component += "</div>";
				component += "</div>";
				
			var css = {
					'text-align' : 'left',
					'top' : '15%',
					'width' : '70%',
					'left' : '15%',
					'right' : '20%',
					'overflow' : 'auto'
			};
			
			Util.blockUI(component + "<br><br><br> <a class='btn btn-default' id='copyAccept'>Accept</a> <a class='btn btn-default' id='copyCancel'>Cancel</a>", css);
				
			$("#copyAccept").on("click", function(event) {
				var newSampleCodeForCopy = $("#newSampleCodeForCopy");
				var isValid = newSampleCodeForCopy[0].checkValidity();
				if(isValid) {
					var newSampleCodeForCopyValue = newSampleCodeForCopy.val();
					localReference.createSample(newSampleCodeForCopyValue);
					Util.unblockUI();
				} else {
					Util.showError("Invalid code.", function() {}, true);
				}
			});
			
			$("#copyCancel").on("click", function(event) { 
				Util.unblockUI();
			});
			
		});
	}
	
	this.childrenAdded = function() {
		var $childrenStorageDropdown = FormUtil.getDefaultBenchDropDown('childrenStorageSelector', true);
		if($childrenStorageDropdown && !$("#childrenStorageSelector").length) {
			var $childrenStorageDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenStorageDropdown, 'Storage');
			$("#newChildrenOnBenchDropDown").append($childrenStorageDropdownWithLabel);
		}
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		var sampleType = profile.getSampleTypeForSampleTypeCode(this.sampleTypeCode);
		var sampleTypeDisplayName = sampleType.description;
		
		if(!sampleTypeDisplayName) {
				sampleTypeDisplayName = this.sampleTypeCode;
		}

		var component = "";
		
			component += "<div class='row'>";
			component += "<div class='" + this.formColumnClass + "'>";
			
			var message = null;
			var pinButton = "";
			var editButton = "";
			var hierarchyButton = "";
			var copyButton = "";
			
			if (this.mode === SampleFormMode.CREATE) {
				message = "Create";
			} else if (this.mode === SampleFormMode.EDIT) {
				message = "Update";
				pinButton = this.getPINButton();
				hierarchyButton = this.getHierarchyButton();
				copyButton = this.getCopyButton();
				sampleTypeDisplayName = sample.code;
			} else if (this.mode === SampleFormMode.VIEW) {
				message = "View";
				pinButton = this.getPINButton();
				editButton = this.getEditButton();
				hierarchyButton = this.getHierarchyButton();
				copyButton = this.getCopyButton();
				sampleTypeDisplayName = sample.code;
			}
			
			if(this.isELNSubExperiment) {
				message += " Sub Experiment"; 
			}
			component += "<h2>" + message + " " + sampleTypeDisplayName + " " + pinButton + " " + copyButton + " " + hierarchyButton + " " + editButton + "</h2>";
			
			if (this.mode !== SampleFormMode.CREATE) {
				component += "<img data-preview-loaded='false' class='zoomableImage' id='preview-image' src='./img/image_loading.gif' style='height:300px; margin-right:20px; display:none;'></img>";
			}
			
			component += "<form class='form-horizontal' role='form' action='javascript:void(0);' onsubmit='mainController.currentView.createSample();'>";
			
			//
			// SELECT PROJECT/SPACE AND CODE
			//
			
			component += "<div>";
			component += "<legend>Identification Info</legend>";
			//Space/Project
			var spaceSelectEnabled = true;
			if(!this.isELNSubExperiment && 
					this.mode === SampleFormMode.CREATE && 
					this.profile.getSpaceForSampleType(this.sampleTypeCode) !== null) {
				spaceSelectEnabled = false;
			}
			
			if(spaceSelectEnabled) {
				component += "<div class='form-group'>";
				if(this.isELNSubExperiment) {
					component += "<label class='control-label " + this.labelColumnClass+ "'>Experiment&nbsp;(*):</label>";
				} else {
					component += "<label class='control-label " + this.labelColumnClass+ "'>Space&nbsp;(*):</label>";
				}
				
				component += "<div class='" + this.controlColumnClass + "'>";
				if(this.isELNSubExperiment) {
					component += "<input class='form-control' type='text' id='sampleSpaceProject' alt='Experiment Identifier' required>";
				} else {
					component += "<select class='form-control' id='sampleSpaceProject' required>";
					component += "<option disabled=\"disabled\" selected></option>";
					for(var i = 0; i < this.spaces.length; i++) {
						component += "<option value='"+this.spaces[i]+"'>"+this.spaces[i]+"</option>";
					}
					component += "</select>";
				}
				component += "</div>";
				component += "</div>";
			}
			
			//Code
			component += "<div class='form-group'>";
			component += "<label class='control-label  " + this.labelColumnClass+ "'>Code&nbsp;(*):</label>";
			component += "<div class='" + this.controlColumnClass + "'>";
			component += "<input type='text' class='form-control' placeholder='Code' id='sampleCode' pattern='[a-zA-Z0-9_\\-\\.]+' required>";
			component += "</div>";
			if(this.mode === SampleFormMode.CREATE) {
				component += "<div class='" + this.controlColumnClass + "'>";
				component += " (Allowed characters are: letters, numbers, '-', '_', '.')";
				component += "</div>";
			}
			
			component += "</div>";
			
			
			component += "</div>";
			
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
			if((this.mode !== SampleFormMode.VIEW) && this.isELNSubExperiment) {
				component += "<div>";
				component += "<div class='form-group'>";
				component += "<a class='btn btn-default' style='margin-left:25px;' id='generate_children'>Generate Children</a>";
				component += "</div>";
				component += "</div>";
				component += "<div id='newChildrenOnBenchDropDown'></div>";
			}
			
			
			//
			// SAMPLE TYPE FIELDS
			//
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				component += "<div>";
				
				if(propertyTypeGroup.name) {
					component += "<legend>" + propertyTypeGroup.name + "</legend>";
					var storagePropertyGroup = this.profile.getPropertyGroupFromStorage(propertyTypeGroup.name);
					if(storagePropertyGroup) {
						var containerId = "sampleStorage" + this.storages.length + 1;
						var storage = new Storage(this.serverFacade,containerId, this.profile, this.sampleTypeCode, this.sample, this.mode === SampleFormMode.VIEW);
						storage.init(storagePropertyGroup);
						this.storages.push(storage);
						component += "<div id='" + containerId + "'></div>"; // When a storage is used, the storage needs a container
					}
				} else {
					component += "<legend> Metadata</legend>";
				}
				
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(this.storages.length > 0 && this.storages[this.storages.length - 1].isPropertyFromStorage(propertyType.code)) { continue; } // When a storage is used, the storage controls the rendering of the properties
					
					var requiredText = "";
					if (propertyType.mandatory) {
						requiredText = "&nbsp;(*)";
					}
					
					component += "<div class='form-group'>";
					component += "<label class='control-label " + this.labelColumnClass+ "'>" + propertyType.label + requiredText + ":</label>";

					component += "<div class='" + this.controlColumnClass + "'>";
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
				component += "</div>";
			}
			
			//
			// Extra component placeholder defined by Profile.sampleFormContentExtra(sampleTypeCode, sample)
			//
			component += "<div id='sample-form-content-extra'></div>";
			
			//
			// FORM SUBMIT
			//
			if(!(this.mode === SampleFormMode.VIEW)) {
				component += "<fieldset style='margin-top:20px;'>";
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
			component += "<div id='dataSetViewerContainer' style='margin-top:10px;'></div>";
			
			component += "</form>";
			
			component += "</div>";
			component += "</div>";
			
			
		//Add form to layout
		$("#"+this.containerId).append(component);
		this.storages.forEach(function(storage) {storage.repaint()});
		
		//Enable Events
		$("#sampleCode").change(
			function() {
				$(this).val($(this).val().toUpperCase()); //Codes can only be upper case
			}
		);
		
		var localInstance = this;
		
		if (this.mode !== SampleFormMode.CREATE) {
			this.enablePINButtonEvent();
			this.enableCopyButtonEvent();
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
		var sampleType = this.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		if(sampleType !== null) {
			sampleTable = new SampleTable(this.serverFacade,"sampleSearchContainer", this.profile, sampleTypeCode, false, false, true, false, true);
			sampleTable.init();
		}
	}
	
	this.createSample = function(isCopyWithNewCode) {
		Util.blockUI();
		var localReference = this;
		
		//Other properties
		var properties = {};
		
		var sampleType = profile.getSampleTypeForSampleTypeCode(this.sampleTypeCode);
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
		
		var experimentIdentifier = null;
		var sampleSpace = null;
		var sampleProject = null;
		var sampleExperiment = null;
		
		if(this.sample !== null) {
			experimentIdentifier = this.sample.experimentIdentifierOrNull;
		} else if(this.isELNSubExperiment) {
			experimentIdentifier = $("#sampleSpaceProject").val();
		} else {
			sampleSpace = $("#sampleSpaceProject").val();
			if(!sampleSpace) {
				sampleSpace = this.profile.getSpaceForSampleType(this.sampleTypeCode);
			}
			experimentIdentifier = this.profile.getExperimentIdentifierForSample(this.sampleTypeCode, sampleCode, properties);
		}
		
		if(experimentIdentifier != null) {
			sampleSpace = experimentIdentifier.split("/")[1];
			sampleProject = experimentIdentifier.split("/")[2];
			sampleExperiment = experimentIdentifier.split("/")[3];
		}
		
		//Children to create
		var samplesToCreate = [];
		this.sampleLinksChildren.getSamples().forEach(function(child) {
			if(child.newSample) {
				if(this.profile.storagesConfiguration["isEnabled"]) {
					child.properties = {};
					child.properties[localReference.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["NAME_PROPERTY"]] = $("#childrenStorageSelector").val();
					child.properties[localReference.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["ROW_PROPERTY"]] = 1;
					child.properties[localReference.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["COLUMN_PROPERTY"]] = 1;
					child.properties[localReference.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["BOX_PROPERTY"]] = $("#sampleSpaceProject").val().replace(/\//g,'\/') + "_" + $("#sampleCode").val() + "_EXP_RESULTS";
					child.properties[localReference.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["USER_PROPERTY"]] = localReference.serverFacade.openbisServer.getSession().split("-")[0];
				}
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
				"sampleExperiment" : sampleExperiment,
				"sampleCode" : sampleCode,
				"sampleType" : this.sampleTypeCode,
				//Other Properties
				"sampleProperties" : properties,
				//Parent links
				"sampleParents": sampleParentsFinal,
				//Children links
				"sampleChildren": sampleChildrenFinal,
				"sampleChildrenNew": samplesToCreate,
				"sampleChildrenRemoved": sampleChildrenRemovedFinal
		};
		
		if(isCopyWithNewCode) {
			parameters["method"] = "insertSample";
			parameters["sampleCode"] = isCopyWithNewCode;
			parameters["sampleParents"] = [];
			parameters["sampleChildren"] = [];
			parameters["sampleChildrenNew"] = [];
			parameters["sampleChildrenRemoved"] = [];
		}
		
		if(this.profile.allDataStores.length > 0) {
			this.serverFacade.createReportFromAggregationService(this.profile.allDataStores[0].code, parameters, function(response) {
				localReference.createSampleCallback(response, localReference, isCopyWithNewCode);
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
		
		return false;
	}

	this.createSampleCallback = function(response, localReference, isCopyWithNewCode) {
		if(response.error) { //Error Case 1
			Util.showError(response.error.message, function() {Util.unblockUI();});
		} else if (response.result.columns[1].title === "Error") { //Error Case 2
			var stacktrace = response.result.rows[0][1].value;
			Util.showStacktraceAsError(stacktrace);
		} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
			var sampleType = profile.getSampleTypeForSampleTypeCode(this.sampleTypeCode);
			var sampleTypeDisplayName = sampleType.description;
			if(!sampleTypeDisplayName) {
				sampleTypeDisplayName = this.sampleTypeCode;
			}
			var message = "";
			if(this.mode === SampleFormMode.CREATE) {
				message = "Created.";
			} else if(this.mode === SampleFormMode.EDIT) {
				message = "Updated.";
			} else if(isCopyWithNewCode) {
				message = "copied with new code: " + isCopyWithNewCode + ".";
			}
			
			var callbackOk = function() {
				Util.unblockUI();
				if(localReference.experimentIdentifier) {
					mainController.sideMenu.refreshSubExperiment(localReference.experimentIdentifier);
				} else if(isCopyWithNewCode && localReference.isELNSubExperiment) {
					mainController.sideMenu.refreshSubExperiment(localReference.sample.experimentIdentifierOrNull);
				}
//				TO-DO: The Sample is not necessarily searchable after creation since the index runs asynchronously
//				localReference.serverFacade.searchWithType(localReference.sampleTypeCode, $("#sampleCode")[0].value, function(data) {
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
								img.show();
							}
						};
					_this.serverFacade.listFilesForDataSet(data.result[0].code, "/", true, listFilesForDataSetCallback);
				}
			};
		
		this.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [this.sample.permId], previewCallback);
	}
	
	this._generateChildren = function() {
		// Utility self contained methods
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
			
			//Number of Replicas
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
			}
			
			var generatedChildrenWithReplicas = [];
			for(var i = 0; i < generatedChildren.length; i++) {
				for(var j = 0; j < numberOfReplicas; j++) {
					generatedChildrenWithReplicas.push(generatedChildren[i] + "_" + (j + 1));
				}
			}
			
			return generatedChildrenWithReplicas;
		}
		
		var showPreview = function() {
			$("#previewChildrenGenerator").empty();
			
			var generatedChildren = getGeneratedChildrenCodes();
			//Show generated children
			if(generatedChildren) {
				for(var i = 0; i < generatedChildren.length; i++) {
					$("#previewChildrenGenerator").append(generatedChildren[i] + "<br />");
				}
			}
		}
		
		var _this = this;
		// Buttons
		var $generateButton = $("<a>", { "class" : "btn btn-default" }).append("Generate!");
		$generateButton.click(function(event) { 
			var generatedChildrenSpace = null;
			if(_this.isELNSubExperiment) {
				generatedChildrenSpace = $("#sampleSpaceProject")[0].value.split("/")[1];
			} else {
				generatedChildrenSpace = $("#sampleSpaceProject").val();
			}
			
			var numberOfReplicas = parseInt($("#childrenReplicas").val());
			if(isNaN(numberOfReplicas) || numberOfReplicas < 0 || numberOfReplicas > 1000) {
				Util.showError("The number of children replicas should be an integer number bigger than 0 and lower than 1000.", function() {}, true);
				return;
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
				
				_this.childrenAdded();
				Util.unblockUI();
			}
			
			
		});
		
		var $cancelButton = $("<a>", { "class" : "btn btn-default" }).append("<span class='glyphicon glyphicon-remove'></span>");
		$cancelButton.click(function(event) { 
			Util.unblockUI();
		});
		
		var $selectAllButton = $("<a>", { "class" : "btn btn-default" }).append("Enable/Disable All");
		$selectAllButton.attr("ison", "false");
		
		$selectAllButton.click(function(event) {
			var $button = $(this);
			var isOn = !($button.attr("ison") === "true");
			$button.attr("ison", isOn);
			
			var $parentsFields = $("#parentsToGenerateChildren").find("input");
			for(var i = 0; i < $parentsFields.length; i++) {
				var $parentField = $parentsFields[i];
				$parentField.checked = isOn;
			}
			
			showPreview();
		});
		
		// Parents
		var $parents = $("<div>");
		var parentsIdentifiers = this.sampleLinksParents.getSamplesIdentifiers();
		var parentsByType = {}; //This is the main model
		var parentsByIdentifier = {}; // Used by the getGeneratedChildrenCodes function
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
		
		var $parentsTable = $("<table>", { "class" : "table table-bordered table-compact" });
		var $headerRow = $("<tr>");
		$parentsTable.append($headerRow);
		var maxDepth = 0;
		for (var key in parentsByType) {
			$headerRow.append($("<th>", {"class" : "text-center-important"}).text(key));
			maxDepth = Math.max(maxDepth, parentsByType[key].length);
		}

		for (var idx = 0; idx < maxDepth; idx++) {
			var $tableRow = $("<tr>");
			for (key in parentsByType) {
				if (idx < parentsByType[key].length) {
					var parent = parentsByType[key][idx];
					var parentProperty = {
							code : parent.identifier,
							description : parent.identifier,
							label : parent.code,
							dataType : "BOOLEAN"
					};
					
					var $checkBox = $('<input>', {'style' : 'margin-bottom:7px;', 'type' : 'checkbox', 'id' : parent.identifier, 'alt' : parent.identifier, 'placeholder' : parent.identifier });
					$checkBox.change(function() { 
						showPreview();
					});
					
					var $field = $('<div>');
					$field.append($checkBox);
					$field.append(" " + parent.code);
					
					$tableRow.append($("<td>").append($field));
 				} else {
 					$tableRow.append($("<td>").html("&nbsp;"));
 				}
			}
			$parentsTable.append($tableRow);
		}
		
		$parents.append($parentsTable);
		
		var $parentsComponent = $("<div>", { "id" : 'parentsToGenerateChildren'} );
		$parentsComponent.append($("<legend>").append("Parents ").append($selectAllButton))
		$parentsComponent.append($parents);
		
		// Children
		var $childrenComponent = $("<div>");
		$childrenComponent.append($("<legend>").text("Children"))
		
		var $childrenTypeDropdown = FormUtil.getSampleTypeDropdown('childrenTypeSelector', true);
		var $childrenTypeDropdownWithLabel = FormUtil.getFieldForComponentWithLabel($childrenTypeDropdown, 'Type');
		$childrenComponent.append($childrenTypeDropdownWithLabel);
		
		var $childrenReplicas = FormUtil._getInputField('number', 'childrenReplicas', 'Children Replicas', '1', true);
		$childrenReplicas.val("1");
		$childrenReplicas.keyup(function() { 
			showPreview();
		});
		
		var $childrenReplicasWithLabel = FormUtil.getFieldForComponentWithLabel($childrenReplicas, 'Children Replicas');
		$childrenComponent.append($childrenReplicasWithLabel);
		
		// Preview
		var $previewComponent = $("<div>");
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
		Util.blockUI($childrenGenerator, {'text-align' : 'left', 'top' : '10%', 'width' : '80%', 'left' : '10%', 'right' : '10%', 'height' : '80%', 'overflow' : 'auto'});
	}
}