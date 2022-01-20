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

function SampleTableController(parentController, title, experimentIdentifier, projectPermId, showInProjectOverview, experiment, heightOverride) {
	this._parentController = parentController;
	this._sampleTableModel = new SampleTableModel(title, experimentIdentifier, projectPermId, showInProjectOverview, experiment, heightOverride);
	this._sampleTableView = new SampleTableView(this, this._sampleTableModel);
	this.typeAndFileController = null;

	this.init = function(views) {
		var _this = this;
		Util.blockUI();
		
		var callback = function() {
			_this._sampleTableView.repaint(views);
			Util.unblockUI();
		};
		
		if(this._sampleTableModel.experimentIdentifier || projectPermId) {
			this._loadExperimentData(callback);
		} else {
			callback();
		}
	}
	
	this._loadExperimentData = function(callback) {
		var _this = this;
		
		var properyKeyValueList = null;
		if (this._sampleTableModel.showInProjectOverview) {
			properyKeyValueList = [{ "$SHOW_IN_PROJECT_OVERVIEW" : "\"true\"" }];
		}
		
		//
		var advancedSampleSearchCriteria = null;
		
		if(this._sampleTableModel.experiment) {
			advancedSampleSearchCriteria = {
					entityKind : "SAMPLE",
					logicalOperator : "AND",
					rules : { "1" : { type : "Experiment", name : "ATTR.PERM_ID", value : this._sampleTableModel.experiment.permId } }
			}
		} else if(this._sampleTableModel.projectPermId) {
			advancedSampleSearchCriteria = {
					entityKind : "SAMPLE",
					logicalOperator : "AND",
					rules : { "1" : { type : "Experiment", name : "ATTR.PROJECT_PERM_ID", value : this._sampleTableModel.projectPermId } }
			}
		}
		
		if(this._sampleTableModel.showInProjectOverview) {
			advancedSampleSearchCriteria.rules["2"] = { type : "Property", name : "$SHOW_IN_PROJECT_OVERVIEW", value : "true", operator : "thatEqualsString" };
		}
		//
		require(["as/dto/sample/id/SampleIdentifier"], function(SampleIdentifier) {
			var expeId = _this._sampleTableModel.experimentIdentifier;
			if (expeId) {
				var dummySampleId = new SampleIdentifier(IdentifierUtil.createDummySampleIdentifierFromExperimentIdentifier(expeId));
				mainController.openbisV3.getRights([ dummySampleId], null).done(function(rightsByIds) {
					_this._sampleTableModel.sampleRights = rightsByIds[dummySampleId];
					callback();
				});
			} else
			{
				callback();
			}
		});
		this._reloadTableWithAllSamples(advancedSampleSearchCriteria);
	}
	
	this._reloadTableWithAllSamples = function(advancedSampleSearchCriteria) {
			var _this = this;
			//Create and display table
			var withExperiment = !this._sampleTableModel.experimentIdentifier && !this._sampleTableModel.experiment;
			var tableHeight = 90;
			if(this._sampleTableModel.heightOverride) {
				tableHeight = this._sampleTableModel.heightOverride;
			}
            this._dataGridController = SampleDataGridUtil.getSampleDataGrid(this._sampleTableModel.experimentIdentifier || this._sampleTableModel.projectPermId,
                    advancedSampleSearchCriteria, null, null, null, null, null, null, true, true,
                    withExperiment, tableHeight);
			
			
			var extraOptions = [];
			extraOptions.push({ name : "Delete", action : function(selected) {
				if(selected != undefined && selected.length == 0){
					Util.showUserError("Please select at least one " + ELNDictionary.sample + " to delete!");
				} else {
					var warningText = "The next " + ELNDictionary.samples + " will be deleted: ";
					
					var sampleIdentifiers = [];
					for(var sIdx = 0; sIdx < selected.length; sIdx++) {
						sampleIdentifiers.push(selected[sIdx].identifier);
					}
					
					Util.blockUI();
					mainController.serverFacade.searchWithIdentifiers(sampleIdentifiers, function(selectedSamples) {
						var samplePermIds = [];
						for(var sIdx = 0; sIdx < selectedSamples.length; sIdx++) {
							var selectedSample = selectedSamples[sIdx];
							samplePermIds.push(selectedSample.permId);
							warningText += selectedSample.identifier + " ";
							
							for(var idx = 0; idx < selectedSample.children.length; idx++) {
								var child = selectedSample.children[idx];
								if(child.sampleTypeCode === "STORAGE_POSITION") {
									samplePermIds.push(child.permId);
								}
							}
						}
						
						var modalView = new DeleteEntityController(function(reason) {
							mainController.serverFacade.deleteSamples(samplePermIds, reason, function(data) {
								if(data.error) {
									Util.showError(data.error.message);
								} else {
									Util.showSuccess("" + ELNDictionary.Sample + "/s moved to Trashcan");
									mainController.refreshView();
								}
							});
						}, true, warningText);
						modalView.init();
					});
					
					
				}
			}});
	
            extraOptions.push({ name : "Move", action : function(selected) {
                if(selected != undefined && selected.length == 0){
                    Util.showUserError("Please select at least one " + ELNDictionary.sample + " to move!");
                } else {
                    var sampleIds = selected.map(s => s.permId);
                    var moveSampleController = new MoveSampleController(sampleIds, function() {
                        mainController.refreshView();
                    });
                    moveSampleController.init();
                }
            }});

			if(profile.mainMenu.showBarcodes) {
                extraOptions.push({ name : "Generate Barcodes", action : function(selected) {
			        var selectedBarcodes = [];
			        for(var sIdx = 0; sIdx < selected.length; sIdx++) {
                        if(selected[sIdx].properties["$BARCODE"]) {
                            selectedBarcodes.push(selected[sIdx].properties["$BARCODE"]);
                        } else {
                            selectedBarcodes.push(selected[sIdx].permId);
                        }
			        }

                    document.title = "Barcodes Generator";
			        var barcodesGeneratorViews = mainController._getNewViewModel(true, true, false);
			        BarcodeUtil.preGenerateBarcodes(barcodesGeneratorViews, selectedBarcodes);
			        this.currentView = null;
                }});

                extraOptions.push({ name : "Update Custom Barcodes", action : function(selected) {
			        BarcodeUtil.readBarcode(selected);
			        this.currentView = null;
                }});
			}
			
			this._dataGridController.init(this._sampleTableView.getTableContainer(), extraOptions);
	}
	
	this.refreshHeight = function() {
		if (this._dataGridController) {
			this._dataGridController.refreshHeight();
		}
	}

    this.registerSamples = function(experimentIdentifier) {
        var _this = this;
        var allowedSampleTypes = this.getAllowedSampleTypes();
        var experimentsByType = {};
        var spacesByType = {};
        if (this._sampleTableModel.sampleTypeCodeToUse) {
            allowedSampleTypes = [this._sampleTableModel.sampleTypeCodeToUse, "STORAGE_POSITION"];
            experimentsByType[this._sampleTableModel.sampleTypeCodeToUse] = experimentIdentifier;
            var space = IdentifierUtil.getSpaceCodeFromIdentifier(experimentIdentifier);
            spacesByType["STORAGE_POSITION"] = profile.getStorageSpaceForSpace(space);
        } else {
            allowedSampleTypes.forEach(t => experimentsByType[t] = experimentIdentifier);
        }
        var title = 'Register ' + ELNDictionary.Samples;
        var batchController = new BatchController(title, "REGISTRATION", allowedSampleTypes, function(file) {
            Util.blockUI();
            mainController.serverFacade.fileUpload(file, function() {
                mainController.serverFacade.registerSamples(allowedSampleTypes, experimentsByType, spacesByType, "sample-file-upload", 
                function(result) {
                    _this._handleResult(result, "created", experimentIdentifier);
                });
            });
        });
        batchController.init();
    }

    this.updateSamples = function(experimentIdentifier) {
        var _this = this;
        var allowedSampleTypes = this.getAllowedSampleTypes();
        var experimentsByType = {};
        var spacesByType = {};
        if (this._sampleTableModel.sampleTypeCodeToUse) {
            allowedSampleTypes = [this._sampleTableModel.sampleTypeCodeToUse, "STORAGE_POSITION"];
        }
        var title = 'Update ' + ELNDictionary.Samples;
        var batchController = new BatchController(title, "UPDATE", allowedSampleTypes, function(file) {
            Util.blockUI();
            mainController.serverFacade.fileUpload(file, function() {
                mainController.serverFacade.updateSamples(allowedSampleTypes, "sample-file-upload", 
                        function(result) {
                    _this._handleResult(result, "updated", experimentIdentifier);
                });
            });
        });
        batchController.init();
    }

    this.getAllowedSampleTypes = function() {
        return profile.getAllSampleTypes().map(t => t.code).filter(function(type) {
            return profile.isSampleTypeHidden(type) == false;
        });
    }

    this._handleResult = function(result, verb, experimentIdentifier) {
        Util.showSuccess(result[0].length + " " + ELNDictionary.Samples + " successfully " + verb, function() {
            Util.unblockUI();
            mainController.changeView('showSamplesPage', experimentIdentifier);
        });
    }
    
    this._countEntities = function(description) {
        if (description.length == 0) {
            return 0;
        }
        
    }
}