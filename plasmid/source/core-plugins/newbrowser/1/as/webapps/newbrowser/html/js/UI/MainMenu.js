function MainMenu(containerId, profile) {
	this.containerId = containerId;
	this.profile = profile;
	this.menuStructure = profile.menuStructure;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		
		//
		// Main Menu
		//
		var menuInner = "<div class='mainMenuContainer'>";
			for (var k = 0; k < this.menuStructure.length; k++) {
				var groupOfMenuItems = this.menuStructure[k];
				
				menuInner += "<div class='mainMenuIconContainer'>";
				menuInner += "<h2>" + groupOfMenuItems.displayName + "</h2><hr>";
				
				for(var i = 0; i < groupOfMenuItems.menuItems.length; i++) {
					var menuItem = groupOfMenuItems.menuItems[i];
					
					menuInner += "<span class='mainMenuIcon'>";
					menuInner += "<a href='javascript:" + menuItem.href+ "(\"" + menuItem.hrefArgs + "\");'>" + "<img src='" + menuItem.image + "' />" + "</a> ";
					menuInner += "<p>" + menuItem.displayName + "</p>";
					menuInner += "</span>";
				}
			}
			
		menuInner += "</div>";
				
		$("#"+this.containerId).append(menuInner);
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