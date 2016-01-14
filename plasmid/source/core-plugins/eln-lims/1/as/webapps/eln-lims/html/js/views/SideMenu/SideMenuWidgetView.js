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
    
    var $toggleMenuButtonIcon = null;
    var $toggleNavButtonIcon = null;
    var toggleMenuSizeBig = false;
    var DISPLAY_NAME_LENGTH_SHORT = 15;
    var DISPLAY_NAME_LENGTH_LONG = 300;
    var cutDisplayNameAtLength = DISPLAY_NAME_LENGTH_SHORT; // Fix for long names
    
    this._updateNavButtonIcon = function() {
    	if(this._sideMenuWidgetModel.isTreeNavigation) {
    		$toggleNavButtonIcon.removeClass( "glyphicon-align-left" );
    		$toggleNavButtonIcon.addClass( "glyphicon-list" );
    	} else {
    		$toggleNavButtonIcon.removeClass( "glyphicon-list" );
    		$toggleNavButtonIcon.addClass( "glyphicon-align-left" );
    	}
    }
    this.toggleNavType = function() {
    	this._sideMenuWidgetModel.isTreeNavigation = !this._sideMenuWidgetModel.isTreeNavigation;
    	this._updateNavButtonIcon();
    	this.repaint();
    }
    
    this.toggleMenuSize = function() {
    	toggleMenuSizeBig = !toggleMenuSizeBig;
    	if(toggleMenuSizeBig) {
    		$toggleMenuButtonIcon.removeClass("glyphicon-menu-right");
    		$toggleMenuButtonIcon.addClass("glyphicon-menu-left");
        	$("#sideMenu").removeClass("col-md-2");
        	$("#mainContainer").removeClass("col-md-10");
        	$("#sideMenu").addClass("col-md-6");
        	$("#mainContainer").addClass("col-md-6");
        	cutDisplayNameAtLength = DISPLAY_NAME_LENGTH_LONG;
    	} else {
    		$toggleMenuButtonIcon.removeClass("glyphicon-menu-left");
    		$toggleMenuButtonIcon.addClass("glyphicon-menu-right");
    		$("#sideMenu").removeClass("col-md-6");
    		$("#mainContainer").removeClass("col-md-6");
    		$("#sideMenu").addClass("col-md-2");
    		$("#mainContainer").addClass("col-md-10");
    		cutDisplayNameAtLength = DISPLAY_NAME_LENGTH_SHORT;
    	}
    	if(!this._sideMenuWidgetModel.isTreeNavigation) {
    		this.repaint();
    	}
    };
    
    this.hideSideMenu = function() {
        this._sideMenuWidgetModel.$container.hide();
        $("#mainContainer").removeClass("col-md-10");
        $("#mainContainer").addClass("col-md-12");

        var $toggleButtonShow = $("<a>", {"class": "btn btn-default", "id": "toggleButtonShow", "href": "javascript:mainController.sideMenu.showSideMenu();", "style": "position: fixed; top:0px; left:0px;"})
                .append($("<span>", {"class": "glyphicon glyphicon-resize-small"}));

        $("#main").append($toggleButtonShow);
        this._sideMenuWidgetModel.isHidden = true;
    };

    this.showSideMenu = function() {
        this._sideMenuWidgetModel.$container.show();
        $("#toggleButtonShow").remove();
        $("#mainContainer").removeClass("col-md-12");
        $("#mainContainer").addClass("col-md-10");
        this._sideMenuWidgetModel.isHidden = false;
    };
    
    this.repaintFirst = function($container) {
        var _this = this;
        var $widget = $("<div>");
        //
        // Fix Header
        //
        var $header = $("<div>", {"id": "sideMenuHeader"});
        var $headerItemList = $("<ul>", {"class": "nav navbar-nav"});
        var $headerItemList2 = $("<ul>", {"class": "nav navbar-nav"});
        $header.append($("<nav>", {"class": "navbar navbar-default", "role": "navigation", "style": "margin:0px; border-left-width:0px; border-right-width:0px;"})
                        .append($headerItemList).append($("<br>")).append($headerItemList2)
                       );

        var $toggleButton = $("<li>")
                .append($("<a>", {"href": "javascript:mainController.sideMenu.hideSideMenu();"})
                        .append($("<span>", {"class": "glyphicon glyphicon-resize-full"}))
                        );
        
        $toggleMenuButtonIcon = $("<span>", {"class": "glyphicon glyphicon-menu-right"});
        var $toggleMenuButton = $("<li>")
        .append($("<a>", {"href": "javascript:mainController.sideMenu.toggleMenuSize();"})
                .append($toggleMenuButtonIcon)
                );
        
        $toggleNavButtonIcon = $("<span>", { "class" : "glyphicon" });
        this._updateNavButtonIcon();
        var $toggleNavButton = $("<li>")
        .append($("<a>", {"href": "javascript:mainController.sideMenu.toggleNavType();"})
                .append($toggleNavButtonIcon)
                );
        
        var dropDownSearch = "";
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

        if (searchDomains.length > 1) {
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
        searchElement.keyup(searchFunction);

        var $searchForm = $("<li>")
                .append($("<form>", {"class": "navbar-form", "onsubmit": "return false;", "style": "padding-right:0px;"})
                        .append(searchElement));
        $searchForm.css({"width" : "100%"});
        var $searchFormDropdown = $("<li>")
        .append($("<form>", {"class": "navbar-form", "onsubmit": "return false;"})
                .append(dropDownSearch));
        
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
        $headerItemList.append($toggleMenuButton);
        $headerItemList.append($toggleNavButton);
        
        $headerItemList2.append($searchForm);
        if(dropDownSearch !== "") {
        	$headerItemList2.append($searchFormDropdown);
        }
        
        var $body = $("<div>", {"id": "sideMenuBody"});
        $widget
                .append($header)
                .append($body);

        var $title = $("<div>", {"class": "sideMenuTitle"});
        $header
                .append($title);

        $container.empty();
        $container.append($widget);

        //
        // Print Menu
        //
        this._sideMenuWidgetModel.menuDOMTitle = $title;
        this._sideMenuWidgetModel.menuDOMBody = $body;
        this.repaint();
    };
    
    this.repaint = function() {
    	if(this._sideMenuWidgetModel.isTreeNavigation) {
    		this.repaintTreeMenu();
    	} else {
    		this.repaintListMenu();
    	}
    }
    
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
    
    this.repaintTreeMenu = function() {
    	var _this = this;
    	this._sideMenuWidgetModel.menuDOMTitle.empty();
        this._sideMenuWidgetModel.menuDOMBody.empty();
        var tree = $("<div>", { "id" : "tree" });
        
        //
        // Body
        //
        var rootNode = { title : "Main Menu", key : "MAIN_MENU", menuData : this._sideMenuWidgetModel.menuStructure };
        var sourceByKey = { "MAIN_MENU" : rootNode };
        var treeModel = [rootNode];
        var todo = [this._sideMenuWidgetModel.menuStructure];

        while(todo.length > 0) {
        	var modelNode = todo.shift();
        	var treeModelNode = sourceByKey[modelNode.uniqueId];
        	if(modelNode.newMenuIfSelected && modelNode.newMenuIfSelected.children.length !== 0) {
        		treeModelNode.folder = true;
        		if(!treeModelNode.children) {
        			treeModelNode.children = [];
        		}
        		
        		for(var cIdx = 0; cIdx < modelNode.newMenuIfSelected.children.length; cIdx++) {
        			var modelNodeChild = modelNode.newMenuIfSelected.children[cIdx];
        			var $titleWithLink = this._getDisplayNameLinkForNode(modelNodeChild, true);
        			treeModelChild = {title : $titleWithLink.outerHTML, key : modelNodeChild.uniqueId, menuData : modelNodeChild};
        			treeModelNode.children.push(treeModelChild);
        			todo.push(modelNodeChild);
        			sourceByKey[treeModelChild.key] = treeModelChild;
        		}
        	}
        }
        
        glyph_opts = {
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
        
//        var updateIcons = function(treeDataNode) {
//        	var menuData = treeDataNode.node.data.menuData;
//    		if(menuData.parent && (menuData.parent.uniqueId == "UTILITIES")) {
//    			var node = treeDataNode.node;
//    			var $span = $(node.span);
//    			var $icon = $span.find("> span.fancytree-icon");
//    			$icon.removeClass("glyphicon-file");
//    			$icon.addClass("glyphicon-briefcase");
//    		}
//        }
        
        var onClickOrActivate = function(event, data){
    		var menuData = data.node.data.menuData;
    		data.node.setExpanded(true);
    		if(menuData.isSelectable) {
    			_this._sideMenuWidgetModel.pointerToMenuNode = menuData;
    			if(menuData.newViewIfSelected) {
    				mainController.changeView(menuData.newViewIfSelected, menuData.newViewIfSelectedData);
    			}
    		}
    	};
    	
        tree.fancytree({
        	extensions: ["dnd", "edit", "glyph"], //, "wide"
        	glyph: glyph_opts,
        	source: treeModel,
        	activate: onClickOrActivate
//        	click: onClickOrActivate
        });
        
        this._sideMenuWidgetModel.menuDOMBody.append(tree);
        
        //Expand Tree Node
        var expandToParent = function(tree, menuData, isRoot) {
        	var node = tree.fancytree("getTree").getNodeByKey(menuData.uniqueId);
        	node.setExpanded(true);
        	if(isRoot) {
        		node.setActive(true);
        	}
        	if(menuData.parent) {
        		expandToParent(tree, menuData.parent, false);
        	}
        }
        
        var menuToPaint = this._sideMenuWidgetModel.pointerToMenuNode;
        expandToParent(tree, menuToPaint, true);
    }
    
    this.repaintListMenu = function() {
        var _this = this;
        var menuToPaint = this._sideMenuWidgetModel.pointerToMenuNode;
        //
        // Title
        //
        var titleShowTooltip = menuToPaint.displayName.length > cutDisplayNameAtLength;
        if (titleShowTooltip) {
            var titleDisplayName = (menuToPaint.displayName).substring(0, cutDisplayNameAtLength) + "...";
        } else {
            var titleDisplayName = (menuToPaint.displayName);
        }
        //
        this._sideMenuWidgetModel.menuDOMTitle.empty();
        var isBackButtonShown = menuToPaint.parent !== null;
        if (isBackButtonShown) {
            var backButton = $("<a>", {"id": "back-button", "href": "javascript:void(0);", "style": "float:left; color:black; padding-left:10px;"}).append($("<span>", {"class": "glyphicon glyphicon-arrow-left"}));
            var backButtonClick = function(menuItem) {
                return function() {
                    var parent = menuItem.parent;
                    _this._sideMenuWidgetModel.pointerToMenuNode = parent;
                    _this.repaint();

                    if (parent.newViewIfSelected !== null) {
                        mainController.changeView(parent.newViewIfSelected, parent.newViewIfSelectedData);
                    }
                };
            };
            backButton.click(backButtonClick(menuToPaint));
            this._sideMenuWidgetModel.menuDOMTitle.append(backButton);
        }

        var $titleAsTextOrLink = null;
        if (menuToPaint.newViewIfSelected && menuToPaint.newViewIfSelected !== "showBlancPage") {
            $titleAsTextOrLink = $("<a>", {"href": "javascript:void(0);"}).append(titleDisplayName);

            var clickFunction = function(menuToPaint) {
                return function() {
                    mainController.changeView(menuToPaint.newViewIfSelected, menuToPaint.newViewIfSelectedData);
                };
            };

            $titleAsTextOrLink.click(clickFunction(menuToPaint));
        } else {
            $titleAsTextOrLink = $("<span>").text(titleDisplayName);
        }

        if (titleShowTooltip) {
            $titleAsTextOrLink.attr("title", menuToPaint.displayName);
            $titleAsTextOrLink.tooltipster();
        }

        var $mainTitle = $("<span>").append($titleAsTextOrLink);

        if (isBackButtonShown) {
            $mainTitle.css({
                "margin-left": "-24px"
            });
        }

        this._sideMenuWidgetModel.menuDOMTitle.append($mainTitle);


        //
        // Body
        //
        this._sideMenuWidgetModel.menuDOMBody.empty();
        for (var mIdx = 0; mIdx < menuToPaint.newMenuIfSelected.children.length; mIdx++) {
            var menuItem = menuToPaint.newMenuIfSelected.children[mIdx];


            var $menuItem = $("<div>", {"class": "sideMenuItem"});

            var menuItemDisplayName = menuItem.displayName;
            if (!menuItemDisplayName) {
                menuItemDisplayName = menuItem.unqueId;
            }

            //
            var itemShowTooltip = menuItemDisplayName.length > cutDisplayNameAtLength;
            if (itemShowTooltip) {
                var itemDisplayName = menuItemDisplayName.substring(0, cutDisplayNameAtLength) + "...";
            } else {
                var itemDisplayName = menuItemDisplayName;
            }
            //
            var $menuItemLink = this._getDisplayNameLinkForNode(menuItem);
            var $menuItemTitle = $("<span>").append($menuItemLink);

            if (itemShowTooltip) {
                $menuItem.attr("title", menuItemDisplayName);
                $menuItem.tooltipster();
            }

            $menuItem.append($menuItemTitle);

            if (menuItem.isTitle) {
                $menuItem.addClass("sideMenuItemTitle");
            }

            if (menuItem.isSelectable) {
                $menuItem.addClass("sideMenuItemSelectable");
                if (menuItem.newMenuIfSelected && menuItem.newMenuIfSelected.children.length > 0) {
                    $menuItem.append("<span class='glyphicon glyphicon-chevron-right put-chevron-right'></span>");
                }

                var clickFunction = function(menuItem) {
                    return function() {
                        if (menuItem.newMenuIfSelected && menuItem.newMenuIfSelected.children.length > 0) {
                            _this._sideMenuWidgetModel.pointerToMenuNode = menuItem;
                            _this.repaint();
                        }

                        if (menuItem.newViewIfSelected !== null) {
                            mainController.changeView(menuItem.newViewIfSelected, menuItem.newViewIfSelectedData);
                        }
                    };
                };

                $menuItem.click(clickFunction(menuItem));
            }

            this._sideMenuWidgetModel.menuDOMBody.append($menuItem);
        }

        $(window).resize();
    };
}