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

//
// Configuration
//
var profile = new YeastLabProfile();
var openbisServer = new openbis();

//
// Global Variables
//
var inspector = null; // The samples that are currently being inspected
var navigationBar = null; //Top Bar
var sampleTable = null; //Table that holds the samples
var sampleForm = null; //Form to Create a new Sample

//
// Inital App Setup
//
$(document).ready(function() {
	$('#main').hide();
	
	var username = $("#username").value;
	if(username == null || username.length==0) {
		$("#username").focus();
	} else {
		$("#login-button").focus();
	}
	
	$('#login-form').submit(function() {
		Util.blockUI();
		openbisServer.login( $.trim($('#username').val()), $.trim($('#password').val()), function(data) { enterApp(data) })
	});
	
	openbisServer.ifRestoredSessionActive(function(data) { enterApp(data) });
	
	// Make the ENTER key the default button
	$("login-form input").keypress(function (e) {
		if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {
			$('button[type=submit].default').click();
			return false;
		} else {
			return true;
		}
	});
});

function enterApp(data) {
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
	openbisServer.listSampleTypes(
		function(result) {
			
			//Load Sample Types
			profile.allTypes = result.result;
			
			//Init profile
			profile.init();
			
			//Start App
			inspector = new Inspector("mainContainer", profile);
			navigationBar = new NavigationBar("sectionsContainer", null, profile);
			navigationBar.repaint();
			
			showMainMenu();
			Util.unblockUI();
			
			openbisServer.listDataStores(
				function(dataStores) {
					profile.allDataStores = dataStores.result;
				}
			);
		}
	);
}

//
// Functions that trigger view updates
//

function showInspectors() {
	//Update menu
	navigationBar.updateMenu(null);
	
	//Show Inspectors
	inspector.repaint();
}

function showMainMenu() {
	//Update menu
	var breadCrumbPage = new BreadCrumbPage('main-menu', 'showMainMenu', null, 'Main Menu');
	navigationBar.updateBreadCrumbPage(breadCrumbPage);
	
	//Show Main menu
	var mainMenu = new MainMenu("mainContainer", profile);
	mainMenu.init();
}

function showSamplesPage(sampleTypeCode) {
	//Update menu
	var sampleType = profile.getTypeForTypeCode(sampleTypeCode);
	var breadCrumbPage = new BreadCrumbPage(sampleTypeCode, "showSamplesPage", sampleTypeCode, sampleType.description);
	navigationBar.updateBreadCrumbPage(breadCrumbPage);
	
	//Show Sample Table
	sampleTable = new SampleTable("mainContainer", profile, sampleTypeCode, true, true, false, false, false);
	sampleTable.init();
}

function showSearchPage(event) {
	//Only search with at least 3 characters
	if(event.target.value.length < 3) {
		return;
	}
	
	//Clean page and update menu
	navigationBar.updateMenu(null);
	
	//Update Main Container
	sampleTable = new SampleTable("mainContainer", profile, profile.searchType["TYPE"], true, false, false, true, false);
	Search.searchWithText(event.target.value, function(data) {
		sampleTable.reloadWithSamples(data);
		Util.unblockUI();
	});
}

function showCreateSamplePage(sampleTypeCode) {
	//Update menu
	var sampleTypeDisplayName = profile.getTypeForTypeCode(sampleTypeCode).description;
	var breadCrumbPage = new BreadCrumbPage('new-sample', "showCreateSamplePage", sampleTypeCode, 'Create '+sampleTypeDisplayName);
	navigationBar.updateBreadCrumbPage(breadCrumbPage);

	//Show Form
	var isELNExperiment = profile.isELNExperiment(sampleTypeCode);
	sampleForm = new SampleForm("mainContainer", profile, sampleTypeCode, isELNExperiment, SampleFormMode.CREATE, null);
	sampleForm.init();
}

function showEditSamplePage(sample) {
	//Update menu
	var sampleTypeDisplayName = profile.getTypeForTypeCode(sample.sampleTypeCode).description;
	var breadCrumbPage = new BreadCrumbPage('edit-sample', "showEditSamplePage", sample, 'Edit '+sampleTypeDisplayName);
	navigationBar.updateBreadCrumbPage(breadCrumbPage);

	//Show Form
	var isELNExperiment = profile.isELNExperiment(sample.sampleTypeCode);
	sampleForm = new SampleForm("mainContainer", profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.EDIT, sample);
	sampleForm.init();
}

function showViewSamplePage(sample) {
	//Update menu
	var sampleTypeDisplayName = profile.getTypeForTypeCode(sample.sampleTypeCode).description;
	var breadCrumbPage = new BreadCrumbPage('view-sample', "showViewSamplePage", sample, 'View '+sampleTypeDisplayName);
	navigationBar.updateBreadCrumbPage(breadCrumbPage);

	//Show Form
	var isELNExperiment = profile.isELNExperiment(sample.sampleTypeCode);
	sampleForm = new SampleForm("mainContainer", profile, sample.sampleTypeCode, isELNExperiment, SampleFormMode.VIEW, sample);
	sampleForm.init();
}