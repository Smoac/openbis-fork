var SampleDataGridUtil = new function() {
	this.getSampleDataGrid = function(sampleTypeCode, samples, rowClick) {
		var sampleType = profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		var propertyCodes = profile.getAllPropertiCodesForTypeCode(sampleTypeCode);
		var propertyCodesDisplayNames = profile.getPropertiesDisplayNamesForTypeCode(sampleTypeCode, propertyCodes);
		
		//Fill Columns model
		var columns = [ {
			label : 'Identifier',
			property : 'identifier',
			isExportable: true,
			sortable : true
		}, {
			label : 'Space',
			property : 'default_space',
			isExportable: true,
			sortable : true
		}, {
			label : 'Parents',
			property : 'parents',
			isExportable: true,
			sortable : true
		}, {
			label : 'Experiment',
			property : 'experiment',
			isExportable: true,
			sortable : true
		}, {
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
		}];
		
		for (var idx = 0; idx < propertyCodes.length; idx++) {
			var propertyCode = propertyCodes[idx];
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
		
		columns.push(mainController.createOperationsColumn());
			
		//Fill data model
		var getDataList = function(callback) {
			var dataList = [];
			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
				var sample = samples[sIdx];
				var sampleModel = { 'identifier' : sample.identifier, 'default_space' : sample.spaceCode, 'permId' : sample.permId, 'experiment' : sample.experimentIdentifierOrNull };
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
}