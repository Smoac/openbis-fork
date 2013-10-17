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
	
	//Pagination related attributes
	this._filteredSamples = null;
	this._samplesToPaint = null;
	this._total = null;
	this._start = null;
	
	if(isEmbedded) {
		this._limit = 5;
	} else {
		this._limit = 20;
	}
	this._adjacentPages = 2;
	
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
		for(var i = 0; i < this._samplesToPaint.length; i++) {
			if(this._samplesToPaint[i].permId === samplePermId) {
				sample = this._samplesToPaint[i];
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
		
	this._getPaginationComponent = function(total, start, limit, adjacentPages) {
		//Check if there is elements
		if(total === 0) {
			return $("<div>").append("No elements found.");
		}
		
		//Current page
		var currentPage = (start / limit) + 1;
		//Number of pages
		var numPages = Math.floor(total / limit);
		if(total % limit !== 0) { //The last page usually is not complete
			numPages++;
		}
		//Shown To fix for the last page
		var shownTo = start + limit;
		if(shownTo > total) {
			shownTo = total;
		}
		
		//Pagination component
		var $component = $("<span>");
		$component.append("Showing " + (start + 1) + " to " + shownTo + " from " + total + " ");

		var localReference = this;
		var paginationClick = function(firstElementFromPage) {
			return function() {
				localReference._reloadWithSamplesAndPagination(firstElementFromPage);
			}
		}
		
		//Start Pages
		if(currentPage == 1) {
			var $paginationItem = $("<a>", { class: "btn paginationItem" }).attr('disabled', 'disabled').append($("<i>", { class: "icon-fast-backward" }));
			$component.append($paginationItem);
			
			$paginationItem = $("<a>", { class: "btn paginationItem" }).attr('disabled', 'disabled').append($("<i>", { class: "icon-step-backward" }));
			$component.append($paginationItem);
		} else {
			var $paginationItem = $("<a>", { class: "btn paginationItem", click: paginationClick(0) }).append($("<i>", { class: "icon-fast-backward" }));
			$component.append($paginationItem);
			
			var firstElementFromPage =  limit * (currentPage - 2);
			$paginationItem = $("<a>", { class: "btn paginationItem", click: paginationClick(firstElementFromPage) }).append($("<i>", { class: "icon-step-backward" }));
			$component.append($paginationItem);
		}
		
		//Middle Pages
		for(var pageNumber = 1; pageNumber <= numPages; pageNumber++) {
			if(!(pageNumber < currentPage - adjacentPages) && !(pageNumber > currentPage + adjacentPages)) {
				var firstElementFromPage =  limit * (pageNumber - 1);
				$paginationItem = null;
				if(currentPage === pageNumber) {
					$paginationItem = $("<a>", { class: "btn btn-primary paginationItem", click: paginationClick(firstElementFromPage) }).append(pageNumber);
				} else {
					$paginationItem = $("<a>", { class: "btn paginationItem", click: paginationClick(firstElementFromPage) }).append(pageNumber)
				}
				$component.append($paginationItem);
			}
		}
		
		//End Pages
		if(currentPage == numPages) {
			var $paginationItem = $("<a>", { class: "btn paginationItem" }).attr('disabled', 'disabled').append($("<i>", { class: "icon-forward" }));
			$component.append($paginationItem);
			
			$paginationItem = $("<a>", { class: "btn paginationItem" }).attr('disabled', 'disabled').append($("<i>", { class: "icon-fast-forward" }));
			$component.append($paginationItem);
		} else {
			var firstElementFromPage =  limit * (currentPage);
			var $paginationItem = $("<a>", { class: "btn paginationItem", click: paginationClick(firstElementFromPage) }).append($("<i>", { class: "icon-step-forward" }));
			$component.append($paginationItem);
			
			firstElementFromPage =  limit * (numPages - 1);
			$paginationItem = $("<a>", { class: "btn paginationItem", click: paginationClick(firstElementFromPage) }).append($("<i>", { class: "icon-fast-forward" }));
			$component.append($paginationItem);
		}

		return $component;
	}
	
	this.repaint = function() {
		if($("#tableContainer").length > 0) {
			this.repaintTable();
			return;
		}
		
		$("#"+this.sampleTableId).empty();
		
		//
		// Table Containers
		//
		var component = "";
			component += "<div class='row-fluid'>";
			component += "<div class='span12'>";
			component += "<div id='vis'>";
			component += "<div class='tableContainerBorder'>";
			component += "<div id='tableContainer'></div>";
			component += "</div>";
			component += "</div>";
			component += "</div>";
			component += "</div>";
			
		$("#"+this.sampleTableId).append(component);
		
		//
		// Properties to show on the columns
		//
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
		}
		
		//
		// Table Headers
		//
		if(this.sampleTypeCode == "SEARCH") {
			$("#tableContainer").append("<div id='paginationContainerTop' class='paginationTop'></div>");
			$("#tableContainer").append("<div class='wrapper' style='clear: both; padding-top: 10px;'>");
		} else {
			$("#tableContainer").append("<div class='tableFilterContainer'><input placeholder='filter visible columns' class='tableFilter search-query' id='table-filter' type='text'></div> <div id='paginationContainerTop' class='paginationTop'></div>");	
		}
		
		if (!this.isEmbedded && !this.isSearch) {
			var toolBoxContainer = "<span class='toolBox' id='toolBoxContainer'></span>";
			$("#paginationContainerTop").append(toolBoxContainer);
			$("#tableContainer").append("<div class='wrapper' style='clear: both; padding-top: 10px;'>");
			$("#toolBoxContainer").append("<input type='file' id='fileToRegister' style='display:none;' /><a class='btn' href=\"javascript:mainController.sampleTable.registerSamples();\"><i class='icon-upload'></i>r</a>");
			$("#toolBoxContainer").append("<input type='file' id='fileToUpdate' style='display:none;' /><a class='btn' href=\"javascript:mainController.sampleTable.updateSamples();\"><i class='icon-upload'></i>u</a>");
			$("#toolBoxContainer").append("<a class='btn' href=\"javascript:mainController.sampleTable.createNewSample();\"><i class='icon-plus-sign'></i></a>");
		}
		
		var tableTemplate = "<table style='width:100%;' class='table table-hover' id=\"sample-table\"><thead>";
		tableTemplate += "<tr class=\"sample-table-header\"><th>Code</th>";
		for (var i = 0; i < sampleTypePropertiesDisplayNames.length; i++) {
			tableTemplate += "<th>" + sampleTypePropertiesDisplayNames[i]+ "</th>";
		}
		tableTemplate += "</tr></thead><tbody id='sample-data-holder'></tbody></table>";
		$("#tableContainer").append(tableTemplate);
		
		//
		// Attach Filter Functions to DOM
		//
		var localReference = this;
		$('#table-filter').keyup(function() { localReference._filter(); });
		
		// Pagination at the bottom
		$("#tableContainer").append("<div id='paginationContainerBottom' class='paginationBottom'></div>");
		
		this.repaintTable();
	}
	
	this.repaintTable = function() {
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
		}
		
		//
		// Table Rows
		//
		if (this._samplesToPaint.length < 1) {
			return;
		}
		
		$("#sample-data-holder").empty();
		var selection = d3.select("#vis").select("#sample-data-holder").selectAll("tr.sample-table-data").data(this._samplesToPaint);
	
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
		
		$("#paginationContainerTop").empty();
		$("#paginationContainerTop").append(this._getPaginationComponent(this._filteredSamples.length, this._start, this._limit, this._adjacentPages));
		$("#paginationContainerBottom").empty();
		$("#paginationContainerBottom").append(this._getPaginationComponent(this._filteredSamples.length, this._start, this._limit, this._adjacentPages));
	}
	
	this._filter = function(filterResults) {
		//Obtain filter tokens
		var filterValue = $('#table-filter').val();
		var filterValueTokensAux = filterValue.toLowerCase().split(" ");
		var filterValueTokens = [];
		
		for(var i = 0; i < filterValueTokensAux.length; i++) {
			if(filterValueTokensAux[i].trim() !== "") {
				filterValueTokens.push(filterValueTokensAux[i]);
			}
		}
		
		//Obtain visible table column codes (only filter using those)
		var sampleTypeProperties = this.profile.typePropertiesForTable[this.sampleTypeCode];
		if(sampleTypeProperties === null || sampleTypeProperties === undefined) {
			sampleTypeProperties = this.profile.getAllPropertiCodesForTypeCode(this.sampleTypeCode);
		}
		
		//filter, iterates all conditions given by the different filter tokens and builds an array of results for each sample
		var filteredSamplesHolder = [];
		for(var i = 0, lenI = this.samples.length; i < lenI; ++i) {
			var filterValueTokensPassed = [];
			for(var j = 0, lenJ = filterValueTokens.length; j < lenJ; ++j) {
				filterValueTokensPassed[j] = (this.samples[i].code.toLowerCase().indexOf(filterValueTokens[j]) !== -1);
				if(!filterValueTokensPassed[j]) {
					for(var z = 0, lenZ = sampleTypeProperties.length; z < lenZ; ++z) {
						var propertyCode = sampleTypeProperties[z];
						var propertyValue = this.samples[i].properties[propertyCode];
						
						if(propertyValue) {
							filterValueTokensPassed[j] = propertyValue.toLowerCase().indexOf(filterValueTokens[j]) !== -1;
							if(filterValueTokensPassed[j]) {
								break;
							}
						}
					}
				}
			}
			
			//Check if the conditions given by the different filter tokens are accomplished
			var pass = true;
			for(var k = 0; k < filterValueTokens.length; k++) {
				pass = pass && filterValueTokensPassed[k];
			}
			if(pass) {
				filteredSamplesHolder.push(this.samples[i]);
			}
		}
		
		//Repaint first page
		this._filteredSamples = filteredSamplesHolder;
		this._reloadWithSamplesAndPagination(0);
	}
	
	this.openEditWindowForSample = function(code, permId) {
		var sample = null;
		for(var i = 0; i < this._samplesToPaint.length; i++) {
			if (this._samplesToPaint[i].permId === permId) {
				sample = this._samplesToPaint[i];
				break;
			}
		}
		mainController.showEditSamplePage(sample); //TO-DO : Fix Global Access
	}
	
	this.reloadWithSamples = function(returnedSamples) {
		var sortedSamples = null;
		if(this.isSearch) {
			sortedSamples = returnedSamples;
		} else {
			sortedSamples = this.profile.searchSorter(returnedSamples);
		}
		
		this.samples = sortedSamples;
		this._filteredSamples = this.samples;
		this._reloadWithSamplesAndPagination(0);
	}
	
	this._reloadWithSamplesAndPagination = function(start) {
		//Repaint first page
		this._total = this._filteredSamples.length;
		this._start = start;
		this._samplesToPaint = this._filteredSamples.slice(this._start, this._start + this._limit);
		this.repaint();
	}
}
