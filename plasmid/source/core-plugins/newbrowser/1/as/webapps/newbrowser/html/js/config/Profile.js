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

/**
 * Creates an instance of DefaultProfile.
 *
 * @constructor
 * @this {DefaultProfile}
 */
function DefaultProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(DefaultProfile.prototype, {
	init: function(serverFacade){
		this.serverFacade = serverFacade;
		//
		// DEFAULTS, TYPICALLY DON'T TOUCH IF YOU DON'T KNOW WHAT YOU DO
		//
		
		this.searchDomains = [ { "@id" : -1, "@type" : "GobalSearch", label : "Global", name : "global"}];
		this.inventorySpaces = ["MATERIALS", "METHODS"];
		
		this.isInventorySpace = function(spaceCode) {
			return ($.inArray(spaceCode, this.inventorySpaces) !== -1);
		}
		
		this.hideCodes = false;
		this.propertyReplacingCode = "NAME";
		this.softLinks = false;
		
		this.sampleTypeDefinitionsExtension = {
		}
		this.searchType = {
			"TYPE" : "SEARCH",
			"DISPLAY_NAME" : "Search",
			"SAMPLE_TYPE_ATTRIBUTES" : ["sampleTypeCode", "MATCHED_TEXT", "MATCHED_FIELD", "PROPERTIES_JSON"],
			"SAMPLE_TYPE_ATTRIBUTES_DISPLAY_NAME" : ["Sample Type", "Matched Text", "Matching Field", "Properties"],	
		};
		
		this.allSpaces = [];
		this.allSampleTypes = [];
		this.allExperimentTypes = [];
		this.allVocabularies = [];
		this.allDataStores = [];
		this.allPropertyTypes = [];
		this.displaySettings = {};
		
		this.typePropertiesForSmallTable = {};
		
		this.storagesConfiguration = {
			"isEnabled" : false
		};
		
		this.getSearchDomains = function() {
			return this.searchDomains;
		}
		
		this.getDefaultDataStoreCode = function() {
			var dataStoreCode = null;
			if(this.allDataStores.length > 0) {
				 var dataStoreCode = this.allDataStores[0].code
			}
			return dataStoreCode;
		}
		
		this.getDefaultDataStoreURL = function() {
			var dataStoreURL = null;
			if(this.allDataStores.length > 0) {
				 var dataStoreURL = this.allDataStores[0].downloadUrl
			}
			return dataStoreURL;
		}
		
		this.getStoragePropertyGroup = function(storagePropertyGroupDisplayName) {
			if(!this.storagesConfiguration["isEnabled"]) {
				return null;
			}
			
			var storagePropertyGroups = this.storagesConfiguration["STORAGE_PROPERTIES"];
			if(!storagePropertyGroups) {
				return null;
			}
			
			for(var i = 0; i < storagePropertyGroups.length; i++) {
				if(storagePropertyGroupDisplayName === storagePropertyGroups[i]["STORAGE_GROUP_DISPLAY_NAME"]) {
					propertyGroup = {};
					propertyGroup.groupDisplayName = storagePropertyGroups[i]["STORAGE_GROUP_DISPLAY_NAME"];
					propertyGroup.nameProperty = storagePropertyGroups[i]["NAME_PROPERTY"];
					propertyGroup.rowProperty = storagePropertyGroups[i]["ROW_PROPERTY"];
					propertyGroup.columnProperty = storagePropertyGroups[i]["COLUMN_PROPERTY"];
					propertyGroup.boxProperty = storagePropertyGroups[i]["BOX_PROPERTY"];
					propertyGroup.userProperty = storagePropertyGroups[i]["USER_PROPERTY"];
					propertyGroup.positionProperty = storagePropertyGroups[i]["POSITION_PROPERTY"];
					return propertyGroup;
				}
			}
			
			return null;
		}
		
		this.getStoragePropertyGroups = function() {
			if(!this.storagesConfiguration["isEnabled"]) {
				return null;
			}
			
			var storagePropertyGroups = this.storagesConfiguration["STORAGE_PROPERTIES"];
			if(!storagePropertyGroups) {
				return null;
			}
			
			var propertyGroups = [];
			for(var i = 0; i < storagePropertyGroups.length; i++) {
				propertyGroups[i] = {};
				propertyGroups[i].groupDisplayName = storagePropertyGroups[i]["STORAGE_GROUP_DISPLAY_NAME"];
				propertyGroups[i].nameProperty = storagePropertyGroups[i]["NAME_PROPERTY"];
				propertyGroups[i].rowProperty = storagePropertyGroups[i]["ROW_PROPERTY"];
				propertyGroups[i].columnProperty = storagePropertyGroups[i]["COLUMN_PROPERTY"];
				propertyGroups[i].boxProperty = storagePropertyGroups[i]["BOX_PROPERTY"];
				propertyGroups[i].userProperty = storagePropertyGroups[i]["USER_PROPERTY"];
				propertyGroups[i].positionProperty = storagePropertyGroups[i]["POSITION_PROPERTY"];
			}
			return propertyGroups;
		}
		
		this.getStorageConfiguation = function(storageCode) {
			if(!this.storagesConfiguration["isEnabled"]) {
				return null;
			}
			
			var configurationMap = this.storagesConfiguration["STORAGE_CONFIGS"][storageCode];
			if(!configurationMap) {
				return null;
			}
			
			var configObj = {
					rowNum : configurationMap["ROW_NUM"],
					colNum : configurationMap["COLUMN_NUM"],
					boxNum : configurationMap["BOX_NUM"]
			}
			
			return configObj;
		}
		
		this.getPropertyGroupFromStorage = function(propertyGroupName) {
			if(!this.storagesConfiguration["isEnabled"]) { return false; }
			var _this = this;
			var propertyGroups = this.storagesConfiguration["STORAGE_PROPERTIES"];
			var selectedPropertyGroup = null;
			if(propertyGroups) {
				propertyGroups.forEach(function(propertyGroup) {
					if(propertyGroup["STORAGE_PROPERTY_GROUP"] === propertyGroupName) {
						selectedPropertyGroup = propertyGroup;
					}
				});
			}
			return selectedPropertyGroup;
		}
		
		
		this.dataSetViewerConf = {
			"DATA_SET_TYPES" : [".*"],
			"FILE_NAMES" : [".*"]
		}
		
		this.getColorForInspectors = function(sampleTypeCode) {
			//Default Color
			var defaultColor = "#ffffc0"
		
			//Convert to HSL
			var rgb = d3.rgb(defaultColor);
			var hsl =  rgb.hsl();
			//Increase Light
			var newColor = d3.hsl(hsl.h, hsl.s, Math.min(0.90, hsl.l + 0.75));
			return newColor;
		}
		
		this.searchSorter = function(searchResults) {
			return searchResults;
		}
		
		//
		// Per Lab Extensions
		//
		
		/*
		 * Used by DataSet Form
		 */
		//Null: Show checkbox to manually input
		//True: Automatic configuration, will unzip the file before atacching the files
		//False: Will do nothing to the file before attaching
		this.isZipDirectoryUpload = function(dataSetType) {
			return null;
		}
		
		/*
		 * Used by Main Menu
		 */
		this.mainMenuContentExtra = function() {
			return "";
		}

		/*
		 * Used by Inspector
		 */
		this.inspectorContentTransformer = function(sample, propertyCode, propertyContent) {
			
			if(propertyContent.indexOf("<root>") != -1) {
				return {
					"isSingleColumn" : true,
					"content" : this.getHTMLTableFromManagePropertyXML(propertyContent)
				};
			} else {
				if(propertyContent === "<root/>\n") { //To clean empty XMLs and don't show them.
					propertyContent = "";
				}
				return {
					"isSingleColumn" : false,
					"content" : propertyContent
				};
			}
		}
	
		this.inspectorContentExtra = function(sample, propertyContent) {
			return "";
		}
		
		this.getHTMLTableFromManagePropertyXML = function(xmlDocument) {
			var table_head = null;
			var table_body = "";
			var dom;
	
			if (window.DOMParser) {
			  parser = new DOMParser();
			  dom = parser.parseFromString(xmlDocument,"text/xml");
			} else {// Internet Explorer
			  dom = new ActiveXObject("Microsoft.XMLDOM");
			  dom.async = false;
			  dom.loadXML(xmlDocument); 
			} 
	
			var html = null;
			var root = dom.childNodes[0];
			var children = root.childNodes;
			for(var i = 0; i < children.length; i++) {
				var child = children[i];
				if (child.localName != null) {
					var keys = child.attributes;
					if (table_head == null) {
						table_head = "<tr>";
						var key = null;
						for (var j = 0; j < keys.length; j++) {
							key = keys[j];
							if(key.localName != "permId") {
								table_head += "<th style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>"+ key.localName + "</th>";
							}
						}
						table_head += "</tr>";
					}
					table_body += "<tr>";
					for (var j = 0; j < keys.length; j++) {
						key = keys[j];
						if(key.localName != "permId") {
							if(key.localName == "date") {
								table_body += "<td style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>" + key.value + "</td>";
							} else {
								table_body += "<td style='text-align:left; width:" + (100/(keys.length-1)) + "%;'>" + key.value + "</td>";
							}
						}
					}
					table_body += "</tr>";
				}
			}
			html = "<table style='font-family:helvetica; font-size:90%; width: 100%;'><thead>" + table_head + "</thead><tbody>"+ table_body + "</tbody></table>";
			return html;
		}
		
		/*
		 * Returns a Jquery component that is appended at the end of the form before the data set viewer
		 */
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
		}
	
		/*
		 * Used by DataSet Uploader
		 */
		this.getDataSetTypeForFileName = function(allDatasetFiles, fileName) {
			return null;
		}
		
		//
		// Utility methods used to navigate the configuration easily
		//
		this.isPropertyPressent = function(sampleType, propertyTypeCode) {
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(propertyType.code === propertyTypeCode) {
						return propertyType;
					}
				}
			}
			return null;
		}
		
		this.isELNExperiment = function(sampleTypeCode) {
			return false;
		}
	
		this.getPropertyType = function(propertyTypeCode) {
			for (var i = 0; i < this.allPropertyTypes.length; i++) {
				if(this.allPropertyTypes[i].code === propertyTypeCode) {
					return this.allPropertyTypes[i];
				}
			}
			return null;
		}
		this.getVocabularyById = function(id) {
			for (var i = 0; i < this.allVocabularies.length; i++) {
				var vocabulary = this.allVocabularies[i];
				if (vocabulary["@id"] === id || vocabulary["id"] === id) { //Either the id is a number or can be a string, are actually different ids.
					return vocabulary;
				}
			}
			return null;
		}
	
		this.getExperimentTypeForExperimentTypeCode = function(typeCode) {
			for(var i = 0; i < this.allExperimentTypes.length; i++) {
				if(this.allExperimentTypes[i].code === typeCode) {
					return this.allExperimentTypes[i];
				}
			}
			return null;
		}
		
		this.getSampleTypeForSampleTypeCode = function(typeCode) {
			for(var i = 0; i < this.allSampleTypes.length; i++) {
				if(this.allSampleTypes[i].code === typeCode) {
					return this.allSampleTypes[i];
				}
			}
			return null;
		}
	
		this.getAllPropertiCodesForTypeCode = function(typeCode) {
			var allPropertiCodes = new Array();
			var sampleType = this.getSampleTypeForSampleTypeCode(typeCode);
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					allPropertiCodes.push(propertyType.code);
				}
			}
			return allPropertiCodes;
		}
	
		this.getPropertyDisplayNamesForTypeCode = function(sampleTypeCode, propertyTypeCode) {
			var propertyDisplayName = "";
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					if(propertyType.code === propertyTypeCode) {
						propertyDisplayName = propertyType.label;
					}
				}
			}
		
			return propertyDisplayName;
		}
	
		this.getPropertiesDisplayNamesForTypeCode = function(sampleTypeCode, propertiesTypeCode) {
			var allPropertiDisplayNames = new Array();
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
			for(var i = 0; i < propertiesTypeCode.length; i++) {
				var propertyTypeCode = propertiesTypeCode[i];
				var propertyTypeDisplayName = this.getPropertyDisplayNamesForTypeCode(sampleTypeCode, propertyTypeCode);
				allPropertiDisplayNames.push(propertyTypeDisplayName);
			}
		
			return allPropertiDisplayNames;
		}
	
		this.getPropertyTypeFrom = function(sampleType, propertyCode) {
			for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
				var propertyTypeGroup = sampleType.propertyTypeGroups[i];
				for(var j = 0; j < propertyTypeGroup.propertyTypes.length; j++) {
					var propertyType = propertyTypeGroup.propertyTypes[j];
					if(propertyType.code === propertyCode) {
						return propertyType;
					}
				}
			}
			return null;
		}
		
		this.getAllSampleTypes = function() {
			return this.allSampleTypes;
		}
		
		this.initPropertyTypes = function(callback) {
			var _this = this; 
			this.serverFacade.listPropertyTypes(function(data) {
				_this.allPropertyTypes = data.result;
				callback();
			});
		}
		
		this.initVocabulariesForSampleTypes = function(callback) {
			//Build Vocabularies from sample types
			for(var sampleTypeIdx = 0; sampleTypeIdx < this.allSampleTypes.length; sampleTypeIdx++) {
				var sampleType = this.allSampleTypes[sampleTypeIdx];
				for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
					var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
					for(var j = 0; j < propertyGroup.length; j++) {
						var propertyType = propertyGroup[j];
						if (propertyType.dataType === "CONTROLLEDVOCABULARY" && isNaN(propertyType.vocabulary)) {
							var add = true;
							for(var k = 0; k < this.allVocabularies.length; k++) {
								if(this.allVocabularies[k].code === propertyType.vocabulary.code) {
									add = false;
								}
							}
							if(add) {
								this.allVocabularies.push(propertyType.vocabulary);
							}
						}
					}
				}
			}
		
			//Get all the Vocabularies from openbis and fix terms
			var localReference = this;
			this.serverFacade.listVocabularies(
				function(result) {
					//Load Vocabularies
					var allVocabularies = result.result;
					for (var i = 0; i < allVocabularies.length; i++) {
						var vocabulary = allVocabularies[i];
						for(var j = 0; j < localReference.allVocabularies.length; j++) {
							var localVocabulary = localReference.allVocabularies[j];
							if (vocabulary.code === localVocabulary.code) {
								localVocabulary.terms = vocabulary.terms;
							}
						}
					}
					
					localReference.serverFacade.listSpaces(function(spaces) {
						if($.inArray("INVENTORY", spaces) === -1) {
							mainController.serverFacade.createReportFromAggregationService(localReference.getDefaultDataStoreCode(), {"method" : "init" }, function() {
								localReference.serverFacade.listSpaces(function(spaces) {
									localReference.allSpaces = spaces;
									callback();
								});
							});
								
						} else {
							localReference.allSpaces = spaces;
							callback();
						}
					})
				}
			);
		}
		
		this.initSearchDomains = function(callback) {
			var _this = this;
			this.serverFacade.listSearchDomains(function(data) {
				if(data && data.result) {
					for(var i = 0; i < data.result.length; i++) {
						_this.searchDomains.push(data.result[i]);
					}
				}
				callback();
			});
		}
		
		//
		// Initializes
		//
		this.init = function(callbackWhenDone) {
			var _this = this;
			
			this.initPropertyTypes(function(){
				_this.initVocabulariesForSampleTypes(function() {
					_this.initSearchDomains(function() {
						callbackWhenDone();
					});
				});
			});
		}
	}
});