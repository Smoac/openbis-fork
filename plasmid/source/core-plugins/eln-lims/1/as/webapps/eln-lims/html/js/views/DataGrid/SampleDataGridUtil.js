var SampleDataGridUtil = new function() {
	this.getSampleDataGrid = function(sampleTypeCode, samples, rowClick) {
		var sampleType = profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		var propertyCodes = profile.getAllPropertiCodesForTypeCode(sampleTypeCode);
		var propertyCodesDisplayNames = profile.getPropertiesDisplayNamesForTypeCode(sampleTypeCode, propertyCodes);
		
		//Fill Columns model
		var columns = [];
		
		columns.push({
			label : 'Identifier',
			property : 'identifier',
			isExportable: true,
			sortable : true,
			render : function(data) {
				var href = Util.getURLFor(mainController.sideMenu.getCurrentNodeId(), "showViewSamplePageFromPermId", data.permId);
				var link = $("<a>", { "href" : href, "class" : "browser-compatible-javascript-link" }).append(data.identifier);
				return link;
			},
			filter : function(data, filter) {
				return data.identifier.indexOf(filter) !== -1;
			},
			sort : function(data1, data2, asc) {
				var value1 = data1.identifier;
				var value2 = data2.identifier;
				var sortDirection = (asc)? 1 : -1;
				return sortDirection * naturalSort(value1, value2);
			}
		});
		
		if($.inArray("NAME", propertyCodes) !== -1) {
			columns.push({
				label : 'Name',
				property : 'NAME',
				isExportable: true,
				sortable : true
			});
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
			label : 'Experiment',
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
									var previewImage = $("<img>", { 'src' : downloadUrl, 'class' : 'zoomableImage', 'style' : 'height:80px;' });
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
		
		for (var idx = 0; idx < propertyCodes.length; idx++) {
			var propertiesToSkip = ["NAME"];
			var propertyCode = propertyCodes[idx];
			if($.inArray(propertyCode, propertiesToSkip) !== -1) {
				continue;
			}
			var propertyType = profile.getPropertyType(propertyCode);
			if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
				var getVocabularyColumn = function(propertyType) {
					return function() {
						return {
							label : propertyCodesDisplayNames[idx],
							property : propertyCodes[idx],
							isExportable: true,
							sortable : true,
							render : function(data) {
								return FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
							},
							filter : function(data, filter) {
								var value = FormUtil.getVocabularyLabelForTermCode(propertyType, data[propertyType.code]);
								return value && value.indexOf(filter) !== -1;
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
				columns.push(newVocabularyColumnFunc());
			} else {
				columns.push({
					label : propertyCodesDisplayNames[idx],
					property : propertyCodes[idx],
					isExportable: true,
					sortable : true
				});
			}
		}
		
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
		
		columns.push(this.createOperationsColumn());
		
		//Fill data model
		var getDataList = function(callback) {
			var dataList = [];
			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
				var sample = samples[sIdx];
				var sampleModel = { 'identifier' : sample.identifier, 
									'default_space' : sample.spaceCode,
									'permId' : sample.permId,
									'experiment' : sample.experimentIdentifierOrNull,
									'registrationDate' : Util.getFormatedDate(new Date(sample.registrationDetails.registrationDate)),
									'modificationDate' : Util.getFormatedDate(new Date(sample.registrationDetails.modificationDate))
								};
				for (var pIdx = 0; pIdx < propertyCodes.length; pIdx++) {
					var propertyCode = propertyCodes[pIdx];
					sampleModel[propertyCode] = sample.properties[propertyCode];
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
			
		//Create and return a data grid controller
		var configKey = "SAMPLE_TABLE_"+ sampleType.code;
		var dataGridController = new DataGridController(null, columns, getDataList, rowClick, false, configKey);
		return dataGridController;
	}
	
	this.createOperationsColumn = function() {
		return {
			label : "Operations",
			property : 'operations',
			isExportable: false,
			sortable : false,
			render : function(data) {
				//Dropdown Setup
				var $dropDownMenu = $("<span>", { class : 'dropdown' });
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
					mainController.changeView('showSampleHierarchyPage', data.permId);
				});
				$list.append($openHierarchy);
				
				var $openHierarchy = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Open Hierarchy Table'}).append("Open Hierarchy Table"));
				$openHierarchy.click(function(e) {
					mainController.changeView('showSampleHierarchyTablePage', data.permId);
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