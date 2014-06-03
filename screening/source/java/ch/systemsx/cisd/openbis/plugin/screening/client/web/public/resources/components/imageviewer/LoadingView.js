define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// LOADING VIEW
	//

	function LoadingView(controller) {
		this.init(controller);
	}

	$.extend(LoadingView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>");
		},

		render : function() {
			return this.panel;
		},

		refresh : function() {
			this.panel.text(this.controller.isLoading() ? "loading..." : "");
		}

	});

	return LoadingView;

});