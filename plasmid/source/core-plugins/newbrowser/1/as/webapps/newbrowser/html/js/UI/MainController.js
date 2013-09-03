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
// Setup
//
$(document).ready(function() {
	$('#main').hide();
	
	$('body').css('background-image', 'url(./images/notebook_side.png)');
	$('body').css('background-repeat', 'repeat-y');
	
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
		Util.unblockUI();
		$("#username").focus();
		Util.showError('The given username or password is not correct.');
		return;
	}
	
	//
	// Start App if credentials are ok
	//
	$('body').css('background-image', 'none');
	$('body').css('background-repeat', 'none');
	
	$("#login-form-div").hide();
	$("#main").show();
	
	openbisServer.listSampleTypes(
		function(result) {
		
			//Load Sample Types
			profile.allTypes = result.result;
			
			openbisServer.listVocabularies(
				function(result) {
					//Load Vocabularies
					profile.allVocabularies = result.result;
					
					//Init profile
					profile.init();
			
					//Start App
					inspector = new Inspector("mainContainer", profile);
					navigationBar = new NavigationBar("sectionsContainer", null, profile);
					navigationBar.repaint();
			
					showMainMenu();
					Util.unblockUI();
				}
			);
		}
	);
}

//
// Trigger View Updates
//
function showInspectors() {
	//Update menu
	navigationBar.updateMenu(null);
	
	//Show Inspectors
	inspector.repaint();
}

function showMainMenu() {
	//Update menu
	var breadCrumbPage = new BreadCrumbPage('main-menu', 'javascript:showMainMenu()', 'Main Menu');
	navigationBar.updateBreadCrumbPage(breadCrumbPage);
	
	//Show Main menu
	var mainMenu = new MainMenu("mainContainer", profile);
	mainMenu.init();
}

function showSamplesPage(sampleTypeCode) {
	//Update menu
	var sampleType = profile.getTypeForTypeCode(sampleTypeCode);
	var breadCrumbPage = new BreadCrumbPage(sampleTypeCode, "javascript:showSamplesPage(\"" + sampleTypeCode + "\")", sampleType.description);
	navigationBar.updateBreadCrumbPage(breadCrumbPage);
	
	//Show Sample Table
	sampleTable = new SampleTable("mainContainer", profile, sampleTypeCode, true, true, false, false, false);
	sampleTable.init();
}

function showCreateSamplePage(sampleTypeCode) {
	//Update menu
	var sampleTypeDisplayName = profile.getTypeForTypeCode(sampleTypeCode).description;
	var breadCrumbPage = new BreadCrumbPage('new-sample', "javascript:showCreateSamplePage('" + sampleTypeCode + "')", 'Create '+sampleTypeDisplayName);
	navigationBar.updateBreadCrumbPage(breadCrumbPage);

	//Show Form
	var isELNExperiment = sampleTypeCode === profile.ELNExperiment;
	sampleForm = new SampleForm("mainContainer", profile, sampleTypeCode, isELNExperiment);
	sampleForm.init();
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
