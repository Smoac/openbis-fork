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
		
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];
		this.notShowTypes = ["SYSTEM_EXPERIMENT"];
		this.inventorySpaces = [];
		
		this.sampleTypeDefinitionsExtension = {
		}
		this.searchType = {
			"TYPE" : "SEARCH",
			"DISPLAY_NAME" : "Search",
			"SAMPLE_TYPE_PROPERTIES" : ["TYPE", "MATCHED_TEXT", "MATCHED_FIELD"],
			"SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME" : ["Sample Type", "Matched Text", "Matching Field"],	
		};
		
		this.allSampleTypes = [];
		this.allExperimentTypes = [];
		this.allVocabularies = [];
		this.allDataStores = [];
		this.allPropertyTypes = [];
		this.displaySettings = {};
		
		this.typeGroups = {
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] //All types not present in other groups and not in notShowTypes, is a box where everything that is not configured goes by default
			}
		};
		
		this.typePropertiesForTable = {};
		this.typePropertiesForSmallTable = {};
		
		this.colorForInspectors = {};
		
		this.storagesConfiguration = {
			"isEnabled" : false
		};
		
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
			//Get default color if found
			var defaultColor = "#ffffc0"
			var profileColor = this.colorForInspectors[sampleTypeCode];
		
			if (profileColor !== null && profileColor !== undefined) {
				defaultColor = profileColor;
			}
		
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
		 * Used by Sample Table
		 */
		
		this.isShowUnavailablePreviewOnSampleTable = true;
		
		/*
		 * Used by Sample Form
		 */
		
		this.getSpaceForSampleType = function(type) {
			return "DEFAULT";
		}
		
		this.getExperimentIdentifierForSample = function(type, code, properties) {
			return "/" + this.getSpaceForSampleType(type) + "/PROJECT_" + type + "/FOLDER_" + type;
		}
		
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
			return {
					"isSingleColumn" : false,
					"content" : propertyContent
			};
		}
	
		this.inspectorContentExtra = function(sample, propertyContent) {
			return "";
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
	
		this.getGroupTypeCodeForTypeCode = function(typeCode) {
			for(typeGroupCode in this.typeGroups) {
				for(var i = 0; i < this.typeGroups[typeGroupCode]["LIST"].length; i++) {
					var sampleTypeCode = this.typeGroups[typeGroupCode]["LIST"][i];
					if(sampleTypeCode === typeCode) {
						return this.typeGroups[typeGroupCode]["TYPE"];
					}
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
			var sampleTypes = [];
			for(var i = 0; i < this.allSampleTypes.length; i++) {
				var sampleType = this.allSampleTypes[i];
				if($.inArray(sampleType.code, this.notShowTypes) === -1) {
					sampleTypes.push(sampleType);
				}
			}
			return sampleTypes;
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
							this.allVocabularies.push(propertyType.vocabulary);
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
					callback();
				}
			);
		}
		
		//
		// Initializes the Others list with all sampleType codes that are neither in typeGroups or notShowTypes
		//
		this.init = function(callbackWhenDone) {
			var _this = this;
			
			for(var i = 0; i < this.allSampleTypes.length; i++) {
				var sampleType = this.allSampleTypes[i];
				if($.inArray(sampleType.code, this.notShowTypes) === -1) {
					if(this.getGroupTypeCodeForTypeCode(sampleType.code) === null) {
						this.typeGroups["OTHERS"]["LIST"].push(sampleType.code);
					}
				}
			}
		
			this.initPropertyTypes(function(){
				_this.initVocabulariesForSampleTypes(function() {
					callbackWhenDone();
				});
			});
		}
	}
});