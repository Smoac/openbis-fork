/* Copyright 2014 ETH Zuerich, Scientific IT Services
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

function SampleTableView(sampleTableController, sampleTableModel) {
	this._sampleTableController = sampleTableController;
	this._sampleTableModel = sampleTableModel;
	this._tableContainer = $("<div>");
	
	this.repaint = function($container) {
		$container.empty();
		var $title = $("<h1>").append(this._sampleTableModel.experimentIdentifier + " Samples");
		$container.append($title);
		
		var $toolbox = $("<div>", { 'id' : 'toolBoxContainer', class : 'toolBox'});
		$toolbox.append(this._getExperimentSampleTypesDropdown()).append(" ").append(this._getOptionsMenu());
		
		$container.append($toolbox);
		$container.append(this._tableContainer);
	}
	
	this.getTableContainer = function() {
		return this._tableContainer;
	}
	
	//
	// Menus
	//
	this._getOptionsMenu = function() {
		var _this = this;
		var $dropDownMenu = $("<span>", { class : 'dropdown' });
		var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Options ").append($("<b>", { class : 'caret' }));
		var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
		$dropDownMenu.append($caret);
		$dropDownMenu.append($list);
		
		var $createSampleOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Create Sample'}).append("Create Sample"));
		$createSampleOption.click(function() {
			_this.createNewSample(_this._sampleTableModel.experimentIdentifier);
		});
		$list.append($createSampleOption);
		
		var $batchRegisterOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Batch Register Samples'}).append("Batch Register Samples"));
		$batchRegisterOption.click(function() {
			_this.registerSamples(_this._sampleTableModel.experimentIdentifier);
		});
		$list.append($batchRegisterOption);
		
		var $batchUpdateOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Batch Update Samples'}).append("Batch Update Samples"));
		$batchUpdateOption.click(function() {
			_this.updateSamples();
		});
		$list.append($batchUpdateOption);
			
		return $dropDownMenu;
	}
	
	this._getExperimentSampleTypesDropdown = function() {
		var _this = this;
		var	$sampleTypesSelector = $('<select>', { 'id' : 'sampleTypeCodesToShow', class : 'form-control' });
		$sampleTypesSelector.append($('<option>', { 'value' : '' }).text(''));
		for(sampleTypeCode in this._sampleTableModel.sampleTypesOnExperiment) {
			$sampleTypesSelector.append($('<option>', { 'value' : sampleTypeCode }).text(sampleTypeCode));
		}
		$sampleTypesSelector.change(function(event) {
			var sampleTypeToShow = $(this).val();
			_this._sampleTableController._reloadTableWithSampleType(sampleTypeToShow);
		});
		
		return $("<span>").append($sampleTypesSelector);
	}
	
	//
	// Menu Operations
	//
	this.createNewSample = function(experimentIdentifier) {
		var _this = this;
		var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true);
		Util.blockUI("Select the type for the sample: <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeDropdownCancel'>Cancel</a>");
		
		$("#sampleTypeDropdown").on("change", function(event) {
			var sampleTypeCode = $("#sampleTypeDropdown")[0].value;
			var argsMap = {
					"sampleTypeCode" : sampleTypeCode,
					"experimentIdentifier" : experimentIdentifier
			}
			
			var argsMapStr = JSON.stringify(argsMap);
			Util.unblockUI();
			mainController.changeView("showCreateSubExperimentPage", argsMapStr);
		});
		
		$("#sampleTypeDropdownCancel").on("click", function(event) { 
			Util.unblockUI();
		});
	}
	
	this.registerSamples = function(experimentIdentifier) {
		var _this = this;
		var typeAndFileController = new TypeAndFileController('Register Samples', function(type, file) {
			Util.blockUI();
			mainController.serverFacade.fileUpload(typeAndFileController.getFile(), function(result) {
				//Code After the upload
				mainController.serverFacade.uploadedSamplesInfo(typeAndFileController.getSampleTypeCode(), "sample-file-upload", 
				function(infoData) {
					var finalCallback = function(data) {
						if(data.error) {
							Util.showError(data.error.message, function() {Util.unblockUI();});
						} else if(data.result) {
							var extraMessage = "<br> It can take a couple of minutes to have them available.";
							Util.showSuccess(data.result + extraMessage, function() {
								Util.unblockUI();
								mainController.changeView('showSamplesPage', experimentIdentifier);
							});
						} else {
							Util.showError("Unknown response. Probably an error happened.", function() {Util.unblockUI();});
						}
					};
					
					if(infoData.result.identifiersPressent) {
						mainController.serverFacade.registerSamples(typeAndFileController.getSampleTypeCode(), "sample-file-upload", null, finalCallback);
					} else {
						mainController.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(data) {
							var spaces = [];
							for(var i = 0; i < data.result.length; i++) {
								spaces.push(data.result[i].code);
							}
							
							var component = "<select id='sampleSpaceSelector' class='form-control' required>";
							component += "<option disabled=\"disabled\" selected></option>";
							for(var i = 0; i < spaces.length; i++) {
								component += "<option value='"+spaces[i]+"'>"+spaces[i]+"</option>";
							}
							component += "</select>";
							
							Util.blockUI("Space not found, please select it for automatic generation: <br><br>" + component + "<br> or <a class='btn btn-default' id='spaceSelectionCancel'>Cancel</a>");
							
							$("#sampleSpaceSelector").on("change", function(event) {
								var space = $("#sampleSpaceSelector")[0].value;
								Util.blockUI();
								mainController.serverFacade.registerSamples(typeAndFileController.getSampleTypeCode(), "sample-file-upload", '/' + space, finalCallback);
							});
							
							$("#spaceSelectionCancel").on("click", function(event) { 
								Util.unblockUI();
							});
							
						});
					}
				}
			);
			});
		});
		typeAndFileController.init();
	}
	
	this.updateSamples = function() {
		var typeAndFileController = new TypeAndFileController('Update Samples', function(type, file) {
			Util.blockUI();
			var finalCallback = function(data) {
				if(data.error) {
					Util.showError(data.error.message, function() {Util.unblockUI();});
				} else if(data.result) {
					Util.showSuccess(data.result, function() {Util.unblockUI();});
				} else {
					Util.showError("Unknown response. Probably an error happened.", function() {Util.unblockUI();});
				}
			};
			
			mainController.serverFacade.fileUpload(typeAndFileController.getFile(), function(result) {
				//Code After the upload
				mainController.serverFacade.updateSamples(typeAndFileController.getSampleTypeCode(), "sample-file-upload", null,finalCallback);
			});
		});
		typeAndFileController.init();
	}
}