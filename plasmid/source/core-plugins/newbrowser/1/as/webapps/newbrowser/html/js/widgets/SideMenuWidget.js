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
 * @this {SideMenuWidget}
 * @param {MainController} mainController Used to control view changes.
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {ServerFacade} serverFacade Used to access all server side calls.
 */
function SideMenuWidget(mainController, containerId, serverFacade) {
	this._mainController = mainController;
	this._containerId = containerId;
	this._serverFacade = serverFacade;
	this._menuDOMTitle = null;
	this._menuDOMBody = null;
	this._menuStructure = new SideMenuWidgetComponent(false, true, "Main Menu", null, { children : [] }, 'showHelloPage', null);
	this._pointerToMenuNode = this._menuStructure;
	
	this._getProjectNodeForCode = function(projectCode) {
		for(var sIdx = 0; sIdx < this._menuStructure.newMenuIfSelected.children.length; sIdx++) {
			var spaceNode = this._menuStructure.newMenuIfSelected.children[sIdx];
			if(spaceNode.isTitle && !spaceNode.isSelectable) {
				continue;
			}
			var projectsFromSpace = spaceNode.newMenuIfSelected.children;
			for(var pIdx = 0; pIdx < projectsFromSpace.length; pIdx++) {
				var projectNode = projectsFromSpace[pIdx];
				if(projectNode.displayName === projectCode) {
					return projectNode;
				}
			}
		}
		return null;
	}
	
	this.init = function() {
		var _this = this;
		
		this._menuStructure.newMenuIfSelected.children.push(
			new SideMenuWidgetComponent(false, true, "Lab Notebook", this._menuStructure, null, null, null)
		);
		
		this._serverFacade.listSpacesWithProjectsAndRoleAssignments(null, function(dataWithSpacesAndProjects) {
			//Fill Spaces
			var spaces = dataWithSpacesAndProjects.result;
			var projectsToAskForExperiments = [];
			for(var i = 0; i < spaces.length; i++) {
				var space = spaces[i];
				var newMenuIfSelectedSpace = {
						children : []
				}
				var menuItemSpace = new SideMenuWidgetComponent(true, false, space.code,  _this._menuStructure, newMenuIfSelectedSpace, null, null);
				_this._menuStructure.newMenuIfSelected.children.push(menuItemSpace);
				
				//Fill Projects
				for(var j = 0; j < space.projects.length; j++) {
					var project = space.projects[j];
					delete project["@id"];
					delete project["@type"];
					projectsToAskForExperiments.push(project);
					
					var newMenuIfSelectedProject = {
							children : []
					}
					var menuItemProject = new SideMenuWidgetComponent(true, false, project.code, menuItemSpace, newMenuIfSelectedProject, null, null);
					newMenuIfSelectedSpace.children.push(menuItemProject);
				}
			}
			
			//Fill Experiments
			_this._serverFacade.listExperiments(projectsToAskForExperiments, function(experiments) {
				for(var i = 0; i < experiments.result.length; i++) {
					var experiment = experiments.result[i];
					var projectCode = experiment.identifier.split("/")[2];
					var projectNode = _this._getProjectNodeForCode(projectCode);
					
					var menuItemExperiment = new SideMenuWidgetComponent(true, false, experiment.code, projectNode, null, "showViewExperiment", experiment.identifier);
					projectNode.newMenuIfSelected.children.push(menuItemExperiment);
				}
				
				//Fill Inventory
				_this._menuStructure.newMenuIfSelected.children.push(
						new SideMenuWidgetComponent(false, true, "Inventory", _this._menuStructure, null, null, null)
				);
				
				var profile = _this._mainController.profile;
				var typeGroups = _this._mainController.profile.typeGroups;
				for(typeGroupCode in typeGroups) {
					var newMenuIfSelectedTypeGroup = {
							children : []
					}
				
					for(var i = 0; i < typeGroups[typeGroupCode]["LIST"].length; i++) {
						var sampleType = profile.getTypeForTypeCode(typeGroups[typeGroupCode]["LIST"][i]);
					
						if(sampleType !== null) {
							var description = Util.getEmptyIfNull(sampleType.description);
							if(description === "") {
								description = sampleType.code;
							}
							
							var menuItemSampleType = new SideMenuWidgetComponent(true, false, description, _this._menuStructure, null, "showSamplesPage", sampleType.code)
							newMenuIfSelectedTypeGroup.children.push(menuItemSampleType);
						}
					}
					
					_this._menuStructure.newMenuIfSelected.children.push(
							new SideMenuWidgetComponent(true, false, typeGroups[typeGroupCode]["DISPLAY_NAME"], _this._menuStructure, newMenuIfSelectedTypeGroup, null, null)
					);
				}
				
				_this._repaint();
			});
		});
		
		$(window).scroll(function(event){
			var $element = $("#sideMenu");
			var scrollTop = $(document).scrollTop();
			$element.css('top', scrollTop + "px"); 
		});
		
		$(window).resize(function(event){
			var $element = $("#sideMenu");
			var height = $(window).height();
			$element.css('min-height', height + "px"); 
		});
	}
	
	this.hideSideMenu = function() {
		$("#" + this._containerId).hide();
		$("#mainContainer").removeClass("col-md-10");
		$("#mainContainer").addClass("col-md-12");
		
		var $toggleButtonShow = $("<a>", { "class" : "btn btn-default", "id" : "toggleButtonShow", "href" : "javascript:mainController.sideMenu.showSideMenu();", "style" : "position: absolute; top:0px; left:0px;"})
								.append($("<span>", { "class" : "glyphicon glyphicon-resize-horizontal" }));
		
		$("#main").append($toggleButtonShow);
	}
	
	this.showSideMenu = function() {
		$("#" + this._containerId).show();
		$("#toggleButtonShow").remove();
		$("#mainContainer").removeClass("col-md-12");
		$("#mainContainer").addClass("col-md-10");
	}
	
	this._repaint = function() {
		var _this = this;
		var $container = $("#" + this._containerId);
		var $widget = $("<div>");
		
		//
		// Fix Header
		//
		var $header = $("<div>");
		var $headerItemList = $("<ul>", { "class" : "nav navbar-nav"});
			$header
				.append(
						$("<nav>", { "class" : "navbar navbar-default", "role" : "navigation", "style" : "margin:0px;"})
							.append($headerItemList)
					);
		
		var $pinButton = $("<li>")
							.append($("<a>", { "id" : "pin-button", "href" : "javascript:mainController.changeView(\"showInspectors\",null)" })
										.append($("<img>", { "src" : "./img/pin-icon.png", "style" : "width:16px; height:16px;"}))
										.append($("<span>", { "id" : "num-pins" }).append(this._mainController.inspector.inspectedSamples.length))
									);
		
		var $toggleButton = $("<li>")
				.append($("<a>", { "href" : "javascript:mainController.sideMenu.hideSideMenu();" })
					.append($("<span>", { "class" : "glyphicon glyphicon-resize-horizontal" }))
				);
		
		var $searchForm = $("<li>")
						.append($("<form>", { "class" : "navbar-form", "onsubmit" : "return false;"})
									.append($("<input>", { "id" : "search", "type" : "text", "onkeyup" : "mainController.changeView(\"showSearchPage\", event.target.value);", "class" : "form-control search-query", "placeholder" : "Search"}))
								);
		
		var logoutButton = $("<a>", { "id" : "logout-button", "href" : "" }).append($("<span>", { "class" : "glyphicon glyphicon-off"}));
		logoutButton.click(function() { 
			$('body').addClass('bodyLogin');
			_this._serverFacade.logout(function(data) { 
				$("#login-form-div").show();
				$("#main").hide();
			});
		});
		var $logoutButton = $("<li>").append(logoutButton);
		
		$headerItemList.append($logoutButton);
		$headerItemList.append($pinButton);
		$headerItemList.append($toggleButton);
		$headerItemList.append($searchForm);
		
		var $body = $("<div>");
			$widget
				.append($header)
				.append($body);
		
		var $title = $("<div>", { "class" : "sideMenuTitle" });
			$header
				.append($title);
		
		$container.empty();
		$container.append($widget);
			
		//
		// Print Menu
		//
		this._menuDOMTitle = $title;
		this._menuDOMBody = $body;
		this.repaint();
	}
	
	this.repaint = function() {
		var _this = this;
		var menuToPaint = this._pointerToMenuNode;
		
		//
		// Title
		//
		this._menuDOMTitle.empty();
		var isBackButtonShown = menuToPaint.parent !== null;
		if(isBackButtonShown) {
			var backButton = $("<a>", { "id" : "back-button", "href" : "javascript:void(0);", "style" : "float:left; color:black; padding-left:10px;" }).append($("<span>", { "class" : "glyphicon glyphicon-arrow-left"}));
			var backButtonClick = function(menuItem) {
				return function() {
						_this._pointerToMenuNode = menuItem.parent;
						_this.repaint();
				}
			};
			backButton.click(backButtonClick(menuToPaint));
			this._menuDOMTitle.append(backButton);
		}
		
		var $mainTitle = $("<span>").append(menuToPaint.displayName);
		if(isBackButtonShown) {
			$mainTitle.css({
				"margin-left" : "-24px"
			});
		}
		
		this._menuDOMTitle.append($mainTitle);
		
		
		//
		// Body
		//
		this._menuDOMBody.empty();
		for(var mIdx = 0; mIdx < menuToPaint.newMenuIfSelected.children.length; mIdx++ ) {
			var menuItem = menuToPaint.newMenuIfSelected.children[mIdx];
			
			
			var $menuItem = $("<div>", { "class" : "sideMenuItem" });
			var $menuItemTitle = $("<span>").append(menuItem.displayName);
			$menuItem.append($menuItemTitle);
			
			if(menuItem.isTitle) {
				$menuItem.addClass("sideMenuItemTitle");
			}
			
			if(menuItem.isSelectable) {
				$menuItem.addClass("sideMenuItemSelectable");
				if(menuItem.newMenuIfSelected && menuItem.newMenuIfSelected.children.length > 0) {
					$menuItem.append("<span class='glyphicon glyphicon-chevron-right put-chevron-right'></span>");
				}
				
				var clickFunction = function(menuItem) {
					return function() {
						if(menuItem.newMenuIfSelected && menuItem.newMenuIfSelected.children.length > 0) {
							_this._pointerToMenuNode = menuItem;
							_this.repaint();
						}
						
						if(menuItem.newViewIfSelected !== null) {
							_this._mainController.changeView(menuItem.newViewIfSelected, menuItem.newViewIfSelectedData);
						}
					}
				};
				
				$menuItem.click(clickFunction(menuItem));
			}
			
			this._menuDOMBody.append($menuItem);
		}
		
		$(window).resize();
	}
	
}

function SideMenuWidgetComponent(isSelectable, isTitle, displayName, parent, newMenuIfSelected, newViewIfSelected, newViewIfSelectedData) {
	this.isSelectable = isSelectable;
	this.isTitle = isTitle;
	this.displayName = displayName;
	this.parent = parent;
	this.newMenuIfSelected = newMenuIfSelected;
	this.newViewIfSelected = newViewIfSelected;
	this.newViewIfSelectedData = newViewIfSelectedData;
}