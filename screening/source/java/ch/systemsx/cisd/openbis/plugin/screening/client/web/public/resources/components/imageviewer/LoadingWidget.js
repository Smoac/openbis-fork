define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/LoadingView" ], function($, AbstractWidget, LoadingView) {

	//
	// LOADING WIDGET
	//

	function LoadingWidget() {
		this.init();
	}

	$.extend(LoadingWidget.prototype, AbstractWidget.prototype, {

		init : function() {
			AbstractWidget.prototype.init.call(this, new LoadingView(this));
		},

		setLoading : function(loading) {
			var thisWidget = this;

			if (this.isLoading() != loading) {
				if (!this.timeoutId) {
					this.timeoutId = setTimeout(function() {
						thisWidget.loading = loading;
						thisWidget.timeoutId = null;
						thisWidget.refresh();
					}, 100);
				}
			} else {
				if (this.timeoutId) {
					clearTimeout(this.timeoutId);
					this.timeoutId = null;
				}
			}
		},

		isLoading : function() {
			if (this.loading != undefined) {
				return this.loading;
			} else {
				return false;
			}
		}

	});

	return LoadingWidget;

});
