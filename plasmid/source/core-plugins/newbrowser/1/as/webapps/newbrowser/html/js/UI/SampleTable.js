/*
 * Copyright 2013 ETH Zuerich, CISD
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

/**
 * Creates an instance of SampleTable.
 *
 *
 * @constructor
 * @this {SampleTable}
 * @param {ServerFacade} serverFacade Used to access all server side calls.
 * @param {string} sampleTableId The Container where the Inspector DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 * @param {string} sampleTypeCode The code of the sample type to be displayed on the table.
 * @param {boolean} inspectEnabled Enables the button that allows to pin samples.
 * @param {boolean} enableAdd Enables the event that allows to add samples, this is hardwired thinking that a proper SampleLinksTable exists with certain id.
 * @param {boolean} isSearch Enables search related behaviour that checks in what field was matched.
 * @param {boolean} isEmbedded When enabled the sample table will be inside a box of limited height with his own scroll.
 * @param {Inspector} inspector Used to add selected samples to show them as notes.
 */
function SampleTable(serverFacade, sampleTableId, profile, sampleTypeCode, inspectEnabled, enableEdit, enableAdd, isSearch, isEmbedded, inspector) {
	this.serverFacade = serverFacade;
	this.sampleTableId = sampleTableId;
	this.profile = profile;
	this.sampleTypeCode = sampleTypeCode;
	this.inspectEnabled = inspectEnabled;
	this.enableEdit = enableEdit;
	this.enableAdd = enableAdd;
	this.isSearch = isSearch;
	this.samples = new Array();
	this.isEmbedded = isEmbedded;
	this.inspector = inspector;

	this.init = function() {
		Util.blockUI();
		var localReference = this;
		this.serverFacade.searchWithType(this.sampleTypeCode, null, function(data) {
			localReference.reloadWithSamples(data);
			Util.unblockUI();
		});
	}
	
	this.createNewSample = function() {
		mainController.showCreateSamplePage(this.sampleTypeCode); //TO-DO : Fix global access
	}
	
	this.registerSamples = function() {
		var localReference = this;
		$("#fileToRegister").unbind('change');
		$("#fileToRegister").change(function() {
			Util.blockUI();
			localReference.serverFacade.fileUpload("fileToRegister", function(result) {
				//Code After the upload
				localReference.serverFacade.uploadedSamplesInfo(localReference.sampleTypeCode, "sample-file-upload", 
					function(infoData) {
						var finalCallback = function(data) {
							if(data.error) {
								Util.showError(data.error.message, function() {Util.unblockUI();});
							} else if(data.result) {
								var extraMessage = "<br> It can take a couple of minutes to have them available.";
								Util.showSuccess(data.result + extraMessage, function() {Util.unblockUI();});
							} else {
								Util.showError("Unknown response. Probably an error happened.", function() {Util.unblockUI();});
							}
						};
						
						if(infoData.result.identifiersPressent) {
							localReference.serverFacade.registerSamples(localReference.sampleTypeCode, "sample-file-upload", null, finalCallback);
						} else {
							localReference.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
								var spaces = [];
								for(var i = 0; i < data.result.length; i++) {
									spaces.push(data.result[i].code);
								}
								
								var component = "<select id='sampleSpaceSelector' required>";
								component += "<option disabled=\"disabled\" selected></option>";
								for(var i = 0; i < spaces.length; i++) {
									component += "<option value='"+spaces[i]+"'>"+spaces[i]+"</option>";
								}
								component += "</select>";
								
								Util.blockUI("Space not found, please select it for automatic generation: <br><br>" + component + "<br> or <a class='btn' id='spaceSelectionCancel'>Cancel</a>");
								
								$("#sampleSpaceSelector").on("change", function(event) {
									var space = $("#sampleSpaceSelector")[0].value;
									Util.blockUI();
									localReference.serverFacade.registerSamples(localReference.sampleTypeCode, "sample-file-upload", '/' + space, finalCallback);
								});
								
								$("#spaceSelectionCancel").on("click", function(event) { 
									Util.unblockUI();
								});
								
							});
						}
					}
				);
			});
		});
		$("#fileToRegister").click();
	}
	
	this.updateSamples = function() {
		var localReference = this;
		$("#fileToUpdate").unbind('change');
		$("#fileToUpdate").change(function() {
			Util.blockUI();
			var finalCallback = function(data) {
				if(data.error) {
					Util.showError(data.error.message, function() {Util.unblockUI();});
				} else if(data.result) {
					Util.showSuccess(data.result, function() {Util.unblockUI();});
				} else {
					Util.showError("Unknown response. Probably an error happened.", function() {Util.unblockUI();});
				}
			};
			
			localReference.serverFacade.fileUpload("fileToUpdate", function(result) {
				//Code After the upload
				localReference.serverFacade.updateSamples(localReference.sampleTypeCode, "sample-file-upload", null,finalCallback);
			});
		});
		$("#fileToUpdate").click();
	}
	
	this.previewNote = function(samplePermId, attachTo) {
		var sample = null;
		for(var i = 0; i < this.samples.length; i++) {
			if(this.samples[i].permId === samplePermId) {
				sample = this.samples[i];
				break;
			}
		}
		var localReference = this;
		
		document.getElementById(attachTo).onmouseover = function(event){
			var content = localReference.inspector.getInspectorTable(sample, false, true, false);
			
			$("#navbar").tooltip({
				html: true,
				placement: 'bottom',
				title: content,
				trigger: 'hover',
				animation: false
			});
		
			$("#navbar").tooltip('toggle');
		};
		
		document.getElementById(attachTo).onmouseout = function() {
			$("#navbar").tooltip('destroy');
		}
		
		document.getElementById(attachTo).onclick = function() {
			var isInspected = localReference.inspector.toggleInspectSample(sample);
			if(isInspected) {
				$('#' + attachTo).addClass('inspectorClicked');
			} else {
				$('#' + attachTo).removeClass('inspectorClicked');
			}
		}
	}
	
	this.repaint = function() {
		$("#"+this.sampleTableId).empty();
		
		//
		// Table Containers
		//
		var component = "";
		
			component += "<div class='row-fluid'>";
			component += "<div class='span12'>";
			component += "<div id='vis'>";
			component += "<div id='tableMessages'></div>";
			
			if(this.isEmbedded) {
				component += "<div class='tableContainerBorder' style='height: 350px; width:100%; overflow: auto;'>";
			} else {
				component += "<div class='tableContainerBorder'>";
			}
			
			component += "<div id='tableContainer'></div>";
			component += "</div>";
			component += "</div>";
			component += "</div>";
			component += "</div>";
			
		$("#"+this.sampleTableId).append(component);
		
		//
		// Table Headers
		//
		var tableTemplate = "<table style='width:100%;' class='table table-hover' id=\"sample-table\"><thead>";
		
		var sampleTypeProperties = null;
		var sampleTypePropertiesDisplayNames = null;
		
		if(this.sampleTypeCode == "SEARCH") {
			sampleTypeProperties = this.profile.searchType["SAMPLE_TYPE_PROPERTIES"];
			sampleTypePropertiesDisplayNames = this.profile.searchType["SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME"];
		} else {
			sampleTypeProperties = this.profile.typePropertiesForTable[this.sampleTypeCode];
			if(sampleTypeProperties === null || sampleTypeProperties === undefined) {
				sampleTypeProperties = this.profile.getAllPropertiCodesForTypeCode(this.sampleTypeCode);
			}
		
			sampleTypePropertiesDisplayNames = this.profile.getPropertiesDisplayNamesForTypeCode(this.sampleTypeCode, sampleTypeProperties);
			
			tableTemplate += "<tr style='border:none; border-collapse:collapse;'>";
			tableTemplate += "<td style='border:none; border-collapse:collapse;'><input placeholder='Code filter' style=\"width: 100%\" id=\"CODE_filter\" type=\"text\"></td>";
			for(var i=0; i<sampleTypePropertiesDisplayNames.length;i++) {
				tableTemplate += "<td style='border:none; border-collapse:collapse;'><input placeholder='"+sampleTypePropertiesDisplayNames[i]+" filter' style=\"width: 100%\" id=\""+sampleTypeProperties[i]+"_filter\" type=\"text\"></td>";
			}
			tableTemplate += "<td></td>";
			tableTemplate += "<td></td>";
			tableTemplate += "</tr>";
		}
	
		tableTemplate += "<tr class=\"sample-table-header\"><th>Code</th>";
		for (var i = 0; i < sampleTypePropertiesDisplayNames.length; i++) {
			tableTemplate += "<th>" + sampleTypePropertiesDisplayNames[i]+ "</th>";
		}
		
		if (this.isEmbedded || this.isSearch) {
			tableTemplate += "<th></th>";
			tableTemplate += "<th></th>";
		} else {
			tableTemplate += "<th style='white-space: nowrap;'><input type='file' id='fileToRegister' style='display:none;' /><a class='btn' href=\"javascript:mainController.sampleTable.registerSamples();\"><i class='icon-upload'></i>r</a></th>";
			tableTemplate += "<th style='white-space: nowrap;'><input type='file' id='fileToUpdate' style='display:none;' /><a class='btn' href=\"javascript:mainController.sampleTable.updateSamples();\"><i class='icon-upload'></i>u</a></th>";
			tableTemplate += "<th><a class='btn' href=\"javascript:mainController.sampleTable.createNewSample();\"><i class='icon-plus-sign'></i></a></th>";
		}
		tableTemplate += "</tr></thead><tbody id='sample-data-holder'></tbody></table>";
	
		$("#tableContainer").append(tableTemplate);
		
		//
		// Attach Filter Functions to DOM
		//
		var localReference = this;
		$('#CODE_filter').keyup(function() {
			var filterResults = [];
				filterResults[0] = $('#CODE_filter').val();
			for(var i=0;i<sampleTypeProperties.length;i++) {
				filterResults[i+1] = $('#'+sampleTypeProperties[i].replace('$','\\$')+'_filter').val();
			}
		
			localReference.filter (
				filterResults
			);
		});	
	
		for(var i = 0; i < sampleTypeProperties.length; i++) {
			$('#'+sampleTypeProperties[i].replace('$','\\$')+'_filter').keyup(function() {
				var filterResults = [];
					filterResults[0] = $('#CODE_filter').val();
				for(var i=0;i<sampleTypeProperties.length;i++) {
					filterResults[i+1] = $('#'+sampleTypeProperties[i].replace('$','\\$')+'_filter').val();
				}
			
				localReference.filter(
					filterResults
				);
			});	
		}
		
		//
		// Table Rows
		//
		if (this.samples.length < 1) {
			$("#tableMessages").append("<p style='background:yellow;'>No data found.<p>");
			return;
		}
		
		var selection = d3.select("#vis").select("#sample-data-holder").selectAll("tr.sample-table-data").data(this.samples);
	
		//Code under enter is run if there is no HTML element for a data element
	
		var onClickFunction = null;
		
		if(this.enableAdd) {
			onClickFunction = function(sample) {
				var sampleTypeGroup = localReference.profile.getGroupTypeCodeForTypeCode(sample.sampleTypeCode);
				mainController.sampleForm.addLinkedSample(sampleTypeGroup, sample); //TO-DO : Fix Global Access
			}
		} else {
			onClickFunction = function(sample) {
				mainController.showViewSamplePage(sample); //TO-DO : Fix Global Access
			}
		}
	
		var searchText = $('#search').val();
		var searchRegexpText = ("*" + searchText + "*").replace(/\*/g, ".*");
		var searchRegexp = new RegExp(searchRegexpText, "i");
		
		var localReference = this;
		selection.enter()
			.append("tr")
			.attr("class", "sample-table-data")
			.attr("id", function(sample){ return sample.permId })
			.attr("permId", function(sample){ return sample.permId })
			.style("cursor", "pointer")
			.on("click", onClickFunction)
			.selectAll("td").data(function(sample) {
				var tableFields = null;
			
				if(localReference.isSearch) {
					tableFields = [sample.identifier.slice(11,50), sample.sampleTypeCode, sample.properties, sample.properties ];
				} else {
					tableFields = [sample.code];
					for(var i=0; i<sampleTypeProperties.length; i++) {
						var tableFieldValue = sample.properties[sampleTypeProperties[i]];
						tableFields[tableFields.length] = Util.getEmptyIfNull(tableFieldValue);
					}
				}
				
				if(localReference.inspectEnabled) {
					var inspectedClass = "";
					if(localReference.inspector.containsSample(sample) !== -1) {
						inspectedClass = "inspectorClicked";
					}
					tableFields[tableFields.length] = "<a id='PIN_" + sample.permId + "' class='btn pinBtn " + inspectedClass + "' onmouseover=\"mainController.sampleTable.previewNote('" + sample.permId + "', 'PIN_" + sample.permId + "');\" ><img src='./images/pin-icon.png' style='width:16px; height:16px;' /></a>";
				}
				
				if(localReference.enableEdit) {
					tableFields[tableFields.length] = "<a class='btn' href=\"javascript:mainController.sampleTable.openEditWindowForSample('"+sample.code+"', '"+sample.permId+"');\"><i class='icon-edit'></i></a>";
					tableFields[tableFields.length] = "";
				}
				
				return tableFields;
				})
				.enter()
				.append("td")
				.append("div")
				.attr("class", "sample-table-data-cell")
				.html(
					function(d, index) {
						if (localReference.isSearch && index == 2) {
							if (searchText && searchText.length > 0 && d) {
								for (propertyName in d) {
									var propertyValue = d[propertyName];
									if (propertyValue && searchRegexp.test(propertyValue)) {
										if(propertyValue.indexOf("<root>") != -1) {
											if(profile.getHTMLTableFromXML) {
												return profile.getHTMLTableFromXML(propertyValue);
											} else {
												return propertyValue;
											}
										} else {
											return propertyValue;
										}
									}
								}
							}
						} else if (localReference.isSearch && index == 3) {
							if (searchText && searchText.length > 0 && d) {
							
								for (propertyName in d) {
									var propertyValue = d[propertyName];
									if (propertyValue && searchRegexp.test(propertyValue)) {
										return propertyName;
									}
								}
							}
						} else {
							return d;
						}
						return "";
					}
				);
			
				$('a').click(function(e){
				   e.stopPropagation();
				});
	}
	
	this.filterInternal = function(filter, property, element, index, array) {
		filter = filter.toLowerCase();
		if (filter.length < 1) return true;
		
		if (property == "CODE") {
			if (element.code.toLowerCase().indexOf(filter) != -1) {
				return true;
			}
		}	
		
		if (element.properties[property] == undefined) {
			return false;
		}
		
		if (element.properties[property].toLowerCase().indexOf(filter) != -1) {
			return true;
		}
		
		return false;
	}
	
	this.filter = function(filterResults) {
		var sampleTypeProperties = this.profile.typePropertiesForTable[this.sampleTypeCode];
		if(sampleTypeProperties === null || sampleTypeProperties === undefined) {
			sampleTypeProperties = this.profile.getAllPropertiCodesForTypeCode(this.sampleTypeCode);
		}
		
		var displayedSamples;
		
		displayedSamples = this.samples.filter(this.filterInternal.curry(((filterResults[0] == undefined)?"":filterResults[0]), "CODE"));
		for(var i=0; i < sampleTypeProperties.length;i++) {
				displayedSamples = displayedSamples.filter(this.filterInternal.curry((filterResults[i+1] == undefined)?"":filterResults[i+1],sampleTypeProperties[i]));
		}
		
		
		var selection2 = d3.select("#vis").select("#sample-table").selectAll("tr.sample-table-data").data(this.samples);
			// Code under enter is run if there is no HTML element for a data element
			selection2.style("display", function(data) {
			 		if ($.inArray(data, displayedSamples) != -1) {
						return "table-row"
					} 
					else {
						return  "none"
					}
				});
			
	};
	
	this.openEditWindowForSample = function(code, permId) {
		var sample = null;
		for(var i = 0; i < this.samples.length; i++) {
			if (this.samples[i].permId === permId) {
				sample = this.samples[i];
				break;
			}
		}
		mainController.showEditSamplePage(sample); //TO-DO : Fix Global Access
	}
	
	this.reloadWithSamples = function(returnedSamples)
	{
		if(this.isSearch) {
			this.samples = returnedSamples;
		} else {
			this.samples = this.profile.searchSorter(returnedSamples);
		}
		
		this.repaint();
	}
	
}