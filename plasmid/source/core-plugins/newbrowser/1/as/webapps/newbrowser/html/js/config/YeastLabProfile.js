function YeastLabProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(YeastLabProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.notShowTypes = ["SYSTEM_EXPERIMENT", "ILLUMINA_FLOW_CELL", "ILLUMINA_FLOW_LANE", "LIBRARY", "LIBRARY_POOL", "MASTER_SAMPLE","MS_INJECTION","RAW_SAMPLE","TEMPLATE_SAMPLE", "SEARCH"];
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
		
		/* New Sample definition tests
		this.sampleTypeDefinitionsExtension = {
				"SYSTEM_EXPERIMENT" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Protocol",
														"TYPE": "GENERAL_PROTOCOL",
														"MIN_COUNT" : 1
													}
													,
													{
														"LABEL" : "Yeast",
														"TYPE": "YEAST",
														"MIN_COUNT" : 0
													}
												],
				}
		}
		*/
		
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