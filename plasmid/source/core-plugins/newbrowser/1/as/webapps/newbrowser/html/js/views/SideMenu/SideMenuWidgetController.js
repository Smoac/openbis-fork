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
    
    this.deleteUniqueIdAndMoveToParent = function(uniqueId) {
    	var itemsToCheck = [this._sideMenuWidgetModel.menuStructure];
    	var currentItem = null;
    	while(currentItem = itemsToCheck.shift()) {
    		if(currentItem.newMenuIfSelected) {
    			for (var i = 0; i < currentItem.newMenuIfSelected.children.length; i++) {
        			var currentItemChild = currentItem.newMenuIfSelected.children[i];
            		if(currentItemChild.uniqueId === uniqueId) {
            			currentItem.newMenuIfSelected.children.splice(i,1);
            			mainController.changeView(currentItem.newViewIfSelected, currentItem.newViewIfSelectedData);
            			this._sideMenuWidgetModel.pointerToMenuNode = currentItem;
            			this._sideMenuWidgetView.repaint();
            			return;
            		}
            		itemsToCheck.push(currentItemChild);
        		}
    		}
    	}
    };
    
    this.hideSideMenu = function() {
        this._sideMenuWidgetView.hideSideMenu();
    };

    this.showSideMenu = function() {
        this._sideMenuWidgetView.showSideMenu();
    };
    
    this.refreshProject = function(spaceCode, projectCode) {
        var menuItemSpace = this._getSpaceNodeForCode(spaceCode);
        var newMenuIfSelectedProject = {
            children: []
        };
        var projectIdentifier = "/" + spaceCode + "/" + projectCode;
        var menuItemProject = new SideMenuWidgetComponent(true, false, projectCode, projectIdentifier, menuItemSpace, newMenuIfSelectedProject, "showProjectPageFromIdentifier", projectIdentifier, "(Project)");
        menuItemSpace.newMenuIfSelected.children.push(menuItemProject);
        menuItemSpace.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent);
        this._sideMenuWidgetView.repaint();
    };

    this.refreshExperiment = function(experiment, isInventory) {
        var projectIdentifierEnd = experiment.identifier.lastIndexOf("/");
        var projectIdentifier = experiment.identifier.substring(0, projectIdentifierEnd);
        var projectNode = this._getProjectNodeForIdentifier(projectIdentifier);

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
                        var experimentNode = _this._getExperimentNodeForIdentifier(subExperiment.experimentIdentifierOrNull);
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
    this.init = function($container) {
    	this._sideMenuWidgetModel.$container = $container;
        var _this = this;

        this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                new SideMenuWidgetComponent(false, true, "Lab Notebook", "Lab Notebook", this._sideMenuWidgetModel.menuStructure, null, null, null, "")
                );

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
                var menuItemSpace = new SideMenuWidgetComponent(true, false, space.code, space.code, _this._sideMenuWidgetModel.menuStructure, newMenuIfSelectedSpace, 'showSpacePage', space.code, "(Space)");
                _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(menuItemSpace);

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
                    for (var i = 0; i < experiments.result.length; i++) {
                        var experiment = experiments.result[i];
                        experimentsToAskForSamples.push(experiment);
                        var projectIdentifierEnd = experiment.identifier.lastIndexOf("/");
                        var projectIdentifier = experiment.identifier.substring(0, projectIdentifierEnd);
                        var projectNode = _this._getProjectNodeForIdentifier(projectIdentifier);

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
                        projectNode.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent); //Sort Experiments
                    }
                }

                //Fill Sub Experiments
                mainController.serverFacade.listSamplesForExperiments(experimentsToAskForSamples, function(subExperiments) {
                    if (subExperiments.result) {
                        for (var i = 0; i < subExperiments.result.length; i++) {
                            var subExperiment = subExperiments.result[i];
                            var experimentNode = _this._getExperimentNodeForIdentifier(subExperiment.experimentIdentifierOrNull);
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
                            experimentNode.newMenuIfSelected.children.sort(naturalSortSideMenuWidgetComponent); //Sort Sub Experiments
                        }
                    }
                    
                    //Fill Inventory
                    _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                            new SideMenuWidgetComponent(false, true, "Inventory", "Inventory", _this._sideMenuWidgetModel.menuStructure, null, null, null, "")
                            );

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
                        var menuItemSpace = new SideMenuWidgetComponent(true, false, space.code, space.code, _this._sideMenuWidgetModel.menuStructure, newMenuIfSelectedSpace, 'showSpacePage', space.code, "(Space)");
                        _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(menuItemSpace);

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
                    }

                    mainController.serverFacade.listExperiments(projectsToAskForExperiments, function(experiments) {
                        var experimentsToAskForSamples = [];

                        if (experiments.result) {
                            for (var i = 0; i < experiments.result.length; i++) {
                                var experiment = experiments.result[i];
                                experimentsToAskForSamples.push(experiment);
                                var projectIdentifierEnd = experiment.identifier.lastIndexOf("/");
                                var projectIdentifier = experiment.identifier.substring(0, projectIdentifierEnd);
                                var projectNode = _this._getProjectNodeForIdentifier(projectIdentifier);

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
                        }


                        //Fill Utils
                        _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                                new SideMenuWidgetComponent(false, true, "Utilities", "Utilities", _this._sideMenuWidgetModel.menuStructure, null, null, null, "")
                                );
                        _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                                new SideMenuWidgetComponent(true, false, "Sample Browser", "Sample Browser", _this._sideMenuWidgetModel.menuStructure, null, "showSamplesPage", null, "")
                                );
                        if (profile.storagesConfiguration["isEnabled"]) {
                            _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                                    new SideMenuWidgetComponent(true, false, "Storage Manager", "Storage Manager", _this._sideMenuWidgetModel.menuStructure, null, "showStorageManager", null, "")
                                    );
                        }
                        _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                                new SideMenuWidgetComponent(true, false, "Trashcan", "Trashcan", _this._sideMenuWidgetModel.menuStructure, null, "showTrashcanPage", null, "")
                                );
                        _this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.push(
                                new SideMenuWidgetComponent(true, false, "Vocabulary Viewer", "Vocabulary Viewer", _this._sideMenuWidgetModel.menuStructure, null, "showVocabularyManagerPage", null, "")
                                );
                        _this._sideMenuWidgetView.repaintFirst($container);
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
    
    //
    // Utility Methods
    //
    this._getSpaceNodeForCode = function(spaceCode) {
        for (var sIdx = 0; sIdx < this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.length; sIdx++) {
            var spaceNode = this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children[sIdx];
            if (spaceNode.isTitle && !spaceNode.isSelectable) {
                continue;
            }
            if (spaceNode.uniqueId === spaceCode) {
                return spaceNode;
            }
        }
        return null;
    };

    this._getProjectNodeForIdentifier = function(projectIdentifier) {
        for (var sIdx = 0; sIdx < this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children.length; sIdx++) {
            var spaceNode = this._sideMenuWidgetModel.menuStructure.newMenuIfSelected.children[sIdx];
            if (spaceNode.isTitle && !spaceNode.isSelectable) {
                continue;
            }
            var projectsFromSpace = spaceNode.newMenuIfSelected.children;
            for (var pIdx = 0; pIdx < projectsFromSpace.length; pIdx++) {
                var projectNode = projectsFromSpace[pIdx];
                if (projectNode.uniqueId === projectIdentifier) {
                    return projectNode;
                }
            }
        }
        return null;
    };

    this._getExperimentNodeForIdentifier = function(experimentIdentifier) {
        var projectIdentifierEnd = experimentIdentifier.lastIndexOf("/");
        var projectIdentifier = experimentIdentifier.substring(0, projectIdentifierEnd);
        var projectNode = this._getProjectNodeForIdentifier(projectIdentifier);
        var experimentsFromProject = projectNode.newMenuIfSelected.children;
        for (var eIdx = 0; eIdx < experimentsFromProject.length; eIdx++) {
            var experimentNode = experimentsFromProject[eIdx];
            if (experimentNode.uniqueId === experimentIdentifier) {
                return experimentNode;
            }
        }
        return null;
    };
}

function SideMenuWidgetComponent(isSelectable, isTitle, displayName, uniqueId, parent, newMenuIfSelected, newViewIfSelected, newViewIfSelectedData, contextTitle) {
    this.isSelectable = isSelectable;
    this.isTitle = isTitle;
    this.displayName = displayName;
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