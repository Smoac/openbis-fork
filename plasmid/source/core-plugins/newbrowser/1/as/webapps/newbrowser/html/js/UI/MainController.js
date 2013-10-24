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
	
	// Atributes - Widgets typically hold both the model and the view, they are here so they can be accessed by inline HTML/Javascript when needed.
	this.inspector = null; // The samples that are currently being inspected
	this.navigationBar = null; //Top Bar
	this.sampleTable = null; //Table that holds the samples
	this.sampleForm = null; //Form to Create a new Sample
	
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
				localReference.navigationBar = new NavigationBar(localReference, "sectionsContainer", null, localReference.profile.menuStructure);
				localReference.navigationBar.repaint();
			
				localReference.showMainMenu();
				Util.unblockUI();
			
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
	// Functions that trigger view Changes
	//
	
	this.showInspectors = function() {
		//Update menu
		this.navigationBar.updateMenu(null);
		
		//Show Inspectors
		this.inspector.repaint();
	}
	
	this.showMainMenu = function() {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('main-menu', 'showMainMenu', null, 'Main Menu');
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Main menu
		var mainMenu = new MainMenu(this, "mainContainer", this.profile.menuStructure);
		mainMenu.init();
	}
	
	this.showSamplesPage = function(sampleTypeCode) {
		//Update menu
		var sampleType = this.profile.getTypeForTypeCode(sampleTypeCode);
		var sampleTypeDisplayName = sampleType.description;
		if(sampleTypeDisplayName === null) {
			sampleTypeDisplayName = sampleTypeCode;
		}

		var breadCrumbPage = new BreadCrumbPage(sampleTypeCode+"-table", "showSamplesPage", sampleTypeCode, sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Sample Table
		this.sampleTable = new SampleTable(this.serverFacade, "mainContainer", this.profile, sampleTypeCode, true, true, false, false, false, this.inspector);
		this.sampleTable.init();
	}

	this.lastSearchId = 0; //Used to discard search responses that don't pertain to the last search call.
	
	this.showSearchPage = function(event) {
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
				localReference.sampleTable = new SampleTable(this.serverFacade, "mainContainer", localReference.profile, localReference.profile.searchType["TYPE"], true, false, false, true, false, localReference.inspector);
				$("#search").addClass("search-query-searching");
				localReference.serverFacade.searchWithText(event.target.value, function(data) {
					if(localSearchId === localReference.lastSearchId) {
						$("#search").removeClass("search-query-searching");
						localReference.sampleTable.reloadWithSamples(data);
						Util.unblockUI();
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

	this.showCreateSamplePage = function(sampleTypeCode) {
		//Update menu
		var sampleTypeDisplayName = this.profile.getTypeForTypeCode(sampleTypeCode).description;
		if(sampleTypeDisplayName === null) {
			sampleTypeDisplayName = sampleTypeCode;
		}
		var breadCrumbPage = new BreadCrumbPage('new-sample-'+sampleTypeCode, "showCreateSamplePage", sampleTypeCode, 'Create '+sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sampleTypeCode);
		this.sampleForm = new SampleForm(this.serverFacade, this.inspector, "mainContainer", this.profile, sampleTypeCode, isELNExperiment, SampleFormMode.CREATE, null);
		this.sampleForm.init();
	}

	this.showEditSamplePage = function(sample) {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('edit-sample-'+sample.permId, "showEditSamplePage", sample, 'Update '+sample.code);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sample.sampleTypeCode);
		this.sampleForm = new SampleForm(this.serverFacade, this.inspector, "mainContainer", this.profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.EDIT, sample);
		this.sampleForm.init();
	}

	this.showViewSamplePage = function(sample) {
		//Update menu
		var breadCrumbPage = new BreadCrumbPage('view-sample-'+sample.permId, "showViewSamplePage", sample, 'View '+sample.code);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sample.sampleTypeCode);
		this.sampleForm = new SampleForm(this.serverFacade, this.inspector, "mainContainer", this.profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.VIEW, sample);
		this.sampleForm.init();
	}
}
