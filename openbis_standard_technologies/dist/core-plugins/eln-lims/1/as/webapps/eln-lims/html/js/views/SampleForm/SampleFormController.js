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

function SampleFormController(mainController, mode, sample, paginationInfo) {
	this._mainController = mainController;
	this._sampleFormModel = new SampleFormModel(mode, sample, paginationInfo);
	this._sampleFormView = new SampleFormView(this, this._sampleFormModel);
//	this._storageControllers = [];
	this._plateController = null;
	
	this.init = function(views, loadFromTemplate) {
		// Loading datasets
		var _this = this;
		_this._sampleFormModel.views = views;

		if(mode !== FormMode.CREATE) {
			require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/id/SampleIdentifier", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
					function(SamplePermId, SampleIdentifier, SampleFetchOptions) {
					var id = new SamplePermId(sample.permId);
					var fetchOptions = new SampleFetchOptions();
					fetchOptions.withSpace();
					fetchOptions.withProject();
					fetchOptions.withExperiment();
					fetchOptions.withParents();
					fetchOptions.withChildren();
					mainController.openbisV3.getSamples([ id ], fetchOptions).done(function(map) {
						_this._sampleFormModel.v3_sample = map[id];
						var expeId = _this._sampleFormModel.v3_sample.getExperiment().getIdentifier().getIdentifier();
						var dummySampleId = new SampleIdentifier(IdentifierUtil.createDummySampleIdentifierFromExperimentIdentifier(expeId));
						mainController.openbisV3.getRights([ id, dummySampleId ], null).done(function(rightsByIds) {
							_this._sampleFormModel.rights = rightsByIds[id];
							_this._sampleFormModel.sampleRights = rightsByIds[dummySampleId]; 
							mainController.serverFacade.listDataSetsForSample(_this._sampleFormModel.sample, true, function(datasets) {
								if(!datasets.error) {
									_this._sampleFormModel.datasets = datasets.result;
								}
								
								//Load view
								_this._sampleFormView.repaint(views);
								Util.unblockUI();
							});
						});
					});
			});
		} else {
//			if(sample.sampleTypeCode === "ORDER") {
//				mainController.serverFacade.searchWithIdentifiers(["/ELN_SETTINGS/ORDER_TEMPLATE"], function(data) {
//					if(data[0]) { //Template found
//						sample.properties = data[0].properties;
//					}
//					//Load view
//					_this._sampleFormView.repaint(views, true);
//					Util.unblockUI();
//				});
//			} else {
//				//Load view
//				_this._sampleFormView.repaint(views);
//				Util.unblockUI();
//			}
			//Load view
            _this._sampleFormView.repaint(views, loadFromTemplate);
            Util.unblockUI();
		}
		
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
		
		var samplesToDelete = [this._sampleFormModel.sample.permId];
		
		for(var idx = 0; idx < this._sampleFormModel.sample.children.length; idx++) {
			var child = this._sampleFormModel.sample.children[idx];
			if(child.sampleTypeCode === "STORAGE_POSITION") {
				samplesToDelete.push(child.permId);
			}
		}
		
		mainController.serverFacade.deleteSamples(samplesToDelete, reason, function(response) {
			if(response.error) {
				Util.showError(response.error.message);
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
			Util.showUserError("The same entity can't be a parent and a child, please check: " + intersection);
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
				var minProducts;
				if(profile.sampleTypeDefinitionsExtension && 
					profile.sampleTypeDefinitionsExtension["REQUEST"] && 
					profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"] && 
					profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]) {
					maxProducts = profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]["MAX_COUNT"];
					minProducts = profile.sampleTypeDefinitionsExtension["REQUEST"]["SAMPLE_PARENTS_HINT"][0]["MIN_COUNT"];
				}
				
				if(maxProducts && (sampleParentsFinal.length + newSampleParents.length) > maxProducts) {
					Util.showUserError("There is more than " + maxProducts + " product.");
					return;
				}
				if(minProducts && (sampleParentsFinal.length + newSampleParents.length) < minProducts) {
					Util.showUserError("There is less than " + maxProducts + " product.");
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
					properties["$ANNOTATIONS_STATE"] = FormUtil.getXMLFromAnnotations(annotationsStateObj);
				}
			}
			//
			
			var experimentIdentifier = sample.experimentIdentifierOrNull;
			if(experimentIdentifier) { //If there is a experiment detected, the sample should be attached to the experiment completely.
				sampleSpace = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
				sampleProject = IdentifierUtil.getProjectCodeFromExperimentIdentifier(experimentIdentifier);
				sampleExperiment = IdentifierUtil.getCodeFromIdentifier(experimentIdentifier);
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
						
						var storagePropertyGroup = profile.getStoragePropertyGroup();
						storagePosition.properties[storagePropertyGroup.nameProperty] = $("#childrenStorageSelector").val();
						storagePosition.properties[storagePropertyGroup.rowProperty] = 1;
						storagePosition.properties[storagePropertyGroup.columnProperty] = 1;
						storagePosition.properties[storagePropertyGroup.boxSizeProperty] = "1X1";
						var boxProperty = sample.code + "_EXP_RESULTS";
						if (experimentIdentifier) {
							boxProperty = experimentIdentifier.replace(/\//g,'\/') + "_" + boxProperty;
						}
						storagePosition.properties[storagePropertyGroup.boxProperty] = boxProperty;
						storagePosition.properties[storagePropertyGroup.userProperty] = mainController.serverFacade.openbisServer.getSession().split("-")[0];
						storagePosition.properties[storagePropertyGroup.positionProperty] = "A1";
					
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
						samplesToDelete.push(child.permId);
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
				
				if(!copyCommentsLogOnCopy && parameters["sampleProperties"]["$XMLCOMMENTS"]) {
					delete parameters["sampleProperties"]["$XMLCOMMENTS"];
				}
				
				parameters["sampleParents"] = sampleParentsFinal;
				if(!linkParentsOnCopy) {
					parameters["sampleParents"] = [];
				}
				
				parameters["sampleChildren"] = sampleChildrenFinal;
				if(!copyChildrenOnCopy) {
					parameters["sampleChildren"] = [];
				} else if(profile.storagesConfiguration["isEnabled"]) {
					// Copying children no longer copies storage information
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
			var permId = null;
			if(response.result.columns[2].title === "RESULT" && response.result.rows[0][2].value) {
				permId = response.result.rows[0][2].value;
			}
			
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
				
				var searchUntilFound = null;
				    searchUntilFound = function() {
					mainController.serverFacade.searchWithUniqueId(permId, function(data) {
						if(data && data.length > 0) {
							mainController.changeView('showViewSamplePageFromPermId',data[0].permId);
							Util.unblockUI();
						} else { // Recursive call, only if not found yet due to reindexing
							setTimeout(searchUntilFound, 100);
						}
					});
				}
				searchUntilFound(); //First call
			}
			
			if(samplesToDelete) {
				mainController.serverFacade.deleteSamples(samplesToDelete,  "Deleted to trashcan from eln sample form " + _this._sampleFormModel.sample.identifier, 
															function(response) {
																if(response.error) {
																	Util.showError("Deletions failed, other changes were committed: " + response.error.message, callbackOk);
																} else {
																	Util.showSuccess(message, callbackOk);
																}
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