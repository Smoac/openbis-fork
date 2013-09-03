function SampleTable(sampleTableId, profile, sampleTypeCode,inspectEnabled, enableEdit, enableAdd, isSearch, isEmbedded) {
	this.sampleTableId = sampleTableId;
	this.profile = profile;
	this.sampleTypeCode = sampleTypeCode;
	this.inspectEnabled = inspectEnabled;
	this.enableEdit = enableEdit;
	this.enableAdd = enableAdd;
	this.isSearch = isSearch;
	this.samples = new Array();
	this.isEmbedded = isEmbedded;
	
	this.init = function() {
		Util.blockUI();
		var localReference = this;
		Search.searchWithType(this.sampleTypeCode, null, function(data) {
			localReference.reloadWithSamples(data);
			Util.unblockUI();
		});
	}
	
	this.createNewSample = function() {
		showCreateSamplePage(this.sampleTypeCode);
	}
	
	this.previewNote = function(sampleCode, attachTo) {
		var sample = null;
		for(var i = 0; i < this.samples.length; i++) {
			if(this.samples[i].code === sampleCode) {
				sample = this.samples[i];
				break;
			}
		}
		
		document.getElementById(attachTo).onmouseover = function(event){
			var content = inspector.getInspectorTable(sample, false, true, false);
			
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
			var isInspected = inspector.toggleInspectSample(sample);
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
		
		var SAMPLE_TYPE_PROPERTIES = null;
		var SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME = null;
		
		if(this.sampleTypeCode == "SEARCH") {
			SAMPLE_TYPE_PROPERTIES = this.profile.searchType["SAMPLE_TYPE_PROPERTIES"];
			SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME = this.profile.searchType["SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME"];
		} else {
			SAMPLE_TYPE_PROPERTIES = this.profile.typePropertiesForTable[this.sampleTypeCode];
			if(SAMPLE_TYPE_PROPERTIES === null || SAMPLE_TYPE_PROPERTIES === undefined) {
				SAMPLE_TYPE_PROPERTIES = this.profile.getAllPropertiCodesForTypeCode(this.sampleTypeCode);
			}
		
			SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME = this.profile.getPropertiesDisplayNamesForTypeCode(this.sampleTypeCode, SAMPLE_TYPE_PROPERTIES);
			
			tableTemplate += "<tr style='border:none; border-collapse:collapse;'>";
			tableTemplate += "<td style='border:none; border-collapse:collapse;'><input placeholder='Code filter' style=\"width: 100%\" id=\"CODE_filter\" type=\"text\"></td>";
			for(var i=0; i<SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME.length;i++) {
				tableTemplate += "<td style='border:none; border-collapse:collapse;'><input placeholder='"+SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME[i]+" filter' style=\"width: 100%\" id=\""+SAMPLE_TYPE_PROPERTIES[i]+"_filter\" type=\"text\"></td>";
			}
			tableTemplate += "<td></td>";
			tableTemplate += "<td></td>";
			tableTemplate += "</tr>";
		}
	
		tableTemplate += "<tr class=\"sample-table-header\"><th>Code</th>";
		for (var i = 0; i < SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME.length; i++) {
			tableTemplate += "<th>" + SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME[i]+ "</th>";
		}
		tableTemplate += "<th></th>";
		if (isEmbedded) {
			tableTemplate += "<th></th>";
		} else {
			tableTemplate += "<th><center><a class='btn' href=\"javascript:sampleTable.createNewSample();\"><i class='icon-plus-sign'></i></a></center></th>";
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
			for(var i=0;i<SAMPLE_TYPE_PROPERTIES.length;i++) {
				filterResults[i+1] = $('#'+SAMPLE_TYPE_PROPERTIES[i]+'_filter').val();
			}
		
			localReference.filter (
				filterResults
			);
		});	
	
		for(var i = 0; i < SAMPLE_TYPE_PROPERTIES.length; i++) {
			$('#'+SAMPLE_TYPE_PROPERTIES[i]+'_filter').keyup(function() {
				var filterResults = [];
					filterResults[0] = $('#CODE_filter').val();
				for(var i=0;i<SAMPLE_TYPE_PROPERTIES.length;i++) {
					filterResults[i+1] = $('#'+SAMPLE_TYPE_PROPERTIES[i]+'_filter').val();
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
				sampleForm.addLinkedSample(sampleTypeGroup, sample);
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
					for(var i=0; i<SAMPLE_TYPE_PROPERTIES.length; i++) {
						var tableFieldValue = sample.properties[SAMPLE_TYPE_PROPERTIES[i]];
						tableFields[tableFields.length] = Util.getEmptyIfNull(tableFieldValue);
					}
				}
				
				if(localReference.inspectEnabled) {
					var inspectedClass = "";
					if(inspector.containsSample(sample.id) !== -1) {
						inspectedClass = "inspectorClicked";
					}
					tableFields[tableFields.length] = "<center><a id='PIN_" + sample.code + "' class='btn pinBtn " + inspectedClass + "' onmouseover=\"sampleTable.previewNote('" + sample.code + "', 'PIN_" + sample.code + "');\" ><img src='./images/pin-icon.png' style='width:16px; height:16px;' /></center>";
				}
				
				if(localReference.enableEdit) {
					tableFields[tableFields.length] = "<center><a class='btn' href=\"javascript:sampleTable.openEditWindowForSample('"+sample.code+"', '"+sample.permId+"');\"><i class='icon-edit'></i></a></center>";
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
		var SAMPLE_TYPE_PROPERTIES = this.profile.typePropertiesForTable[this.sampleTypeCode]; //this.sampleTypeConfig["SAMPLE_TYPE_PROPERTIES"];
		if(SAMPLE_TYPE_PROPERTIES === null || SAMPLE_TYPE_PROPERTIES === undefined) {
			SAMPLE_TYPE_PROPERTIES = this.profile.getAllPropertiCodesForTypeCode(this.sampleTypeCode);
		}
		
		var displayedSamples;
	
		displayedSamples = this.samples.filter(this.filterInternal.curry(((filterResults[0] == undefined)?"":filterResults[0]), "CODE"));
		for(var i=0; i < SAMPLE_TYPE_PROPERTIES.length;i++) {
				displayedSamples = displayedSamples.filter(this.filterInternal.curry((filterResults[i+1] == undefined)?"":filterResults[i+1],SAMPLE_TYPE_PROPERTIES[i]));
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
		showEditSamplePage(sample);
		
		/*
		var editURLTemplate = this.profile.openbisUrl + "openbis/?viewMode=embedded#action=EDITING&entity=SAMPLE";
		var codeParam = "&code="+code;
		var permId = "&permId="+permId;
		var sessionId = "&sessionID="+openbisServer.getSession();
		window.open(editURLTemplate+codeParam+permId+sessionId,null,null);
		*/
	}
	
	this.reloadWithSamples = function(returnedSamples)
	{
		this.samples = returnedSamples;
		this.repaint();
	}
	
}