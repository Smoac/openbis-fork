//This layout manager can potentially be used outside the ELN
// Requires Jquery, Jquery UI and Bootstrap

var LayoutManager = {
	body : null,
	mainContainer : null,
	firstColumn : null,
	secondColumn : null,
	secondColumnHeader : null,
	secondColumnContent : null,
	thirdColumn : null,
	isResizingColumn : false,
	_init : function() {
		if (this.mainContainer !== null) {
			this.firstColumn.resizable("destroy");
			this.secondColumn.resizable("destroy");

			this.mainContainer.empty();
		}

		// Reload DOM
		if (this.mainContainer === null) {
			this.body = $(document.body);
			this.mainContainer = $("#mainContainer");
		}

		this.firstColumn = $("<div>");
		this.firstColumn.css({
			display : "none",
			"overflow-x " : "hidden",
			"overflow-y " : "hidden"
		});
		this.secondColumn = $("<div>");
		this.secondColumn.css({
			display : "none",
			"overflow-x " : "hidden",
			"overflow-y " : "hidden"
		});
		this.secondColumnHeader = $("<div>");
		this.secondColumnHeader.css({
			display : "none",
			"overflow-x " : "hidden",
			"overflow-y " : "hidden"
		});
		this.secondColumnContent = $("<div>");
		this.secondColumnContent.css({
			display : "none",
			"overflow-x " : "hidden",
			"overflow-y " : "hidden"
		});
		this.thirdColumn = $("<div>");
		this.thirdColumn.css({
			display : "none",
			"overflow-x " : "hidden",
			"overflow-y " : "hidden"
		});
		//

		this.secondColumn.append(this.secondColumnHeader);
		this.secondColumn.append(this.secondColumnContent);
		this.mainContainer.append(this.firstColumn).append(this.secondColumn)
				.append(this.thirdColumn);

		// column scrolls functionality
		this.body.css({
			"overflow" : "hidden"
		});
		this.firstColumn.css({
			"overflow-x" : "hidden",
			"overflow-y" : "auto",
			"padding" : "0"
		});
		this.secondColumnContent.css({
			"overflow-x" : "hidden",
			"overflow-y" : "auto"
		});
		this.secondColumn.css({
			"padding" : "0"
		});
		this.thirdColumn.css({
			"overflow-x" : "hidden",
			"overflow-y" : "auto",
			"padding" : "0"
		});

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
		var _this = this;
		this.firstColumn.resizable({
			handles : 'e',
			ghost : true,
			start : function(event, ui) {
				_this.isResizingColumn = true;
			},
			stop : function(event, ui) {
				var widthChange = ui.size.width - ui.originalSize.width;
				_this.secondColumn.css('width', _this.secondColumn.width()
						+ (-1 * widthChange));
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
				_this.thirdColumn.css('width', _this.thirdColumn.width()
						+ (-1 * widthChange));
				_this.isResizingColumn = false;
			}
		});
	},
	_setDesktopLayout : function(view) {
		var height = Math.max(document.documentElement.clientHeight,
				window.innerHeight || 0);
		var headerHeight = 0;

		this.firstColumn.append(view.menu);
		this.firstColumn.css({
			display : "block",
			height : height
		});
		this.firstColumn.addClass("col-md-3");

		var secondColumnClass;

		if (view.auxContent) {
			secondColumnClass = "col-md-6";
		} else {
			secondColumnClass = "col-md-9";
		}
		this.secondColumn.css({
			display : "block"
		});
		this.secondColumn.addClass(secondColumnClass);

		if (view.header) {
			headerHeight = 200;
			this.secondColumnHeader.append(view.header);
			this.secondColumnHeader.css({
				display : "block",
				height : headerHeight
			});
		}

		this.secondColumnContent.append(view.content);
		this.secondColumnContent.css({
			display : "block",
			height : height - headerHeight
		});

		if (view.auxContent) {
			this.thirdColumn.append(view.auxContent);
			this.thirdColumn.css({
				display : "block",
				height : height
			});
			this.thirdColumn.addClass("col-md-3");
		}
	},
	_setTabletLayout : function(view) {
		var height = Math.max(document.documentElement.clientHeight,
				window.innerHeight || 0);
		var headerHeight = 0;

		this.firstColumn.append(view.menu);
		this.firstColumn.css({
			display : "block",
			height : height
		});
		this.firstColumn.addClass("col-sm-3");

		if (view.header) {
			headerHeight = 200;
			this.secondColumnHeader.append(view.header);
			this.secondColumnHeader.css({
				display : "block",
				height : headerHeight
			});
		}

		this.secondColumnContent.append(view.content);
		this.secondColumnContent.css({
			display : "block",
			height : height - headerHeight
		});

		if (view.auxContent) {
			this.secondColumnContent.append(view.auxContent);
		}

		this.secondColumn.css({
			display : "block"
		});
		this.secondColumn.addClass("col-sm-9");
	},
	_setMobileLayout : function(view) {
		var height = Math.max(document.documentElement.clientHeight,
				window.innerHeight || 0);
		this.firstColumn.append(view.menu);

		if (view.header) {
			this.firstColumn.append(view.header);
		}

		this.firstColumn.append(view.content);

		if (view.auxContent) {
			this.firstColumn.append(view.auxContent);
		}

		this.firstColumn.css({
			display : "block",
			height : height
		});
		this.firstColumn.addClass("col-xs-12");
	},
	canReload : function() {
		return this.isResizingColumn === false;
	},
	reloadView : function(view) {
		var DESKTOP_SIZE = 992;
		var TABLET_SIZE = 768;
		var MOBILE_SIZE = 0;

		this._init();

		var width = Math.max(document.documentElement.clientWidth,
				window.innerWidth || 0);
		if (width > DESKTOP_SIZE) {
			this._setDesktopLayout(view);
		} else if (width > TABLET_SIZE) {
			this._setTabletLayout(view);
		} else if (width > MOBILE_SIZE) {
			this._setMobileLayout(view);
		} else {
			alert("Layout manager unable to set layout, this should never happen.");
		}
	}
}

//$(window).resize(function() {
//	if(LayoutManager.canReload()) {
//		var newWidth = Math.max(document.documentElement.clientWidth, window.innerWidth || 0);
//		console.log("Resize called " + newWidth);
//		LayoutManager.reloadView(globalView);
//	}
//});