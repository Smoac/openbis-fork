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

/**
 * Creates an instance of SideMenuWidget.
 *
 * @constructor
 * @this {SideMenuWidgetController}
 * @param {MainController} mainController Used to control view changes.
 */
function SideMenuWidgetController(mainController) {
    this._mainController = mainController;
    this._sideMenuWidgetModel = new SideMenuWidgetModel();
    this._sideMenuWidgetView = new SideMenuWidgetView(this, this._sideMenuWidgetModel);
    
    //
    // External API for real time updates
    //
    
    this.deleteUniqueIdAndMoveToParent = function(uniqueId, notMoveToParent, notRepaint) {
    	var itemsToCheck = [this._sideMenuWidgetModel.menuStructure];
    	var currentItem = null;
    	while(currentItem = itemsToCheck.shift()) {
    		if(currentItem.newMenuIfSelected) {
    			for (var i = 0; i < currentItem.newMenuIfSelected.children.length; i++) {
        			var currentItemChild = currentItem.newMenuIfSelected.children[i];
            		if(currentItemChild.uniqueId === uniqueId) {
            			currentItem.newMenuIfSelected.children.splice(i,1);
            			if(!notMoveToParent) {
            				this._sideMenuWidgetModel.pointerToMenuNode = currentItem;
            				mainController.changeView(currentItem.newViewIfSelected, currentItem.newViewIfSelectedData);
            			}
            			
            			if(!notRepaint) {
            				this._sideMenuWidgetView.repaint();
            			}
            			return;
            		}
            		itemsToCheck.push(currentItemChild);
        		}
    		}
    	}
    };
    
    this.toggleMenuSize = function() {
    	this._sideMenuWidgetView.toggleMenuSize();
    }
    
    this.toggleNavType = function() {
    	this._sideMenuWidgetView.toggleNavType();
    }
    
    this.hideSideMenu = function() {
        this._sideMenuWidgetView.hideSideMenu();
    };

    this.showSideMenu = function() {
        this._sideMenuWidgetView.showSideMenu();
    };
    
    this.getCurrentNodeId = function() {
    	return this._sideMenuWidgetModel.pointerToMenuNode.uniqueId;
    }
    
    this.moveToNodeId = function(uniqueId) {
    	var nodesToCheck = [this._sideMenuWidgetModel.menuStructure];
    	while(nodesToCheck.length > 0) {
    		var current = nodesToCheck[0];
    		nodesToCheck.splice(0, 1);
    		
    		if(current.uniqueId === uniqueId) {
    			this._sideMenuWidgetModel.pointerToMenuNode = current;
    			this._sideMenuWidgetView.repaint();
    			break;
    		} else {
    			if(current.newMenuIfSelected) {
    				for (var i = 0; i < current.newMenuIfSelected.children.length; i++) {
        				nodesToCheck.push(current.newMenuIfSelected.children[i]);
        			}
    			}
    		}
    	}
    }
    
    
    this.refreshProject = function(spaceCode, projectCode) {
        var menuItemSpace = this._getUniqueId(spaceCode);
        var newMenuIfSelectedProject = {
            children: []
        };
        var projectIdentifier = "/" + spaceCode + "/" + projectCode;
        var menuItemProject = new SideMenuWidgetComponent(true, false, projectCode, projectIdentifier, menuItemSpace, newMenuIfSelectedProject, "showProjectPageFromIdentifier", projectIdentifier, "(Project)");
        menuItemSpace.newMenuIfSelected.children.push(menuItemProject);
        menuItemSpace.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent);
        this._sideMenuWidgetView.repaint();
    };

    this._getUniqueId = function(uniqueId) {
    	var itemsToCheck = [this._sideMenuWidgetModel.menuStructure];
    	var currentItem = null;
    	while(currentItem = itemsToCheck.shift()) {
    		if(currentItem.newMenuIfSelected) {
    			for (var i = 0; i < currentItem.newMenuIfSelected.children.length; i++) {
        			var currentItemChild = currentItem.newMenuIfSelected.children[i];
            		if(currentItemChild.uniqueId === uniqueId) {
            			return currentItemChild;
            		}
            		itemsToCheck.push(currentItemChild);
        		}
    		}
    	}
    };
    
    this.updateExperimentName = function(experiment) {
    	var node = this._getUniqueId(experiment.identifier);
    	
    	var displayName = null;
        if (profile.hideCodes) {
            displayName = experiment.properties[profile.propertyReplacingCode];
        }
        if (!displayName) {
            displayName = experiment.code;
        }
        
    	node.displayName = displayName;
    	this._sideMenuWidgetView.repaint();
    }
    
    this.refreshExperiment = function(experiment, isInventory) {
        var projectIdentifierEnd = experiment.identifier.lastIndexOf("/");
        var projectIdentifier = experiment.identifier.substring(0, projectIdentifierEnd);
        var projectNode = this._getUniqueId(projectIdentifier);

        var newMenuIfSelectedExperiment = {
            children: []
        };

        var displayName = null;
        if (profile.hideCodes) {
            displayName = experiment.properties[profile.propertyReplacingCode];
        }
        if (!displayName) {
            displayName = experiment.code;
        }

        var menuItemExperiment = null;
        if (isInventory) {
            menuItemExperiment = new SideMenuWidgetComponent(true, false, displayName, experiment.identifier, projectNode, null, "showSamplesPage", experiment.identifier);
        } else {
            menuItemExperiment = new SideMenuWidgetComponent(true, false, displayName, experiment.identifier, projectNode, newMenuIfSelectedExperiment, "showExperimentPageFromIdentifier", experiment.identifier, "(Experiment)");
        }

        projectNode.newMenuIfSelected.children.push(menuItemExperiment);
        projectNode.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent);
        this._sideMenuWidgetView.repaint();
    };

    this.refreshSubExperiment = function(experimentIdentifier) {
        var _this = this;
        mainController.serverFacade.listExperimentsForIdentifiers([experimentIdentifier], function(data) {
            var experimentToAskForSamples = data.result[0];
            mainController.serverFacade.listSamplesForExperiments([experimentToAskForSamples], function(subExperiments) {
                var nodeCleared = false;
                for (var i = 0; i < subExperiments.result.length; i++) {
                    var subExperiment = subExperiments.result[i];
                    if (subExperiment.experimentIdentifierOrNull) {
                        var experimentNode = _this._getUniqueId(subExperiment.experimentIdentifierOrNull);
                        var displayName = null;
                        if (profile.hideCodes) {
                            displayName = subExperiment.properties[profile.propertyReplacingCode];
                        }
                        if (!displayName) {
                            displayName = subExperiment.code;
                        }
                        if (!nodeCleared) {
                            experimentNode.newMenuIfSelected.children = [];
                            nodeCleared = true;
                        }
                        var menuItemSubExperiment = new SideMenuWidgetComponent(true, false, displayName, subExperiment.identifier, experimentNode, null, "showViewSamplePageFromPermId", subExperiment.permId, "(Sub Exp.)");
                        experimentNode.newMenuIfSelected.children.push(menuItemSubExperiment);
                    }
                }
                _this._sideMenuWidgetView.repaint();
            });
        });
    };
    
    //
    // Init method that builds the menu object hierarchy
    //
    this.init = function($container, initCallback) {
    	this._sideMenuWidgetModel.$container = $container;
        var _this = this;
        
        var labNotebookNode = new SideMenuWidgetComponent(true, true, "LAB NOTEBOOK", "LAB_NOTEBOOK", this._sideMenuWidgetModel.menuStructure, { children: [] }, 'showLabNotebookPage', null, "");
        this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(labNotebookNode);

        mainController.serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
            //Fill Spaces
            var spaces = dataWithSpacesAndProjects.result;
            var projectsToAskForExperiments = [];
            for (var i = 0; i < spaces.length; i++) {
                var space = spaces[i];

                if ($.inArray(space.code, profile.inventorySpaces) !== -1 && profile.inventorySpaces.length > 0) {
                    continue;
                }

                var newMenuIfSelectedSpace = {
                    children: []
                };
                var menuItemSpace = new SideMenuWidgetComponent(true, false, space.code, space.code, labNotebookNode, newMenuIfSelectedSpace, 'showSpacePage', space.code, "(Space)");
                labNotebookNode.newMenuIfSelected.children.push(menuItemSpace);
                
                //Fill Projects
                for (var j = 0; j < space.projects.length; j++) {
                    var project = space.projects[j];
                    delete project["@id"];
                    delete project["@type"];
                    projectsToAskForExperiments.push(project);

                    var newMenuIfSelectedProject = {
                        children: []
                    };
                    var projectIdentifier = "/" + project.spaceCode + "/" + project.code;
                    var menuItemProject = new SideMenuWidgetComponent(true, false, project.code, projectIdentifier, menuItemSpace, newMenuIfSelectedProject, "showProjectPageFromPermId", project.permId, "(Project)");
                    newMenuIfSelectedSpace.children.push(menuItemProject);
                }
                
                newMenuIfSelectedSpace.children.sort(naturalSortSideMenuWidgetComponent); //Sort Projects
            }
            
            //Fill Experiments
            mainController.serverFacade.listExperiments(projectsToAskForExperiments, function(experiments) {
                var experimentsToAskForSamples = [];

                if (experiments.result) {
                    var toSortExperiments = {};

                    for (var i = 0; i < experiments.result.length; i++) {
                        var experiment = experiments.result[i];
                        experimentsToAskForSamples.push(experiment);
                        var projectIdentifierEnd = experiment.identifier.lastIndexOf("/");
                        var projectIdentifier = experiment.identifier.substring(0, projectIdentifierEnd);
                        var projectNode = _this._getUniqueId(projectIdentifier);
                        toSortExperiments[projectNode.uniqueId] = projectNode;
                        var newMenuIfSelectedExperiment = {
                            children: []
                        };

                        var displayName = null;
                        if (profile.hideCodes) {
                            displayName = experiment.properties[profile.propertyReplacingCode];
                        }
                        if (!displayName) {
                            displayName = experiment.code;
                        }

                        var menuItemExperiment = new SideMenuWidgetComponent(true, false, displayName, experiment.identifier, projectNode, newMenuIfSelectedExperiment, "showExperimentPageFromIdentifier", experiment.identifier, "(Experiment)");
                        projectNode.newMenuIfSelected.children.push(menuItemExperiment);
                    }

                    for(uniqueId in toSortExperiments) {
                            toSortExperiments[uniqueId].newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent); //Sort Experiments
                    }
                }

                //Fill Sub Experiments
                mainController.serverFacade.listSamplesForExperiments(experimentsToAskForSamples, function(subExperiments) {
                    if (subExperiments.result) {
                        var toSortSubExperiments = {};

                        for (var i = 0; i < subExperiments.result.length; i++) {
                            var subExperiment = subExperiments.result[i];
                            if(!profile.isSampleTypeHidden(subExperiment.sampleTypeCode)) {
                            	var experimentNode = _this._getUniqueId(subExperiment.experimentIdentifierOrNull);
                                toSortSubExperiments[experimentNode.uniqueId] = experimentNode;
                                if (subExperiment.experimentIdentifierOrNull) {
                                    var displayName = null;
                                    if (profile.hideCodes) {
                                        displayName = subExperiment.properties[profile.propertyReplacingCode];
                                    }
                                    if (!displayName) {
                                        displayName = subExperiment.code;
                                    }
                                    var menuItemSubExperiment = new SideMenuWidgetComponent(true, false, displayName, subExperiment.identifier, experimentNode, null, "showViewSamplePageFromPermId", subExperiment.permId, "(Sub Exp.)");
                                    experimentNode.newMenuIfSelected.children.push(menuItemSubExperiment);
                                }
                            }
                        }

                        var childrenLimit = 20;
                        for(uniqueId in toSortSubExperiments) {
                        	var experimentNode = toSortSubExperiments[uniqueId];
                        	if(experimentNode.newMenuIfSelected.children.length > childrenLimit) {
                        		experimentNode.newMenuIfSelected.children = [];
                        		var menuItemSubExperiment = new SideMenuWidgetComponent(false, false, "Too many Experiment components for the menu, please check the experiment form.", null, experimentNode, null, null, null, "(Too many Experiment components.)");
                        		experimentNode.newMenuIfSelected.children.push(menuItemSubExperiment);
                        	} else {
                        		experimentNode.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent); //Sort Sub Experiments
                        	}
                        }
                        
                    }


                    
                    //Fill Inventory
                    var inventoryNode = new SideMenuWidgetComponent(true, true, "INVENTORY", "INVENTORY", _this._sideMenuWidgetModel.menuStructure, { children: [] }, 'showInventoryPage', null, "");
                    _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(inventoryNode);

                    //Fill Spaces
                    var spaces = dataWithSpacesAndProjects.result;
                    var projectsToAskForExperiments = [];
                    for (var i = 0; i < spaces.length; i++) {
                        var space = spaces[i];

                        if ($.inArray(space.code, profile.inventorySpaces) === -1) {
                            continue;
                        }

                        var newMenuIfSelectedSpace = {
                            children: []
                        };
                        var menuItemSpace = new SideMenuWidgetComponent(true, false, space.code, space.code, inventoryNode, newMenuIfSelectedSpace, 'showSpacePage', space.code, "(Space)");
                        inventoryNode.newMenuIfSelected.children.push(menuItemSpace);

                        //Fill Projects
                        for (var j = 0; j < space.projects.length; j++) {
                            var project = space.projects[j];
                            delete project["@id"];
                            delete project["@type"];
                            projectsToAskForExperiments.push(project);

                            var newMenuIfSelectedProject = {
                                children: []
                            };
                            var projectIdentifier = "/" + project.spaceCode + "/" + project.code;
                            var menuItemProject = new SideMenuWidgetComponent(true, false, project.code, projectIdentifier, menuItemSpace, newMenuIfSelectedProject, "showProjectPageFromPermId", project.permId, "(Project)");
                            newMenuIfSelectedSpace.children.push(menuItemProject);
                        }
                        newMenuIfSelectedSpace.children.sort(naturalSortSideMenuWidgetComponent); //Sort Projects
                    }

                    mainController.serverFacade.listExperiments(projectsToAskForExperiments, function(experiments) {
                        var experimentsToAskForSamples = [];

                        if (experiments.result) {
                            for (var i = 0; i < experiments.result.length; i++) {
                                var experiment = experiments.result[i];
                                experimentsToAskForSamples.push(experiment);
                                var projectIdentifierEnd = experiment.identifier.lastIndexOf("/");
                                var projectIdentifier = experiment.identifier.substring(0, projectIdentifierEnd);
                                var projectNode = _this._getUniqueId(projectIdentifier);

                                var newMenuIfSelectedExperiment = {
                                    children: []
                                };

                                var displayName = null;
                                if (profile.hideCodes) {
                                    displayName = experiment.properties[profile.propertyReplacingCode];
                                }
                                if (!displayName) {
                                    displayName = experiment.code;
                                }

                                var menuItemExperiment = new SideMenuWidgetComponent(true, false, displayName, experiment.identifier, projectNode, newMenuIfSelectedExperiment, "showSamplesPage", experiment.identifier, "");
                                projectNode.newMenuIfSelected.children.push(menuItemExperiment);
                            }
                            projectNode.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent); //Sort Experiments
                        }

                        //Clean unused menu parts
                        if(!profile.mainMenu.showLabNotebook) {
                        	_this.deleteUniqueIdAndMoveToParent("LAB_NOTEBOOK", true, true);
                        }
                        
                        if(!profile.mainMenu.showInventory) {
                        	_this.deleteUniqueIdAndMoveToParent("INVENTORY", true, true);
                        }
                        
                        //Fill Utils
                        var utilities = new SideMenuWidgetComponent(true, true, "UTILITIES", "UTILITIES", _this._sideMenuWidgetModel.menuStructure, { children : [] } , null, null, "");
                        _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(utilities);
                        
            			_this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                                new SideMenuWidgetComponent(true, false, "ABOUT", "ABOUT", _this._sideMenuWidgetModel.menuStructure, null, "showAbout", null, "")
                        );
            			
                        if(profile.mainMenu.showDrawingBoard) {
                        	utilities.newMenuIfSelected.children.push(
                                	new SideMenuWidgetComponent(true, false, "DRAWING BOARD", "DRAWING_BOARD", utilities, null, "showDrawingBoard", null, "")
                                );
                        }
                        
                        if(profile.mainMenu.showSampleBrowser) {
                        	 utilities.newMenuIfSelected.children.push(
                                     new SideMenuWidgetComponent(true, false, "SAMPLE BROWSER", "SAMPLE_BROWSER", utilities, null, "showSamplesPage", null, "")
                                     );
                        }
                        
                        if(profile.mainMenu.showExports) {
                       	 utilities.newMenuIfSelected.children.push(
                                    new SideMenuWidgetComponent(true, false, "EXPORT BUILDER", "EXPORT_BUILDER", utilities, null, "showExportTreePage", null, "")
                                    );
                        }
                       
                        if(profile.mainMenu.showStorageManager && profile.storagesConfiguration["isEnabled"]) {
                        	utilities.newMenuIfSelected.children.push(
                                    new SideMenuWidgetComponent(true, false, "STORAGE MANAGER", "STORAGE_MANAGER", utilities, null, "showStorageManager", null, "")
                                    );
                        }
                        
                        if(profile.mainMenu.showAdvancedSearch) {
                        	utilities.newMenuIfSelected.children.push(
                                    new SideMenuWidgetComponent(true, false, "ADVANCED SEARCH", "ADVANCED_SEARCH", utilities, null, "showAdvancedSearchPage", null, "")
                                    );
                        }
                        
                        if(profile.mainMenu.showTrashcan) {
                        	utilities.newMenuIfSelected.children.push(
                                    new SideMenuWidgetComponent(true, false, "TRASHCAN", "TRASHCAN", utilities, null, "showTrashcanPage", null, "")
                                    );
                        }
                        
                        if(profile.mainMenu.showVocabularyViewer) {
	                        utilities.newMenuIfSelected.children.push(
	                                new SideMenuWidgetComponent(true, false, "VOCABULARY VIEWER", "VOCABULARY_VIEWER", utilities, null, "showVocabularyManagerPage", null, "")
	                                );
                        }
                        
                        var nextToDo = function() {
                        	_this._sideMenuWidgetView.repaintFirst($container);
                			initCallback();
                        }
                        
                        if(profile.mainMenu.showUserManager) {
                        	mainController.serverFacade.listPersons(function(data) {
                    			if(data.result && data.result.length > 0) {
                    				utilities.newMenuIfSelected.children.push(
                                            new SideMenuWidgetComponent(true, false, "USER MANAGER", "USER_MANAGER", utilities, null, "showUserManagerPage", null, "")
                                    );
                    			}
                    			
                    			nextToDo();
                            });
                        } else {
                        	nextToDo();
                        }
                        
                    });

                });

            });
        });

        $(window).scroll(function(event) {
            var sideMenuWidth = $("#sideMenu").width();
            var windowWidth = $(window).width();
            var ratio = sideMenuWidth / windowWidth;
            if (ratio < 0.9) { //For small screens where the menu takes all the screen, we don't move it
                var $element = $("#sideMenu");
                var scrollTop = $(document).scrollTop();
                $element.css('top', scrollTop + "px");
            }
        });

        $(window).resize(function(event) {
            //Use all height
            var $element = $("#sideMenu");
            var windowHeight = $(window).height();
            $element.css('min-height', windowHeight + "px");

            //Set the children of the menu scrollable
            var $elementHead = $("#sideMenuHeader");
            var sideMenuHeaderHeight = $elementHead.height();
            var $elementBody = $("#sideMenuBody");
            $elementBody.css('overflow-y', 'auto');
            $elementBody.css('overflow-x', 'hidden');
            //TO-DO 17px is a hack to be able to scroll properly to the last item on the iPad
            $elementBody.css('max-height', (windowHeight - sideMenuHeaderHeight - 17) + "px");
        });
    };

}

function SideMenuWidgetComponent(isSelectable, isTitle, displayName, uniqueId, parent, newMenuIfSelected, newViewIfSelected, newViewIfSelectedData, contextTitle) {
    this.isSelectable = isSelectable;
    this.isTitle = isTitle;
    this.displayName = displayName; //(displayName.charAt(0) + displayName.slice(1).toLowerCase()).replace(/_/g, ' ');
    this.uniqueId = uniqueId;
    this.contextTitle = contextTitle;
    this.parent = parent;
    this.newMenuIfSelected = newMenuIfSelected;
    this.newViewIfSelected = newViewIfSelected;
    this.newViewIfSelectedData = newViewIfSelectedData;
}

var naturalSortSideMenuWidgetComponent = function(componentA, componentB){
  	return naturalSort(componentA.displayName, componentB.displayName);
}