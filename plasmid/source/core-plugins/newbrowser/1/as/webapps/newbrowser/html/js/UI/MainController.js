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
 * - openBIS facade
 * - Configuration profile
 * - Atributes used by inline HTML/Javascript
 * - enterApp method
 * - showView methods
 */
function MainController() {
	//
	// Atributes
	//
	
	// openBIS API Object
	this.openbisServer = new openbis();
	
	// Configuration
	this.profile = new YeastLabProfile(this.openbisServer);
	
	//Utility methods
	this.searchFacade = new SearchFacade(this.profile, this.openbisServer);
	
	// Atributes - Typically hold both the model and the view, they are here so they can be accessed by inline HTML/Javascript when needed.
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
		this.openbisServer.listSampleTypes(
			function(result) {
			
				//Load Sample Types
				localReference.profile.allTypes = result.result;
			
				//Init profile
				localReference.profile.init();
			
				//Start App
				localReference.inspector = new Inspector(localReference.searchFacade, "mainContainer", localReference.profile);
				localReference.navigationBar = new NavigationBar(localReference, "sectionsContainer", null, localReference.profile);
				localReference.navigationBar.repaint();
			
				localReference.showMainMenu();
				Util.unblockUI();
			
				//Get datastores for automatic DSS configuration, the first one will be used
				localReference.openbisServer.listDataStores(
					function(dataStores) {
						localReference.profile.allDataStores = dataStores.result;
					}
				);
				
				//Get display settings
				localReference.openbisServer.getUserDisplaySettings( function(response) {
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
		var mainMenu = new MainMenu(this, "mainContainer", this.profile);
		mainMenu.init();
	}
	
	this.showSamplesPage = function(sampleTypeCode) {
		//Update menu
		var sampleType = this.profile.getTypeForTypeCode(sampleTypeCode);
		var breadCrumbPage = new BreadCrumbPage(sampleTypeCode+"-table", "showSamplesPage", sampleTypeCode, sampleType.description);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Sample Table
		this.sampleTable = new SampleTable(this, "mainContainer", this.profile, sampleTypeCode, true, true, false, false, false);
		this.sampleTable.init();
	}

	this.showSearchPage = function(event) {
		//Only search with at least 3 characters
		if(event.target.value.length < 3) {
			return;
		}
		
		//Clean page and update menu
		this.navigationBar.updateMenu(null);
		
		//Update Main Container
		this.sampleTable = new SampleTable(this, "mainContainer", this.profile, this.profile.searchType["TYPE"], true, false, false, true, false);
		var localReference = this;
		this.searchFacade.searchWithText(event.target.value, function(data) {
			localReference.sampleTable.reloadWithSamples(data);
			Util.unblockUI();
		});
	}

	this.showCreateSamplePage = function(sampleTypeCode) {
		//Update menu
		var sampleTypeDisplayName = this.profile.getTypeForTypeCode(sampleTypeCode).description;
		var breadCrumbPage = new BreadCrumbPage('new-sample', "showCreateSamplePage", sampleTypeCode, 'Create '+sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sampleTypeCode);
		this.sampleForm = new SampleForm(this, "mainContainer", this.profile, sampleTypeCode, isELNExperiment, SampleFormMode.CREATE, null);
		this.sampleForm.init();
	}

	this.showEditSamplePage = function(sample) {
		//Update menu
		var sampleTypeDisplayName = this.profile.getTypeForTypeCode(sample.sampleTypeCode).description;
		var breadCrumbPage = new BreadCrumbPage('edit-sample', "showEditSamplePage", sample, 'Edit '+sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sample.sampleTypeCode);
		this.sampleForm = new SampleForm(this, "mainContainer", this.profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.EDIT, sample);
		this.sampleForm.init();
	}

	this.showViewSamplePage = function(sample) {
		//Update menu
		var sampleTypeDisplayName = this.profile.getTypeForTypeCode(sample.sampleTypeCode).description;
		var breadCrumbPage = new BreadCrumbPage('view-sample', "showViewSamplePage", sample, 'View '+sampleTypeDisplayName);
		this.navigationBar.updateBreadCrumbPage(breadCrumbPage);
		
		//Show Form
		var isELNExperiment = this.profile.isELNExperiment(sample.sampleTypeCode);
		this.sampleForm = new SampleForm(this, "mainContainer", this.profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.VIEW, sample);
		this.sampleForm.init();
	}
}
