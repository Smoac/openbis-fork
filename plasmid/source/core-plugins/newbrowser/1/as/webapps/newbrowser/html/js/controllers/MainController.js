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
	this.navigationBar = null; //Top Bar, typically h
	
	//Others
	this.currentView = null;
	
	//
	// Validates and enters the app
	//
	
	this.enterApp = function(data) {
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
		// Start App if credentials are ok
		//
		$('body').removeClass('bodyLogin');
		$("#login-form-div").hide();
		$("#main").show();
	
		//Get Metadata from all sample types before showing the main menu
		
		var localReference = this;
		this.serverFacade.listSampleTypes(
			function(result) {
			
				//Load Sample Types
				localReference.profile.allTypes = result.result;
			
				//Init profile
				localReference.profile.init();
			
				//Start App
				localReference.inspector = new Inspector(localReference.serverFacade, "mainContainer", localReference.profile);
				localReference.navigationBar = new NavigationBar(localReference, "sectionsContainer", null, localReference.profile.inventoryStructure);
				localReference.navigationBar.repaint();
			
				localReference.changeView("showMainMenu", null);
				Util.unblockUI();
				
				var openNewSampleTab = Util.queryString.samplePermId;
				
				if(openNewSampleTab) {
					localReference.changeView("showViewSamplePageFromPermId", openNewSampleTab);
				}
				//Get datastores for automatic DSS configuration, the first one will be used
				localReference.serverFacade.listDataStores(
					function(dataStores) {
						localReference.profile.allDataStores = dataStores.result;
					}
				);
				
				//Get display settings
				localReference.serverFacade.getUserDisplaySettings( function(response) {
					if(response.result) {
						localReference.profile.displaySettings = response.result;
					}
				});
			}
		);
	}
	
	//
	// Main View Changer - Everything on the application rely on this method to alter the views
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
			case "showInspectors":
				document.title = "Show Inspectors";
				this._showInspectors();
				break;
			case "showMainMenu":
				document.title = "Main Menu";
				this._showMainMenu();
				break;
			case "showSearchPage":
				document.title = "Search";
				this._showSearchPage(arg);
				break;
			case "showSamplesPage":
				document.title = "Samples " + arg;
				this._showSamplesPage(arg);
				break;
			case "showSampleHierarchyPage":
				document.title = "Hierarchy " + arg;
				this._showSampleHierarchyPage(arg);
				break;
			case "showCreateSamplePage":
				document.title = "Create Sample " + arg;
				this._showCreateSamplePage(arg);
				break;
			case "showEditSamplePage":
				document.title = arg;
				this._showEditSamplePage(arg);
				break;
			case "showViewExperiment":
				var _this = this;
				this.serverFacade.getELNExperimentSampleIdForExperiment(arg, function(permId) {
					_this.changeView("showViewSamplePageFromPermId", permId);
				});
				break;
			case "showViewSamplePageFromPermId":
				var _this = this;
				this.serverFacade.searchWithUniqueId(arg, function(data) {
					if(!data[0]) {
						window.alert("The item is no longer available, refresh the page, if the problem persists tell your admin that the Lucene index is probably corrupted.");
					} else {
						document.title = data[0].code;
						_this._showViewSamplePage(data[0]);
					}
				});
				break;
			case "showCreateDataSetPage":
				document.title = "Create Data Set for " + arg;
				this._showCreateDataSetPage(arg);
				break;
			default:
				window.alert("The system tried to create a non existing view");
				break;
		}
	}
	
	//
	// Functions that trigger view changes, should only be called from the main controller changeView method
	//
	this._showInspectors = function() {
		//Update menu
		this.navigationBar.updateMenu(null);
		
		//Show Inspectors
		this.inspector.repaint();
		
		history.pushState(null, "", ""); //History Push State
	}
	
	this._showMainMenu = function() {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('main-menu', 'showMainMenu', null, 'Main Menu');
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Main menu
		var mainMenu = new MainMenu(this, "mainContainer", this.profile.inventoryStructure, this.profile.mainMenuContentExtra());
		mainMenu.init();
		
		this.currentView = mainMenu;
		history.pushState(null, "", ""); //History Push State
	}
	
	this._showSamplesPage = function(sampleTypeCode) {
		//Update menu
		var sampleType = this.profile.getTypeForTypeCode(sampleTypeCode);
		var sampleTypeDisplayName = sampleType.description;
		if(sampleTypeDisplayName === null) {
			sampleTypeDisplayName = sampleTypeCode;
		}

		var breadCrumbPage = new BreadCrumbPage("sample-table", "showSamplesPage", sampleTypeCode, sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Sample Table
		var sampleTable = new SampleTable(this.serverFacade, "mainContainer", this.profile, sampleTypeCode, true, true, false, false, false, this.inspector);
		sampleTable.init();
		
		this.currentView = sampleTable;
		history.pushState(null, "", ""); //History Push State
	}

	this._showSampleHierarchyPage = function(permId) {
		
		//Show View
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(permId, function(data) {
			var breadCrumbPage = new BreadCrumbPage('sample-hierarchy', "showSampleHierarchyPage", data[0].permId, 'Hierarchy '+data[0].code);
			localInstance.navigationBar.updateBreadCrumbPage(breadCrumbPage);
			
			var sampleHierarchy = new SampleHierarchy(localInstance.serverFacade, localInstance.inspector, "mainContainer", localInstance.profile, data[0]);
			sampleHierarchy.init();
			localInstance.currentView = sampleHierarchy;
			history.pushState(null, "", ""); //History Push State
		});
	}
	
	this._showCreateSamplePage = function(sampleTypeCode) {
		//Update menu
		var sampleTypeDisplayName = this.profile.getTypeForTypeCode(sampleTypeCode).description;
		if(sampleTypeDisplayName === null) {
			sampleTypeDisplayName = sampleTypeCode;
		}
		var breadCrumbPage = new BreadCrumbPage('new-sample', "showCreateSamplePage", sampleTypeCode, 'Create '+sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sampleTypeCode);
		var sampleForm = new SampleForm(this.serverFacade, this.inspector, "mainContainer", this.profile, sampleTypeCode, isELNExperiment, SampleFormMode.CREATE, null);
		sampleForm.init();
		this.currentView = sampleForm;
		history.pushState(null, "", ""); //History Push State
	}

	this._showEditSamplePage = function(sample) {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('edit-sample', "showEditSamplePage", sample, 'Update '+sample.code);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var localInstance = this;
		this.serverFacade.searchWithUniqueId(sample.permId, function(data) {
			var isELNExperiment = localInstance.profile.isELNExperiment(data[0].sampleTypeCode);
			var sampleForm = new SampleForm(localInstance.serverFacade, localInstance.inspector, "mainContainer", localInstance.profile, data[0].sampleTypeCode, isELNExperiment, SampleFormMode.EDIT, data[0]);
			sampleForm.init();
			localInstance.currentView = sampleForm;
			history.pushState(null, "", ""); //History Push State
		});
	}

	this._showViewSamplePage = function(sample) {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('view-sample', "showViewSamplePageFromPermId", sample.permId, 'View '+ sample.code);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
			
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sample.sampleTypeCode);
		var sampleForm = new SampleForm(this.serverFacade, this.inspector, "mainContainer", this.profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.VIEW, sample);
		sampleForm.init();
		this.currentView = sampleForm;
		history.pushState(null, "", ""); //History Push State
	}
	
	this._showCreateDataSetPage = function(sample) {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('new-dataset', "showCreateDataSetPage", sample, 'Create Data Set for '+sample.code);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var datasetForm = new DataSetForm(this.serverFacade, "mainContainer", this.profile, sample, DataSetFormMode.VIEW);
		datasetForm.init();
		this.currentView = datasetForm;
		history.pushState(null, "", ""); //History Push State
	}
	
	this.lastSearchId = 0; //Used to discard search responses that don't pertain to the last search call.
	
	this._showSearchPage = function(event) {
		//Only search with at least 3 characters
		if(event.target.value.length < 3) {
			return;
		}
		
		this.lastSearchId++;
		var localSearchId = this.lastSearchId;
		var localReference = this;
		
		var possibleSearch = function() {
			if(localSearchId === localReference.lastSearchId) { //Trigger it if no new have started
				//Clean page and update menu
				localReference.navigationBar.updateMenu(null);
				
				//Update Main Container
				var sampleTable = new SampleTable(localReference.serverFacade, "mainContainer", localReference.profile, localReference.profile.searchType["TYPE"], true, false, false, true, false, localReference.inspector);
				$("#search").addClass("search-query-searching");
				localReference.serverFacade.searchWithText(event.target.value, function(data) {
					if(localSearchId === localReference.lastSearchId) {
						$("#search").removeClass("search-query-searching");
						sampleTable.reloadWithSamples(data);
						localReference.currentView = sampleTable;
						Util.unblockUI();
						history.pushState(null, "", ""); //History Push State
					} else {
						//Discard old response, was triggered but a new one was started
					}
				});
			} else {
				//Discard it
			}
		}
		
		setTimeout(possibleSearch, 800);
	}
}
