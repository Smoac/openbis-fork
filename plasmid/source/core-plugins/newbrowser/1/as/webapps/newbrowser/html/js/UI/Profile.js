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
		this.menuStructure = [];
		
		this.searchType = {
			"TYPE" : "SEARCH",
			"DISPLAY_NAME" : "Search",
			"SAMPLE_TYPE_PROPERTIES" : ["TYPE", "MATCHED_TEXT", "MATCHED_FIELD"],
			"SAMPLE_TYPE_PROPERTIES_DISPLAY_NAME" : ["Sample Type", "Matched Text", "Matching Field"],	
		};
		
		this.allTypes = [];
		this.allVocabularies = [];
		this.allDataStores = [];
		this.displaySettings = {};
		
		this.typeGroups = {
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] //All types not present in other groups and not in notShowTypes, is a box where everything that is not configured goes by default
			}
		};
		
		this.typePropertiesForTable = {};
		
		this.colorForInspectors = {};
		
		this.storagesConfiguration = {
			"isEnabled" : false
		};
		
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
		 * Used by Sample Form
		 */
		
		this.getSpaceForSampleType = function(type) {
			return "DEFAULT";
		}
		
		this.getExperimentIdentifierForSample = function(type, code, properties) {
			return "/" + this.getSpaceForSampleType(type) + "/PROJECT_" + type + "/FOLDER_" + type;
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
	
		//
		// Utility methods used to navigate the configuration easily
		//
		this.isELNExperiment = function(sampleTypeCode) {
			return $.inArray(sampleTypeCode, this.ELNExperiments) !== -1;
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
	
		this.getTypeForTypeCode = function(typeCode) {
			for(var i = 0; i < this.allTypes.length; i++) {
				if(this.allTypes[i].code === typeCode) {
					return this.allTypes[i];
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
			var sampleType = this.getTypeForTypeCode(typeCode);
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
			var sampleType = this.getTypeForTypeCode(sampleTypeCode);
		
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
			var sampleType = this.getTypeForTypeCode(sampleTypeCode);
		
			for(var i = 0; i < propertiesTypeCode.length; i++) {
				var propertyTypeCode = propertiesTypeCode[i];
				var propertyTypeDisplayName = this.getPropertyDisplayNamesForTypeCode(sampleTypeCode, propertyTypeCode);
				allPropertiDisplayNames.push(propertyTypeDisplayName);
			}
		
			return allPropertiDisplayNames;
		}
	
		this.initVocabulariesForSampleTypes = function() {
			//Build Vocabularies from sample types
			for(var sampleTypeIdx = 0; sampleTypeIdx < this.allTypes.length; sampleTypeIdx++) {
				var sampleType = this.allTypes[sampleTypeIdx];
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
				}
			);
		}
	
		this.initMenuStructure = function() {
			//
			// Build menu into an in memory structure (Can be used to render it in different manners Menu+Drop Down)
			//
			var groupOfMenuItems = new GroupOfMenuItems("EXPERIMENTS","Experiments",[]);
			for(var i = 0; i < this.ELNExperiments.length; i++) {
				var sampleTypeCode = this.ELNExperiments[i];
				var sampleType = this.getTypeForTypeCode(sampleTypeCode);
			
				if(sampleType !== null) {
					var description = Util.getEmptyIfNull(sampleType.description);
					if(description === "") {
						description = sampleType.code;
					}
					var menuItem = new MenuItem("./images/experiment-icon.png", "showSamplesPage", sampleType.code, description);
					groupOfMenuItems.menuItems.push(menuItem);
				}
			}
			if(groupOfMenuItems.menuItems.length > 0) {
					this.menuStructure.push(groupOfMenuItems);
			}
		
			for(typeGroupCode in this.typeGroups) {
				groupOfMenuItems = new GroupOfMenuItems(typeGroupCode,this.typeGroups[typeGroupCode]["DISPLAY_NAME"],[]);
			
				for(var i = 0; i < this.typeGroups[typeGroupCode]["LIST"].length; i++) {
					var sampleType = this.getTypeForTypeCode(this.typeGroups[typeGroupCode]["LIST"][i]);
				
					if(sampleType !== null) {
						var description = Util.getEmptyIfNull(sampleType.description);
						if(description === "") {
							description = sampleType.code;
						}
						var menuItem = new MenuItem("./images/notebook-icon.png", "showSamplesPage", sampleType.code, description);
						groupOfMenuItems.menuItems.push(menuItem);
					}
				}
				if(groupOfMenuItems.menuItems.length > 0) {
					this.menuStructure.push(groupOfMenuItems);
				}
			}
		}
		//
		// Initializes the Others list with all sampleType codes that are neither in typeGroups or notShowTypes
		//
		this.init = function() {
			for(var i = 0; i < this.allTypes.length; i++) {
				var sampleType = this.allTypes[i];
				if($.inArray(sampleType.code, this.notShowTypes) === -1) {
					if(this.getGroupTypeCodeForTypeCode(sampleType.code) === null) {
						this.typeGroups["OTHERS"]["LIST"].push(sampleType.code);
					}
				}
			}
		
			this.initVocabulariesForSampleTypes();
			this.initMenuStructure();
		}
	}
});

