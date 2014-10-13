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
								
									localReference.sideMenu = new SideMenuWidget(localReference, "sideMenu", localReference.serverFacade);
									localReference.sideMenu.init();
									
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
				this.serverFacade.searchDataSetWithUniqueId(arg, function(data) {
					if(!data.result || !data.result[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						document.title = "View Data Set " + data.result[0].code;
						_this._showViewDataSetPage(data.result[0]);
						window.scrollTo(0,0);
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
	
	this._showInspectors = function() {
		//Show Inspectors
		//var examineController = new ExamineController(this);
		//examineController.init($("#mainContainer"));
		//this.currentView = examineController;
		this.inspector.repaint();
	}
	
	this._showBlancPage = function() {
		//Show Hello Page
		$("#mainContainer").empty();
		this.currentView = null;
	}
	
	this._showSamplesPage = function(sampleTypeCode) {
		//Update menu
		var sampleType = this.profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
		
		//Show Sample Table
		var sampleTable = new SampleTable(this.serverFacade, "mainContainer", this.profile, sampleTypeCode, true, true, false, false, false, this.inspector);
		sampleTable.init();
		
		this.currentView = sampleTable;
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
	
	this._showViewDataSetPage = function(dataset) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.VIEW, null, dataset);
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
		if(value.length === 0) {
			return;
		}
		
		this.lastSearchId++;
		var localSearchId = this.lastSearchId;
		var localReference = this;
		
		var possibleSearch = function() {
			if(localSearchId === localReference.lastSearchId) { //Trigger it if no new have started
				
				if(value.length < 3) {
					var isOk = window.confirm("Are you sure you want to make a search with " + value.length +" characters? You can expect a lot of results.");
					if(!isOk) {
						$("#search").removeClass("search-query-searching");
						return;
					}
				}
				
				$("#search").addClass("search-query-searching");
				if(!searchDomain || searchDomain === profile.getSearchDomains()[0].name) { //Global Search
					localReference.serverFacade.searchWithText(value, function(data) {
						if(localSearchId === localReference.lastSearchId) {
							$("#search").removeClass("search-query-searching");
							//Update Main Container
							var sampleTable = new SampleTable(localReference.serverFacade, "mainContainer", localReference.profile, localReference.profile.searchType["TYPE"], true, false, false, true, false, localReference.inspector);
							sampleTable.reloadWithSamples(data);
							localReference.currentView = sampleTable;
							Util.unblockUI();
							history.pushState(null, "", ""); //History Push State
						} else {
							//Discard old response, was triggered but a new one was started
						}
					});
				} else { //Search Domain
					localReference.serverFacade.searchOnSearchDomain(searchDomain, value, function(data) {
						if(localSearchId === localReference.lastSearchId) {
							$("#search").removeClass("search-query-searching");
							
							var columns = [ {
								label : 'Identifier',
								property : 'identifier',
								sortable : true
							}, {
								label : 'Path',
								property : 'pathInDataSet',
								sortable : true
							}, {
								label : 'Position',
								property : 'position',
								sortable : true
							}];
							
							var getDataList = function(callback) {
								var dataList = [];
								for(var i = 0; i < data.result.length; i++) {
									var resultLocation = data.result[i].resultLocation;
									dataList.push({
										permId : resultLocation.dataSetCode,
										identifier : resultLocation.identifier,
										pathInDataSet : resultLocation.pathInDataSet,
										position : resultLocation.position
									});
								}
								callback(dataList);
							};
							
							var rowClick = function(e) {
								mainController.changeView('showViewDataSetPageFromPermId', e.data.permId);
							}
							
							var dataGrid = new DataGridController(localReference, searchDomainLabel + " Search Results", columns, getDataList, rowClick);
							localReference.currentView = dataGrid;
							dataGrid.init($("#mainContainer"));
							history.pushState(null, "", ""); //History Push State
						} else {
							//Discard old response, was triggered but a new one was started
						}
						
					});
				}
				
			} else {
				//Discard it
			}
		}
		
		setTimeout(possibleSearch, 800);
	}
}
