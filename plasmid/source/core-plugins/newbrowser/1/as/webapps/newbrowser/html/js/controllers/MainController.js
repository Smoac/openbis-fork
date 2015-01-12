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
 * Class MainController
 * 
 * This class is used as central point of control into the application.
 *
 * It holds:
 * - server facade
 * - Configuration profile
 * - Atributes used by inline HTML/Javascript
 * - enterApp method
 * - showView methods
 *
 * @constructor
 * @this {MainController}
 * @param {DefaultProfile} profile Profile used to configure the app.
 */
function MainController(profile) {
	//
	// Atributes
	//
	
	// Server Facade Object
	this.serverFacade = new ServerFacade(new openbis()); //Client APP Facade, used as control point to know what is used and create utility methods.

	// Configuration
	this.profile = profile;
	this.profile.serverFacade = this.serverFacade;
	FormUtil.profile = this.profile;
	
	// Atributes - Widgets typically hold both the model and the view, they are here so they can be accessed by inline HTML/Javascript when needed.
	
	//Views With State or always visible
	this.inspector = null; // The samples that are currently being inspected
	this.sideMenu = null;
	
	//Others
	this.currentView = null;
	
	//
	// Validates and enters the app
	//
	
	this.enterApp = function(data) {
		var localReference = this;
		//
		// Check Credentials
		//
		if(data.result == null){
			$("#username").focus();
			var callback = function() {Util.unblockUI();};
			Util.showError('The given username or password is not correct.', callback);
			return;
		}
		
		//
		// Back Button Logic
		//
		//BackButton Logic
		var backButtonLogic = function(e) {
			var queryString = Util.queryString();
			var viewName = queryString.viewName;
			var viewData = queryString.viewData
			if(viewName && viewData) {
				localReference.changeView(viewName, viewData);
			}
		}
		window.addEventListener("popstate", function(e) {backButtonLogic(e);}); 
		
		//
		// Start App if credentials are ok
		//
		$('body').removeClass('bodyLogin');
		$("#login-form-div").hide();
		$("#main").show();
		
		//Get Metadata from all sample types before showing the main menu
		this.serverFacade.listSampleTypes (
			function(result) {
				//Load Sample Types
				localReference.profile.allSampleTypes = result.result;
				
				//Load datastores for automatic DSS configuration, the first one will be used
				localReference.serverFacade.listDataStores(function(dataStores) {
						localReference.profile.allDataStores = dataStores.result;
				
						//Load display settings
						localReference.serverFacade.getUserDisplaySettings( function(response) {
							if(response.result) {
								localReference.profile.displaySettings = response.result;
							}
							
							//Load Experiment Types
							localReference.serverFacade.listExperimentTypes(function(experiments) {
								localReference.profile.allExperimentTypes = experiments.result;
								
								
								//Init profile
								localReference.profile.init(function() {
									//Start App
									localReference.inspector = new Inspector(localReference.serverFacade, "mainContainer", localReference.profile);
									
									localReference.sideMenu = new SideMenuWidgetController(localReference);
									localReference.sideMenu.init($("#sideMenu"));
									
									//Page reload using the URL info
									var queryString = Util.queryString();
									var viewName = queryString.viewName;
									var viewData = queryString.viewData;
									var hideMenu = queryString.hideMenu;
									if(viewName && viewData) {
										localReference.changeView(viewName, viewData);
										if(hideMenu === "true") {
											localReference.sideMenu.hideSideMenu();
										}
									} else {
										localReference.changeView("showBlancPage", null);
									}
									Util.unblockUI();
								});
								
							});
						});
				});
			}
		);
	}
	
	//
	// Main View Changer - Everything on the application rely on this method to alter the views, arg should be a string
	//
	this.changeView = function(newViewChange, arg) {
		//
		// Dirty forms management, to avoid loosing changes.
		//
		var discardChanges = null;
		if( this.currentView && 
			this.currentView.isDirty && 
			this.currentView.isDirty()) {
			//Ask the user if wants to leave the view in case is dirty
			discardChanges = window.confirm("Leaving this window will discard any changes, are you sure?");
		}
		
		if(discardChanges != null && !discardChanges) {
			return;
		}
		//
		//
		//
		
		switch (newViewChange) {
			case "showUserManagerPage":
				document.title = "User Manager";
				this._showUserManager();
				window.scrollTo(0,0);
				break;
			
			case "showVocabularyManagerPage":
				document.title = "Vocabulary Manager";
				this._showVocabularyManager();
				window.scrollTo(0,0);
				break;
			case "showTrashcanPage":
				document.title = "Trashcan Manager";
				this._showTrashcan();
				window.scrollTo(0,0);
				break;
			case "showStorageManager":
				document.title = "Storage Manager";
				this._showStorageManager();
				window.scrollTo(0,0);
				break;
			case "showInspectors":
				document.title = "Inspectors";
				this._showInspectors();
				window.scrollTo(0,0);
				break;
			case "showBlancPage":
				document.title = "";
				this._showBlancPage();
				window.scrollTo(0,0);
				break;
			case "showSearchPage":
				document.title = "Search";
				var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
				var argsMap = JSON.parse(cleanText);
				var searchText = argsMap["searchText"];
				var searchDomain = argsMap["searchDomain"];
				var searchDomainLabel = argsMap["searchDomainLabel"];
				this._showSearchPage(searchText, searchDomain, searchDomainLabel);
				window.scrollTo(0,0);
				break;
			case "showSpacePage":
				var _this = this;
				this.serverFacade.getSpaceFromCode(arg, function(space) {
					document.title = "Space " + space.code;
					_this._showSpacePage(space);
					window.scrollTo(0,0);
				});
				break;
			case "showProjectPageFromIdentifier":
				var _this = this;
				this.serverFacade.getProjectFromIdentifier(arg, function(project) {
					document.title = "Project " + project.code;
					_this._showProjectPage(project);
					window.scrollTo(0,0);
				});
				break;
			case "showProjectPageFromPermId":
				var _this = this;
				this.serverFacade.getProjectFromPermId(arg, function(project) {
					document.title = "Project " + project.code;
					_this._showProjectPage(project);
					window.scrollTo(0,0);
				});
				break;
			case "showEditProjectPageFromPermId":
				var _this = this;
				this.serverFacade.getProjectFromPermId(arg, function(project) {
					document.title = "Project " + project.code;
					_this._showEditProjectPage(project);
					window.scrollTo(0,0);
				});
				break;
			case "showCreateProjectPage":
				document.title = "Create Project";
				this._showCreateProjectPage(arg);
				window.scrollTo(0,0);
				break;
			case "showCreateExperimentPage":
				var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
				var argsMap = JSON.parse(cleanText);
				var experimentTypeCode = argsMap["experimentTypeCode"];
				var projectIdentifier = argsMap["projectIdentifier"];
				document.title = "Create Experiment " + experimentTypeCode;
				var experiment = {
						experimentTypeCode : experimentTypeCode,
						identifier : projectIdentifier
				}
				this._showExperimentPage(experiment, FormMode.CREATE);
				window.scrollTo(0,0);
				break;
			case "showExperimentPageFromIdentifier":
				var _this = this;
				this.serverFacade.listExperimentsForIdentifiers([arg], function(data) {
					document.title = "Experiment " + arg;
					_this._showExperimentPage(data.result[0], FormMode.VIEW);
					window.scrollTo(0,0);
				});
				break;
			case "showEditExperimentPageFromIdentifier":
				var _this = this;
				this.serverFacade.listExperimentsForIdentifiers([arg], function(data) {
					document.title = "Experiment " + arg;
					_this._showExperimentPage(data.result[0], FormMode.EDIT);
					window.scrollTo(0,0);
				});
				break;
			case "showCreateSubExperimentPage":
				var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
				var argsMap = JSON.parse(cleanText);
				var sampleTypeCode = argsMap["sampleTypeCode"];
				var experimentIdentifier = argsMap["experimentIdentifier"];
				document.title = "Create Sample " + arg;
				this._showCreateSubExperimentPage(sampleTypeCode, experimentIdentifier);
				window.scrollTo(0,0);
				break;
			case "showSamplesPage":
				document.title = arg + " List";
				this._showSamplesPage(arg);
				window.scrollTo(0,0);
				break;
			case "showSampleHierarchyPage":
				document.title = "Hierarchy " + arg;
				this._showSampleHierarchyPage(arg);
				window.scrollTo(0,0);
				break;
			case "showCreateSamplePage":
				document.title = "Create Sample " + arg;
				this._showCreateSamplePage(arg);
				window.scrollTo(0,0);
				break;
			case "showEditSamplePageFromPermId":
				var _this = this;
				this.serverFacade.searchWithUniqueId(arg, function(data) {
					if(!data[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						document.title = data[0].code;
						var isELNSubExperiment = $.inArray(data[0].spaceCode, _this.profile.inventorySpaces) === -1 && _this.profile.inventorySpaces.length > 0;
						_this._showEditSamplePage(data[0], isELNSubExperiment);
						window.scrollTo(0,0);
					}
				});
				break;
			case "showViewSamplePageFromPermId":
				var _this = this;
				this.serverFacade.searchWithUniqueId(arg, function(data) {
					if(!data[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						document.title = data[0].code;
						var isELNSubExperiment = $.inArray(data[0].spaceCode, _this.profile.inventorySpaces) === -1&& _this.profile.inventorySpaces.length > 0;
						_this._showViewSamplePage(data[0], isELNSubExperiment);
						window.scrollTo(0,0);
					}
				});
				break;
			case "showCreateDataSetPageFromPermId":
				var _this = this;
				this.serverFacade.searchWithUniqueId(arg, function(data) {
					if(!data[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						document.title = "Create Data Set for " + data[0].code;
						_this._showCreateDataSetPage(data[0]);
						window.scrollTo(0,0);
					}
				});
				break;
			case "showViewDataSetPageFromPermId":
				var _this = this;
				this.serverFacade.searchDataSetWithUniqueId(arg, function(dataSetData) {
					if(!dataSetData.result || !dataSetData.result[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						_this.serverFacade.searchWithIdentifier(dataSetData.result[0].sampleIdentifierOrNull, function(sampleData) {
							document.title = "View Data Set " + dataSetData.result[0].code;
							_this._showViewDataSetPage(sampleData[0], dataSetData.result[0]);
							window.scrollTo(0,0);
						});
					}
				});
				break;
			case "showEditDataSetPageFromPermId":
				var _this = this;
				this.serverFacade.searchDataSetWithUniqueId(arg, function(dataSetData) {
					if(!dataSetData.result || !dataSetData.result[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						_this.serverFacade.searchWithIdentifier(dataSetData.result[0].sampleIdentifierOrNull, function(sampleData) {
							document.title = "Edit Data Set " + dataSetData.result[0].code;
							_this._showEditDataSetPage(sampleData[0], dataSetData.result[0]);
							window.scrollTo(0,0);
						});
					}
				});
				break;
			default:
				window.alert("The system tried to create a non existing view");
				break;
		}
		
		//
		// Permanent URLs
		//
		var url = window.location.href.split("?")[0] + "?viewName=" + newViewChange + "&viewData=" + arg;
		history.pushState(null, "", url); //History Push State
	}
	
	//
	// Functions that trigger view changes, should only be called from the main controller changeView method
	//
	this._showStorageManager = function() {
		var storageManagerController = new StorageManagerController(this);
		storageManagerController.init($("#mainContainer"));
		this.currentView = storageManagerController;
	}
	
	this._showVocabularyManager = function() {
		var vocabularyManagerController = new VocabularyManagerController(this);
		vocabularyManagerController.init($("#mainContainer"));
		this.currentView = vocabularyManagerController;
	}
	
	this._showInspectors = function() {
		//Show Inspectors
		this.inspector.repaint();
	}
	
	this._showBlancPage = function() {
		//Show Hello Page
		$("#mainContainer").empty();
		
		this.currentView = null;
	}
	
	this._showUserManager = function() {
		var userManagerController = new UserManagerController(this);
		userManagerController.init($("#mainContainer"));
		this.currentView = userManagerController;
	}
	
	this._showSamplesPage = function(experimentIdentifier) {
		var sampleTableController = null;
		
		if(experimentIdentifier === "null") { //Fix for reloads when there is text on the url
			experimentIdentifier = null;
		}
		
		if(experimentIdentifier) {
			sampleTableController = new SampleTableController(this, "Experiment " + experimentIdentifier, experimentIdentifier);
		} else {
			sampleTableController = new SampleTableController(this, "Sample Browser", null);
		}
		
		sampleTableController.init($("#mainContainer"));
		this.currentView = sampleTableController;
	}

	this._showSampleHierarchyPage = function(permId) {
		//Show View
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(permId, function(data) {
			var sampleHierarchy = new SampleHierarchy(localInstance.serverFacade, localInstance.inspector, "mainContainer", localInstance.profile, data[0]);
			sampleHierarchy.init();
			localInstance.currentView = sampleHierarchy;
		});
	}
	
	this._showCreateSubExperimentPage = function(sampleTypeCode, experimentIdentifier) {
		//Update menu
		var sampleTypeDisplayName = this.profile.getSampleTypeForSampleTypeCode(sampleTypeCode).description;
		if(sampleTypeDisplayName === null) {
			sampleTypeDisplayName = sampleTypeCode;
		}
		
		//Show Form
		var sample = {
				sampleTypeCode : sampleTypeCode,
				experimentIdentifierOrNull : experimentIdentifier,
				spaceCode : experimentIdentifier.substring(1, experimentIdentifier.indexOf('/', 1)),
				properties : {}
		}
		var sampleFormController = new SampleFormController(this, FormMode.CREATE, sample);
		this.currentView = sampleFormController;
		sampleFormController.init($("#mainContainer"));
	}
	
	this._showCreateSamplePage = function(sampleTypeCode) {
		//Show Form
		var sample = {
				sampleTypeCode : sampleTypeCode,
				properties : {}
		}
		var sampleFormController = new SampleFormController(this, FormMode.CREATE, sample);
		this.currentView = sampleFormController;
		sampleFormController.init($("#mainContainer"));
	}
	
	this._showTrashcan = function() {
		var trashcanController = new TrashManagerController(this);
		this.trashcanController = trashcanController;
		trashcanController.init($("#mainContainer"));
	}
	
	this._showViewSamplePage = function(sample, isELNSubExperiment) {
		//Show Form
		var sampleFormController = new SampleFormController(this, FormMode.VIEW, sample);
		this.currentView = sampleFormController;
		sampleFormController.init($("#mainContainer"));
		
	}
	
	this._showEditSamplePage = function(sample, isELNSubExperiment) {
		//Show Form
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(sample.permId, function(data) {
			var sampleFormController = new SampleFormController(localInstance, FormMode.EDIT, data[0]);
			localInstance.currentView = sampleFormController;
			sampleFormController.init($("#mainContainer"));
		});
	}
	
	this._showSpacePage = function(space) {
		//Show Form
		var spaceFormController = new SpaceFormController(this, space);
		spaceFormController.init($("#mainContainer"));
		this.currentView = spaceFormController;
	}
	
	this._showCreateProjectPage = function(spaceCode) {
		//Show Form
		var projectFormController = new ProjectFormController(this, FormMode.CREATE, {spaceCode : spaceCode});
		projectFormController.init($("#mainContainer"));
		this.currentView = projectFormController;
	}
	
	this._showProjectPage = function(project) {
		//Show Form
		var projectFormController = new ProjectFormController(this, FormMode.VIEW, project);
		projectFormController.init($("#mainContainer"));
		this.currentView = projectFormController;
	}
	
	this._showEditProjectPage = function(project) {
		//Show Form
		var projectFormController = new ProjectFormController(this, FormMode.EDIT, project);
		projectFormController.init($("#mainContainer"));
		this.currentView = projectFormController;
	}
	
	this._showExperimentPage = function(experiment, mode) {
		//Show Form
		var experimentFormController = new ExperimentFormController(this, mode, experiment);
		experimentFormController.init($("#mainContainer"));
		this.currentView = experimentFormController;
	}
	
	this._showCreateDataSetPage = function(sample) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.CREATE, sample, null);
		newView.init($("#mainContainer"));
		this.currentView = newView;
	}
	
	this._showViewDataSetPage = function(sample, dataset) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.VIEW, sample, dataset);
		newView.init($("#mainContainer"));
		this.currentView = newView;
	}
	
	this._showEditDataSetPage = function(sample, dataset) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.EDIT, sample, dataset);
		newView.init($("#mainContainer"));
		this.currentView = newView;
	}
	
	this.lastSearchId = 0; //Used to discard search responses that don't pertain to the last search call.
	
	this._showSearchPage = function(value, searchDomain, searchDomainLabel) {
		this.lastSearchId++;
		var localSearchId = this.lastSearchId;
		var localReference = this;
		
		if(value.length === 0) {
			return;
		}
		
		var possibleSearch = function(localSearchId) {
			return function() {
				if(localSearchId === localReference.lastSearchId) { //Trigger it if no new have started
					
					if(value.length < 1) {
						return;
					}
//					if(value.length < 3) {
//						var isOk = window.confirm("Are you sure you want to make a search with " + value.length +" characters? You can expect a lot of results.");
//						if(!isOk) {
//							$("#search").removeClass("search-query-searching");
//							return;
//						}
//					}
					
					$("#search").addClass("search-query-searching");
					if(!searchDomain || searchDomain === profile.getSearchDomains()[0].name) { //Global Search
						localReference.serverFacade.searchWithText(value, function(data) {
							$("#search").removeClass("search-query-searching");
							
							var columns = [ {
								label : 'Code',
								property : 'code',
								sortable : true
							}, {
								label : 'Preview',
								property : 'preview',
								sortable : false,
								render : function(data) {
									var previewContainer = $("<div>");
									mainController.serverFacade.searchDataSetsWithTypeForSamples("ELN_PREVIEW", [data.permId], function(data) {
										data.result.forEach(function(dataset) {
											var listFilesForDataSetCallback = function(dataFiles) {
												var downloadUrl = profile.allDataStores[0].downloadUrl + '/' + dataset.code + "/" + dataFiles.result[1].pathInDataSet + "?sessionID=" + mainController.serverFacade.getSession();
												var previewImage = $("<img>", { 'src' : downloadUrl, 'class' : 'zoomableImage', 'style' : 'height:80px;' });
												previewImage.click(function(event) {
													Util.showImage(downloadUrl);
													event.stopPropagation();
												});
												previewContainer.append(previewImage);
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
							}, {
								label : 'Sample Type',
								property : 'sampleTypeCode',
								sortable : true
							}, {
								label : 'Matched Text',
								property : 'matchedText',
								sortable : true
							}, {
								label : 'Matched Field',
								property : 'matchedField',
								sortable : true
							}, {
								label : 'Properties',
								property : 'properties',
								sortable : true,
								render : function(data) {
									var toShow = data.properties;
									if(data.properties.length > 200) {
										toShow = toShow.substring(0, 200) + "...";
									}
									return toShow;
								},
								filter : function(data, filter) {
									return false;
								},
								sort : function(data1, data2, asc) {
									return 0;
								}
							}];
							
							var getDataList = function(callback) {
								var dataList = [];
								var searchRegexp = new RegExp(value, "i");
								var matchedText = null;
								var matchedField = null;
								
								//Check Properties
								for(var i = 0; i < data.length; i++) {
									var sample = data[i];
									for (propertyName in sample.properties) {
										var propertyValue = sample.properties[propertyName];
										if (propertyValue && searchRegexp.test(propertyValue)) {
											var cleanPropertyValue = ""
											
											if(propertyValue.indexOf("<root>") != -1) {
												if(profile.getHTMLTableFromXML) {
													return profile.getHTMLTableFromXML(propertyValue);
												} else {
													if(propertyValue) {
														propertyValue = Util.replaceURLWithHTMLLinks(propertyValue);
													}
													cleanPropertyValue = propertyValue;
												}
											} else {
												if(propertyValue) {
													propertyValue = Util.replaceURLWithHTMLLinks(propertyValue);
												}
												cleanPropertyValue = propertyValue;
											}
											
											matchedText = cleanPropertyValue;
											matchedField = propertyName;
											break;
										}
									}
									
									//properties
									dataList.push({
										permId : sample.permId,
										code : sample.code,
										sampleTypeCode : sample.sampleTypeCode,
										matchedText : matchedText,
										matchedField : matchedField,
										properties : Util.getMapAsString(sample.properties)
									});
								}
								callback(dataList);
							};
							
							var rowClick = function(e) {
								mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
							}
							
							var dataGrid = new DataGridController("Search Results", columns, getDataList, rowClick);
							localReference.currentView = dataGrid;
							dataGrid.init($("#mainContainer"));
							history.pushState(null, "", ""); //History Push State
						});
					} else { //Search Domain
						localReference.serverFacade.searchOnSearchDomain(searchDomain, value, function(data) {
							var dataSetCodes = [];
							for(var i = 0; i < data.result.length; i++) {
								var result = data.result[i];
								var resultLocation = result.resultLocation;
								if(resultLocation.entityKind === "DATA_SET") {
									dataSetCodes.push(resultLocation.code);
								}
							}
							
							localReference.serverFacade.getSamplesForDataSets(dataSetCodes, function(samplesData) {
								var getSampleIdentifierForDataSetCode = function(dataSetCode) {
									for(var i = 0; i < samplesData.result.length; i++) {
										if(samplesData.result[i].code === dataSetCode) {
											return samplesData.result[i].sampleIdentifierOrNull;
										}
									}
									return null;
								}
								
								if(localSearchId === localReference.lastSearchId) {
									$("#search").removeClass("search-query-searching");
									
									var columns = [ {
										label : 'Entity Kind',
										property : 'kind',
										sortable : true
									}, {
										label : 'Code',
										property : 'code',
										sortable : true
									}, {
										label : 'Score',
										property : 'score',
										sortable : true
									}, {
										label : 'Found at',
										property : 'location',
										sortable : true
									}, {
										label : 'Evalue',
										property : 'evalue',
										sortable : true
									}, {
										label : 'Bit Score',
										property : 'bitScore',
										sortable : true
									}, {
										label : 'No Mismatches',
										property : 'numberOfMismatches',
										sortable : true
									}, {
										label : 'No Gaps',
										property : 'totalNumberOfGaps',
										sortable : true
									}, {
										label : 'Sequence (Start - End)',
										property : 'sequenceStartEnd',
										sortable : true
									}, {
										label : 'Query (Start - End)',
										property : 'queryStartEnd',
										sortable : true
									}];
									
									var getDataList = function(callback) {
										var dataList = [];
										if(data.result) {
											for(var i = 0; i < data.result.length; i++) {
												var result = data.result[i];
												var resultLocation = result.resultLocation;
												
												var code = resultLocation.code;
												var numberOfMismatches = resultLocation.alignmentMatch.numberOfMismatches;
												var totalNumberOfGaps = resultLocation.alignmentMatch.totalNumberOfGaps;
												var sequenceStartEnd = resultLocation.alignmentMatch.sequenceStart + "-" + resultLocation.alignmentMatch.sequenceEnd;
												var queryStartEnd = resultLocation.alignmentMatch.queryStart + "-" + resultLocation.alignmentMatch.queryEnd;
												var location = null;
												
												if(resultLocation.propertyType) {
													location = "Property: " + resultLocation.propertyType;
												} else if(resultLocation.pathInDataSet) {
													location = "Path: " + resultLocation.pathInDataSet;
												}
												
												if(resultLocation.entityKind === "DATA_SET") {
													code += "<br> Sample: " + getSampleIdentifierForDataSetCode(resultLocation.code);
												}
												
												dataList.push({
													kind : resultLocation.entityKind,
													code : code,
													permId : resultLocation.permId,
													score : result.score.score,
													bitScore : result.score.bitScore,
													evalue : result.score.evalue,
													numberOfMismatches : numberOfMismatches,
													totalNumberOfGaps : totalNumberOfGaps,
													location : location,
													sequenceStartEnd : sequenceStartEnd,
													queryStartEnd : queryStartEnd
												});
											}
										}
										
										callback(dataList);
									};
									
									var rowClick = function(e) {
										switch(e.data.kind) {
											case "SAMPLE":
												mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
												break;
											case "DATA_SET":
												mainController.changeView('showViewDataSetPageFromPermId', e.data.permId);
												break;
										}
									}
									
									var dataGrid = new DataGridController(searchDomainLabel + " Search Results", columns, getDataList, rowClick);
									localReference.currentView = dataGrid;
									dataGrid.init($("#mainContainer"));
									history.pushState(null, "", ""); //History Push State
								} else {
									//Discard old response, was triggered but a new one was started
								}
							});
						});
					}
					
				} else {
					//Discard it
				}
			}
		}
		
		setTimeout(possibleSearch(localSearchId), 800);
	}
}
