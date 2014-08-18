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

function SampleFormController(mainController, mode, sample) {
	this._mainController = mainController;
	this._sampleFormModel = new SampleFormModel(mode, sample);
	this._sampleFormView = new SampleFormView(this, this._sampleFormModel);
	this._storageControllers = [];
	
	this.init = function($container) {
		this._sampleFormView.repaint($container);
	}
	
	this.isDirty = function() {
		return this._sampleFormModel.isFormDirty;
	}
	
	this.isLoaded = function() {
		return this._sampleFormModel.isFormLoaded;
	}
	
	this.getLastStorageController = function() {
		return this._storageControllers[this._storageControllers.length-1];
	}
	
	this.addStorageController = function(storagePropertyGroupName) {
		var storageController = new StorageController({
			title : null,
			storagePropertyGroupSelector : "off",
			storageSelector : "on",
			userSelector : "off",
			boxSelector: "on",
			rackSelector: "on",
			contentsSelector: "off"
		});
		storageController.getModel().storagePropertyGroup = profile.getStoragePropertyGroup(storagePropertyGroupName);
		this._storageControllers.push(storageController);
	}
	
	this.createUpdateCopySample = function(isCopyWithNewCode, linkParentsOnCopy, copyChildrenOnCopy) {
		Util.blockUI();
		var _this = this;
		
		//
		// Parents/Children Links
		//
		if(!this._sampleFormModel.sampleLinksParents.isValid()) {
			Util.showError("Missing Parents.");
			return;
		}
		var sampleParentsFinal = this._sampleFormModel.sampleLinksParents.getSamplesIdentifiers();
		
		if(!this._sampleFormModel.sampleLinksChildren.isValid()) {
			Util.showError("Missing Children.");
			return;
		}
		var sampleChildrenFinal = this._sampleFormModel.sampleLinksChildren.getSamplesIdentifiers();
		var sampleChildrenRemovedFinal = this._sampleFormModel.sampleLinksChildren.getSamplesRemovedIdentifiers();
		
		//
		// Check that the same sample is not a parent and a child at the same time
		//
		var intersect_safe = function(a, b) {
		  var ai=0, bi=0;
		  var result = new Array();
		  
		  while( ai < a.length && bi < b.length )
		  {
		     if      (a[ai] < b[bi] ){ ai++; }
		     else if (a[ai] > b[bi] ){ bi++; }
		     else /* they're equal */
		     {
		       result.push(a[ai]);
		       ai++;
		       bi++;
		     }
		  }

		  return result;
		}
		
		sampleParentsFinal.sort();
		sampleChildrenFinal.sort();
		var intersection = intersect_safe(sampleParentsFinal, sampleChildrenFinal);
		if(intersection.length > 0) {
			Util.showError("The same entity can't be a parent and a child, please check: " + intersection);
			return;
		}
		
		//
		//Identification Info
		//
		var sampleCode = this._sampleFormModel.sample.code;
		var properties = this._sampleFormModel.sample.properties;
		
		var sampleSpace = this._sampleFormModel.sample.spaceCode; //ELN Sub Experiments will have a space by default but not common Samples
		if(!sampleSpace) { //Common Samples have maybe a default space to be attached to
			sampleSpace = profile.getSpaceForSampleType(this.sampleTypeCode);
		}
		
		var experimentIdentifier = this._sampleFormModel.sample.experimentIdentifierOrNull; //ELN Sub Experiments will have a experiment by default but not common Samples
		if(!experimentIdentifier) { //Common Samples have maybe a default experiment to be attached to
			experimentIdentifier = profile.getExperimentIdentifierForSample(
					this._sampleFormModel.sample.sampleTypeCode,
					this._sampleFormModel.sample.code,
					properties);
		}
		
		var sampleProject = null;
		var sampleExperiment = null;
		
		if(experimentIdentifier) { //If there is a experiment detected, the sample should be attached to the experiment completely.
			sampleSpace = experimentIdentifier.split("/")[1];
			sampleProject = experimentIdentifier.split("/")[2];
			sampleExperiment = experimentIdentifier.split("/")[3];
		}
		
		//Children to create
		var samplesToCreate = [];
		this._sampleFormModel.sampleLinksChildren.getSamples().forEach(function(child) {
			if(child.newSample) {
			  child.properties = {};
				if(profile.storagesConfiguration["isEnabled"]) {
					child.properties[_this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["NAME_PROPERTY"]] = $("#childrenStorageSelector").val();
					child.properties[_this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["ROW_PROPERTY"]] = 1;
					child.properties[_this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["COLUMN_PROPERTY"]] = 1;
					child.properties[_this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["BOX_PROPERTY"]] = $("#sampleSpaceProject").val().replace(/\//g,'\/') + "_" + $("#sampleCode").val() + "_EXP_RESULTS";
					child.properties[_this.profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["USER_PROPERTY"]] = _this.serverFacade.openbisServer.getSession().split("-")[0];
				}
				samplesToCreate.push(child);
			}
		});
		
		//Method
		var method = "";
		if(this._sampleFormModel.mode === FormMode.CREATE) {
			method = "insertSample";
		} else if(this._sampleFormModel.mode === FormMode.EDIT) {
			method = "updateSample";
		}
		
		var parameters = {
				//API Method
				"method" : method,
				//Identification Info
				"sampleSpace" : sampleSpace,
				"sampleProject" : sampleProject,
				"sampleExperiment" : sampleExperiment,
				"sampleCode" : sampleCode,
				"sampleType" : this._sampleFormModel.sample.sampleTypeCode,
				//Other Properties
				"sampleProperties" : properties,
				//Parent links
				"sampleParents": sampleParentsFinal,
				//Children links
				"sampleChildren": sampleChildrenFinal,
				"sampleChildrenNew": samplesToCreate,
				"sampleChildrenRemoved": sampleChildrenRemovedFinal
		};
		
		//
		// Copy override - This part modifies what is done for a create/update and adds a couple of extra parameters needed to copy to the bench correctly
		//
		if(isCopyWithNewCode) {
			parameters["method"] = "copySample";
			parameters["sampleCode"] = isCopyWithNewCode;
			if(!linkParentsOnCopy) {
				parameters["sampleParents"] = [];
			}
			if(!copyChildrenOnCopy) {
				parameters["sampleChildren"] = [];
			} else if(profile.storagesConfiguration["isEnabled"]) {
				//1. All properties belonging to benches, to not to copy
				parameters["notCopyProperties"] = [];
				parameters["defaultBenchPropertyList"] = [];
				for(var i = 0; i < profile.storagesConfiguration["STORAGE_PROPERTIES"].length; i++) {
					var storagePropertyGroup = profile.storagesConfiguration["STORAGE_PROPERTIES"][i];
					var listToUse = "notCopyProperties";
					if(i === 0) {
						listToUse = "defaultBenchPropertyList";
					}
					parameters[listToUse].push(storagePropertyGroup["NAME_PROPERTY"]);
					parameters[listToUse].push(storagePropertyGroup["ROW_PROPERTY"]);
					parameters[listToUse].push(storagePropertyGroup["COLUMN_PROPERTY"]);
					parameters[listToUse].push(storagePropertyGroup["BOX_PROPERTY"]);
					parameters[listToUse].push(storagePropertyGroup["USER_PROPERTY"]);
				}
				
				//2. Default Bench properties
				var defaultStoragePropertyGroup = profile.storagesConfiguration["STORAGE_PROPERTIES"][0];
				parameters["defaultBenchProperties"] = {};
				var $benchDropdown = FormUtil.getDefaultBenchDropDown();
				var defaultBench = $benchDropdown.children()[1].value;
				parameters["defaultBenchProperties"][defaultStoragePropertyGroup["NAME_PROPERTY"]] = defaultBench;
				parameters["defaultBenchProperties"][defaultStoragePropertyGroup["ROW_PROPERTY"]] = 1;
				parameters["defaultBenchProperties"][defaultStoragePropertyGroup["COLUMN_PROPERTY"]] = 1;
				parameters["defaultBenchProperties"][defaultStoragePropertyGroup["BOX_PROPERTY"]] = this._sampleFormModel.sample.experimentIdentifierOrNull.replace(/\//g,'\/') + "_" + isCopyWithNewCode + "_EXP_RESULTS";
				parameters["defaultBenchProperties"][defaultStoragePropertyGroup["USER_PROPERTY"]] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
			}
			parameters["sampleChildrenNew"] = [];
			parameters["sampleChildrenRemoved"] = [];
		}
		
		//
		// Sending the request to the server
		//
		if(profile.getDefaultDataStoreCode()) {
			mainController.serverFacade.createReportFromAggregationService(profile.getDefaultDataStoreCode(), parameters, function(response) {
				_this._createUpdateCopySampleCallback(_this, isCopyWithNewCode, response);
			});
		} else {
			Util.showError("No DSS available.", function() {Util.unblockUI();});
		}
		
		return false;
	}
	
	this._createUpdateCopySampleCallback = function(_this, isCopyWithNewCode, response) {
		if(response.error) { //Error Case 1
			Util.showError(response.error.message, function() {Util.unblockUI();});
		} else if (response.result.columns[1].title === "Error") { //Error Case 2
			var stacktrace = response.result.rows[0][1].value;
			Util.showStacktraceAsError(stacktrace);
		} else if (response.result.columns[0].title === "STATUS" && response.result.rows[0][0].value === "OK") { //Success Case
			var sampleType = profile.getSampleTypeForSampleTypeCode(_this._sampleFormModel.sample.sampleTypeCode);
			var sampleTypeDisplayName = sampleType.description;
			if(!sampleTypeDisplayName) {
				sampleTypeDisplayName = _this._sampleFormModel.sample.sampleTypeCode;
			}
			
			var message = "";
			if(isCopyWithNewCode) {
				message = "Copied with new code: " + isCopyWithNewCode + ".";
			} else if(_this._sampleFormModel.mode === FormMode.CREATE) {
				message = "Created.";
			} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
				message = "Updated.";
			}
			
			var callbackOk = function() {
				Util.unblockUI();
				if((isCopyWithNewCode || _this._sampleFormModel.mode === FormMode.CREATE) && _this._sampleFormModel.sample.experimentIdentifierOrNull) {
					mainController.sideMenu.refreshSubExperiment(_this._sampleFormModel.sample.experimentIdentifierOrNull);
				}
//				TO-DO: The Sample is not necessarily searchable after creation since the index runs asynchronously
//				localReference.serverFacade.searchWithType(localReference.sampleTypeCode, $("#sampleCode")[0].value, function(data) {
//					mainController.changeView('showViewSamplePageFromPermId',data[0].permId);
//				});
			}
			
			Util.showSuccess(sampleTypeDisplayName + " " + message, callbackOk);
			_this._sampleFormModel.isFormDirty = false;
		} else { //This should never happen
			Util.showError("Unknown Error.", function() {Util.unblockUI();});
		}
	}
}