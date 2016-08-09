var SampleDataGridUtil = new function() {
	this.getSampleDataGrid = function(mandatoryConfigPostKey, samples, rowClick, customOperations, customColumns, optionalConfigPostKey, isOperationsDisabled, isLinksDisabled, isMultiselectable) {
		
		var foundPropertyCodes = {};
		var foundSampleTypes = {};
		for(var sIdx = 0; sIdx < samples.length; sIdx++) {
			var sample = samples[sIdx];
			if(!foundSampleTypes[sample.sampleTypeCode]) {
				foundSampleTypes[sample.sampleTypeCode] = true;
				var propertyCodes = profile.getAllPropertiCodesForTypeCode(sample.sampleTypeCode);
				for(var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
					foundPropertyCodes[propertyCodes[pIdx]] = true;
				}
			}
		}
		
		//Fill Columns model
		var columns = [];
		
		columns.push({
			label : 'Identifier',
			property : 'identifier',
			isExportable: true,
			sortable : true,
			render : function(data) {
				return (isLinksDisabled)?data.identifier:FormUtil.getFormLink(data.identifier, "Sample", data.permId);
			},
			filter : function(data, filter) {
				return data.identifier.toLowerCase().indexOf(filter) !== -1;
			},
			sort : function(data1, data2, asc) {
				var value1 = data1.identifier;
				var value2 = data2.identifier;
				var sortDirection = (asc)? 1 : -1;
				return sortDirection * naturalSort(value1, value2);
			}
		});
		
		if(foundPropertyCodes["NAME"]) {
			columns.push({
				label : 'Name',
				property : 'NAME',
				isExportable: true,
				sortable : true,
				render : function(data) {
					return (isLinksDisabled)?data.NAME:FormUtil.getFormLink(data.NAME, "Sample", data.permId);
				}
			});
		}
		
		if(customColumns) {
			columns = columns.concat(customColumns);
		}
		
		columns.push({
			label : 'Space',
			property : 'default_space',
			isExportable: true,
			sortable : true
		});
		
		columns.push({
			label : 'Parents',
			property : 'parents',
			isExportable: true,
			sortable : true
		});
		
		columns.push({
			label : ELNDictionary.ExperimentELN + '/' + ELNDictionary.ExperimentInventory,
			property : 'experiment',
			isExportable: true,
			sortable : true
		});
		
		columns.push({
			label : 'Preview',
			property : 'preview',
			isExportable: false,
			sortable : false,
			render : function(data) {
				var previewContainer = $("<div>");
				mainController.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [data.permId], function(data) {
					data.result.forEach(function(dataset) {
						var listFilesForDataSetCallback = function(dataFiles) {
							for(var pathIdx = 0; pathIdx < dataFiles.result.length; pathIdx++) {
								if(!dataFiles.result[pathIdx].isDirectory) {
									var downloadUrl = profile.allDataStores[0].downloadUrl + '/' + dataset.code + "/" + dataFiles.result[pathIdx].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
									var previewImage = $("<img>", { 'src' : downloadUrl, 'class' : 'zoomableImage', 'style' : 'width:100%;' });
									previewImage.click(function(event) {
										Util.showImage(downloadUrl);
										event.stopPropagation();
									});
									previewContainer.append(previewImage);
									break;
								}
							}
						};
						mainController.serverFacade.listFilesForDataSet(dataset.code, "/", true, listFilesForDataSetCallback);
					});
				});
				return previewContainer;
			},
			filter : function(data, filter) {
				return false;
			},
			sort : function(data1, data2, asc) {
				return 0;
			}
		});
		
		var propertyColumnsToSort = [];
		for (propertyCode in foundPropertyCodes) {
			var propertiesToSkip = ["NAME", "XMLCOMMENTS", "ANNOTATIONS_STATE"];
			if($.inArray(propertyCode, propertiesToSkip) !== -1) {
				continue;
			}
			var propertyType = profile.getPropertyType(propertyCode);
			if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
				var getVocabularyColumn = function(propertyType) {
					return function() {
						return {
							label : propertyType.label,
							property : propertyType.code,
							isExportable: true,
							sortable : true,
							render : function(data) {
								return FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
							},
							filter : function(data, filter) {
								var value = FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
								return value && value.toLowerCase().indexOf(filter) !== -1;
							},
							sort : function(data1, data2, asc) {
								var value1 = FormUtil.getVocabularyLabelForTermCode(propertyType, data1[propertyType.code]);
								if(!value1) {
									value1 = ""
								};
								var value2 = FormUtil.getVocabularyLabelForTermCode(propertyType, data2[propertyType.code]);
								if(!value2) {
									value2 = ""
								};
								var sortDirection = (asc)? 1 : -1;
								return sortDirection * naturalSort(value1, value2);
							}
						};
					}
				}
				
				var newVocabularyColumnFunc = getVocabularyColumn(propertyType);
				propertyColumnsToSort.push(newVocabularyColumnFunc());
			} else {			
				propertyColumnsToSort.push({
					label : propertyType.label,
					property : propertyType.code,
					isExportable: true,
					sortable : true
				});
			}
		}
		
		columns.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});
		propertyColumnsToSort.sort(function(propertyA, propertyB) {
			return propertyA.label.localeCompare(propertyB.label);
		});
		columns = columns.concat(propertyColumnsToSort);
		columns.push({
			label : '---------------',
			property : null,
			isExportable: false,
			sortable : false
		});
		
		columns.push({
			label : 'Registration Date',
			property : 'registrationDate',
			isExportable: false,
			sortable : true
		});
		
		columns.push({
			label : 'Modification Date',
			property : 'modificationDate',
			isExportable: false,
			sortable : true
		});
		
		if(!isOperationsDisabled && customOperations) {
			columns.push(customOperations);
		} else if(!isOperationsDisabled) {
			columns.push(this.createOperationsColumn());
		}
		
		//Fill data model
		var getDataList = SampleDataGridUtil.getDataList(samples);
			
		//Create and return a data grid controller
		var configKey = "SAMPLE_TABLE_" + mandatoryConfigPostKey;
		if(optionalConfigPostKey) {
			configKey += "_" + optionalConfigPostKey;
		}
		var dataGridController = new DataGridController(null, columns, getDataList, rowClick, false, configKey, isMultiselectable);
		return dataGridController;
	}
	
	this.getDataList = function(samples) {
		return function(callback) {
			var dataList = [];
			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
				var sample = samples[sIdx];
				
				var registrationDate = null;
				if(sample.registrationDetails && sample.registrationDetails.registrationDate) {
					registrationDate = Util.getFormatedDate(new Date(sample.registrationDetails.registrationDate));
				}
				
				var modificationDate = null;
				if(sample.registrationDetails && sample.registrationDetails.modificationDate) {
					modificationDate = Util.getFormatedDate(new Date(sample.registrationDetails.modificationDate));
				}
				
				var sampleModel = { '$object' : sample,
									'identifier' : sample.identifier, 
									'default_space' : sample.spaceCode,
									'permId' : sample.permId,
									'experiment' : sample.experimentIdentifierOrNull,
									'registrationDate' : registrationDate,
									'modificationDate' : modificationDate
								};
				
				if(sample.properties) {
					for(var propertyCode in sample.properties) {
						sampleModel[propertyCode] = sample.properties[propertyCode];
					}
				}
				
				var parents = "";
				if(sample.parents) {
					for (var paIdx = 0; paIdx < sample.parents.length; paIdx++) {
						if(paIdx !== 0) {
							parents += ", ";
						}
						parents += sample.parents[paIdx].identifier;
					}
				}
				
				sampleModel['parents'] = parents;
				
				dataList.push(sampleModel);
			}
			callback(dataList);
		};
	}
	
	this.createOperationsColumn = function() {
		return {
			label : "Operations",
			property : 'operations',
			isExportable: false,
			sortable : false,
			render : function(data) {
				//Dropdown Setup
				var $dropDownMenu = $("<span>", { class : 'dropdown table-options-dropdown' });
				var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
				var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
				$dropDownMenu.append($caret);
				$dropDownMenu.append($list);
				
				var clickFunction = function($dropDown) {
					return function(event) {
						event.stopPropagation();
						event.preventDefault();
						$caret.dropdown('toggle');
					};
				}
				$dropDownMenu.dropdown();
				$dropDownMenu.click(clickFunction($dropDownMenu));
				
				var $openHierarchy = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Open Hierarchy'}).append("Open Hierarchy"));
				$openHierarchy.click(function(e) {
					mainController.changeView('showSampleHierarchyPage', data.permId, true);
				});
				$list.append($openHierarchy);
				
				var $openHierarchy = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Open Hierarchy Table'}).append("Open Hierarchy Table"));
				$openHierarchy.click(function(e) {
					mainController.changeView('showSampleHierarchyTablePage', data.permId, true);
				});
				$list.append($openHierarchy);
				
				var $openHierarchy = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Move'}).append("Move"));
				$openHierarchy.click(function(e) {
					var moveSampleController = new MoveSampleController(data.permId, function() {
						mainController.refreshView();
					});
					moveSampleController.init();
				});
				$list.append($openHierarchy);
				
				return $dropDownMenu;
			},
			filter : function(data, filter) {
				return false;
			},
			sort : function(data1, data2, asc) {
				return 0;
			}
		}
	}
}