//YEASTLAB PROFILE
function YeastLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(YeastLabProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.notShowTypes = ["SYSTEM_EXPERIMENT", "ILLUMINA_FLOW_CELL", "ILLUMINA_FLOW_LANE", "LIBRARY", "LIBRARY_POOL", "MASTER_SAMPLE","MS_INJECTION","RAW_SAMPLE","TEMPLATE_SAMPLE", "SEARCH"];
	
		this.typeGroups = {
			"METHODS" : {
				"TYPE" : "METHODS",
				"DISPLAY_NAME" : "Methods",
				"LIST" : ["GENERAL_PROTOCOL", "PCR", "WESTERN_BLOTTING","READOUT"]
			},
			"MATERIALS" : {
				"TYPE" : "MATERIALS",
				"DISPLAY_NAME" : "Materials",
				"LIST" : ["CHEMICAL", "ANTIBODY", "MEDIA", "SOLUTIONS_BUFFERS", "ENZYME", "OLIGO"]
			},
			"SAMPLES" : {
				"TYPE" : "SAMPLES",
				"DISPLAY_NAME" : "Samples",
				"LIST" : ["PLASMID", "YEAST", "BACTERIA"]
			},
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] 
			}
		};
	
		this.typePropertiesForTable = {
			"SYSTEM_EXPERIMENT" : ["NAME", "GOALS", "RESULT_INTERPRETATION"],
			"GENERAL_PROTOCOL" : ["NAME", "FOR_WHAT", "PROTOCOL_TYPE"],
			"PCR" : ["NAME", "FOR_WHAT", "TEMPLATE", "PUBLICATION"],
			"WESTERN_BLOTTING" : ["NAME", "FOR_WHAT", "STORAGE"],
			"CHEMICAL" : ["NAME", "SUPPLIER", "ARTICLE_NUMBER", "LOCAL_ID", "STORAGE"],
			"ANTIBODY" : ["NAME", "STORAGE", "HOST", "FOR_WHAT"],
			"MEDIA" : ["NAME", "STORAGE", "FOR_WHAT", "ORGANISM"],
			"SOLUTIONS_BUFFERS" : ["NAME", "STORAGE", "FOR_WHAT"],
			"ENZYME" : ["NAME", "SUPPLIER", "ARTICLE_NUMBER", "KIT"],
			"OLIGO" : ["TARGET", "DIRECTION", "RESTRICTION_ENZYME", "PROJECT"],
			"PLASMID" : ["OWNER", "OWNER_NUMBER", "PLASMID_NAME", "BACTERIAL_ANTIBIOTIC_RESISTANCE", "YEAST_MARKER"],
			"YEAST" : ["OWNER", "OWNER_NUMBER", "YEAST_STRAIN_NAME", "PROJECT", "GENETIC_BACKGROUND", "MATING_TYPE", "FREEZER_NAME", "ROW", "COLUMN", "BOX_NUMBER"],
			"BACTERIA" : ["BACTERIA_STRAIN_NAME", "BACTERIA_GENOTYPE", "FOR_WHAT", "SUPPLIER", "ARTICLE_NUMBER", "COMMENTS"]
		}
	
		this.colorForInspectors = {
			"GENERAL_PROTOCOL" : "#CCFFCC",
			"PCR" : "#CCFFCC",
			"WESTERN_BLOTTING" : "#CCFFCC",
			"CHEMICAL" : "#E3E3E3",
			"ANTIBODY" : "#E3E3E3",
			"MEDIA" : "#E3E3E3",
			"SOLUTIONS_BUFFERS" : "#E3E3E3",
			"ENZYME" : "#E3E3E3",
			"BACTERIA" : "#E3E3E3",
			"OLIGO" : "#ACE8FC",
			"PLASMID" : "#FCDEC0",
			"YEAST" : "#CCCC99",
			"SAMPLE_PROPERTY_TEST" : "#000000"
		};
	
		this.storagesConfiguration = {
			"isEnabled" : true,
			/*
			 * Should be the same across all storages, if not correct behaviour is not guaranteed.
			*/
			"STORAGE_PROPERTIES": {
						"NAME_PROPERTY" : "FREEZER_NAME", //Should be a Vocabulary.
						"ROW_PROPERTY" : "ROW", //Vocabulary on YeastLab, can be (Vocabulary, text and integer).
						"COLUMN_PROPERTY" : "COLUMN", //Integer on YeastLab, can be (Vocabulary, text and integer).
						"BOX_PROPERTY" : "BOX_NUMBER" //Should be text.
			},
			/*
			 * Where the storage will be painted.
			*/
			"STORAGE_PROPERTY_GROUP" : "Storage information",
			/*
			 * Storages map, can hold configurations for several storages.
			*/
			"STORAGE_CONFIGS": {
				"-80DEGREES" : { //Freezer name given by the NAME_PROPERTY
								"ROW_NUM" : 9, //Number of rows
								"COLUMN_NUM" : 9, //Number of columns
								"BOX_NUM" : 999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
							}
			}
		};
	
		/*
		 * Used by Sample Form
		 */
		this.getSpaceForSampleType = function(type) {
			if(type === "BACTERIA") {
				return "YEAST_LAB";
			} else if(type === "SOLUTIONS_BUFFERS") {
				return "YEAST_LAB";
			} else if(type === "MEDIA") {
				return "YEAST_LAB";
			} else if(type === "ENZYME") {
				return "YEAST_LAB";
			} else if(type === "CHEMICAL") {
				return "YEAST_LAB";
			} else if(type === "ANTIBODY") {
				return "YEAST_LAB";
			} else if(type === "OLIGO") {
				return "YEAST_LAB";
			} else if(type === "PLASMID") {
				return "YEAST_LAB";
			} else if(type === "WESTERN_BLOTTING") {
				return "YEAST_LAB";
			} else if(type === "READOUT") {
				return "YEAST_LAB";
			} else if(type === "PCR") {
				return "YEAST_LAB";
			} else if(type === "GENERAL_PROTOCOL") {
				return "YEAST_LAB";
			} else if(type === "YEAST") {
				return "YEAST_LAB";
			} else if(type === "POMBE") {
				return "YEAST_LAB";
			} else {
				return null;
			}
		}
		
		this.getExperimentIdentifierForSample = function(type, code, properties) {
			if(type === "BACTERIA") {
				return "/YEAST_LAB/BACTERIA/LAB_BENCH_BACTERIA";
			} else if(type === "SOLUTIONS_BUFFERS") {
				return "/YEAST_LAB/CHEMICALS/SOLUTIONS_BUFFERS";
			} else if(type === "MEDIA") {
				return "/YEAST_LAB/CHEMICALS/MEDIA";
			} else if(type === "ENZYME") {
				return "/YEAST_LAB/CHEMICALS/ENZYMES";
			} else if(type === "CHEMICAL") {
				return "/YEAST_LAB/CHEMICALS/CHEMICALS";
			} else if(type === "ANTIBODY") {
				return "/YEAST_LAB/CHEMICALS/ANTIBODIES";
			} else if(type === "OLIGO") {
				return "/YEAST_LAB/OLIGO/81_BOXES";
			} else if(type === "PLASMID") {
				return "/YEAST_LAB/PLASMIDS/LAB_BENCH_PLASMIDS";
			} else if(type === "WESTERN_BLOTTING") {
				return "/YEAST_LAB/PROTOCOLS/WESTERN_BLOTTING";
			} else if(type === "READOUT") {
				return "/YEAST_LAB/PROTOCOLS/READOUTS";
			} else if(type === "PCR") {
				return "/YEAST_LAB/PROTOCOLS/PCR";
			} else if(type === "GENERAL_PROTOCOL") {
				return "/YEAST_LAB/PROTOCOLS/GENERAL_PROTOCOLS";
			} else if(type === "YEAST") {
				return "/YEAST_LAB/YEAST/LAB_BENCH_YEASTS";
			} else if(type === "POMBE") {
				return "/YEAST_LAB/YEAST/LAB_BENCH_POMBE";
			} else {
				return null;
			}
		}
		
		this.getHTMLTableFromXML = function(xmlDocument) {
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
	
		this.inspectorContentTransformer = function(sample, propertyCode, propertyContent) {
		
			if(propertyContent.indexOf("<root>") != -1) {
				return {
					"isSingleColumn" : true,
					"content" : this.getHTMLTableFromXML(propertyContent)
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
	
		this.searchSorter = function(searchResults) {
		
			var getChars = function(code) {
				var theChars = code.replace(/[0-9]/g, '')
				return theChars;
			}
		
			var getNums = function(code) {
				var thenum = code.replace( /^\D+/g, '');
				if(thenum.length > 0) {
					return parseInt(thenum);
				} else {
					return 0;
				}
			}
		
			var customSort = function(sampleA, sampleB){
				var aCode = getChars(sampleA.code);
				var bCode = getChars(sampleB.code);
			
				var returnValue = null;
				if(aCode < bCode) {
					returnValue = -1;
				} else if(aCode > bCode) {
					returnValue = 1;
				} else {
					var aNum = getNums(sampleA.code);
					var bNum = getNums(sampleB.code);
					returnValue = aNum - bNum;
				}
				return -1 * returnValue;
			}
		
			var sortedResults = searchResults.sort(customSort);
		
			return sortedResults;
		}
	
		this.inspectorContentExtra = function(extraContainerId, sample) {
			// When requesting information about the sample, we don't need parents and children, so send a copy of the saple without that information.
			var sampleToSend = $.extend({}, sample);
			delete sampleToSend.parents;
			delete sampleToSend.children; 
		
			var localReference = this;
			this.serverFacade.listDataSetsForSample(sampleToSend, true, function(datasets) {
				for(var i = 0; i < datasets.result.length; i++) {
					var dataset = datasets.result[i];
					if(dataset.dataSetTypeCode === "SEQ_FILE") {
						var listFilesForDataSetWithDataset = function(dataset) {
							localReference.serverFacade.listFilesForDataSet(dataset.code, "/", true, function(files) {
								for(var i = 0; i < files.result.length; i++) {
										var isDirectory = files.result[i].isDirectory;
										var pathInDataSet = files.result[i].pathInDataSet;
										if (/\.svg$/.test(pathInDataSet) && !isDirectory) {
											var downloadUrl = localReference.allDataStores[0].downloadUrl + '/' + dataset.code + "/" + pathInDataSet + "?sessionID=" + localReference.serverFacade.getSession();
											d3.xml(downloadUrl, "image/svg+xml", 
												function(xml) {
													var importedNode = document.importNode(xml.documentElement, true);
													d3.select(importedNode)
														.attr("width", 400 - 20)
														.attr("height", 400 - 20)
														.attr("viewBox", "200 200 650 650");
													var inspectorNode = d3.select("#"+extraContainerId).node();
													if(inspectorNode) { //Sometimes the user hides the sticker very clicky and the node doesn't exist anymore
														inspectorNode.appendChild(importedNode);
													}
												});
										}
								}
							});
						}
						listFilesForDataSetWithDataset(dataset);
					}
				}
			}
			);
			return "";
		}
	}
});

//Example profile with basic configuration that don´t requires any programming
function PhosphoProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(PhosphoProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		//Put on this list all experiment types, ELN experiments need to have both an experiment type and a sample type with the same CODE.
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];

		//Black list, put on this list all types that you don´t want to appear on the menu and the ELN experiments.
		this.notShowTypes = ["SYSTEM_EXPERIMENT", "FJELMER_TEST", "SEARCH"];
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.typeGroups = {
			"BIOLOGICAL_SAMPLES_GROUP" : {
				"TYPE" : "BIOLOGICAL_SAMPLES_GROUP",
				"DISPLAY_NAME" : "Biological Samples",
				"LIST" : ["BIOLOGICAL_SAMPLE", "BIOL_APMS", "BIOL_BASIC", "BIOL_CLINICAL", "BIOL_DDB", "BIOL_DDB_PATIENT", "BIOL_IRRELEVANT", "BIOL_MICROORGANISMS", "BIOL_PHOSPHO", "BIOL_SYNTHETIC", "BIOL_XL"] 
			},
			"MS_INJECTION_GROUP" : {
				"TYPE" : "MS_INJECTION_GROUP",
				"DISPLAY_NAME" : "MS Injection",
				"LIST" : ["MS_INJECTION"] 
			},
			"ROSETTA_GROUP" : {
				"TYPE" : "ROSETTA_GROUP",
				"DISPLAY_NAME" : "Rosetta",
				"LIST" : ["ROSETTA_DENOVO"] 
			},
			"WORKFLOW_GROUP" : {
				"TYPE" : "WORKFLOW_GROUP",
				"DISPLAY_NAME" : "Work Flow",
				"LIST" : ["WORKFLOW"] 
			},
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] 
			}
		};
		
		//The properties you want to appear on the tables, if you don´t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {
				"BIOLOGICAL_SAMPLE" : ["NAME", "COMMENT", "TREATMENT_TYPE1", "TREATMENT_VALUE1", "TREATMENT_TYPE2", "TREATMENT_VALUE2", "TREATMENT_TYPE3", "TREATMENT_VALUE3"],
				"BIOL_APMS" : ["NAME", "COMMENT", "BAIT", "DIGESTION"],
				"BIOL_BASIC" : ["NAME", "COMMENT", "BIOLOGICAL_SAMPLE_TYPE", "TAX_ID", "SAMPLE_PREPARATION", "FRACTIONATION", "DIGESTION", "LABELING"],
				"BIOL_CLINICAL" : ["NAME", "COMMENT", "DIGESTION", "LABELING"],
				"BIOL_DDB" : ["NAME", "EM_PATIENTS", "CK_PATIENTS", "GENOME", "STRAIN_NAME", "PRIMARY_CELL_TYPE"],
				"BIOL_MICROORGANISMS" : ["NAME", "BIOLOGICAL_SAMPLE_TYPE", "TAX_ID", "STRAIN", "SAMPLE_PREPARATION"],
				"BIOL_PHOSPHO" : ["NAME", "SAMPLE_PREPARATION", "TREATMENT_PH_1", "TREATMENT_MO_1_VALUE", "TREATMENT_MO_1_TIME"],
				"BIOL_SYNTHETIC" : ["NAME", "TYPE_SYNTHETIC", "SYNTHETIC_PEPTIDE"],
				"BIOL_XL" : ["NAME", "COMMENT","CROSS_LINKER"],
				"MS_INJECTION" : ["INSTRUMENT_TYPE"]
		};
		
		//The colors for the notes, if you don´t specify the color, light yellow will be used by default.
		this.colorForInspectors = {};
		
		//The configuration for the visual storages.
		this.storagesConfiguration = {
			"isEnabled" : false
		};
}
});

//Example profile with basic configuration that don´t requires any programming
function ExampleProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(ExampleProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		//Put on this list all experiment types, ELN experiments need to have both an experiment type and a sample type with the same CODE.
		this.ELNExperiments = ["SYSTEM_EXPERIMENT"];

		//Black list, put on this list all types that you don´t want to appear on the menu and the ELN experiments.
		this.notShowTypes = ["SYSTEM_EXPERIMENT"];
	
		//Use this with all known types to create groups, if a type is not specified by default will be added to the OTHERS group.
		this.typeGroups = {
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] 
			}
		};
		
		//The properties you want to appear on the tables, if you don´t specify the list, all of them will appear by default.
		this.typePropertiesForTable = {};
		
		//The colors for the notes, if you don´t specify the color, light yellow will be used by default.
		this.colorForInspectors = {};
		
		//The configuration for the visual storages.
		this.storagesConfiguration = {
			"isEnabled" : false
		};
}
});