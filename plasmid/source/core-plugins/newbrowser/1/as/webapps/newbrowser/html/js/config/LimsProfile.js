
function LimsProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(LimsProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		//this.notShowTypes = ["SYSTEM_EXPERIMENT", "ILLUMINA_FLOW_CELL", "ILLUMINA_FLOW_LANE", "LIBRARY", "LIBRARY_POOL", "MASTER_SAMPLE","MS_INJECTION","RAW_SAMPLE","TEMPLATE_SAMPLE", "SEARCH"];
		this.inventorySpaces = ["YEAST_LAB"];
		this.isShowUnavailablePreviewOnSampleTable = false;
		this.typeGroups = {
			"METHODS" : {
				"TYPE" : "METHODS",
				"DISPLAY_NAME" : "Methods",
				"LIST" : ["GENERAL_PROTOCOL", "PCR", "WESTERN_BLOTTING","READOUT"]
			},
			"MATERIALS" : {
				"TYPE" : "MATERIALS",
				"DISPLAY_NAME" : "Materials",
				"LIST" : ["CHEMICAL", "ANTIBODY", "MEDIA", "SOLUTIONS_BUFFERS", "ENZYME", "OLIGO", "INHIBITORS"]
			},
			"SAMPLES" : {
				"TYPE" : "SAMPLES",
				"DISPLAY_NAME" : "Samples",
				"LIST" : ["PLASMID", "YEAST", "BACTERIA"]
			},
			"MICE" : {
				"TYPE" : "MICE",
				"DISPLAY_NAME" : "Mice",
				"LIST" : ["MOUSE"]
			},
			"OTHERS" : {
				"TYPE" : "OTHERS",
				"DISPLAY_NAME" : "Others",
				"LIST" : [] 
			}
		};



		this.storagesConfiguration = {
				"isEnabled" : true,
				/*
				 * Should be the same across all storages, if not correct behaviour is not guaranteed.
				*/
				"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Storage 1", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Group 1", //Storage Group Name
					"NAME_PROPERTY" : "FREEZER_NAME_1", //Should be a Vocabulary.
					"ROW_PROPERTY" : "ROW_1", //Should be an integer.
					"COLUMN_PROPERTY" : "COLUMN_1",  //Should be an integer.
					"BOX_PROPERTY" : "BOX_NUMBER_1", //Should be text.
					"USER_PROPERTY" : "USER_PROPERTY_1" //Should be text.
				},
				{
					"STORAGE_PROPERTY_GROUP" : "Storage 2", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Storage Group 2", //Storage Group Name
					"NAME_PROPERTY" : "FREEZER_NAME_2", //Should be a Vocabulary.
					"ROW_PROPERTY" : "ROW_2", //Should be an integer.
					"COLUMN_PROPERTY" : "COLUMN_2", //Should be an integer.
					"BOX_PROPERTY" : "BOX_NUMBER_2", //Should be text.
					"USER_PROPERTY" : "USER_PROPERTY_2" //Should be text.
				}],
				/*
				 * Storages map, can hold configurations for several storages.
				*/
				"STORAGE_CONFIGS": {
					"MINUS80_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},
					"MINUS80_2" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},	
					"MINUS20_1" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 9, //Number of rows
									"COLUMN_NUM" : 9, //Number of columns
									"BOX_NUM" : 3 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								},																
					"BENCH" : { //Freezer name given by the NAME_PROPERTY
									"ROW_NUM" : 1, //Number of rows
									"COLUMN_NUM" : 1, //Number of columns
									"BOX_NUM" : 99999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
								}
				}
			};
	
		
		this.sampleTypeDefinitionsExtension = {
				"PLASMID_EXPRESSION" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Protocol",
														"TYPE": "PROTOCOLS",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}
													,
													{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMIDS",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : []
													}
													,
													{
														"LABEL" : "Inhibitor",
														"TYPE": "INHIBITORS",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "CONCENTRATION", "MANDATORY" : false }]
													}
													,
													{
														"LABEL" : "Cell Line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 1,
														"ANNOTATION_PROPERTIES" : []
													}
												],
				}
		}
		
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
		
		this.getDataSetTypeForFileName = function(allDatasetFiles, fileName) {
			if(fileName.endsWith("gb") || fileName.endsWith("fasta") || fileName.endsWith("xdna") || fileName.endsWith("fa")) {
				return "SEQ_FILE";
			} else if(fileName.endsWith("ab1")) {
				return "RAW_DATA";
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