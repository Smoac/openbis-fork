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
 * Creates an instance of MainMenu.
 *
 * @constructor
 * @this {MainMenu}
 * @param {string} containerId The Container where the Inspector DOM will be atached.
 * @param {List<GroupOfMenuItems>} menuStructure The menu structure.
 */
function MainMenu(mainController, containerId, menuStructure, mainMenuContentExtra) {
	this.mainController = mainController;
	this.containerId = containerId;
	this.menuStructure = menuStructure;
	this.mainMenuContentExtra = mainMenuContentExtra;
	this.inventoryWidget = null;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		var mainMenuWrapper = $("#"+this.containerId);
		mainMenuWrapper.empty();
		
		//Browser Widget Title/Container
		mainMenuWrapper.append($("<h1>", {"style" : "clear: both;"}).text("Spaces > Projects > Experiments"));
		mainMenuWrapper.append($("<div>", { "id" : "browserWidgetContainer", "style" : "clear: both;"}));
		
		//Inventory Widget Title/Container
		mainMenuWrapper.append($("<h1>", {"style" : "clear: both;"}).text("Inventory"));
		mainMenuWrapper.append($("<div>", { "id" : "inventoryWidgetContainer", "style" : "clear: both;"}));
		
		//Extra content form the profile
		var $mainMenuExtra = $("<div>", { id: "mainMenuExtra", style: "clear:both;" });
		$mainMenuExtra.append(this.mainMenuContentExtra);
		mainMenuWrapper.append($mainMenuExtra);
		
		//Browser Widget
		this.browserWidget = new BrowserWidget("browserWidgetContainer", this.mainController, this.mainController.serverFacade);
		this.browserWidget.init();
		
		//Inventory Widget
		this.inventoryWidget = new InventoryWidget(this.mainController, "inventoryWidgetContainer", this.menuStructure);
		this.inventoryWidget.init();
	}
}