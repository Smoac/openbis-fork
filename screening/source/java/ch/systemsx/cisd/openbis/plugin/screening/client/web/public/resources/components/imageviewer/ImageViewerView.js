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
			var table = $("<table>").appendTo(this.panel);
			var row = $("<tr>").appendTo(table);

			var formCell = $("<td>").addClass("formCell").appendTo(row);
			var formPanel = $("<div>").addClass("formPanel").appendTo(formCell);

			formPanel.append(this.renderDataSetChooserWidget()).append(this.renderImageParametersWidget());

			var imageCell = $("<td>").addClass("imageCell").appendTo(row);
			var imagePanel = $("<div>").addClass("imagePanel").appendTo(imageCell);

			imagePanel.append(this.renderImageWidget());

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