define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget" ], function($, AbstractView, AbstractWidget) {

	//
	// RESOLUTION CHOOSER VIEW
	//

	function ResolutionChooserView(controller) {
		this.init(controller);
	}

	$.extend(ResolutionChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("resolutionChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			$("<label>").text("Resolution").attr("for", "resolutionChooserSelect").appendTo(this.panel);

			var select = $("<select>").attr("id", "resolutionChooserSelect").addClass("form-control").appendTo(this.panel);
			$("<option>").attr("value", "").text("Default").appendTo(select);

			this.controller.getResolutions().forEach(function(resolution) {
				var value = resolution.width + "x" + resolution.height;
				$("<option>").attr("value", value).text(value).appendTo(select);
			});

			select.change(function() {
				if (select.val() == "") {
					thisView.controller.setSelectedResolution(null);
				} else {
					thisView.controller.setSelectedResolution(select.val());
				}
			});

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var select = this.panel.find("select");

			if (this.controller.getSelectedResolution() != null) {
				select.val(this.controller.getSelectedResolution());
			} else {
				select.val("");
			}
		}

	});

	//
	// RESOLUTION CHOOSER
	//

	function ResolutionChooserWidget(resolutions) {
		this.init(resolutions);
	}

	$.extend(ResolutionChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(resolutions) {
			AbstractWidget.prototype.init.call(this, new ResolutionChooserView(this));
			this.setResolutions(resolutions);
		},

		getSelectedResolution : function() {
			return this.selectedResolution;
		},

		setSelectedResolution : function(resolution) {
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