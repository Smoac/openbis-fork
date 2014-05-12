define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/ImageLoader", "components/imageviewer/ImageWidget" ], function($,
		AbstractView, ImageLoader, ImageWidget) {

	//
	// DATA SET IMAGE VIEWER VIEW
	//

	function DataSetImageViewerView(controller) {
		this.init(controller);
	}

	$.extend(DataSetImageViewerView.prototype, AbstractView.prototype, {
		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.imageLoader = new ImageLoader();
			this.panel = $("<div>").addClass("imageViewer");
		},

		render : function() {
			this.panel.append(this.controller.getChannelChooserWidget().render());
			this.panel.append(this.controller.getResolutionChooserWidget().render());
			this.panel.append(this.controller.getChannelStackChooserWidget().render());

			this.refresh();

			return this.panel;
		}

	});

	return DataSetImageViewerView;

});