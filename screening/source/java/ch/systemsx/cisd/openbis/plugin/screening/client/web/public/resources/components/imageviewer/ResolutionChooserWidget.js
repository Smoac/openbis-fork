define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ResolutionChooserView" ], function($, AbstractWidget,
		ResolutionChooserView) {

	//
	// RESOLUTION CHOOSER WIDGET
	//

	function ResolutionChooserWidget(resolutions) {
		this.init(resolutions);
	}

	$.extend(ResolutionChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(resolutions) {
			AbstractWidget.prototype.init.call(this, new ResolutionChooserView(this));
			this.setResolutions(resolutions);
		},

		doGetState : function(state) {
			state.selectedResolution = this.getSelectedResolution();
		},

		doSetState : function(state) {
			this.setSelectedResolution(state.selectedResolution);
		},

		getSelectedResolution : function() {
			if (this.selectedResolution) {
				return this.selectedResolution;
			} else {
				return null;
			}
		},

		setSelectedResolution : function(resolution) {
			if (resolution != null && $.inArray(resolution, this.getResolutionsCodes()) == -1) {
				resolution = null;
			}

			if (this.selectedResolution != resolution) {
				this.selectedResolution = resolution;
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		getResolutions : function() {
			if (this.resolutions) {
				return this.resolutions;
			} else {
				return [];
			}
		},

		getResolutionsCodes : function() {
			return this.getResolutions().map(function(resolution) {
				return resolution.width + "x" + resolution.height;
			});
		},

		setResolutions : function(resolutions) {
			if (!resolutions) {
				resolutions = [];
			}

			if (this.getResolutions().toString() != resolutions.toString()) {
				this.resolutions = resolutions;
				this.refresh();
				this.notifyChangeListeners();
			}
		}

	});

	return ResolutionChooserWidget;

});