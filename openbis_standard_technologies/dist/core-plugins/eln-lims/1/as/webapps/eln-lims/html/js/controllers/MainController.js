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
	
	this.openbisV1 = new openbis();
	this.openbisV3 = null;
	
	
	this.loadV3 = function(callback) {
		require(['openbis'], function(openbis) {
			//Boilerplate
			var testProtocol = window.location.protocol;
			var testHost = window.location.hostname;
			var testPort = window.location.port;
			
			var testUrl = testProtocol + "//" + testHost + ":" + testPort;
			var testApiUrl = testUrl + "/openbis/openbis/rmi-application-server-v3.json";
			
			mainController.openbisV3 = new openbis(testApiUrl);
			mainController.openbisV3._private.sessionToken = mainController.serverFacade.getSession();
			callback();
		});
	};
	
	// Server Facade Object
	this.serverFacade = new ServerFacade(this.openbisV1); //Client APP Facade, used as control point to know what is used and create utility methods.

	// Configuration
	this.profile = profile;
	this.profile.serverFacade = this.serverFacade;
	FormUtil.profile = this.profile;
	
	// Attributes - Widgets typically hold both the model and the view, they are here so they can be accessed by inline HTML/Javascript when needed.
	
	// Controllers currently being used
	this.sideMenu = null;
	this.currentView = null;
	
	// Views currently being displayed
	this.views = {
			menu : null,
			header : null,
			content : null,
			auxContent : null
	}
	
	//Refresh Functionality
	this.lastViewChange = null;
	this.lastArg = null;
	this.refreshView = function() {
		this.changeView(this.lastViewChange, this.lastArg);
	}
	
	//Functionality to keep state
	this.backStack = [];
	
	//
	// Validates and enters the app
	//
	
	this.enterApp = function(data, username, password) {
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
		var _this = this;
		this.backButtonLogic = function(e) {
			if(	this.currentView && 
				this.currentView.finalize) {
				this.currentView.finalize();
			}
			
			var toPop = null;
			if(this.backStack.length > 0) {
				toPop = this.backStack.pop();
			}
			
			if(toPop && toPop.view !== null) {
				this.views.header = toPop.view.header;
				this.views.content = toPop.view.content;
				this.views.auxContent = toPop.view.auxContent;
				
				LayoutManager.reloadView(this.views);
			} else {
				var queryString = Util.queryString();
				var viewName = queryString.viewName;
				var viewData = queryString.viewData
				if(viewName && viewData) {
					localReference._changeView(viewName, viewData, false, false);
				}
			}
		}
		
		window.addEventListener("popstate", function(e) {
			_this.backButtonLogic(e);
		}); 
		
		//
		// Start App if credentials are ok
		//
		$('body').removeClass('bodyLogin');
		$("#login-form-div").hide();
		$("#main").show();
		
		//Get Metadata from all sample types before showing the main menu
		this.loadV3(function() {
			_this.serverFacade.listSampleTypes (
					function(result) {
						//Load Sample Types
						localReference.profile.allSampleTypes = result.result;
						
						//Load datastores for automatic DSS configuration, the first one will be used
						localReference.serverFacade.listDataStores(function(dataStores) {
								localReference.profile.allDataStores = dataStores.result;
								
								var nextInit = function() {
									//Load display settings
									localReference.serverFacade.getUserDisplaySettings( function(response) {
										if(response.result) {
											localReference.profile.displaySettings = response.result;
										}
										
										//Load Experiment Types
										localReference.serverFacade.listExperimentTypes(function(experiments) {
											localReference.profile.allExperimentTypes = experiments.result;
											
											
											//Init profile
											var startAppFunc = function() {
												//Start App
												localReference.sideMenu = new SideMenuWidgetController(localReference);
												localReference.views.menu = $("<div>");
												localReference.sideMenu.init(localReference.views.menu, function() {
													//Page reload using the URL info
													var queryString = Util.queryString();
													var menuUniqueId = queryString.menuUniqueId;
													var viewName = queryString.viewName;
													var viewData = queryString.viewData;
													var hideMenu = queryString.hideMenu;
													
													LayoutManager.reloadView(localReference.views);
													if(viewName && viewData) {
														localReference.sideMenu.moveToNodeId(menuUniqueId);
														localReference.changeView(viewName, viewData);
													} else {
														localReference.changeView("showBlancPage", null);
													}
													
													Util.unblockUI();
												});
											};
											
											localReference.profile.init(startAppFunc);
										});
									});
								}
								
								nextInit();
						});
					}
				);
		});
	}
	
	//
	// Main View Changer - Everything on the application rely on this method to alter the views, arg should be a string
	//
	this.changeView = function(newViewChange, arg, shouldStateBePushToHistory) {
		this._changeView(newViewChange, arg, true, shouldStateBePushToHistory);
	}
	this._changeView = function(newViewChange, arg, shouldURLBePushToHistory, shouldStateBePushToHistory) {
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
		// Finalize view, used to undo mayor modifications to how layout and events are handled
		//
		
		if(	this.currentView && 
			this.currentView.finalize) {
			this.currentView.finalize();
		}
		//
		//
		//
		
		try {
			switch (newViewChange) {
				case "showExportTreePage":
					document.title = "Export Builder";
					var newView = new ExportTreeController(this);
					var content = this._getBackwardsCompatibleMainContainer();
					newView.init(content);
					this.currentView = newView;
					//window.scrollTo(0,0);
					break;
				case "showLabNotebookPage":
					document.title = "Lab Notebook";
					var newView = new LabNotebookController(this);
					var content = this._getBackwardsCompatibleMainContainer();
					newView.init(content);
					this.currentView = newView;
					//window.scrollTo(0,0);
					break;
				case "showInventoryPage":
					document.title = "Inventory";
					var newView = new InventoryController(this);
					var content = this._getBackwardsCompatibleMainContainer();
					newView.init(content);
					this.currentView = newView;
					//window.scrollTo(0,0);
					break;
				case "showAdvancedSearchPage":
					document.title = "Advanced Search";
					var argToUse = null;
					try {
						var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
						argToUse = JSON.parse(cleanText);
					} catch(err) {
						argToUse = arg;
					}
					this._showAdvancedSearchPage(argToUse);
					//window.scrollTo(0,0);
					break;
				case "showUserManagerPage":
					document.title = "User Manager";
					this._showUserManager();
					//window.scrollTo(0,0);
					break;
				case "showVocabularyManagerPage":
					document.title = "Vocabulary Browser";
					this._showVocabularyManager();
					//window.scrollTo(0,0);
					break;
				case "showTrashcanPage":
					document.title = "Trashcan";
					this._showTrashcan();
					//window.scrollTo(0,0);
					break;
				case "showStorageManager":
					document.title = "Storage Manager";
					this._showStorageManager();
					//window.scrollTo(0,0);
					break;
				case "showBlancPage":
					document.title = "Main Menu";
					this._showBlancPage();
					break;
				case "showSearchPage":
					document.title = "Search";
					var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
					var argsMap = JSON.parse(cleanText);
					var searchText = argsMap["searchText"];
					var searchDomain = argsMap["searchDomain"];
					var searchDomainLabel = argsMap["searchDomainLabel"];
					this._showSearchPage(searchText, searchDomain, searchDomainLabel);
					//window.scrollTo(0,0);
					break;
				case "showSpacePage":
					var _this = this;
					this.serverFacade.getSpaceFromCode(arg, function(space) {
						document.title = "Space " + space.code;
						_this._showSpacePage(space);
						//window.scrollTo(0,0);
					});
					break;
				case "showProjectPageFromIdentifier":
					var _this = this;
					this.serverFacade.getProjectFromIdentifier(arg, function(project) {
						document.title = "Project " + project.code;
						_this._showProjectPage(project);
						//window.scrollTo(0,0);
					});
					break;
				case "showProjectPageFromPermId":
					var _this = this;
					this.serverFacade.getProjectFromPermId(arg, function(project) {
						document.title = "Project " + project.code;
						_this._showProjectPage(project);
						//window.scrollTo(0,0);
					});
					break;
				case "showEditProjectPageFromPermId":
					var _this = this;
					this.serverFacade.getProjectFromPermId(arg, function(project) {
						document.title = "Project " + project.code;
						_this._showEditProjectPage(project);
						//window.scrollTo(0,0);
					});
					break;
				case "showCreateProjectPage":
					document.title = "Create Project";
					this._showCreateProjectPage(arg);
					//window.scrollTo(0,0);
					break;
				case "showCreateExperimentPage":
					var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
					var argsMap = JSON.parse(cleanText);
					var experimentTypeCode = argsMap["experimentTypeCode"];
					var projectIdentifier = argsMap["projectIdentifier"];
					
					document.title = "Create " + ELNDictionary.getExperimentKindName(projectIdentifier) + " " + experimentTypeCode;
					var experiment = {
							experimentTypeCode : experimentTypeCode,
							identifier : projectIdentifier
					}
					this._showExperimentPage(experiment, FormMode.CREATE);
					//window.scrollTo(0,0);
					break;
				case "showExperimentPageFromIdentifier":
					var _this = this;
					this.serverFacade.listExperimentsForIdentifiers([arg], function(data) {
						document.title = "" + ELNDictionary.getExperimentKindName(arg) + " " + arg;
						_this._showExperimentPage(data.result[0], FormMode.VIEW);
						//window.scrollTo(0,0);
					});
					break;
				case "showCreateDataSetPageFromExpPermId":
					var _this = this;
					var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : arg } };
					var experimentCriteria = { entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules };
					this.serverFacade.searchForExperimentsAdvanced(experimentCriteria, null, function(data) {
						document.title = "Create Data Set for " + data.objects[0].code;
						_this._showCreateDataSetPage(data.objects[0]);
						//window.scrollTo(0,0);
					});
					break;
				case "showEditExperimentPageFromIdentifier":
					var _this = this;
					this.serverFacade.listExperimentsForIdentifiers([arg], function(data) {
						document.title = "" + ELNDictionary.getExperimentKindName(arg) + " " + arg;
						_this._showExperimentPage(data.result[0], FormMode.EDIT);
						//window.scrollTo(0,0);
					});
					break;
				case "showCreateSubExperimentPage":
					var cleanText = decodeURIComponent(arg); //If the JSON is written on the URL we need to clean special chars
					var argsMap = JSON.parse(cleanText);
					var sampleTypeCode = argsMap["sampleTypeCode"];
					var experimentIdentifier = argsMap["experimentIdentifier"];
					document.title = "Create " + ELNDictionary.Sample + " " + arg;
					this._showCreateSubExperimentPage(sampleTypeCode, experimentIdentifier);
					//window.scrollTo(0,0);
					break;
				case "showSamplesPage":
					document.title = "" + ELNDictionary.Sample + " Browser";
					this._showSamplesPage(arg);
					//window.scrollTo(0,0);
					break;
				case "showSampleHierarchyPage":
					document.title = "Hierarchy " + arg;
					this._showSampleHierarchyPage(arg);
					//window.scrollTo(0,0);
					break;
				case "showSampleHierarchyTablePage":
					document.title = "Table Hierarchy " + arg;
					this._showSampleHierarchyTablePage(arg);
					//window.scrollTo(0,0);
					break;
				case "showEditSamplePageFromPermId":
					var _this = this;
					this.serverFacade.searchWithUniqueId(arg, function(data) {
						if(!data[0]) {
							window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
						} else {
							document.title = "" + ELNDictionary.Sample + " " + data[0].code;
							var isELNSubExperiment = $.inArray(data[0].spaceCode, _this.profile.inventorySpaces) === -1 && _this.profile.inventorySpaces.length > 0;
							_this._showEditSamplePage(data[0], isELNSubExperiment);
							//window.scrollTo(0,0);
						}
					});
					break;
				case "showViewSamplePageFromPermId":
					var _this = this;
					this.serverFacade.searchWithUniqueId(arg, function(data) {
						if(!data[0]) {
							window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
						} else {
							document.title = "" + ELNDictionary.Sample + " " + data[0].code;
							var isELNSubExperiment = $.inArray(data[0].spaceCode, _this.profile.inventorySpaces) === -1&& _this.profile.inventorySpaces.length > 0;
							_this._showViewSamplePage(data[0], isELNSubExperiment);
							//window.scrollTo(0,0);
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
							//window.scrollTo(0,0);
						}
					});
					break;
				case "showViewDataSetPageFromPermId":
					var _this = this;
					this.serverFacade.searchDataSetWithUniqueId(arg, function(dataSetData) {
						if(!dataSetData.result || !dataSetData.result[0]) {
							window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
						} else {
							if(dataSetData.result[0].sampleIdentifierOrNull) {
								_this.serverFacade.searchWithIdentifiers([dataSetData.result[0].sampleIdentifierOrNull], function(sampleData) {
									document.title = "Data Set " + dataSetData.result[0].code;
									_this._showViewDataSetPage(sampleData[0], dataSetData.result[0]);
									//window.scrollTo(0,0);
								});
							} else if(dataSetData.result[0].experimentIdentifier) {
								_this.serverFacade.listExperimentsForIdentifiers([dataSetData.result[0].experimentIdentifier], function(experimentResults) {
									var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : experimentResults.result[0].permId } };
									var experimentCriteria = { entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules };
									_this.serverFacade.searchForExperimentsAdvanced(experimentCriteria, null, function(experimentData) {
										document.title = "Data Set " + dataSetData.result[0].code;
										_this._showViewDataSetPage(experimentData.objects[0], dataSetData.result[0]);
										//window.scrollTo(0,0);
									});
								});
							}
						}
					});
					break;
				case "showEditDataSetPageFromPermId":
					var _this = this;
					this.serverFacade.searchDataSetWithUniqueId(arg, function(dataSetData) {
						if(!dataSetData.result || !dataSetData.result[0]) {
							window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
						} else {
							_this.serverFacade.searchWithIdentifiers([dataSetData.result[0].sampleIdentifierOrNull], function(sampleData) {
								document.title = "Data Set " + dataSetData.result[0].code;
								_this._showEditDataSetPage(sampleData[0], dataSetData.result[0]);
								//window.scrollTo(0,0);
							});
						}
					});
					break;
				case "showDrawingBoard":
					var _this = this;
					document.title = "Drawing board";
					_this._showDrawingBoard();
					//window.scrollTo(0,0);
					break;
				case "showAbout":
					$.get('version.txt', function(data) {
						Util.showInfo("Current Version: " + data);
					}, 'text');
					break;
				default:
					window.alert("The system tried to create a non existing view");
					break;
			}
		} catch(err) {
			Util.manageError(err);
		}
		

		
		//
		// Permanent URLs
		//
		if (shouldURLBePushToHistory) {
			var menuUniqueId = this.sideMenu.getCurrentNodeId();
			var url = Util.getURLFor(menuUniqueId, newViewChange, arg);
			history.pushState(null, "", url); //History Push State
			
			var toPush = null;
			if(shouldStateBePushToHistory) {
				toPush = {
						header : null,
						content : null,
						auxContent : null
				}
				
				if(this.views.header) {
					toPush.header = this.views.header.children();
					toPush.header.detach();
				}
				
				if(this.views.content) {
					toPush.content = this.views.content.children();
					toPush.content.detach();
				}
				
				if(this.views.auxContent) {
					toPush.auxContent = this.views.auxContent.children();
					toPush.auxContent.detach();
				}
			}
			
			this.backStack.push({
				view : toPush,
				url : url
			});
		}
		
		//
		// Refresh Functionality
		//
		this.lastViewChange = newViewChange;
		this.lastArg = arg;
	}
	
	//
	// Functions that trigger view changes, should only be called from the main controller changeView method
	//
	this._getBackwardsCompatibleMainContainer = function(id) {
		var content = $("<div>");
		content.css("padding", "10px");
		
		if(id) {
			content.attr("id", id);
		}
		
		this.views.header = null;
		this.views.content = content;
		this.views.auxContent = null;
		LayoutManager.reloadView(this.views);
		
		return content;
	}
	
	this._getNewViewModel = function(withHeaderOrHeaderId, withContentOrContentId, withAuxContentOrAuxContentId) {
		var header = null;
		var content = null;
		var auxContent = null;
		
		if(withHeaderOrHeaderId) {
			header = $("<div>");
			header.css({ 
				"padding" : "10px",
				"background-color" : "rgb(248, 248, 248)"
			});
			
			if((typeof withHeaderOrHeaderId === 'string' || withHeaderOrHeaderId instanceof String)) {
				header.attr("id", withHeaderOrHeaderId);
			}
		}
		
		if(withContentOrContentId) {
			content = $("<div>");
			content.css("padding", "10px");
			if((typeof withContentOrContentId === 'string' || withContentOrContentId instanceof String)) {
				content.attr("id", withContentOrContentId);
			}
		}
		
		if(withAuxContentOrAuxContentId) {
			auxContent = $("<div>");
			if((typeof withContentAuxOrContentAuxId === 'string' || withAuxContentOrAuxContentId instanceof String)) {
				auxContent.attr("id", withAuxContentOrAuxContentId);
			}
		}
		
		this.views.header = header;
		this.views.content = content;
		this.views.auxContent = auxContent;
		
		LayoutManager.reloadView(this.views);
		
		var modificableViews = {
				header : this.views.header,
				content : this.views.content,
				auxContent : this.views.auxContent
		};
		
		return modificableViews;
	}
	
	this._showStorageManager = function() {
		var content = this._getBackwardsCompatibleMainContainer();
		
		var storageManagerController = new StorageManagerController(this);
		storageManagerController.init(content);
		this.currentView = storageManagerController;
	}
	
	this._showVocabularyManager = function() {
		var content = this._getBackwardsCompatibleMainContainer();
		
		var vocabularyManagerController = new VocabularyManagerController(this);
		vocabularyManagerController.init(content);
		this.currentView = vocabularyManagerController;
	}
	
	this._showBlancPage = function() {
		var content = this._getBackwardsCompatibleMainContainer();
		content.append("Welcome to openBIS ELN-LIMS.");
	}
	
	this._showDrawingBoard = function() {
		var content = this._getBackwardsCompatibleMainContainer();
		
		var drawingBoardsController = new DrawingBoardsController(this);
		drawingBoardsController.init(content);
		this.currentView = drawingBoardsController;
	}
	
	this._showUserManager = function() {
		var content = this._getBackwardsCompatibleMainContainer();
		
		var userManagerController = new UserManagerController(this);
		userManagerController.init(content);
		this.currentView = userManagerController;
	}
	
	this._showSamplesPage = function(experimentIdentifier) {
		var content = this._getBackwardsCompatibleMainContainer();
		
		var sampleTableController = null;
		
		if(experimentIdentifier === "null") { //Fix for reloads when there is text on the url
			experimentIdentifier = null;
		}
		
		if(experimentIdentifier) {
			var _this = this;
			this.serverFacade.listExperimentsForIdentifiers([experimentIdentifier], function(data) {
				sampleTableController = new SampleTableController(this, "" + ELNDictionary.getExperimentKindName(experimentIdentifier) + " " + experimentIdentifier, experimentIdentifier, null, null, data.result[0]);
				sampleTableController.init(content);
				_this.currentView = sampleTableController;
			});
		} else {
			sampleTableController = new SampleTableController(this, "" + ELNDictionary.Sample + " Browser", null);
			sampleTableController.init(content);
			this.currentView = sampleTableController;
		}
		
	}

	this._showSampleHierarchyPage = function(permId) {
		//Show View
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(permId, function(data) {
			var content = localInstance._getBackwardsCompatibleMainContainer("container-sample-hierarchy");
			var sampleHierarchy = new SampleHierarchy(localInstance.serverFacade, "container-sample-hierarchy", localInstance.profile, data[0]);
			sampleHierarchy.init();
			localInstance.currentView = sampleHierarchy;
		});
	}
	
	this._showSampleHierarchyTablePage = function(permId) {
		//Show View
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(permId, function(data) {
			var content = localInstance._getBackwardsCompatibleMainContainer();
			var sampleHierarchyTableController = new SampleHierarchyTableController(this, data[0]);
			sampleHierarchyTableController.init(content);
			localInstance.currentView = sampleHierarchyTableController;
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
		var views = this._getNewViewModel(true, true, false);
		sampleFormController.init(views);
	}
	
	this._showTrashcan = function() {
		var trashcanController = new TrashManagerController(this);
		this.trashcanController = trashcanController;
		var content = this._getBackwardsCompatibleMainContainer();
		trashcanController.init(content);
	}
	
	this._showViewSamplePage = function(sample, isELNSubExperiment) {
		//Show Form
		var sampleFormController = new SampleFormController(this, FormMode.VIEW, sample);
		this.currentView = sampleFormController;
		var views = this._getNewViewModel(true, true, true);
		sampleFormController.init(views);
	}
	
	this._showEditSamplePage = function(sample, isELNSubExperiment) {
		//Show Form
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(sample.permId, function(data) {
			var sampleFormController = new SampleFormController(localInstance, FormMode.EDIT, data[0]);
			localInstance.currentView = sampleFormController;
			var views = localInstance._getNewViewModel(true, true, false);
			sampleFormController.init(views);
		});
	}
	
	this._showSpacePage = function(space) {
		//Show Form
		var spaceFormController = new SpaceFormController(this, space);
		var content = this._getBackwardsCompatibleMainContainer();
		spaceFormController.init(content);
		this.currentView = spaceFormController;
	}
	
	this._showCreateProjectPage = function(spaceCode) {
		//Show Form
		var projectFormController = new ProjectFormController(this, FormMode.CREATE, {spaceCode : spaceCode});
		var views = this._getNewViewModel(true, true, false);
		projectFormController.init(views);
		this.currentView = projectFormController;
	}
	
	this._showProjectPage = function(project) {
		//Show Form
		var projectFormController = new ProjectFormController(this, FormMode.VIEW, project);
		var views = this._getNewViewModel(true, true, false);
		projectFormController.init(views);
		this.currentView = projectFormController;
	}
	
	this._showEditProjectPage = function(project) {
		//Show Form
		var projectFormController = new ProjectFormController(this, FormMode.EDIT, project);
		var views = this._getNewViewModel(true, true, false);
		projectFormController.init(views);
		this.currentView = projectFormController;
	}
	
	this._showExperimentPage = function(experiment, mode) {
		//Show Form
		var experimentFormController = new ExperimentFormController(this, mode, experiment);
		var views = this._getNewViewModel(true, true, mode === FormMode.VIEW);
		experimentFormController.init(views);
		this.currentView = experimentFormController;
	}
	
	this._showCreateDataSetPage = function(entity) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.CREATE, entity, null);
		var content = this._getBackwardsCompatibleMainContainer();
		newView.init(content);
		this.currentView = newView;
	}
	
	this._showViewDataSetPage = function(sample, dataset) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.VIEW, sample, dataset);
		var content = this._getBackwardsCompatibleMainContainer();
		newView.init(content);
		this.currentView = newView;
	}
	
	this._showEditDataSetPage = function(sample, dataset) {
		//Show Form
		var newView = new DataSetFormController(this, FormMode.EDIT, sample, dataset);
		var content = this._getBackwardsCompatibleMainContainer();
		newView.init(content);
		this.currentView = newView;
	}
	
	this._showAdvancedSearchPage = function(freeText) {
		//Show Form
		var newView = null;
		
		if(freeText) {
			newView = new AdvancedSearchController(this, freeText);
		} else {
			newView = new AdvancedSearchController(this);
		}
		
		var content = this._getBackwardsCompatibleMainContainer();
		newView.init(content);
		if(freeText) {
			newView.search();
		}
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
					
					$("#search").addClass("search-query-searching");
					if(!searchDomain || searchDomain === profile.getSearchDomains()[0].name) { //Global Search
						if(profile.searchSamplesUsingV3OnDropbox) {
							localReference._legacyGlobalSearch(value);
						} else {
							$("#search").removeClass("search-query-searching");
							localReference.changeView("showAdvancedSearchPage", value);
						}
					} else if(searchDomain == "data-set-file-search") { 
						localReference.serverFacade.searchOnSearchDomain(searchDomain, value, function(data) {
							
							if(localSearchId === localReference.lastSearchId) {
								$("#search").removeClass("search-query-searching");
								
								var columns = [ {
									label : 'Entity Kind',
									property : 'entityKind',
									sortable : true
								}, {
									label : 'Entity Type',
									property : 'entityType',
									sortable : true
								}, {
									label : 'Code',
									property : 'code',
									sortable : true
								}, {
									label : 'Path',
									property : 'pathInDataSet',
									sortable : true
								}];
								
								var getDataList = function(callback) {
									var dataList = [];
									if(data.result) {
										for(var i = 0; i < data.result.length; i++) {
											var result = data.result[i];
											
											dataList.push({
												entityKind : result.resultLocation.entityKind,
												entityType : result.resultLocation.entityType,
												permId : result.resultLocation.permId,
												code : result.resultLocation.code,
												pathInDataSet : result.resultLocation.pathInDataSet
											});
										}
									}
									callback(dataList);
								};
								
								var rowClick = function(e) {
									switch(e.data.entityKind) {
										case "DATA_SET":
											mainController.changeView('showViewDataSetPageFromPermId', e.data.permId);
											break;
									}
								}
								
								var dataGrid = new DataGridController(searchDomainLabel + " Search Results", columns, [], null, getDataList, rowClick, true, "SEARCH_" + searchDomainLabel);
								localReference.currentView = dataGrid;
								var content = localReference._getBackwardsCompatibleMainContainer();
								dataGrid.init(content);
								history.pushState(null, "", ""); //History Push State
							}
						});
					} else {
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
										label : 'Found in',
										property : 'location',
										sortable : true
									}, {
										label : 'Sequence (Start - End)',
										property : 'sequenceStartEnd',
										sortable : true
									}, {
										label : 'Query (Start - End)',
										property : 'queryStartEnd',
										sortable : true
									}, {
										label : 'No. Mismatches',
										property : 'numberOfMismatches',
										sortable : true
									}, {
										label : 'No. Gaps',
										property : 'totalNumberOfGaps',
										sortable : true
									}, {
										label : 'E-value',
										property : 'evalue',
										sortable : true
									}, {
										label : 'Score',
										property : 'score',
										sortable : true
									}, {
										label : 'Bit Score',
										property : 'bitScore',
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
													code += "<br> " + ELNDictionary.Sample + ": " + getSampleIdentifierForDataSetCode(resultLocation.code);
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
									
									var dataGrid = new DataGridController(searchDomainLabel + " Search Results", columns, [], null, getDataList, rowClick, true, "SEARCH_" + searchDomainLabel);
									localReference.currentView = dataGrid;
									var content = localReference._getBackwardsCompatibleMainContainer();
									dataGrid.init(content);
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
	
	this._legacyGlobalSearch = function(value) {
		var localReference = this;
		localReference.serverFacade.searchWithText(value, function(data) {
			$("#search").removeClass("search-query-searching");
			
			var columns = [ {
				label : 'Code',
				property : 'code',
				sortable : true
			}, {
				label : 'Score',
				property : 'score',
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
				label : 'Matched',
				property : 'matched',
				sortable : true,
				filter : function(data, filter) {
					var matchedValue = data.matched.text();
					return matchedValue.toLowerCase().indexOf(filter) !== -1;
				},
				sort : function(data1, data2, asc) {
					var value1 = data1.matched.text();
					var value2 = data2.matched.text();
					var sortDirection = (asc)? 1 : -1;
					return sortDirection * naturalSort(value1, value2);
				}
			}];
			columns.push(SampleDataGridUtil.createOperationsColumn());
			
			var getDataList = function(callback) {
				var dataList = [];
				var words = value.split(" ");
				var searchRegexpes = [];
				for(var sIdx = 0; sIdx < words.length; sIdx++) {
					var word = words[sIdx];
					searchRegexpes[sIdx] = new RegExp(word, "i");
				}
				
				var addMatchedPairs = function(matchedPairs, fieldName, fieldValue) {
					for(var tIdx = 0; tIdx < searchRegexpes.length; tIdx++) {
						if(searchRegexpes[tIdx].test(fieldValue)) {
							var match = {};
							match.name = fieldName;
							match.found = words[tIdx];
							match.value = fieldValue;
							matchedPairs.push(match);
						}
					}
				}
				
				for(var i = 0; i < data.length; i++) {
					var matchedPairs = [];
					var sample = data[i];
					
					addMatchedPairs(matchedPairs, "Code", sample.code); //Check Code
					addMatchedPairs(matchedPairs, "Sample Type", sample.sampleTypeCode); //Check Type
					
					//Check Properties
					for (propertyName in sample.properties) {
						var propertyValue = sample.properties[propertyName];
						addMatchedPairs(matchedPairs, "Property " + propertyName, propertyValue); //Check Properties
					}
					
					//Check date fields
					var regEx = /\d{4}-\d{2}-\d{2}/g;
					var match = value.match(regEx);
					if(match && match.length === 1) {
						var registrationDateValue = Util.getFormatedDate(new Date(sample.registrationDetails.registrationDate));
						if(registrationDateValue.indexOf(match[0]) !== -1) {
							matchedPairs.push({ name : "Registration Date", value : registrationDateValue, found : match[0]});
						}
						var modificationDateValue = Util.getFormatedDate(new Date(sample.registrationDetails.modificationDate));
						if(modificationDateValue.indexOf(match[0]) !== -1) {
							matchedPairs.push({ name : "Modification Date", value : modificationDateValue, found : match[0]});
						}
					}
					
					var $container = $("<p>");
					var score = 0;
					for(var mIdx = 0; mIdx < matchedPairs.length; mIdx++) {
						switch(matchedPairs[mIdx].name) {
							case "Code":
								score+= 1000;
								break;
							case "Sample Type":
								score+= 100;
								break;
							default:
								score+= 10;
								break;
							break;
						}
						if(mIdx < 0) {
							$container.append($("<br>"));
						}
						$container.append($("<p>").append($("<strong>").append(matchedPairs[mIdx].name + ": ")).append("Found \"" + matchedPairs[mIdx].found + "\" in \"" + matchedPairs[mIdx].value + "\""));
					}
					
					//properties
					dataList.push({
						permId : sample.permId,
						code : sample.code,
						score : score,
						sampleTypeCode : sample.sampleTypeCode,
						matched : $container
					});
				}
				
				dataList = dataList.sort(function(e1, e2) { 
					return e2.score - e1.score; 
				});
				callback(dataList);
			};
			
			var rowClick = function(e) {
				mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
			}
			
			var dataGrid = new DataGridController("Search Results", columns, [], null, getDataList, rowClick, true, "SEARCH_OPENBIS");
			localReference.currentView = dataGrid;
			var content = localReference._getBackwardsCompatibleMainContainer();
			dataGrid.init(content);
			history.pushState(null, "", ""); //History Push State
		});
	}
}
