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

//When non present it defaults to BOX_POSITION
var ValidationLevel = {
	RACK : 0,
	BOX : 1,
	BOX_POSITION : 2
}
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
		this.mainMenu = {
				showLabNotebook : true,
				showInventory : true,
				showOrders : true,
				showDrawingBoard : false,
				showSampleBrowser : true,
				showExports : true,
				showStorageManager : true,
				showAdvancedSearch : true,
				showTrashcan : true,
				showVocabularyViewer : true,
				showUserManager : true
		}
		
		this.orderLanguage = {
				"ENGLISH" : {
					"DATE_LABEL" : "Date",
					"SUPPLIER_LABEL" : "Supplier",
					"CONTACT_INFO_LABEL" : "Contact Information",
					"ORDER_INFO_LABEL" : "Order Information",
					"ACCOUNT_LABEL" : "Account Number",
					
					"PREFERRED_LANGUAGE_LABEL" : "Preferred Supplier Language",
					"PREFERRED_ORDER_METHOD_LABEL" : "Preferred Supplier Order Method",
					
					"ORDER_MANAGER_LABEL" : "Order Manager",
					"ORDER_MANAGER_CONTACT_DETAILS_LABEL" : "Order Manager Contact Details",
					
					"REQUESTED_PRODUCTS_LABEL" : "Requested Products",
					"PRODUCTS_COLUMN_NAMES_LABEL" : "Name\tCode\tQuantity\tUnit Price\tCurrency",
					
					"SUPPLIER_FAX_LABEL" : "Supplier fax",
					"SUPPLIER_EMAIL_LABEL" : "Supplier Email",
					
					"PRICE_TOTALS_LABEL" : "Total Price",
					"ADDITIONAL_INFO_LABEL" : "Additional Information"
				},
				"GERMAN" : {
					"DATE_LABEL" : "Datum",
					"SUPPLIER_LABEL" : "Lieferant",
					"CONTACT_INFO_LABEL" : "Kontaktdetails",
					"ORDER_INFO_LABEL" : "Bestellungdetails",
					"ACCOUNT_LABEL" : "Account Nummer",
					 
					"PREFERRED_LANGUAGE_LABEL" : "Bevorzugte Lieferanten-Sprache",
					"PREFERRED_ORDER_METHOD_LABEL" : "Bevorzugte Bestellungsart",
					 
					"ORDER_MANAGER_LABEL" : "Besteller",
					"ORDER_MANAGER_CONTACT_DETAILS_LABEL" : "Besteller Kontaktdetails",
					 
					"REQUESTED_PRODUCTS_LABEL" : "Bestellte Produkte",
					"PRODUCTS_COLUMN_NAMES_LABEL" : "Name\tCode\tMenge\tPreis pro Einheit\tWährung",
					"SUPPLIER_FAX_LABEL" : "Lieferant Fax",
					"SUPPLIER_EMAIL_LABEL" : "Lieferant Email",
					 
					"PRICE_TOTALS_LABEL" : "Gesamtpreis",
					"ADDITIONAL_INFO_LABEL" : "Zusätzliches Informationen"
				}
		}
		
		this.searchDomains = [ { "@id" : -1, "@type" : "GobalSearch", label : "Global", name : "global"}];
		this.inventorySpaces = ["MATERIALS", "METHODS", "STOCK_CATALOG"];
		this.inventorySpacesReadOnly = ["STOCK_ORDERS"];
		this.sampleTypeProtocols = ["GENERAL_PROTOCOL", "PCR_PROTOCOL", "WESTERN_BLOTTING_PROTOCOL"];
		this.searchSamplesUsingV3OnDropbox = false;
		this.searchSamplesUsingV3OnDropboxRunCustom = false;
		this.isInventorySpace = function(spaceCode) {
			return ($.inArray(spaceCode, this.inventorySpaces) !== -1) || ($.inArray(spaceCode, this.inventorySpacesReadOnly) !== -1);
		}
		
		this.directLinkEnabled = true;
		this.directFileServer = null; //To be set during initialization using info retrieved from the DSS configuration by the reporting plugin
		this.copyPastePlainText = false;
		this.hideCodes = true;
		this.hideTypes = {
				"sampleTypeCodes" : ["SUPPLIER", "PRODUCT", "REQUEST", "ORDER"],
				"experimentTypeCodes" : []
		}		
		this.propertyReplacingCode = "NAME";
		
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
		
		this.isSampleTypeProtocol = function(sampleTypeCode) {
			return ($.inArray(sampleTypeCode, this.sampleTypeProtocols) !== -1);
		}
		
		this.isSampleTypeHidden = function(sampleTypeCode) {
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);
			if(sampleType && sampleType.listable) {
				return ($.inArray(sampleTypeCode, this.hideTypes["sampleTypeCodes"]) !== -1);
			} else {
				return true;
			}
		}
		
		this.isExperimentTypeHidden = function(experimentTypeCode) {
			return ($.inArray(experimentTypeCode, this.hideTypes["experimentTypeCodes"]) !== -1);
		}
		
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
					propertyGroup.boxSizeProperty = storagePropertyGroups[i]["BOX_SIZE_PROPERTY"];
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
				propertyGroups[i].boxSizeProperty = storagePropertyGroups[i]["BOX_SIZE_PROPERTY"];
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
					validationLevel : configurationMap["VALIDATION_LEVEL"],
					rowNum : configurationMap["ROW_NUM"],
					colNum : configurationMap["COLUMN_NUM"],
					boxNum : configurationMap["BOX_NUM"]
			}
			
			if(configObj.validationLevel === null || configObj.validationLevel === undefined) {
				configObj.validationLevel = ValidationLevel.BOX_POSITION;
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
			
			if(propertyContent.indexOf("<root ") != -1) {
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
		 * Modifies sample before submit
		 */
		this.sampleFormOnSubmit = function(sample, action) {
			if(action) {
				action(sample, null);
			}
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
	
		this.getPropertyTypes = function() {
			return this.allPropertyTypes;
		}
		
		this.getPropertyType = function(propertyTypeCode) {
			for (var i = 0; i < this.allPropertyTypes.length; i++) {
				if(this.allPropertyTypes[i].code === propertyTypeCode) {
					return this.allPropertyTypes[i];
				}
			}
			return null;
		}
		
		this.getVocabularyByCode = function(code) {
			for (var i = 0; i < this.allVocabularies.length; i++) {
				var vocabulary = this.allVocabularies[i];
				if (vocabulary.code === code) {
					return vocabulary;
				}
			}
			return null;
		}
		
		this.getVocabularyTermByCodes = function(vocabularyCode, termCode) {
			var vocabulary = this.getVocabularyByCode(vocabularyCode);
			if(vocabulary) {
				for(var idx = 0; idx < vocabulary.terms.length; idx++) {
					if(vocabulary.terms[idx].code === termCode) {
						return vocabulary.terms[idx];
					}
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
	
		this.getAllPropertiCodesForExperimentTypeCode = function(typeCode) {
			var allPropertiCodes = new Array();
			var type = this.getExperimentTypeForExperimentTypeCode(typeCode);
			for(var i = 0; i < type.propertyTypeGroups.length; i++) {
				var propertyGroup = type.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					allPropertiCodes.push(propertyType.code);
				}
			}
			return allPropertiCodes;
		}
		
		this.getPropertiesDisplayNamesForExperimentTypeCode = function(typeCode, propertiesTypeCode) {
			var allPropertiDisplayNames = new Array();
			for(var i = 0; i < propertiesTypeCode.length; i++) {
				var propertyTypeCode = propertiesTypeCode[i];
				var propertyTypeDisplayName = this.getPropertyDisplayNamesForExperimentTypeCode(typeCode, propertyTypeCode);
				allPropertiDisplayNames.push(propertyTypeDisplayName);
			}
			return allPropertiDisplayNames;
		}
		
		this.getPropertyDisplayNamesForExperimentTypeCode = function(typeCode, propertyTypeCode) {
			var propertyDisplayName = "";
			var type = this.getExperimentTypeForExperimentTypeCode(typeCode);
		
			for(var i = 0; i < type.propertyTypeGroups.length; i++) {
				var propertyGroup = type.propertyTypeGroups[i].propertyTypes;
				for(var j = 0; j < propertyGroup.length; j++) {
					var propertyType = propertyGroup[j];
					if(propertyType.code === propertyTypeCode) {
						propertyDisplayName = propertyType.label;
					}
				}
			}
		
			return propertyDisplayName;
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
				
				_this.serverFacade.listVocabularies(function(result) {
					//Init Vocabularies, so we don't miss vocabularies missing on sample types used only on annotations, etc...
					_this.allVocabularies = result.result;
					//Fix Property Types
					var intToVocabularyCode = {};
					
					//1. Obtain mapping from ids to codes
					for(var pIdx = 0; pIdx < _this.allPropertyTypes.length; pIdx++) {
						var propertyType = _this.allPropertyTypes[pIdx];
						if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
							var vocabularyOrNumber = propertyType.vocabulary;
							if (vocabularyOrNumber !== parseInt(vocabularyOrNumber, 10)) { //Is vocabulary
								intToVocabularyCode[propertyType.vocabulary["@id"]] = propertyType.vocabulary.code
							}
						}
					}
					
					//2. Resolve ids and partial objects from the returned complete vocabularies
					for(var pIdx = 0; pIdx < _this.allPropertyTypes.length; pIdx++) {
						var propertyType = _this.allPropertyTypes[pIdx];
						if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
							var vocabularyOrNumber = propertyType.vocabulary;
							var vocabularyCode = null;
							if (vocabularyOrNumber === parseInt(vocabularyOrNumber, 10)) { //Is number
								vocabularyCode = intToVocabularyCode[vocabularyOrNumber];
							} else {
								vocabularyCode = propertyType.vocabulary.code;
							}
									
							if(!vocabularyCode) {
								alert("[TO-DELETE] Empty Vocabulary during init, this should never happen, tell the developers.");
							}
							var vocabulary = _this.getVocabularyByCode(vocabularyCode);
							propertyType.vocabulary = vocabulary;
							propertyType.terms = vocabulary.terms;
						}
					}
					
					//Continue Init Case
					callback();
				});
				
				
			});
		}
		
		this.initVocabulariesForSampleTypes = function(callback) {
			var _this = this;
			var getVocabularyCodeFromId = function(id) {
				
			}
			this.serverFacade.listVocabularies(function(result) {
				//Init Vocabularies, so we don't miss vocabularies missing on sample types used only on annotations, etc...
				_this.allVocabularies = result.result;
				//Fix Sample Types
				var intToVocabularyCode = {};
				
				//1. Obtain mapping from ids to codes
				for(var sampleTypeIdx = 0; sampleTypeIdx < _this.allSampleTypes.length; sampleTypeIdx++) {
					var sampleType = _this.allSampleTypes[sampleTypeIdx];
					for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
						var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
						for(var j = 0; j < propertyGroup.length; j++) {
							var propertyType = propertyGroup[j];
							if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
								var vocabularyOrNumber = propertyType.vocabulary;
								if (vocabularyOrNumber !== parseInt(vocabularyOrNumber, 10)) { //Is vocabulary
									intToVocabularyCode[propertyType.vocabulary["@id"]] = propertyType.vocabulary.code
								}
							}
						}
					}
				}
				
				//2. Resolve ids and partial objects from the returned complete vocabularies
				for(var sampleTypeIdx = 0; sampleTypeIdx < _this.allSampleTypes.length; sampleTypeIdx++) {
					var sampleType = _this.allSampleTypes[sampleTypeIdx];
					for(var i = 0; i < sampleType.propertyTypeGroups.length; i++) {
						var propertyGroup = sampleType.propertyTypeGroups[i].propertyTypes;
						for(var j = 0; j < propertyGroup.length; j++) {
							var propertyType = propertyGroup[j];
							if (propertyType.dataType === "CONTROLLEDVOCABULARY") {
								var vocabularyOrNumber = propertyType.vocabulary;
								var vocabularyCode = null;
								if (vocabularyOrNumber === parseInt(vocabularyOrNumber, 10)) { //Is number
									vocabularyCode = intToVocabularyCode[vocabularyOrNumber];
								} else {
									vocabularyCode = propertyType.vocabulary.code;
								}
								
								if(!vocabularyCode) {
									alert("[TO-DELETE] Empty Vocabulary during init, this should never happen, tell the developers.");
								}
								var vocabulary = _this.getVocabularyByCode(vocabularyCode);
								propertyType.vocabulary = vocabulary;
								propertyType.terms = vocabulary.terms;
							}
						}
					}
				}
				
				//Init Spaces
				_this.serverFacade.listSpaces(function(spaces) {
					if($.inArray("INVENTORY", spaces) === -1) {
						mainController.serverFacade.createReportFromAggregationService(_this.getDefaultDataStoreCode(), {"method" : "init" }, function() {
							_this.serverFacade.listSpaces(function(spaces) {
								_this.allSpaces = spaces;
								callback();
							});
						});
					} else {
						_this.allSpaces = spaces;
						callback();
					}
				})
			});
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
		
		this.initDirectLinkURL = function(callback) {
			var _this = this;
			this.serverFacade.getDirectLinkURL(function(error, result) {
				if(!error && result.data.protocol) {
					_this.directFileServer = result.data;
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
						_this.initDirectLinkURL(function() {
							callbackWhenDone();
						});
					});
				});
			});
		}
	}
});