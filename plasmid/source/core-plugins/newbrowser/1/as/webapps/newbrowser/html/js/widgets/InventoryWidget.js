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
 * Creates an instance of the inventory widget.
 *
 * @constructor
 * @this {InventoryWidget}
 * @param {string} containerId The Container where the Inspector DOM will be attached.
 * @param {List<GroupOfMenuItems>} menuStructure The menu structure.
 */
function InventoryWidget(mainController, containerId, menuStructure) {
	this.mainController = mainController;
	this.containerId = containerId;
	this.menuStructure = menuStructure;

	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var localReference = this;
		var inventoryWrapper = $("#"+this.containerId);
		inventoryWrapper.empty();
		
		var $mainMenuContainerWrapper = $("<div>", { style: "float:left; margin-right:10px;" });
		var $mainMenuContainer = $("<ul>", { class: "nav nav-pills nav-stacked span5" });
		$mainMenuContainerWrapper.append($mainMenuContainer);
		
		var $mainMenuContainerWrapperSub = $("<div>", { style: "float:left" });
		
		for (var k = 0; k < this.menuStructure.length; k++) {
			var groupOfMenuItems = this.menuStructure[k];
			
			var onHoverEvent = function(groupOfMenuItems) {
				return function() {
					var $mainMenuContainerSub = $("<ul>", { class: "nav nav-pills nav-stacked span5" });
					for(var i = 0; i < groupOfMenuItems.menuItems.length; i++) {
						var menuItem = groupOfMenuItems.menuItems[i];
						
						//this is necessary to avoid using the same menuItem reference in all clicks
						var onClick = function(menuItem) {
							return function() {
								localReference.mainController.changeView(menuItem.href, menuItem.hrefArgs);
							}
						}
						
						var $subMenuOption = $("<li>", {click: onClick(menuItem)})
						.append($("<a>").append(menuItem.displayName));
						
						$mainMenuContainerSub.append($subMenuOption);
					}
					$mainMenuContainerWrapperSub.empty();
					$mainMenuContainerWrapperSub.append($mainMenuContainerSub);
				}
			}
			
			var $mainMenuIconContainer = $("<li>", { mouseenter: onHoverEvent(groupOfMenuItems)})
				.append($("<a>").append(groupOfMenuItems.displayName + "<i class='icon-chevron-right'></i>"));
			
			$mainMenuContainer.append($mainMenuIconContainer);
		}
			
		inventoryWrapper.append($mainMenuContainerWrapper);
		inventoryWrapper.append($mainMenuContainerWrapperSub);
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