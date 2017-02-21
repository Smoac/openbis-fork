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
	this.sampleTypeSelector = null;
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		var $title = $("<div>");
		if(this._sampleTableModel.title && this._sampleTableModel.experimentIdentifier) {
			
			var title = "" + ELNDictionary.getExperimentKindName(this._sampleTableModel.experimentIdentifier) + ": " + this._sampleTableModel.experimentIdentifier.substring(this._sampleTableModel.experimentIdentifier.lastIndexOf("/") + 1);
			if(this._sampleTableModel.experiment && this._sampleTableModel.experiment.properties[profile.propertyReplacingCode]) {
				title = "" + ELNDictionary.getExperimentKindName(this._sampleTableModel.experimentIdentifier) + ": " + this._sampleTableModel.experiment.properties[profile.propertyReplacingCode];
			}
			$title
				.append($("<h2>").append(title))
				.append($("<h4>", { "style" : "font-weight:normal;" } ).append(this._sampleTableModel.experimentIdentifier));
		} else if(this._sampleTableModel.title) {
			$title.append($("<h2>").append(this._sampleTableModel.title));
		}
		$container.append($title);
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._sampleTableModel.experimentIdentifier) {
			var experimentSpace = this._sampleTableModel.experimentIdentifier.split("/")[1];
			var experimentCode = this._sampleTableModel.experimentIdentifier.split("/")[3];
			var allSampleTypes = profile.getAllSampleTypes();
			var sampleTypeCodesFound = [];
			for(var aIdx = 0; aIdx < allSampleTypes.length; aIdx++) {
				var auxSampleTypeCode = allSampleTypes[aIdx].code;
				if(experimentCode.indexOf(auxSampleTypeCode) !== -1) {
					sampleTypeCodesFound.push(auxSampleTypeCode);
				}
			}
			
			var sampleTypeCode = null;
			if(sampleTypeCodesFound.length === 1 && profile.isInventorySpace(experimentSpace)) {
				sampleTypeCode = sampleTypeCodesFound[0];
			}
			
			//Add Sample Type
			if(sampleTypeCode !== null) {
				var $createButton = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
					var argsMap = {
							"sampleTypeCode" : sampleTypeCode,
							"experimentIdentifier" : _this._sampleTableModel.experimentIdentifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					Util.unblockUI();
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				
				toolbarModel.push({ component : $createButton, tooltip: "Create " + Util.getDisplayNameFromCode(sampleTypeCode) });
			}
		}
		
		var tableToolbarModel = [];
		if(this._sampleTableModel.experimentIdentifier) {
			var $options = this._getOptionsMenu();
			toolbarModel.push({ component : $options, tooltip: null });
		} else if(this._sampleTableModel.projectPermId) {

		} else {
			var $allSampleTypes = this._getAllSampleTypesDropdown();
			tableToolbarModel.push({ component : $allSampleTypes, tooltip: null });
		}
		
		if(toolbarModel.length > 0) {
			$container.append(FormUtil.getToolbar(toolbarModel));
		}
		if(toolbarModel.length > 0 && tableToolbarModel.length > 0) {
			$container.append("<br>");
		}
		if(tableToolbarModel.length > 0) {
			$container.append(FormUtil.getToolbar(tableToolbarModel));
		}
		
		
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
		var $caret = $("<a>", { 'href' : '#', 'data-toggle' : 'dropdown', class : 'dropdown-toggle btn btn-default'}).append("Operations ").append($("<b>", { class : 'caret' }));
		var $list = $("<ul>", { class : 'dropdown-menu', 'role' : 'menu', 'aria-labelledby' :'sampleTableDropdown' });
		$dropDownMenu.append($caret);
		$dropDownMenu.append($list);
		
		var $createSampleOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Create ' + ELNDictionary.Sample + ''}).append('Create ' + ELNDictionary.Sample + ''));
		$createSampleOption.click(function() {
			_this.createNewSample(_this._sampleTableModel.experimentIdentifier);
		});
		$list.append($createSampleOption);
		
		var $batchRegisterOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Batch Register ' + ELNDictionary.Sample + 's'}).append("Batch Register " + ELNDictionary.Sample + "s"));
		$batchRegisterOption.click(function() {
			_this.registerSamples(_this._sampleTableModel.experimentIdentifier);
		});
		$list.append($batchRegisterOption);
		
		var $batchUpdateOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Batch Update ' + ELNDictionary.Sample + 's'}).append("Batch Update " + ELNDictionary.Sample + "s"));
		$batchUpdateOption.click(function() {
			_this.updateSamples(_this._sampleTableModel.experimentIdentifier);
		});
		$list.append($batchUpdateOption);
		
		var expKindName = ELNDictionary.getExperimentKindName(_this._sampleTableModel.experimentIdentifier, false);
		var $searchCollectionOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Search Into ' + expKindName  }).append('Search Into ' + expKindName));
		$searchCollectionOption.click(function() {
			
			var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : _this._sampleTableModel.experiment.permId } };
			var rules = { entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules };
			
			mainController.changeView("showAdvancedSearchPage", JSON.stringify(rules));
		});
		$list.append($searchCollectionOption);
		
		return $dropDownMenu;
	}
	
	this._getAllSampleTypesDropdown = function() {
		var _this = this;
		var $sampleTypesSelector = FormUtil.getSampleTypeDropdown(null, false);
		$sampleTypesSelector.change(function() {
			var sampleTypeToShow = $(this).val();
			
			var advancedSampleSearchCriteria = {
					entityKind : "SAMPLE",
					logicalOperator : "AND",
					rules : { "1" : { type : "Attribute", name : "SAMPLE_TYPE", value : sampleTypeToShow } }
			}
			
			_this._sampleTableController._reloadTableWithAllSamples(advancedSampleSearchCriteria);
		});
		
		return $("<span>").append($sampleTypesSelector);
	}
	
	//
	// Menu Operations
	//
	this.createNewSample = function(experimentIdentifier) {
		var _this = this;
		var $dropdown = FormUtil.getSampleTypeDropdown("sampleTypeDropdown", true);
		Util.blockUI("Select the type for the " + ELNDictionary.Sample + ": <br><br>" + $dropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeDropdownCancel'>Cancel</a>");
		
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
		var typeAndFileController = new TypeAndFileController('Register ' + ELNDictionary.Samples + '', "REGISTRATION", function(type, file) {
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
								component += "<option value='"+spaces[i]+"'>"+Util.getDisplayNameFromCode(spaces[i])+"</option>";
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
	
	this.updateSamples = function(experimentIdentifier) {
		var typeAndFileController = new TypeAndFileController('Update ' + ELNDictionary.Samples + '', "UPDATE", function(type, file) {
			Util.blockUI();
			var finalCallback = function(data) {
				if(data.error) {
					Util.showError(data.error.message, function() {Util.unblockUI();});
				} else if(data.result) {
					Util.showSuccess(data.result, function() {
						Util.unblockUI();
						mainController.changeView('showSamplesPage', experimentIdentifier);
					});
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