//This layout manager can potentially be used outside the ELN
// Requires Jquery, Jquery UI and Bootstrap

var LayoutManager = {
	DESKTOP_SIZE : 992,
	TABLET_SIZE : 768,
	MOBILE_SIZE : 0,
	MIN_HEADER_HEIGHT : 120,
	body : null,
	mainContainer : null,
	firstColumn : null,
	secondColumn : null,
	secondColumnHeader : null,
	secondColumnContent : null,
	secondColumnContentResize : function() {
		var width = $( window ).width();
		if (width > LayoutManager.TABLET_SIZE) {
			LayoutManager.secondColumnContent.css({
				height : $( window ).height() - LayoutManager.secondColumnHeader.outerHeight()
			});
		}
	},
	thirdColumn : null,
	isResizingColumn : false,
	isLoadingView : false,
	_init : function(isFirstTime) {
		var _this = this;
		
		if(this.body === null) {
			this.body = $(document.body);
			this.body.css({
				"overflow" : "hidden"
			});
		}
		
		if(this.mainContainer === null) {
			this.mainContainer = $("#mainContainer");
		}
		
		if(isFirstTime) {
			if(this.firstColumn !== null) {
				this.firstColumn.resizable("destroy");
				this.firstColumn.children().detach();
				this.firstColumn.remove();
				this.firstColumn = null;
			}
			
			if(this.secondColumn !== null) {
				this.secondColumn.resizable("destroy");
				this.secondColumn.children().detach();
				this.secondColumn.remove();
				this.secondColumn = null;
				
				this.secondColumnHeader = null;
				this.secondColumnContent = null;
			}
			
			if(this.thirdColumn !== null) {
				this.thirdColumn.children().detach();
				this.thirdColumn.remove();
				this.thirdColumn = null;
			}
		} else {
			this.secondColumnHeader.children().detach();
			this.secondColumnHeader.empty();
			this.secondColumnContent.children().detach();
			this.secondColumnContent.empty();
			this.thirdColumn.children().detach();
			this.thirdColumn.empty();
		}
		
		if(this.firstColumn == null) {
			this.firstColumn = $("<div>");
			this.firstColumn.css({
				"display" : "none",
				"overflow-x" : "hidden",
				"overflow-y" : "hidden",
				"padding" : "0",
				"float" : "left"
			});
		}
		
		if(this.secondColumn == null) {
			this.secondColumn = $("<div>");
			this.secondColumn.css({
				"display" : "none",
				"overflow-x": "hidden",
				"overflow-y": "hidden",
				"padding" : "0",
				"float" : "left"
			});
			this.secondColumnHeader = $("<div>");
			this.secondColumnHeader.css({
				'display' : "none",
				'overflow': "visible" //To show the dropdowns
			});
			this.secondColumnContent = $("<div>");
			this.secondColumnContent.css({
				display : "none",
				"overflow-x" : "auto",
				"overflow-y" : "auto"
			});
			
			this.secondColumn.append(this.secondColumnHeader);
			this.secondColumn.append(this.secondColumnContent);
			$(this.secondColumnHeader).on( "DOMNodeInserted", this.secondColumnContentResize);
			$(this.secondColumnHeader).on( "DOMNodeRemoved", this.secondColumnContentResize);
		}
		
		if(this.thirdColumn == null) {
			this.thirdColumn = $("<div>");
			this.thirdColumn.css({
				"display" : "none",
				"overflow-x" : "hidden",
				"overflow-y" : "auto",
				"padding" : "0",
				"float" : "left"
			});
		}
		
		if (isFirstTime) {
			//Attach created components
			this.mainContainer.append(this.firstColumn).append(this.secondColumn).append(this.thirdColumn);
			
			// 
			// Columns drag functionality
			//
			
			// Only usable in Desktop and tablet

			// Moving to the right +x
			// Add size to first column +x
			// Remove size from second column -1 * +x

			// Moving to the left -x
			// Remove size to the first column -x
			// Add size to the second column -1 * -x
			this.firstColumn.resizable({
				handles : 'e',
				ghost : true,
				start : function(event, ui) {
					_this.isResizingColumn = true;
				},
				stop : function(event, ui) {
					var widthChange = ui.size.width - ui.originalSize.width;
					_this.secondColumn.css('width', _this.secondColumn.width() + (-1 * widthChange) - 1);
					_this.isResizingColumn = false;
				}
			});
			
			// Only usable in Desktop mode

			// Moving to the right +x
			// Add size to the second column +x
			// Remove size from the third column -1 * +x

			// Moving to the left -x
			// Remove size from the second column -x
			// Add size to the third column -1 * -x
			this.secondColumn.resizable({
				handles : 'e',
				ghost : true,
				start : function(event, ui) {
					_this.isResizingColumn = true;
				},
				stop : function(event, ui) {
					var widthChange = ui.size.width - ui.originalSize.width;
					_this.thirdColumn.css('width', _this.thirdColumn.width() + (-1 * widthChange) - 1);
					_this.isResizingColumn = false;
				}
			});
		}
	},
	_setDesktopLayout : function(view, isFirstTime) {
		var width = $( window ).width();
		var height = $( window ).height();
		var headerHeight = 0;

		if(isFirstTime) {
			this.firstColumn.append(view.menu);
			this.firstColumn.css({
				"display" : "block",
				"height" : height,
				"width" : "25%"
			});
		}
		
		var secondColumWidth;
		if (view.auxContent) {
			secondColumWidth = (width - this.firstColumn.width()) * 0.66 - 1;
		} else {
			secondColumWidth = width - this.firstColumn.width() - 1;
		}
		
		this.secondColumn.css({
			"display" : "block",
			"width" : secondColumWidth
		});
		
		
		if (view.header) {
			headerHeight = this.MIN_HEADER_HEIGHT;
			this.secondColumnHeader.append(view.header);
			this.secondColumnHeader.css({
				display : "block",
				"min-height" : headerHeight,
				"height" : "auto"
			});
		} else {
			this.secondColumnHeader.css({ display : "none" });
		}

		if (view.content) {
			this.secondColumnContent.css({
				display : "block",
				height : height - headerHeight
			});
				
			this.secondColumnContent.append(view.content);
		} else {
			this.secondColumnContent.css({ display : "none" });
		}
		

		if (view.auxContent) {
			this.thirdColumn.css({
				"display" : "block",
				"height" : height,
				"width" : (width - this.firstColumn.width()) * 0.34 - 1
			});
			this.thirdColumn.append(view.auxContent);
		} else {
			this.thirdColumn.css({ 
				"display" : "none",
				"width" : "0%"
			});
		}
	},
	_setTabletLayout : function(view, isFirstTime) {
		var width = $( window ).width();
		var height = $( window ).height();
		var headerHeight = 0;

		if(isFirstTime) {
			this.firstColumn.append(view.menu);
			this.firstColumn.css({
				display : "block",
				height : height,
				"width" : "25%"
			});
		}
		
		this.secondColumn.css({
			display : "block",
			"width" : width - this.firstColumn.width() - 1
		});

		if (view.header) {
			headerHeight = this.MIN_HEADER_HEIGHT;
			this.secondColumnHeader.append(view.header);
			this.secondColumnHeader.css({
				display : "block",
				"min-height" : headerHeight,
				"height" : "auto"
			});
		} else {
			this.secondColumnHeader.css({ display : "none" });
		}

		if (view.content) {
			this.secondColumnContent.css({
				display : "block",
				height : height - headerHeight
			});
			
			this.secondColumnContent.append(view.content);
		} else {
			this.secondColumnContent.css({ display : "none" });
		}
		

		if (view.auxContent) {
			this.secondColumnContent.append(view.auxContent);
		}
		this.thirdColumn.css({ display : "none" });
	},
	_setMobileLayout : function(view, isFirstTime) {
		var width = $( window ).width();
		var height = $( window ).height();

		//
		// Empty Column each time
		//
		if(this.firstColumn) {
			this.firstColumn.children().detach();
		}
		if(this.secondColumn) {
			this.secondColumn.children().detach();
		}
		if(this.thirdColumn) {
			this.thirdColumn.children().detach();
		}
		this.firstColumn.empty();
		
		//
		// Set screen size
		//
		this.firstColumn.css({
			display : "block",
			height : height,
			"overflow-y" : "auto",
			"width" : "100%"
		});
		this.secondColumn.css({ display : "none" });
		this.thirdColumn.css({ display : "none" });
		
		//
		// Attach available views
		//
		if (view.menu) {
			this.firstColumn.append(view.menu);
		}

		if (view.header) {
			view.header.css({
				"min-height" : this.MIN_HEADER_HEIGHT,
				"height" : "auto"
			});
			this.firstColumn.append(view.header);
		}
		
		if(view.content) {
			this.firstColumn.append(view.content);
		}

		if (view.auxContent) {
			this.firstColumn.append(view.auxContent);
		}
	},
	canReload : function() {
		return  this.isResizingColumn === false && 
				this.isLoadingView === false && 
				this.firstColumn.width() > 0;
	},
	getContentWidth : function() {
		var width = $( window ).width();
		if (width > this.DESKTOP_SIZE) {
			return this.secondColumn.width();
		} else if (width > this.TABLET_SIZE) {
			return this.secondColumn.width();
		} else if (width > this.MOBILE_SIZE) {
			return this.firstColumn.width();
		} else {
			alert("Layout manager unable to know the layout, this should never happen.");
		}
	},
	reloadView : function(view, forceFirstTime) {
		var _this = this;
		this.isLoadingView = true;

		var isFirstTime = this.mainContainer === null || forceFirstTime === true;
		// console.log("reloadView called with isFirstTime:" + isFirstTime);
		this._init(isFirstTime);

		var width = $( window ).width();
		if (width > this.DESKTOP_SIZE) {
			this._setDesktopLayout(view, isFirstTime);
		} else if (width > this.TABLET_SIZE) {
			this._setTabletLayout(view, isFirstTime);
		} else if (width > this.MOBILE_SIZE) {
			this._setMobileLayout(view, isFirstTime);
		} else {
			alert("Layout manager unable to set layout, this should never happen.");
		}
		
		this.triggerResizeEventHandlers();
		this.isLoadingView = false;
	},
	resizeEventHandlers : [],
	addResizeEventHandler : function(eventHandler) {
		this.resizeEventHandlers.push(eventHandler);
	},
	triggerResizeEventHandlers : function() {
		for(var idx = 0; idx < this.resizeEventHandlers.length; idx++) {
			this.resizeEventHandlers[idx]();
		}
		this.secondColumnContentResize();
	},
	resize : function(view, forceFirstTime) {
		if(this.canReload()) {
			// console.log("reloadView");
			this.reloadView(view, forceFirstTime);
		}
	}
}

var rtime;
var timeout = false;
var delta = 200;

$(window).resize(function() {
    rtime = new Date();
    if (timeout === false) {
        timeout = true;
        setTimeout(resizeend, delta);
    }
});

function resizeend() {
    if (new Date() - rtime < delta) {
        setTimeout(resizeend, delta);
    } else {
        timeout = false;
        LayoutManager.resize(mainController.views, true);
    }
}
