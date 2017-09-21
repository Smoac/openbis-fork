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
				showStock : true,
//				showDrawingBoard : false,
				showObjectBrowser : true,
				showExports : true,
				showStorageManager : true,
				showAdvancedSearch : true,
				showTrashcan : true,
				showSettings : true,
				showVocabularyViewer : true,
				showUserManager : true,
				showUserProfile : true,
		}
		
		this.orderLabInfo = {
				
		}
		this.orderLanguage = {
				"ENGLISH" : {
					"ORDER_FORM" : "Order Form",
					"ORDER_INFORMATION" : "Order Information",
					"ORDER_DATE" : "Date",
					"ORDER_STATUS" : "Status",
					"ORDER_CODE" : "Code",
					
					"COSTUMER_INFORMATION" : "Costumer Information",
					"SHIP_TO" : "Ship To",
					"BILL_TO" : "Bill To",
					"SHIP_ADDRESS" : "Address",
					"PHONE" : "Phone",
					"FAX" : "Fax",
					
					"SUPPLIER_INFORMATION" : "Supplier Information",
					"SUPPLIER" : "Supplier",
					"SUPPLIER_ADDRESS_LINE_1" : "Address",
					"SUPPLIER_ADDRESS_LINE_2" : "       ",
					"SUPPLIER_PHONE" : "Phone",
					"SUPPLIER_FAX" : "Fax",
					"SUPPLIER_EMAIL" : "Email",
					"CUSTOMER_NUMBER" : "Customer No",
					
					"REQUESTED_PRODUCTS_LABEL" : "Requested Products",
					"PRODUCTS_COLUMN_NAMES_LABEL" : "Quantity\t\tName\t\tCatalog Num\t\tUnit Price",
					"PRICE_TOTALS_LABEL" : "Total Price",
					"ADDITIONAL_INFO_LABEL" : "Additional Information"
				},
				"GERMAN" : {
					"ORDER_FORM" : "Bestellformular",
					"ORDER_INFORMATION" : "Bestellinformation",
					"ORDER_DATE" : "Bestelldatum",
					"ORDER_STATUS" : "Bestellstatus",
					"ORDER_CODE" : "Bestellcode",

					"COSTUMER_INFORMATION" : "Kundeninformation",
					"SHIP_TO" : "Lieferung an",
					"BILL_TO" : "Rechnung an",
					"SHIP_ADDRESS" : "Addresse",
					"PHONE" : "Telefon",
					"FAX" : "Fax",

					"SUPPLIER_INFORMATION" : "Lieferanteninformation",
					"SUPPLIER" : "Lieferant",
					"SUPPLIER_ADDRESS_LINE_1" : "Addresse",
					"SUPPLIER_ADDRESS_LINE_2" : "       ",
					"SUPPLIER_PHONE" : "Lieferant Telefon",
					"SUPPLIER_FAX" : "Lieferant Fax",
					"SUPPLIER_EMAIL" : "Lieferant Email",
					"CUSTOMER_NUMBER" : "Kundennummer",

					"REQUESTED_PRODUCTS_LABEL" : "Angeforderte Produkte",
					"PRODUCTS_COLUMN_NAMES_LABEL" : "Anzahl\t\tName\t\tCatalog Num\t\tPreis pro Einheit",
					"PRICE_TOTALS_LABEL" : "Gesamtpreis",
					"ADDITIONAL_INFO_LABEL" : "Zusätzliche Informationen"
				}
		}
		
		this.isAdmin = false;

//		TO-DO Delete Jupyter developer initialization notes
//		source ~/.bash_profile
//		pyenv local miniconda3-latest
//		/home/osboxes/installation
//		python pybis/src/python/ELNJupyter/elnjupyter/server.py --port 8123 --cert cert.pem --key key.pem --openbis http://10.0.2.2:8888
//		jupyterhub -f jupyterhub_config.py --no-ssl
		
