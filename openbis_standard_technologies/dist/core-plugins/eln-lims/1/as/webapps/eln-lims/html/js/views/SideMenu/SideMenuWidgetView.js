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
 * Creates an instance of SideMenuWidgetView.
 *
 * @constructor
 * @this {SideMenuWidgetView}
 */
function SideMenuWidgetView(sideMenuWidgetController, sideMenuWidgetModel) {
    this._sideMenuWidgetController = sideMenuWidgetController;
    this._sideMenuWidgetModel = sideMenuWidgetModel;
    
    var toggleMenuSizeBig = false;
    var DISPLAY_NAME_LENGTH_SHORT = 15;
    var DISPLAY_NAME_LENGTH_LONG = 300;
    var cutDisplayNameAtLength = DISPLAY_NAME_LENGTH_SHORT; // Fix for long names
    
    this.repaint = function($container) {
        var _this = this;
        var $widget = $("<div>");
        //
        // Fix Header
        //
        var $header = $("<div>", {"id": "sideMenuHeader"});
        	$header.css("background-color", "rgb(248, 248, 248)");
        	$header.css("padding", "10px");
        var searchDomains = profile.getSearchDomains();

        var searchFunction = function() {
            var searchText = $("#search").val();
            var domainIndex = $("#search").attr("domain-index");
            var searchDomain = null;
            var searchDomainLabel = null;
            
            if(domainIndex) {
                searchDomain = profile.getSearchDomains()[domainIndex].name;
                searchDomainLabel = profile.getSearchDomains()[domainIndex].label;
            } else {
                searchDomain = profile.getSearchDomains()[0].name;
                searchDomainLabel = profile.getSearchDomains()[0].label;
            }

            var argsMap = {
                "searchText": searchText,
                "searchDomain": searchDomain,
                "searchDomainLabel": searchDomainLabel
            };
            var argsMapStr = JSON.stringify(argsMap);

            mainController.changeView("showSearchPage", argsMapStr);
        };
        
        var dropDownSearch = null;
        if (searchDomains.length > 0) {
            //Prefix function
            var selectedFunction = function(selectedSearchDomain, domainIndex) {
                return function() {
                    var $search = $("#search");
                    $search.attr("placeholder", selectedSearchDomain.label + " Search");
                    $search.attr("domain-index", domainIndex);
                };
            };

            //Dropdown elements
            var dropDownComponents = [];
            for (var i = 0; i < searchDomains.length; i++) {
                dropDownComponents.push({
                    href: selectedFunction(searchDomains[i], i),
                    title: searchDomains[i].label,
                    id: searchDomains[i].name
                });
            }

            dropDownSearch = FormUtil.getDropDownToogleWithSelectedFeedback(null, dropDownComponents, true, searchFunction);
            dropDownSearch.change();
        }


        var searchElement = $("<input>", {"id": "search", "type": "text", "class": "form-control search-query", "placeholder": "Global Search"});
        searchElement.keypress(function (e) {
        	 var key = e.which;
        	 if(key == 13)  // the enter key code
        	  {
        		searchFunction();
        	    return false;  
        	  }
        });
        searchElement.css({"display" : "inline", "width" : "50%"});
        searchElement.css({"padding-top" : "2px"});
        searchElement.css({"margin-left" : "2px"});
        searchElement.css({"margin-right" : "2px"});
        
        var logoutButton = FormUtil.getButtonWithIcon("glyphicon-off", function() {
        	$('body').addClass('bodyLogin');
            mainController.serverFacade.logout(function(data) {
                $("#login-form-div").show();
                $("#mainContainer").hide();
            });
        });
        
        var $searchForm = $("<form>", { "onsubmit": "return false;" })
        					.append(logoutButton)
        					.append(searchElement)
        					.append(dropDownSearch);
        $searchForm.css("width", "100%");
        
        $header.append($searchForm);
        
        var $body = $("<div>", {"id": "sideMenuBody"});
        $body.css("overflow-y", "auto");
        
        $widget.append($header)
               .append($body);

        $container.empty();
        $container.append($widget);

        //
        // Print Menu
        //
        this._sideMenuWidgetModel.menuDOMBody = $body;
        this.repaintTreeMenuDinamic();
    };
    
    this.getLinkForNode = function(displayName, menuId, view, viewData) {
    	var href = Util.getURLFor(menuId, view, viewData);
    	displayName = String(displayName).replace(/<(?:.|\n)*?>/gm, ''); //Clean any HTML tags
        var $menuItemLink = $("<a>", {"href": href, "class" : "browser-compatible-javascript-link browser-compatible-javascript-link-tree" }).text(displayName);
        return $menuItemLink[0].outerHTML;
    }
    
    this.repaintTreeMenuDinamic = function() {
    	var _this = this;
        this._sideMenuWidgetModel.menuDOMBody.empty();
        var $tree = $("<div>", { "id" : "tree" });
        
        //
        // Body
        //
        
        var treeModel = [];
        
        if(profile.mainMenu.showLabNotebook) {
        	var labNotebookLink = _this.getLinkForNode("Lab Notebook", "LAB_NOTEBOOK", "showLabNotebookPage", null);
        	treeModel.push({ title : labNotebookLink, entityType: "LAB_NOTEBOOK", key : "LAB_NOTEBOOK", folder : true, lazy : true, view : "showLabNotebookPage", icon : "glyphicon glyphicon-book" });
        }
        
        if(profile.mainMenu.showInventory) {
        	var inventoryLink = _this.getLinkForNode("Inventory", "INVENTORY", "showInventoryPage", null);
        	treeModel.push({ title : inventoryLink, entityType: "INVENTORY", key : "INVENTORY", folder : true, lazy : true, view : "showInventoryPage" });
        }
        
        var treeModelUtils = [];
        
        if(profile.mainMenu.showDrawingBoard) {
        	var drawingBoardLink = _this.getLinkForNode("Drawing Board", "DRAWING_BOARD", "showDrawingBoard", null);
        	treeModelUtils.push({ title : drawingBoardLink, entityType: "DRAWING_BOARD", key : "DRAWING_BOARD", folder : false, lazy : false, view : "showDrawingBoard" });
        }
        
        if(profile.mainMenu.showSampleBrowser) {
        	var sampleBrowserLink = _this.getLinkForNode("" + ELNDictionary.Sample + " Browser", "SAMPLE_BROWSER", "showSamplesPage", null);
        	treeModelUtils.push({ title : sampleBrowserLink, entityType: "SAMPLE_BROWSER", key : "SAMPLE_BROWSER", folder : false, lazy : false, view : "showSamplesPage", icon : "glyphicon glyphicon-list-alt" });
        }
        
        if(profile.mainMenu.showVocabularyViewer) {
        	var vocabularyBrowserLink = _this.getLinkForNode("Vocabulary Browser", "VOCABULARY_BROWSER", "showVocabularyManagerPage", null);
        	treeModelUtils.push({ title : vocabularyBrowserLink, entityType: "VOCABULARY_BROWSER", key : "VOCABULARY_BROWSER", folder : false, lazy : false, view : "showVocabularyManagerPage", icon : "glyphicon glyphicon-list-alt" });
        }
        
        if(profile.mainMenu.showAdvancedSearch) {
        	var advancedSearchLink = _this.getLinkForNode("Advanced Search", "ADVANCED_SEARCH", "showAdvancedSearchPage", null);
        	treeModelUtils.push({ title : advancedSearchLink, entityType: "ADVANCED_SEARCH", key : "ADVANCED_SEARCH", folder : false, lazy : false, view : "showAdvancedSearchPage", icon : "glyphicon glyphicon-search" });
        }
        
        if(profile.mainMenu.showExports) {
        	var exportBuilderLink = _this.getLinkForNode("Export Builder", "EXPORT_BUILDER", "showExportTreePage", null);
        	treeModelUtils.push({ title : exportBuilderLink, entityType: "EXPORT_BUILDER", key : "EXPORT_BUILDER", folder : false, lazy : false, view : "showExportTreePage", icon : "glyphicon glyphicon-export" });
        }
        
        if(profile.mainMenu.showStorageManager) {
        	var storageManagerLink = _this.getLinkForNode("Storage Manager", "STORAGE_MANAGER", "showStorageManager", null);
        	treeModelUtils.push({ title : storageManagerLink, entityType: "STORAGE_MANAGER", key : "STORAGE_MANAGER", folder : false, lazy : false, view : "showStorageManager" });
        }
        
        if(profile.mainMenu.showUserManager && profile.isAdmin) {
        	var userManagerLink = _this.getLinkForNode("User Manager", "USER_MANAGER", "showUserManagerPage", null);
        	treeModelUtils.push({ title : userManagerLink, entityType: "USER_MANAGER", key : "USER_MANAGER", folder : false, lazy : false, view : "showUserManagerPage", icon : "fa fa-users" });
        }
        
        if(profile.mainMenu.showTrashcan) {
        	var trashCanLink = _this.getLinkForNode("Trashcan", "TRASHCAN", "showTrashcanPage", null);
        	treeModelUtils.push({ title : trashCanLink, entityType: "TRASHCAN", key : "TRASHCAN", folder : false, lazy : false, view : "showTrashcanPage", icon : "glyphicon glyphicon-trash" });
        }
        
        if(profile.mainMenu.showSettings) {
        	var settingsLink = _this.getLinkForNode("Settings", "SETTINGS", "showSettingsPage", null);
        	treeModelUtils.push({ title : settingsLink, entityType: "SETTINGS", key : "SETTINGS", folder : false, lazy : false, view : "showSettingsPage", icon : "glyphicon glyphicon-cog" });
        }
        
        treeModel.push({ title : "Utilities", entityType: "UTILITIES", key : "UTILITIES", folder : true, lazy : false, expanded : true, children : treeModelUtils, icon : "glyphicon glyphicon-wrench" });
        treeModel.push({ title : "About", entityType: "ABOUT", key : "ABOUT", folder : false, lazy : false, view : "showAbout", icon : "glyphicon glyphicon-info-sign" });
        
		var glyph_opts = {
        	    map: {
        	      doc: "glyphicon glyphicon-file",
        	      docOpen: "glyphicon glyphicon-file",
        	      checkbox: "glyphicon glyphicon-unchecked",
        	      checkboxSelected: "glyphicon glyphicon-check",
        	      checkboxUnknown: "glyphicon glyphicon-share",
        	      dragHelper: "glyphicon glyphicon-play",
        	      dropMarker: "glyphicon glyphicon-arrow-right",
        	      error: "glyphicon glyphicon-warning-sign",
        	      expanderClosed: "glyphicon glyphicon-plus-sign",
        	      expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
        	      expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
        	      folder: "glyphicon glyphicon-folder-close",
        	      folderOpen: "glyphicon glyphicon-folder-open",
        	      loading: "glyphicon glyphicon-refresh"
        	    }
        };
    	
    	var onLazyLoad = function(event, data) {
    		var dfd = new $.Deferred();
    	    data.result = dfd.promise();
    	    var type = data.node.data.entityType;
    	    var permId = data.node.key;
    	    
    	    switch(type) {
    	    	case "LAB_NOTEBOOK":
    	    		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
    	    			var USERID = mainController.serverFacade.getUserId().toUpperCase();
    	    			var results = [];
    	    			var spaces = searchResult.objects;
    	                for (var i = 0; i < spaces.length; i++) {
    	                    var space = spaces[i];
    	                    var isInventorySpace = profile.isInventorySpace(space.code);
    	                    var isHiddenSpace = profile.isHiddenSpace(space.code);
        	                if(!isInventorySpace && (space.code === USERID) && !isHiddenSpace) {
        	                	var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
        	                	var spaceLink = _this.getLinkForNode("My Space (" + normalizedSpaceTitle + ")", space.getCode(), "showSpacePage", space.getCode());
        	                    var spaceNode = { title : spaceLink, entityType: "SPACE", key : space.getCode(), folder : true, lazy : true, view : "showSpacePage", viewData: space.getCode() };
        	                    results.push(spaceNode);
        	                }
    	                }
    	                
	                    results.push({ title : "Others", entityType: "LAB_NOTEBOOK_OTHERS", key : "LAB_NOTEBOOK_OTHERS", folder : true, lazy : true, view : "showLabNotebookPage" });
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "LAB_NOTEBOOK_OTHERS":
    	    		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
    	    			var USERID = mainController.serverFacade.getUserId().toUpperCase();
    	    			var results = [];
    	    			var spaces = searchResult.objects;
    	                for (var i = 0; i < spaces.length; i++) {
    	                    var space = spaces[i];
    	                    var isInventorySpace = profile.isInventorySpace(space.code);
    	                    var isHiddenSpace = profile.isHiddenSpace(space.code);
        	                if(!isInventorySpace && (space.code !== USERID) && !isHiddenSpace) {
        	                	var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
        	                	var spaceLink = _this.getLinkForNode(normalizedSpaceTitle, space.getCode(), "showSpacePage", space.getCode());
        	                    var spaceNode = { title : spaceLink, entityType: "SPACE", key : space.getCode(), folder : true, lazy : true, view : "showSpacePage", viewData: space.getCode() };
        	                    results.push(spaceNode);
        	                }
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "INVENTORY":
    	    		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
    	    			var results = [];
    	                var spaces = searchResult.objects;
    	                for (var i = 0; i < spaces.length; i++) {
    	                    var space = spaces[i];
    	                    var isInventorySpace = profile.isInventorySpace(space.code);
    	                    var isHiddenSpace = profile.isHiddenSpace(space.code);
        	                if(((type === "LAB_NOTEBOOK" && !isInventorySpace) || (type === "INVENTORY" && isInventorySpace)) && !isHiddenSpace) {
        	                	var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
        	                	
        	                	var spaceLink = _this.getLinkForNode(normalizedSpaceTitle, space.getCode(), "showSpacePage", space.getCode());
        	                    var spaceNode = { title : spaceLink, entityType: "SPACE", key : space.getCode(), folder : true, lazy : true, view : "showSpacePage", viewData: space.getCode() };
        	                    if(space.getCode() === "STOCK_CATALOG" || space.getCode() === "STOCK_ORDERS") {
        	                    	spaceNode.icon = "fa fa-shopping-cart";
        	                    }
        	                    results.push(spaceNode);
        	                }
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "SPACE":
    	    		var projectRules = { "UUIDv4" : { type : "Attribute", name : "SPACE", value : permId } };
    	    		mainController.serverFacade.searchForProjectsAdvanced({ entityKind : "PROJECT", logicalOperator : "AND", rules : projectRules }, null, function(searchResult) {
    	    			var results = [];
    	                var projects = searchResult.objects;
    	                for (var i = 0; i < projects.length; i++) {
    	                    var project = projects[i];
    	                    var normalizedProjectTitle = Util.getDisplayNameFromCode(project.code);
    	                    var projectLink = _this.getLinkForNode(normalizedProjectTitle, project.getPermId().getPermId(), "showProjectPageFromPermId", project.getPermId().getPermId());
    	                    results.push({ title : projectLink, entityType: "PROJECT", key : project.getPermId().getPermId(), folder : true, lazy : true, view : "showProjectPageFromPermId", viewData: project.getPermId().getPermId() });
    	                    
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "PROJECT":
    	    		var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PROJECT_PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForExperimentsAdvanced({ entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules }, null, function(searchResult) {
    	    			var results = [];
    	                var experiments = searchResult.objects;
    	                for (var i = 0; i < experiments.length; i++) {
    	                    var experiment = experiments[i];
    	                    var experimentDisplayName = experiment.code;
    	                    if(experiment.properties && experiment.properties[profile.propertyReplacingCode]) {
    	                    	experimentDisplayName = experiment.properties[profile.propertyReplacingCode];
    	                    }
    	                    var isInventorySpace = profile.isInventorySpace(experiment.getIdentifier().getIdentifier().split("/")[1]);
    	                    var viewToUse = null;
    	                    var loadSamples = null;
    	                    if(isInventorySpace) {
    	                    	viewToUse = "showSamplesPage";
    	                    	loadSamples = false;
    	                    } else {
    	                    	viewToUse = "showExperimentPageFromIdentifier";
    	                    	loadSamples = true;
    	                    }
    	                    
    	                    var experimentLink = _this.getLinkForNode(experimentDisplayName, experiment.getPermId().getPermId(), viewToUse, experiment.getIdentifier().getIdentifier());
    	                    results.push({ title : experimentLink, entityType: "EXPERIMENT", key : experiment.getPermId().getPermId(), folder : true, lazy : loadSamples, view : viewToUse, viewData: experiment.getIdentifier().getIdentifier() });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "EXPERIMENT":
    	    		var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, null,
    	    		function(searchResult) {
    	    			var samples = searchResult.objects;
    	    			var samplesToShow = [];
    	    			for(var sIdx = 0; sIdx < samples.length; sIdx++) {
    	    				var sample = samples[sIdx];
    	    				var sampleIsExperiment = sample.type.code.indexOf("EXPERIMENT") > -1;
    	    				
    	    				if(sampleIsExperiment) {
    	    					var parentIsExperiment = false;
    	    					if(sample.parents) {
    	    						for(var pIdx = 0; pIdx < sample.parents.length; pIdx++) {
        	    						var parentIdentifier = sample.parents[pIdx].identifier.identifier;
        	    						var parentInELN = profile.isELNIdentifier(parentIdentifier);
        	    						if(parentInELN) {
        	    							parentIsExperiment = parentIsExperiment || sample.parents[pIdx].type.code.indexOf("EXPERIMENT") > -1;
        	    						}
        	    					}
    	    					}
    	    					if(!parentIsExperiment) {
    	    						samplesToShow.push(sample);
	    						}
    	    				}
    	    			}
    	    			
    	    			var getOkResultsFunction = function(dfd, samples) {
    	                	return function() {
    	                		var results = [];
        	                	for (var i = 0; i < samples.length; i++) {
	        	                    var sample = samples[i];
	        	                    var sampleDisplayName = sample.code;
	        	                    if(sample.properties && sample.properties[profile.propertyReplacingCode]) {
	        	                    	sampleDisplayName = sample.properties[profile.propertyReplacingCode];
	        	                    }
	        	                    
	        	                    var sampleLink = _this.getLinkForNode(sampleDisplayName, sample.getPermId().getPermId(), "showViewSamplePageFromPermId", sample.getPermId().getPermId());
	        	                    var sampleNode = { title : sampleLink, entityType: "SAMPLE", key : sample.getPermId().getPermId(), folder : true, lazy : true, view : "showViewSamplePageFromPermId", viewData: sample.getPermId().getPermId(), icon : "fa fa-flask" };
	        	                    results.push(sampleNode);
        	                	}
        	                	
        	                	var datasetRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
                	    		mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
                	    			
                	                var datasets = searchResult.objects;
                	                var experimentDatasets = [];
                	                for (var i = 0; i < datasets.length; i++) {
                	                    var dataset = datasets[i];
                	                    if(!dataset.sample) {
                	                    	experimentDatasets.push(dataset);
                	                    }
                	                }
                	                
                	                if(experimentDatasets.length > 50) {
                	                	Util.showInfo("More than 50 Datasets, please use the dataset viewer on the experiment to navigate them.");
                	                } else {
                	                	for (var i = 0; i < experimentDatasets.length; i++) {
                    	                    var dataset = experimentDatasets[i];
                    	                    var datasetDisplayName = dataset.code;
                    	                    if(dataset.properties && dataset.properties[profile.propertyReplacingCode]) {
                    	                    	datasetDisplayName = dataset.properties[profile.propertyReplacingCode];
                    	                    }
                    	                    
                    	                    var datasetLink = _this.getLinkForNode(datasetDisplayName, dataset.getPermId().getPermId(), "showViewDataSetPageFromPermId", dataset.getPermId().getPermId());
                    	                    results.push({ title : datasetLink, entityType: "DATASET", key : dataset.getPermId().getPermId(), folder : true, lazy : false, view : "showViewDataSetPageFromPermId", viewData: dataset.getPermId().getPermId(), icon : "fa fa-database" });
                    	                }
                	                }
                	                
                	    			dfd.resolve(results);
            	                	Util.unblockUI();
                	    		});
        	                	
    	                	}
    	                }
    	                
    	                var getCancelResultsFunction = function(dfd) {
    	                	return function() {
    	                		dfd.resolve([]);
    	                	}
    	                }
    	                
    	                if(samplesToShow.length > 50) {
    	                	var toExecute = function() {
        	                	Util.blockUIConfirm("Do you want to show " + samplesWithoutELNParents.length + " " + ELNDictionary.Samples + " on the tree?", 
        	    	                	getOkResultsFunction(dfd, samplesToShow),
        	    	                	getCancelResultsFunction(dfd));
        	                }
    	                	
    	                	setTimeout(toExecute, 1000);
    	                } else {
    	                	getOkResultsFunction(dfd, samplesToShow)();
    	                }
    	    		});
    	    		break;
    	    	case "SAMPLE":
    	    		var sampleRules = { "UUIDv4" : { type : "Attribute", name : "PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, null, function(searchResult) {
    	    			var results = [];
    	    			var samples = searchResult.objects;
    	    			if(samples && samples[0] && samples[0].children) {
    	    				if(samples[0].children.length > 50) {
        	                	Util.showInfo("More than 50 Samples, please use the children table to navigate them.");
        	                } else {
        	                	for(var cIdx = 0; cIdx < samples[0].children.length; cIdx++) {
        	    					var sample = samples[0].children[cIdx];
        	    					if(sample.type.code.indexOf("EXPERIMENT") > -1) {
        	    						var sampleDisplayName = sample.code;
                	                    if(sample.properties && sample.properties[profile.propertyReplacingCode]) {
                	                    	sampleDisplayName = sample.properties[profile.propertyReplacingCode];
                	                    }
                	                    
            	    					var sampleLink = _this.getLinkForNode(sampleDisplayName, sample.getPermId().getPermId(), "showViewSamplePageFromPermId", sample.getPermId().getPermId());
                	                    var sampleNode = { title : sampleLink, entityType: "SAMPLE", key : sample.getPermId().getPermId(), folder : true, lazy : true, view : "showViewSamplePageFromPermId", viewData: sample.getPermId().getPermId(), icon : "fa fa-flask" };
                	                    results.push(sampleNode);
        	    					}
        	    				}
        	                }
    	    			}
    	    			
    	    			var datasetRules = { "UUIDv4" : { type : "Sample", name : "ATTR.PERM_ID", value : permId } };
        	    		mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
        	    			
        	                var datasets = searchResult.objects;
        	                if(datasets.length > 50) {
        	                	Util.showInfo("More than 50 Datasets, please use the dataset viewer on the sample to navigate them.");
        	                } else {
        	                	for (var i = 0; i < datasets.length; i++) {
            	                    var dataset = datasets[i];
            	                    var datasetDisplayName = dataset.code;
            	                    if(dataset.properties && dataset.properties[profile.propertyReplacingCode]) {
            	                    	datasetDisplayName = dataset.properties[profile.propertyReplacingCode];
            	                    }
            	                    
            	                    var datasetLink = _this.getLinkForNode(datasetDisplayName, dataset.getPermId().getPermId(), "showViewDataSetPageFromPermId", dataset.getPermId().getPermId());
            	                    results.push({ title : datasetLink, entityType: "DATASET", key : dataset.getPermId().getPermId(), folder : true, lazy : false, view : "showViewDataSetPageFromPermId", viewData: dataset.getPermId().getPermId(), icon : "fa fa-database" });
            	                }
        	                }
        	                
        	                
        	                dfd.resolve(results);
        	    		});
    	    		});
    	    		
    	    		break;
    	    	case "DATASET":
    	    		break;
    	    }
    	};
    	
    	var onActivate = function(event, data) {
    		data.node.setExpanded(true);
    	};
    	
    	var onClick = function(event, data) {
    		if(data.node.data.view) {
    			_this._sideMenuWidgetController._showNodeView(data.node);
    		}
    	};
    	
    	var onCollapse = function(event, data) {
    		if(data.node.lazy) { //Is going to be collapsed
    			data.node.removeChildren();
    			data.node.resetLazy();
    		}
    	};
    	
    	$tree.fancytree({
        	extensions: ["dnd", "edit", "glyph"], //, "wide"
        	glyph: glyph_opts,
        	source: treeModel,
        	lazyLoad : onLazyLoad,
        	click : onClick,
        	activate: onActivate,
        	collapse : onCollapse //Yes, for collapsing event we need to use expand 
        });
		
        this._sideMenuWidgetModel.menuDOMBody.append($tree);
        this._sideMenuWidgetModel.tree = $tree;
        
		var labNotebook = $tree.fancytree("getTree").getNodeByKey("LAB_NOTEBOOK");
		if (labNotebook) {
	        labNotebook.setExpanded(true);
		}
        var inventoryNode = $tree.fancytree("getTree").getNodeByKey("INVENTORY");
		if (inventoryNode) {
			inventoryNode.setExpanded(true).done(function(){
				inventoryNode.visit(function(node){
					node.setExpanded(true).done(function(){
						node.visit(function(node2){
							node2.setExpanded(true);
						})
					});
				})
			});
		}
    }
}