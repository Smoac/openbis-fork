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
	
	this.repaint = function(views) {
		var $container = views.content;
		var _this = this;
		
		var $title = $("<div>");
		if(this._sampleTableModel.title && this._sampleTableModel.experimentIdentifier) {
			
			var title = "" + ELNDictionary.getExperimentKindName(this._sampleTableModel.experimentIdentifier) + ": " + IdentifierUtil.getCodeFromIdentifier(this._sampleTableModel.experimentIdentifier);
			if(this._sampleTableModel.experiment && this._sampleTableModel.experiment.properties[profile.propertyReplacingCode]) {
				title = "" + ELNDictionary.getExperimentKindName(this._sampleTableModel.experimentIdentifier) + ": " + this._sampleTableModel.experiment.properties[profile.propertyReplacingCode];
			}
			
			var spaceCode = IdentifierUtil.getSpaceCodeFromIdentifier(this._sampleTableModel.experimentIdentifier);
			var projectCode = IdentifierUtil.getProjectCodeFromExperimentIdentifier(this._sampleTableModel.experimentIdentifier);
			var experimentCode = IdentifierUtil.getCodeFromIdentifier(this._sampleTableModel.experimentIdentifier);
			var entityPath = FormUtil.getFormPath(spaceCode, projectCode, experimentCode);
			
			$title
				.append($("<h2>").append(title))
				.append($("<h4>", { "style" : "font-weight:normal;" } ).append(entityPath));
		} else if(this._sampleTableModel.title) {
			$title.append($("<h2>").append(this._sampleTableModel.title));
		}
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		if(this._sampleTableModel.experimentIdentifier) {
			var experimentSpace = IdentifierUtil.getSpaceCodeFromIdentifier(this._sampleTableModel.experimentIdentifier);
			var experimentCode = IdentifierUtil.getCodeFromIdentifier(this._sampleTableModel.experimentIdentifier);
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
			
			//
			var mandatorySampleTypeCode = null;
			if(this._sampleTableModel.experiment && 
					this._sampleTableModel.experiment.properties &&
					this._sampleTableModel.experiment.properties["$DEFAULT_OBJECT_TYPE"]) {
				mandatorySampleTypeCode = this._sampleTableModel.experiment.properties["$DEFAULT_OBJECT_TYPE"];
			}
			
			var sampleTypeCodeToUse = (mandatorySampleTypeCode)?mandatorySampleTypeCode:sampleTypeCode;
			this._sampleTableModel.sampleTypeCodeToUse = sampleTypeCodeToUse;
			
			//Add Sample Type
			if(sampleTypeCodeToUse !== null) {
				var $createButton = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
					var argsMap = {
							"sampleTypeCode" : sampleTypeCodeToUse,
							"experimentIdentifier" : _this._sampleTableModel.experimentIdentifier
					}
					var argsMapStr = JSON.stringify(argsMap);
					Util.unblockUI();
					mainController.changeView("showCreateSubExperimentPage", argsMapStr);
				});
				
				toolbarModel.push({ component : $createButton, tooltip: "Create " + Util.getDisplayNameFromCode(sampleTypeCodeToUse) });
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
			var $options = this._getOptionsMenu();
			tableToolbarModel.push({ component : $options, tooltip: null });
		}
		
		var $header = views.header;
		$header.append($title);
		
		if(toolbarModel.length > 0) {
			$header.append(FormUtil.getToolbar(toolbarModel));
		}
		if(tableToolbarModel.length > 0) {
			$header.append(FormUtil.getToolbar(tableToolbarModel));
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
		
		if(_this._sampleTableModel.experimentIdentifier) {
			var $createSampleOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Create ' + ELNDictionary.Sample + ''}).append('Create ' + ELNDictionary.Sample + ''));
			$createSampleOption.click(function() {
				_this.createNewSample(_this._sampleTableModel.experimentIdentifier);
			});
			$list.append($createSampleOption);
		}
		
		
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
		
		if(_this._sampleTableModel.experimentIdentifier) {
			var expKindName = ELNDictionary.getExperimentKindName(_this._sampleTableModel.experimentIdentifier, false);
			var $searchCollectionOption = $("<li>", { 'role' : 'presentation' }).append($("<a>", {'title' : 'Search in ' + expKindName  }).append('Search in ' + expKindName));
			$searchCollectionOption.click(function() {
				
				var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : _this._sampleTableModel.experiment.permId } };
				var rules = { entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules };
				
				mainController.changeView("showAdvancedSearchPage", JSON.stringify(rules));
			});
			$list.append($searchCollectionOption);
		}
		
		return $dropDownMenu;
	}
	
	this._getAllSampleTypesDropdown = function() {
		var _this = this;
		var $sampleTypesSelector = FormUtil.getSampleTypeDropdown(null, false, ["STORAGE", "STORAGE_POSITION"]);
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
	    FormUtil.createNewSample(experimentIdentifier);
	}
	
	this.registerSamples = function(experimentIdentifier) {
		var allowedSampleTypes = null;
		var forcedSpace = null;
		var spaceCodeFromIdentifier = null;
		if(this._sampleTableModel.sampleTypeCodeToUse) {
			allowedSampleTypes = [this._sampleTableModel.sampleTypeCodeToUse, "STORAGE_POSITION"];
		}
		if(experimentIdentifier) {
			spaceCodeFromIdentifier = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
			forcedSpace = IdentifierUtil.getForcedSpaceIdentifier(spaceCodeFromIdentifier);
		}
		
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
					
					var experimentIdentifierOrDelete = experimentIdentifier;
					if(experimentIdentifierOrDelete && typeAndFileController.getSampleTypeCode() === "STORAGE_POSITION") {
						experimentIdentifierOrDelete = "__DELETE__";
						forcedSpace = profile.getStorageSpaceForSpace(spaceCodeFromIdentifier);
					}
					if(infoData.result.identifiersPressent) { //If identifiers are present they should match the space of the experiment
						mainController.serverFacade.registerSamplesWithSilentOverrides(typeAndFileController.getSampleTypeCode(), forcedSpace, experimentIdentifierOrDelete, "sample-file-upload", null, finalCallback);
					} else { // If identifiers are not present the defaultGroup/forcedSpace should be set for auto generation
						mainController.serverFacade.registerSamplesWithSilentOverrides(typeAndFileController.getSampleTypeCode(), forcedSpace, experimentIdentifierOrDelete, "sample-file-upload", forcedSpace, finalCallback);
					}
				}
			);
			});
		}, allowedSampleTypes);
		typeAndFileController.init();
	}
	
	this.updateSamples = function(experimentIdentifier) {
		var allowedSampleTypes = null;
		var forcedSpace = null;
		var spaceCodeFromIdentifier = null;
		if(this._sampleTableModel.sampleTypeCodeToUse) {
			allowedSampleTypes = [this._sampleTableModel.sampleTypeCodeToUse, "STORAGE_POSITION"];
		}
		if(experimentIdentifier) {
			spaceCodeFromIdentifier = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
			forcedSpace = IdentifierUtil.getForcedSpaceIdentifier(spaceCodeFromIdentifier);
		}
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
			
			var experimentIdentifierOrDelete = experimentIdentifier;
			if(experimentIdentifierOrDelete && typeAndFileController.getSampleTypeCode() === "STORAGE_POSITION") {
				experimentIdentifierOrDelete = "__DELETE__";
				forcedSpace = profile.getStorageSpaceForSpace(spaceCodeFromIdentifier);
			}
			
			mainController.serverFacade.fileUpload(typeAndFileController.getFile(), function(result) {
				//Code After the upload
				mainController.serverFacade.updateSamplesWithSilentOverrides(typeAndFileController.getSampleTypeCode(), forcedSpace, experimentIdentifierOrDelete, "sample-file-upload", null,finalCallback);
			});
		}, allowedSampleTypes);
		typeAndFileController.init();
	}
}