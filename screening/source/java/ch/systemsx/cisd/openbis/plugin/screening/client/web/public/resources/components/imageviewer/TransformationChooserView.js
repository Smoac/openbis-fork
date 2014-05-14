define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// TRANSFORMATION CHOOSER VIEW
	//

	function TransformationChooserView(controller) {
		this.init(controller);
	}

	$.extend(TransformationChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("transformationChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			$("<label>").text("Filter").attr("for", "transformationChooserSelect").appendTo(this.panel);

			var select = $("<select>").attr("id", "transformationChooserSelect").addClass("form-control").appendTo(this.panel);

			this.controller.getTransformations().forEach(function(transformation) {
				$("<option>").attr("value", transformation.code).text(transformation.label).appendTo(select);
			});

			select.val(this.controller.getSelectedTransformation());
			select.change(function() {
				thisView.controller.setSelectedTransformation(select.val());
			});

			return this.panel;
		},

		refresh : function() {
			this.panel.empty();
			this.render();
		}

	});

	return TransformationChooserView;

});