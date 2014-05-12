define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// IMAGE VIEWER VIEW
	//

	function ImageViewerView(controller) {
		this.init(controller);
	}

	$.extend(ImageViewerView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("imageViewer");
		},

		render : function() {
			this.panel.append(this.renderDataSetChooserWidget());
			this.panel.append(this.renderImageParametersWidget());
			this.panel.append(this.renderImageWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			this.refreshImageParametersWidget();
		},

		renderDataSetChooserWidget : function() {
			return this.controller.getDataSetChooserWidget().render();
		},

		renderImageParametersWidget : function() {
			return $("<div>").addClass("imageParametersWidgetContainer");
		},

		renderImageWidget : function() {
			return this.controller.getImageWidget().render();
		},

		refreshImageParametersWidget : function() {
			var container = this.panel.find(".imageParametersWidgetContainer");
			var widget = this.controller.getImageParametersWidget(this.controller.getSelectedDataSetCode());
			container.children().detach();
			container.append(widget.render());
		}

	});

	return ImageViewerView;

});