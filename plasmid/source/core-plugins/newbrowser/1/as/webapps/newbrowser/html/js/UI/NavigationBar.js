function NavigationBar(navigationBarId, activeMenuId, profile) {
	this.navigationBarId = navigationBarId;
	this.activeMenuId = activeMenuId;
	this.profile = profile;
	this.breadcrumb = new Array(); 
	
	this.repaint = function() {
		$("#"+this.navigationBarId).empty();
		
		var menu = "";
			menu += "<div class='navbar-wrapper'>";
			menu += "<div id='navbar' class='navbar navbar-fixed-top'>";
			menu += "<div class='navbar-inner'>";
		
			menu += "<div class='pull-left'>";
			menu += "<ul class='nav'>";
			for(var i = 0; i < this.breadcrumb.length; i++) {
				menu += "<li id='" + this.breadcrumb[i].id + "'><a href=\"javascript:navigationBar.executeBreadCrumb(" + i + ")\">" + this.breadcrumb[i].displayName + "</a></li>";
				
				if( i !== this.breadcrumb.length - 1) {
					menu += "<li><a href='#'>></a></li>";
				}
			}
			
			menu += "</ul>";
			menu += "</div>";
		
			menu += "<div class='pull-right'>";
			menu += "<ul class='nav'>";
			menu += "<li><a id='pin-button' href='javascript:showInspectors()'><img src='./images/pin-icon.png' style='width:16px; height:16px;' /><span id='num-pins'>" + inspector.inspectedSamples.length + "</span></a></li>";
			menu += "<li><form class='navbar-search' onsubmit='return false;'><input id='search' type='text' onkeyup='showSearchPage(event);' class='search-query' placeholder='Search'></form></li>";
			menu += "<li><a id='logout-button' href=''><img src='./images/logout-icon.png' style='width:16px; height:16px;' /></a></li>";
			menu += "</ul>";
			menu += "</div>";
		
		
			menu += "</div> <!-- /.navbar-inner -->";
			menu += "</div> <!-- /.navbar -->";
			menu += "</div> <!-- /.navbar-wrapper -->";
		
		$("#"+this.navigationBarId).append(menu);
	
		$('#logout-button').click(function() { 
			openbisServer.logout(function(data) { 
				$("#login-form-div").show();
				$("#main").hide();
				$("#username").focus();
			});
		});
		
		this.updateMenu(this.activeMenuId);
	}
	
	this.updateMenu = function(newActiveId) {
		if(this.activeMenuId != null) { 
			$('#'+this.activeMenuId).removeClass('active');
		}
		
		if(newActiveId != null) {
			$('#'+newActiveId).addClass('active');
		}
		
		this.activeMenuId = newActiveId;
	}
	
	this.executeBreadCrumb = function(breadCrumbIndex) {
		var href = this.breadcrumb[breadCrumbIndex].href;
		var hrefArgs = this.breadcrumb[breadCrumbIndex].hrefArgs;
		window[href](hrefArgs);
	}
	
	this.updateBreadCrumbPage = function(breadCrumbPage) {
		var isFound = false;
		var i;
		
		for(i = 0; i < this.breadcrumb.length; i++) {
			isFound = this.breadcrumb[i].id === breadCrumbPage.id;
			if(isFound) {
				break;
			}
		}
		
		if(isFound) {
			while(this.breadcrumb.length > i) {
				this.breadcrumb.pop();
			}
		}
		
		this.breadcrumb.push(breadCrumbPage);
		this.repaint();
		this.updateMenu(breadCrumbPage.id);
	}
}

function BreadCrumbPage(id, href, hrefArgs, displayName) {
	this.id = id;
	this.href = href;
	this.hrefArgs = hrefArgs;
	this.displayName = displayName;
}