//		Jupyter integration config
		this.jupyterOpenbisEndpoint = "http://10.0.2.2:8888"; //Should not end with slash
		this.jupyterIntegrationServerEndpoint = "https://127.0.0.1:8123";
		this.jupyterEndpoint = "http://127.0.0.1:8000/";
		
		this.forcedDisableRTF = ["FREEFORM_TABLE_STATE","NAME", "SEQUENCE"];
		this.forceMonospaceFont = ["SEQUENCE"];
		
		this.isForcedMonospaceFont = function(propertytype) {
			return (propertytype && $.inArray(propertytype.code, this.forceMonospaceFont) !== -1);
		}
		
		this.isForcedDisableRTF = function(propertytype) {
			return (propertytype && $.inArray(propertytype.code, this.forcedDisableRTF) !== -1);
		}
		
		this.searchDomains = [ { "@id" : -1, "@type" : "GobalSearch", label : "Global", name : "global"}];
		this.inventorySpaces = ["MATERIALS", "METHODS", "STORAGE", "STOCK_CATALOG"];
		this.inventorySpacesReadOnly = ["ELN_SETTINGS", "STOCK_ORDERS"];
		this.sampleTypeProtocols = ["GENERAL_PROTOCOL", "PCR_PROTOCOL", "WESTERN_BLOTTING_PROTOCOL"];
		this.sampleTypeStorageEnabled = ["ANTIBODY", "BACTERIA", "CHEMICAL", "ENZYME", "CELL_LINE", "FLY", "MEDIA", "OLIGO", "PLASMID", "YEAST", "SOLUTION_BUFFER", "RNA", 
		                                 //Extension for ETHZ Costumers until this is configurable on the Settings
		                                 "EBBACTERIA", "TBBACTERIA", "CELL", "STRAIN", "SYNTHETIC_PEPTIDE"];
		this.searchSamplesUsingV3OnDropbox = false;
		this.searchSamplesUsingV3OnDropboxRunCustom = false;
		
		this.isSampleTypeWithStorage = function(sampleTypeCode) {
			return $.inArray(sampleTypeCode, this.sampleTypeStorageEnabled) !== -1;
		}
		
		this.isELNIdentifier = function(identifier) {
			var space = identifier.split("/")[1];
			return !this.isInventorySpace(space);
		}
		
		this.isHiddenSpace = function(spaceCode) {
			return $.inArray(spaceCode, this.hideSpaces) !== -1;
		}
		
		this.isInventorySpace = function(spaceCode) {
			return ($.inArray(spaceCode, this.inventorySpaces) !== -1) || ($.inArray(spaceCode, this.inventorySpacesReadOnly) !== -1);
		}
		
		this.directLinkEnabled = true;
		//To be set during initialization using info retrieved from the DSS configuration by the reporting plugin
		this.cifsFileServer = null;
		this.sftpFileServer = null;
		
		this.copyPastePlainText = false;
		this.hideCodes = true;
		this.hideTypes = {
				"sampleTypeCodes" : ["GENERAL_ELN_SETTINGS", "STORAGE_POSITION", "STORAGE"],
				"experimentTypeCodes" : []
		}
		this.hideSpaces = ["ELN_SETTINGS", "STORAGE"];
		
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
		this.allDatasetTypeCodes = [];
		this.displaySettings = {};
		
		this.typePropertiesForSmallTable = {};
		
		this.storagesConfiguration = {
			"isEnabled" : false
		};
		
		this.isDatasetTypeCode = function(datasetTypeCode) {
			return ($.inArray(datasetTypeCode, this.allDatasetTypeCodes) !== -1);
		}
		this.isSampleTypeProtocol = function(sampleTypeCode) {
			return ($.inArray(sampleTypeCode, this.sampleTypeProtocols) !== -1);
		}
		
		this.isSampleTypeHidden = function(sampleTypeCode) {
			var sampleType = this.getSampleTypeForSampleTypeCode(sampleTypeCode);
			if(sampleType && sampleType.listable) {
				return ($.inArray(sampleTypeCode, this.hideTypes["sampleTypeCodes"]) !== -1);
			} else {
				return false;
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
		
		this.getStoragePropertyGroup = function() {
			if(!this.storagesConfiguration["isEnabled"]) {
				return null;
			}
			
			propertyGroup = {};
			propertyGroup.groupDisplayName = "Physical Storage";
			propertyGroup.nameProperty = "STORAGE_CODE";
			propertyGroup.rowProperty = "STORAGE_RACK_ROW";
			propertyGroup.columnProperty = "STORAGE_RACK_COLUMN";
			propertyGroup.boxProperty = "STORAGE_BOX_NAME";
			propertyGroup.boxSizeProperty = "STORAGE_BOX_SIZE";
			propertyGroup.positionProperty = "STORAGE_BOX_POSITION";
			propertyGroup.userProperty = "STORAGE_USER";
			return propertyGroup;
		}
		
		this.getStorageConfigFromSample = function(sample) {
			return {
				code : sample.code,
				label : sample.properties[profile.propertyReplacingCode],
				validationLevel : ValidationLevel[sample.properties["STORAGE_VALIDATION_LEVEL"]],
				lowRackSpaceWarning : sample.properties["STORAGE_SPACE_WARNING"],
				lowBoxSpaceWarning : sample.properties["BOX_SPACE_WARNING"],
				rowNum : sample.properties["ROW_NUM"],
				colNum : sample.properties["COLUMN_NUM"],
				boxNum : sample.properties["BOX_NUM"]
			};
		}
		
		this.getStoragesConfiguation = function(callbackFunction) {
			var _this = this;
			if(!this.storagesConfiguration["isEnabled"]) {
				callbackFunction(null);
			}
			
			mainController.serverFacade.searchByType("STORAGE", function(results) {
				var configs = [];
				for(var idx = 0; idx < results.length; idx++) {
					configs.push(_this.getStorageConfigFromSample(results[idx]));
				}
				callbackFunction(configs);
			});
		}
		
		this.getStorageConfiguation = function(storageCode, callbackFunction) {
			var _this = this;
			if(!this.storagesConfiguration["isEnabled"]) {
				callbackFunction(null);
			}
			
			mainController.serverFacade.searchWithType("STORAGE", storageCode, null, function(results) {
				callbackFunction(_this.getStorageConfigFromSample(results[0]));
			});
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
		this.dataSetTypeForFileNameMap = [];

		this.getDataSetTypeForFileName = function(allDatasetFiles, fileName) {
			for (var dataSetTypeForFileName of this.dataSetTypeForFileNameMap) {
				if (fileName && fileName.endsWith(dataSetTypeForFileName.fileNameExtension)) {
					return dataSetTypeForFileName.dataSetType;
				}
			}
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
		
		this.getAllSampleTypes = function(skipHidden) {
			if(skipHidden) {
				var allNonHiddenSampleTypes = [];
				for(var sIdx = 0; sIdx < this.allSampleTypes.length; sIdx++) {
					var sampleType = this.allSampleTypes[sIdx];
					if(!this.isSampleTypeHidden(sampleType.code)) {
						allNonHiddenSampleTypes.push(sampleType);
					}
				}
				return allNonHiddenSampleTypes;
			} else {
				return this.allSampleTypes;
			}
		}

		this.datasetViewerImagePreviewIconSize = 25; // width in px
		this.datasetViewerMaxFilesizeForImagePreview = 50000000; // filesize in bytes

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
				if(!error && (result.data.cifs || result.data.sftp)) {
					_this.cifsFileServer = result.data.cifs;
					_this.sftpFileServer = result.data.sftp;
				}
				callback();
			});
		}
		
		this.initIsAdmin = function(callback) {
			var _this = this;
			this.serverFacade.listPersons(function(data) {
				_this.isAdmin = !(data.error);
				callback();
			});
		}
		
		this.initDatasetTypeCodes = function(callback) {
			var _this = this;
			this.serverFacade.listDataSetTypes(function(data) {
				var dataSetTypes = data.result;
				for(var i = 0; i < dataSetTypes.length; i++) {
					var datasetType = dataSetTypes[i];
					_this.allDatasetTypeCodes.push(datasetType.code);
				}
				
				_this.allDatasetTypeCodes.sort(function(a, b){
				    if(a < b) return -1;
				    if(a > b) return 1;
				    return 0;
				});
				
				callback();
			});
		}

		this.initSettings = function(callback) {
			var settingsManager = new SettingsManager(this.serverFacade);
			settingsManager.loadSettingsAndApplyToProfile((function() {
				callback();
			}));
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
							_this.initIsAdmin(function() {
								_this.initDatasetTypeCodes(function() {
									_this.initSettings(function() {
										//Check if the new storage system can be enabled
										var storageRack = _this.getSampleTypeForSampleTypeCode("STORAGE");
										var storagePositionType = _this.getSampleTypeForSampleTypeCode("STORAGE_POSITION");										
										_this.storagesConfiguration = { 
												"isEnabled" : storageRack && storagePositionType
										};
										
										callbackWhenDone();
									});
								});
							});
						});
					});
				});
			});
		}
	}
});