function MainMenu(containerId, profile) {
	this.containerId = containerId;
	this.profile = profile;
	
	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		
		var menuInner = "<div class='mainMenuContainer'>";
			
			menuInner += "<div class='mainMenuIconContainer'>";
			menuInner += "<h2>Laboratory</h2><hr>";
			
			menuInner += "<span class='mainMenuIcon'>";
			menuInner += "<a href='javascript:showSamplesPage(\"SYSTEM_EXPERIMENT\");'>" + "<img src='./images/experiment-icon.png' />" + "</a> ";
			menuInner += "<p>" + "Experiments" + "</p>";
			menuInner += "</span>";
			menuInner += "</div>";
			
			for(typeGroupCode in this.profile.typeGroups) {
				menuInner += "<div class='mainMenuIconContainer'>";
				menuInner += "<h2>" + this.profile.typeGroups[typeGroupCode]["DISPLAY_NAME"] + "</h2><hr>";
				for(var i = 0; i < this.profile.typeGroups[typeGroupCode]["LIST"].length; i++) {
					var sampleType = this.profile.getTypeForTypeCode(this.profile.typeGroups[typeGroupCode]["LIST"][i]);
					
					menuInner += "<span class='mainMenuIcon'>";
					menuInner += "<a href='javascript:showSamplesPage(\""+sampleType.code+"\");'>" + "<img src='./images/notebook-icon.png' />" + "</a> ";
					menuInner += "<p>" +sampleType.description + "</p>";
					menuInner += "</span>";
				}
				menuInner += "</div>";
			}
			
			menuInner += "</div>";
		
		$("#"+this.containerId).append(menuInner);
	}
}