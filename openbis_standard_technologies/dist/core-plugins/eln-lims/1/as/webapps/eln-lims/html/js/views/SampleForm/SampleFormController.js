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
//	this._storageControllers = [];
	this._plateController = null;
	this._windowHandlers = [];
	
	this.init = function(views) {
		// Loading datasets
		var _this = this;
		if(mode !== FormMode.CREATE) {
			mainController.serverFacade.listDataSetsForSample(this._sampleFormModel.sample, true, function(datasets) {
				if(!datasets.error) {
					_this._sampleFormModel.datasets = datasets.result;
				}
				
				//Load view
				_this._sampleFormView.repaint(views);
				Util.unblockUI();
			});
		} else {
			//Load view
			_this._sampleFormView.repaint(views);
			Util.unblockUI();
		}
		
	}
	
	this.finalize = function() {
		for(var whIdx = 0; whIdx < this._windowHandlers.length; whIdx++) {
			$(window).off("resize", this._windowHandlers[whIdx]);
		}
		$("#mainContainer").css("overflow-y", "auto");
	}
		
	this.isDirty = function() {
		return this._sampleFormModel.isFormDirty;
	}
	
	this.setDirty = function() {
		this._sampleFormModel.isFormDirty = true;
	}
	
	this.isLoaded = function() {
		return this._sampleFormModel.isFormLoaded;
	}
	
	this._addCommentsWidget = function($container) {
		var commentsController = new CommentsController(this._sampleFormModel.sample, this._sampleFormModel.mode, this._sampleFormModel);
		if(this._sampleFormModel.mode !== FormMode.VIEW || 
			this._sampleFormModel.mode === FormMode.VIEW && !commentsController.isEmpty()) {
			commentsController.init($container);
			return true;
		} else {
			return false;
		}
	}
	
	this.getLastStorageController = function() {
		return this._storageControllers[this._storageControllers.length-1];
	}
	
	this.getNextCopyCode = function(callback) {
		var _this = this;
		mainController.serverFacade.searchWithType(
				this._sampleFormModel.sample.sampleTypeCode,
				this._sampleFormModel.sample.code + "_*",
				false,
				function(results) {
					callback(_this._sampleFormModel.sample.code + "_" + (results.length + 1));
				});
	}
	
	this.deleteSample = function(reason) {
		var _this = this;
		mainController.serverFacade.deleteSamples([this._sampleFormModel.sample.id], reason, function(data) {
			if(data.error) {
				Util.showError(data.error.message);
			} else {
				Util.showSuccess("" + ELNDictionary.Sample + " Deleted");
				if(_this._sampleFormModel.isELNSample) {
					mainController.sideMenu.deleteNodeByEntityPermId(_this._sampleFormModel.sample.permId, true);
				} else {
					mainController.changeView('showSamplesPage', _this._sampleFormModel.sample.experimentIdentifierOrNull);
				}
			}
		});
	}
	
	this.createUpdateCopySample = function(isCopyWithNewCode, linkParentsOnCopy, copyChildrenOnCopy, copyCommentsLogOnCopy) {
		Util.blockUI();
		var _this = this;
		
		//
		// Parents/Children Links
		//
		if(!isCopyWithNewCode && sample.sampleTypeCode !== "REQUEST") { // REQUESTS are validated below
			if(!_this._sampleFormModel.sampleLinksParents.isValid()) {
				return;
			}
			if(!_this._sampleFormModel.sampleLinksChildren.isValid()) {
				return;
			}
		}
		
		var sampleParentsFinal = _this._sampleFormModel.sampleLinksParents.getSamplesIdentifiers();
		
		var sampleParentsRemovedFinal = _this._sampleFormModel.sampleLinksParents.getSamplesRemovedIdentifiers();
		var sampleParentsAddedFinal = _this._sampleFormModel.sampleLinksParents.getSamplesAddedIdentifiers();
		
		var sampleChildrenFinal = _this._sampleFormModel.sampleLinksChildren.getSamplesIdentifiers();
		
		var sampleChildrenRemovedFinal = _this._sampleFormModel.sampleLinksChildren.getSamplesRemovedIdentifiers();
		var sampleChildrenAddedFinal = _this._sampleFormModel.sampleLinksChildren.getSamplesAddedIdentifiers();
		
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
		
		//On Submit
		sample.parents = _this._sampleFormModel.sampleLinksParents.getSamples();
		var continueSampleCreation = function(sample, newSampleParents, samplesToDelete, newChangesToDo) {
			
			//
			// TODO : Remove this hack without removing the New Producs Widget 
			//
			if(sample.sampleTypeCode === "REQUEST") {
				var maxProducts;
				if(profile.sampleTypeDefinitionsExtension && 
					profile.sampleTypeDefinitionsExtension["REQUEST"] && 
					profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"] && 
					profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]) {
					maxProducts = profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]["MAX_COUNT"];
				}
				
				if(maxProducts && (sampleParentsFinal.length + newSampleParents.length) > maxProducts) {
					Util.showError("There is more than " + maxProducts + " product.");
					return;
				}
			}
			
			//
			//Identification Info
			//
			var sampleSpace = sample.spaceCode;
			var sampleProject = null;
			var sampleExperiment = null;
			var sampleCode = sample.code;
			var properties = $.extend(true, {}, sample.properties); //Deep copy that can be modified before sending to the server and gets discarded in case of failure / simulates a rollback.
			
			//
			// Patch annotations in
			//
			if(newSampleParents) {
				var annotationsStateObj = FormUtil.getAnnotationsFromSample(sample);
				var writeNew = false;
				for(var pIdx = 0; pIdx < newSampleParents.length; pIdx++) {
					var newSampleParent = newSampleParents[pIdx];
					if(newSampleParent.annotations) {
						for(var annotationKey in newSampleParent.annotations) {
							if (newSampleParent.annotations.hasOwnProperty(annotationKey)) {
								FormUtil.writeAnnotationForSample(annotationsStateObj, newSampleParent, annotationKey, newSampleParent.annotations[annotationKey]);
								writeNew = true;
							}
						}
					}
				}
				if(writeNew) {
					properties["ANNOTATIONS_STATE"] = FormUtil.getXMLFromAnnotations(annotationsStateObj);
				}
			}
			//
			
			var experimentIdentifier = sample.experimentIdentifierOrNull;
			if(experimentIdentifier) { //If there is a experiment detected, the sample should be attached to the experiment completely.
				sampleSpace = experimentIdentifier.split("/")[1];
				sampleProject = experimentIdentifier.split("/")[2];
				sampleExperiment = experimentIdentifier.split("/")[3];
			}
			
			//Children to create
			var samplesToCreate = [];
			_this._sampleFormModel.sampleLinksChildren.getSamples().forEach(function(child) {
				if(child.newSample) {
				  child.experimentIdentifier = experimentIdentifier;
				  child.properties = {};
				  child.children = [];
					if(profile.storagesConfiguration["isEnabled"]) {
						var uuid = Util.guid();
						var storagePosition = {
								newSample : true,
								code : uuid,
								identifier : "/STORAGE/" + uuid,
								sampleTypeCode : "STORAGE_POSITION",
								properties : {}
						};
						
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["NAME_PROPERTY"]] = $("#childrenStorageSelector").val();
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["ROW_PROPERTY"]] = 1;
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["COLUMN_PROPERTY"]] = 1;
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["BOX_SIZE_PROPERTY"]] = "1X1";
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["BOX_PROPERTY"]] = experimentIdentifier.replace(/\//g,'\/') + "_" + sample.code + "_EXP_RESULTS";
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["USER_PROPERTY"]] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
						storagePosition.properties[profile.storagesConfiguration["STORAGE_PROPERTIES"][0]["POSITION_PROPERTY"]] = "A1";
					
						child.children.push(storagePosition);
					}
					samplesToCreate.push(child);
				}
			});
			
			if(_this._sampleFormModel.sample.children) {
				_this._sampleFormModel.sample.children.forEach(function(child) {
					if(child.newSample) {
						samplesToCreate.push(child);
					} else if(child.deleteSample) {
						if(!samplesToDelete) {
							samplesToDelete = [];
						}
						samplesToDelete.push(child.id);
					}
				});
			}
			
			//Method
			var method = "";
			if(_this._sampleFormModel.mode === FormMode.CREATE) {
				method = "insertSample";
			} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
				method = "updateSample";
			}
			
			var changesToDo = [];
			
			if(_this._plateController) {
				changesToDo = _this._plateController.getChangesToDo();
			} else if(newChangesToDo) {
				changesToDo = newChangesToDo;
			}
			
			var parameters = {
					//API Method
					"method" : method,
					//Identification Info
					"sampleSpace" : sampleSpace,
					"sampleProject" : sampleProject,
					"sampleExperiment" : sampleExperiment,
					"sampleCode" : sampleCode,
					"sampleType" : sample.sampleTypeCode,
					//Other Properties
					"sampleProperties" : properties,
					//Parent links
					"sampleParents": (sampleParentsRemovedFinal.length === 0 && sampleParentsAddedFinal.length === 0)?null:sampleParentsFinal,
					"sampleParentsNew": newSampleParents,
					//Children links
					"sampleChildrenNew": samplesToCreate,
					"sampleChildrenAdded": sampleChildrenAddedFinal,
					"sampleChildrenRemoved": sampleChildrenRemovedFinal,
					//Other Samples
					"changesToDo" : changesToDo
			};
			
			//
			// Copy override - This part modifies what is done for a create/update and adds a couple of extra parameters needed to copy to the bench correctly
			//
			if(isCopyWithNewCode) {
				parameters["method"] = "copySample";
				parameters["sampleCode"] = isCopyWithNewCode;
				parameters["notCopyProperties"] = [];
				parameters["defaultBenchPropertyList"] = [];
				
				if(!copyCommentsLogOnCopy && parameters["sampleProperties"]["XMLCOMMENTS"]) {
					delete parameters["sampleProperties"]["XMLCOMMENTS"];
				}
				
				parameters["sampleParents"] = sampleParentsFinal;
				if(!linkParentsOnCopy) {
					parameters["sampleParents"] = [];
				}
				
				parameters["sampleChildren"] = sampleChildrenFinal;
				if(!copyChildrenOnCopy) {
					parameters["sampleChildren"] = [];
				} else if(profile.storagesConfiguration["isEnabled"]) {
					//1. All properties belonging to benches, to not to copy
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
						parameters[listToUse].push(storagePropertyGroup["BOX_SIZE_PROPERTY"]);
						parameters[listToUse].push(storagePropertyGroup["USER_PROPERTY"]);
						parameters[listToUse].push(storagePropertyGroup["POSITION_PROPERTY"]);
					}
					
					//2. Default Bench properties
					var defaultStoragePropertyGroup = profile.storagesConfiguration["STORAGE_PROPERTIES"][0];
					parameters["defaultBenchProperties"] = {};
					var defaultBench = "";
					var $benchDropdown = FormUtil.getDefaultBenchDropDown();
					if($benchDropdown.length > 1) {
						defaultBench = $benchDropdown.children()[1].value;
					}
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["NAME_PROPERTY"]] = defaultBench;
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["ROW_PROPERTY"]] = 1;
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["COLUMN_PROPERTY"]] = 1;
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["BOX_PROPERTY"]] = sample.experimentIdentifierOrNull.replace(/\//g,'\/') + "_" + isCopyWithNewCode + "_EXP_RESULTS";
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["BOX_SIZE_PROPERTY"]] = "1X1";
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["USER_PROPERTY"]] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
					parameters["defaultBenchProperties"][defaultStoragePropertyGroup["POSITION_PROPERTY"]] = "A1";
				}
				parameters["sampleChildrenNew"] = [];
				parameters["sampleChildrenRemoved"] = [];
			}
			
			//
			// Sending the request to the server
			//
			if(profile.getDefaultDataStoreCode()) {
				
				mainController.serverFacade.createReportFromAggregationService(profile.getDefaultDataStoreCode(), parameters, function(response) {
					_this._createUpdateCopySampleCallback(_this, isCopyWithNewCode, response, samplesToDelete);
				});
				
			} else {
				Util.showError("No DSS available.", function() {Util.unblockUI();});
			}
		}
		
		profile.sampleFormOnSubmit(sample, continueSampleCreation);
		return false;
	}
	
	this._createUpdateCopySampleCallback = function(_this, isCopyWithNewCode, response, samplesToDelete) {
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
				message = "" + ELNDictionary.Sample + " copied with new code: " + isCopyWithNewCode + ".";
			} else if(_this._sampleFormModel.mode === FormMode.CREATE) {
				message = "" + ELNDictionary.Sample + " Created.";
			} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
				message = "" + ELNDictionary.Sample + " Updated.";
			}
			
			var callbackOk = function() {
				if((isCopyWithNewCode || _this._sampleFormModel.mode === FormMode.CREATE || _this._sampleFormModel.mode === FormMode.EDIT) && _this._sampleFormModel.isELNSample) {
					if(_this._sampleFormModel.mode === FormMode.CREATE) {
						mainController.sideMenu.refreshCurrentNode();
					} else if(_this._sampleFormModel.mode === FormMode.EDIT) {
						mainController.sideMenu.refreshNodeParent(_this._sampleFormModel.sample.permId);
					}
				}
				
				var sampleCodeToOpen = null;
				if(isCopyWithNewCode) {
					sampleCodeToOpen = isCopyWithNewCode;
				} else {
					sampleCodeToOpen = _this._sampleFormModel.sample.code;
				}
				
				var searchUntilFound = null;
				    searchUntilFound = function() {
					mainController.serverFacade.searchWithType(_this._sampleFormModel.sample.sampleTypeCode, sampleCodeToOpen, false, function(data) {
						if(data && data.length === 1) {
							mainController.changeView('showViewSamplePageFromPermId',data[0].permId);
							Util.unblockUI();
						} else { //Recursive call
							searchUntilFound();
						}
					});
				}
				
				searchUntilFound(); //First call
			}
			
			if(samplesToDelete) {
				mainController.serverFacade.deleteSamples(samplesToDelete,  "Deleted to trashcan from eln sample form " + _this._sampleFormModel.sample.identifier, 
															function() {
																Util.showSuccess(message, callbackOk);
																_this._sampleFormModel.isFormDirty = false;
															}, 
															false);
			} else {
				Util.showSuccess(message, callbackOk);
				_this._sampleFormModel.isFormDirty = false;
			}
			
			
		} else { //This should never happen
			Util.showError("Unknown Error.", function() {Util.unblockUI();});
		}
	}
}