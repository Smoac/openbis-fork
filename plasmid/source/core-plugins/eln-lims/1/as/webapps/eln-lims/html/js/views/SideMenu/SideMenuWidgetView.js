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
    
    this.hideSideMenu = function() {
        this._sideMenuWidgetModel.$container.hide();
        $("#mainContainer").removeClass("col-md-10");
        $("#mainContainer").addClass("col-md-12");

        var $toggleButtonShow = $("<a>", {"class": "btn btn-default", "id": "toggleButtonShow", "href": "javascript:mainController.sideMenu.showSideMenu();", "style": "position: fixed; top:0px; left:0px;"})
                .append($("<span>", {"class": "glyphicon glyphicon-resize-horizontal"}));

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
        $header.append($("<nav>", {"class": "navbar navbar-default", "role": "navigation", "style": "margin:0px; border-left-width:0px; border-right-width:0px;"})
                        .append($headerItemList)
                       );

        var $toggleButton = $("<li>")
                .append($("<a>", {"href": "javascript:mainController.sideMenu.hideSideMenu();"})
                        .append($("<span>", {"class": "glyphicon glyphicon-resize-horizontal"}))
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
        $headerItemList.append($searchForm);
        if(dropDownSearch !== "") {
        	$headerItemList.append($searchFormDropdown);
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
        var _this = this;
        var menuToPaint = this._sideMenuWidgetModel.pointerToMenuNode;

        //
        // Title
        //

        // Fix for long names
        var cutDisplayNameAtLength = 15;
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

            var $menuItemTitle = $("<span>").append(itemDisplayName);

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