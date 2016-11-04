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
    
    this.hideSideMenu = function() {
        this._sideMenuWidgetModel.$container.hide();
        
        Util.dragContainerFunc({ pageX : 0 });

        var $toggleButtonShow = $("<a>", {"class": "btn btn-default", "id": "toggleButtonShow", "href": "javascript:mainController.sideMenu.showSideMenu();", "style": "position: fixed; top:0px; left:0px;"})
                .append($("<span>", {"class": "glyphicon glyphicon-resize-small"}));

        $("#main").append($toggleButtonShow);
        this._sideMenuWidgetModel.isHidden = true;
    };

    this.showSideMenu = function() {
        this._sideMenuWidgetModel.$container.show();
        $("#toggleButtonShow").remove();
        Util.dragContainerFunc({ pageX : (window.outerWidth * 0.20) });
        this._sideMenuWidgetModel.isHidden = false;
    };
    
    this.repaint = function($container) {
        var _this = this;
        var $widget = $("<div>");
        //
        // Fix Header
        //
        var $header = $("<div>", {"id": "sideMenuHeader"});
        var $headerItemList = $("<ul>", {"class": "nav navbar-nav"});
        $header.append($("<nav>", {"class": "navbar navbar-default", "role": "navigation", "style": "margin:0px; border-left-width:0px; border-right-width:0px;"})
                        .append($headerItemList).append($("<br>"))
                       );

        var $toggleButton = $("<li>")
                .append($("<a>", {"href": "javascript:mainController.sideMenu.hideSideMenu();"})
                        .append($("<span>", {"class": "glyphicon glyphicon-resize-full"}))
                        );
        
        var searchDomains = profile.getSearchDomains();

        var searchFunction = function() {
            var searchText = $("#search").val();
            var searchDomain = $("#prefix-selected-search-domain").attr("selected-name");
            var searchDomainLabel = $("#prefix-selected-search-domain").attr("selected-label");
            if (!searchDomain) {
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
            //Default Selected for the prefix
            var defaultSelected = "";
            if (searchDomains[0].label.length > 3) {
                defaultSelected = searchDomains[0].label.substring(0, 2) + ".";
            } else {
                defaultSelected = searchDomains[0].label.label;
            }

            //Prefix function
            var selectedFunction = function(selectedSearchDomain) {
                return function() {
                    var $component = $("#prefix-selected-search-domain");
                    $component.empty();
                    if (selectedSearchDomain.label.length > 3) {
                        $component.append(selectedSearchDomain.label.substring(0, 2) + ".");
                    } else {
                        $component.append(selectedSearchDomain.label);
                    }
                    $component.attr('selected-name', selectedSearchDomain.name);
                    $component.attr('selected-label', selectedSearchDomain.label);
                };
            };

            //Dropdown elements
            var dropDownComponents = [];
            for (var i = 0; i < searchDomains.length; i++) {
                dropDownComponents.push({
                    href: selectedFunction(searchDomains[i]),
                    title: searchDomains[i].label,
                    id: searchDomains[i].name
                });
            }

            dropDownSearch = FormUtil.getDropDownToogleWithSelectedFeedback(
                    $('<span>', {id: 'prefix-selected-search-domain', class: 'btn btn-default disabled', 'selected-name': searchDomains[0].name}
                    ).append(defaultSelected), dropDownComponents, true, searchFunction);
            dropDownSearch.change();
        }


        var searchElement = $("<input>", {"id": "search", "type": "text", "class": "form-control search-query", "placeholder": "Search"});
        searchElement.keypress(function (e) {
        	 var key = e.which;
        	 if(key == 13)  // the enter key code
        	  {
        		searchFunction();
        	    return false;  
        	  }
        });
        searchElement.css({"width" : "68%"});
        searchElement.css({"padding-right" : "0px"});
        searchElement.css({"margin-right" : "2px"});
        
        var $searchForm = $("<li>")
                .append($("<form>", {"class": "navbar-form", "onsubmit": "return false;" })
                        .append(searchElement)
                        .append(dropDownSearch)
                        );
        $searchForm.css({"width" : "100%"});
        
        var logoutButton = $("<a>", {"id": "logout-button", "href": ""}).append($("<span>", {"class": "glyphicon glyphicon-off"}));
        logoutButton.click(function() {
            $('body').addClass('bodyLogin');
            mainController.serverFacade.logout(function(data) {
                $("#login-form-div").show();
                $("#main").hide();
            });
        });
        var $logoutButton = $("<li>").append(logoutButton);

        $headerItemList.append($logoutButton);
        $headerItemList.append($toggleButton);
        $headerItemList.append($searchForm);
        
        var $body = $("<div>", {"id": "sideMenuBody"});
        $widget
                .append($header)
                .append($body);

        $container.empty();
        $container.append($widget);

        //
        // Print Menu
        //
        this._sideMenuWidgetModel.menuDOMBody = $body;
        this.repaintTreeMenuDinamic();
    };
    
    this._getDisplayNameLinkForNode = function(menuItem, isTreeMenu) {
        var menuItemDisplayName = menuItem.displayName;
        if (!menuItemDisplayName) {
            menuItemDisplayName = menuItem.unqueId;
        }
        
    	var hrefMenu = null;
        if(menuItem.newMenuIfSelected && menuItem.newMenuIfSelected.children.length !== 0) {
        	hrefMenu = menuItem.uniqueId;
        } else {
        	hrefMenu = menuItem.parent.uniqueId;
        }
        var href = Util.getURLFor(hrefMenu, menuItem.newViewIfSelected, menuItem.newViewIfSelectedData);
        
        var cssClass;
        if(isTreeMenu) {
        	cssClass = "browser-compatible-javascript-link browser-compatible-javascript-link-tree";
        } else {
        	cssClass = "browser-compatible-javascript-link  browser-compatible-javascript-link-menu";
        }
        	
        var $menuItemLink = null;
        if (menuItem.isSelectable) {
        	$menuItemLink = $("<a>", {"href": href, "class" : cssClass }).append(menuItemDisplayName);
        	$menuItemLink = $menuItemLink[0];
        } else {
        	$menuItemLink = menuItemDisplayName;
        }
        return $menuItemLink;
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
        	treeModel.push({ title : "Lab Notebook", entityType: "LAB_NOTEBOOK", key : "LAB_NOTEBOOK", folder : true, lazy : true, view : "showLabNotebookPage", icon : "glyphicon glyphicon-book" });
        }
        
        if(profile.mainMenu.showInventory) {
        	treeModel.push({ title : "Inventory", entityType: "INVENTORY", key : "INVENTORY", folder : true, lazy : true, view : "showInventoryPage" });
        }
        
        var treeModelUtils = [];
        
        if(profile.mainMenu.showDrawingBoard) {
        	treeModelUtils.push({ title : "Drawing Board", entityType: "DRAWING_BOARD", key : "DRAWING_BOARD", folder : false, lazy : false, view : "showDrawingBoard" });
        }
        
        if(profile.mainMenu.showSampleBrowser) {
        	treeModelUtils.push({ title : "" + ELNDictionary.Sample + " Browser", entityType: "SAMPLE_BROWSER", key : "SAMPLE_BROWSER", folder : false, lazy : false, view : "showSamplesPage", icon : "glyphicon glyphicon-list-alt" });
        }
        
        if(profile.mainMenu.showVocabularyViewer) {
        	treeModelUtils.push({ title : "Vocabulary Browser", entityType: "VOCABULARY_BROWSER", key : "VOCABULARY_BROWSER", folder : false, lazy : false, view : "showVocabularyManagerPage", icon : "glyphicon glyphicon-list-alt" });
        }
        
        if(profile.mainMenu.showAdvancedSearch) {
        	treeModelUtils.push({ title : "Advanced Search", entityType: "ADVANCED_SEARCH", key : "ADVANCED_SEARCH", folder : false, lazy : false, view : "showAdvancedSearchPage", icon : "glyphicon glyphicon-search" });
        }
        
        if(profile.mainMenu.showExports) {
        	treeModelUtils.push({ title : "Export Builder", entityType: "EXPORT_BUILDER", key : "EXPORT_BUILDER", folder : false, lazy : false, view : "showExportTreePage", icon : "glyphicon glyphicon-export" });
        }
        
        if(profile.mainMenu.showStorageManager) {
        	treeModelUtils.push({ title : "Storage Manager", entityType: "STORAGE_MANAGER", key : "STORAGE_MANAGER", folder : false, lazy : false, view : "showStorageManager" });
        }
        
        if(profile.mainMenu.showUserManager && profile.isAdmin) {
        	treeModelUtils.push({ title : "User Manager", entityType: "USER_MANAGER", key : "USER_MANAGER", folder : false, lazy : false, view : "showUserManagerPage", icon : "fa fa-users" });
        }
        
        if(profile.mainMenu.showTrashcan) {
        	treeModelUtils.push({ title : "Trashcan", entityType: "TRASHCAN", key : "TRASHCAN", folder : false, lazy : false, view : "showTrashcanPage", icon : "glyphicon glyphicon-trash" });
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
    	    	case "INVENTORY":
    	    		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
    	    			var results = [];
    	                var spaces = searchResult.objects;
    	                for (var i = 0; i < spaces.length; i++) {
    	                    var space = spaces[i];
    	                    var isInventorySpace = profile.isInventorySpace(space.code);
        	                if((type === "LAB_NOTEBOOK" && !isInventorySpace) || (type === "INVENTORY" && isInventorySpace)) {
        	                	var normalizedSpaceTitle = Util.getDisplayNameFromCode(space.code);
        	                	
        	                    var spaceNode = { title : normalizedSpaceTitle, entityType: "SPACE", key : space.getCode(), folder : true, lazy : true, view : "showSpacePage", viewData: space.getCode() };
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
    	                    results.push({ title : normalizedProjectTitle, entityType: "PROJECT", key : project.getPermId().getPermId(), folder : true, lazy : true, view : "showProjectPageFromPermId", viewData: project.getPermId().getPermId() });
    	                    
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
    	                    
    	                    results.push({ title : experimentDisplayName, entityType: "EXPERIMENT", key : experiment.getPermId().getPermId(), folder : true, lazy : loadSamples, view : viewToUse, viewData: experiment.getIdentifier().getIdentifier() });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "EXPERIMENT":
    	    		var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, null,
    	    		function(searchResult) {
    	    			var samples = searchResult.objects;
    	    			
    	    			var getOkResultsFunction = function(dfd, samples) {
    	                	return function() {
    	                		var results = [];
        	                	for (var i = 0; i < samples.length; i++) {
	        	                    var sample = samples[i];
	        	                    var sampleDisplayName = sample.code;
	        	                    if(sample.properties && sample.properties[profile.propertyReplacingCode]) {
	        	                    	sampleDisplayName = sample.properties[profile.propertyReplacingCode];
	        	                    }
	        	                    
	        	                    var sampleNode = { title : sampleDisplayName, entityType: "SAMPLE", key : sample.getPermId().getPermId(), folder : true, lazy : true, view : "showViewSamplePageFromPermId", viewData: sample.getPermId().getPermId() };
	        	                    if(sample.getType().getCode() === "EXPERIMENTAL_STEP") {
	        	                    	sampleNode.icon = "fa fa-flask";
	        	                    }
	        	                    results.push(sampleNode);
        	                	}
        	                	dfd.resolve(results);
        	                	Util.unblockUI();
    	                	}
    	                }
    	                
    	                var getCancelResultsFunction = function(dfd) {
    	                	return function() {
    	                		dfd.resolve([]);
    	                	}
    	                }
    	                
    	                if(samples.length > 50) {
    	                	var toExecute = function() {
        	                	Util.blockUIConfirm("Do you want to show " + samples.length + " " + ELNDictionary.Samples + " on the tree?", 
        	    	                	getOkResultsFunction(dfd, samples),
        	    	                	getCancelResultsFunction(dfd));
        	                }
    	                	
    	                	setTimeout(toExecute, 1000);
    	                } else {
    	                	getOkResultsFunction(dfd, samples)();
    	                }
    	    		});
    	    		break;
    	    	case "SAMPLE":
    	    		var datasetRules = { "UUIDv4" : { type : "Sample", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
    	    			var results = [];
    	                var datasets = searchResult.objects;
    	                
    	                if(datasets.length > 30) {
    	                	Util.showInfo("More than 30 Datasets, please use the dataset viewer on the sample to navigate them.");
    	                } else {
    	                	for (var i = 0; i < datasets.length; i++) {
        	                    var dataset = datasets[i];
        	                    var datasetDisplayName = dataset.code;
        	                    if(dataset.properties && dataset.properties[profile.propertyReplacingCode]) {
        	                    	datasetDisplayName = dataset.properties[profile.propertyReplacingCode];
        	                    }
        	                    
        	                    results.push({ title : datasetDisplayName, entityType: "DATASET", key : dataset.getPermId().getPermId(), folder : true, lazy : false, view : "showViewDataSetPageFromPermId", viewData: dataset.getPermId().getPermId() });
        	                }
    	                }
    	                
    	                
    	                dfd.resolve(results);
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
        
        $tree.fancytree("getTree").getNodeByKey("LAB_NOTEBOOK").setExpanded(true);
        var inventoryNode = $tree.fancytree("getTree").getNodeByKey("INVENTORY");
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