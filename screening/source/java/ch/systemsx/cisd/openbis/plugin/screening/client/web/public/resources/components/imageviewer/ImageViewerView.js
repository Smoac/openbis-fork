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
			this.panel.append(this.renderDataSetImageViewerWidget());
			this.panel.append(this.renderImageWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			this.refreshDataSetImageViewerWidget();
		},

		renderDataSetChooserWidget : function() {
			return this.controller.getDataSetChooserWidget().render();
		},

		renderDataSetImageViewerWidget : function() {
			return $("<div>").addClass("dataSetImageViewerContainer");
		},

		renderImageWidget : function() {
			return this.controller.getImageWidget().render();
		},

		refreshDataSetImageViewerWidget : function() {
			var container = this.panel.find(".dataSetImageViewerContainer");
			var imageViewer = this.controller.getDataSetImageViewerWidget(this.controller.getSelectedDataSetCode());
			container.children().detach();
			container.append(imageViewer.render());
		}

	});

	return ImageViewerView;

});