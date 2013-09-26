/*
 * Copyright 2013 ETH Zuerich, CISD
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
 * Creates an instance of MainMenu.
 *
 * @constructor
 * @this {MainMenu}
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {Profile} profile The profile to be used, typicaly, the global variable that holds the configuration for the application.
 */
function MainMenu(mainController, containerId, profile) {
	this.mainController = mainController;
	this.containerId = containerId;
	this.profile = profile;
	this.menuStructure = profile.menuStructure;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var localReference = this;
		
		$("#"+this.containerId).empty();
		
		var $mainMenuContainer = $("<div>", {
			class: "mainMenuContainer"
		});
		
		for (var k = 0; k < this.menuStructure.length; k++) {
			var groupOfMenuItems = this.menuStructure[k];
		
			var $mainMenuIconContainer = $("<div>", { class: "mainMenuIconContainer"})
				.append($("<h2>").append(groupOfMenuItems.displayName))
				.append($("<hr>"));
				
			for(var i = 0; i < groupOfMenuItems.menuItems.length; i++) {
				var menuItem = groupOfMenuItems.menuItems[i];
				
				//this is necessary to avoid using the same menuItem reference in all clicks
				var onClick = function(menuItem) {
					return function() {
						localReference.mainController[menuItem.href](menuItem.hrefArgs);
					}
				}
				
				var $mainMenuIconLink = $("<a>", {
					href: "#",
					click: onClick(menuItem)
				}).append($("<img>", { src: menuItem.image }));
					
				var $mainMenuIcon = $("<span>", { class: "mainMenuIcon" })
					.append($mainMenuIconLink)
					.append($("<p>").append(menuItem.displayName));
				
				$mainMenuIconContainer.append($mainMenuIcon);
			}
			
			$mainMenuContainer.append($mainMenuIconContainer);
		}
			
		$("#"+this.containerId).append($mainMenuContainer);
	}
}

function GroupOfMenuItems(key, displayName, menuItems) {
	this.key = key;
	this.displayName = displayName;
	this.menuItems = menuItems;
}

function MenuItem(image, href, hrefArgs, displayName) {
	this.image = image;
	this.href = href;
	this.hrefArgs = hrefArgs;
	this.displayName = displayName;
}