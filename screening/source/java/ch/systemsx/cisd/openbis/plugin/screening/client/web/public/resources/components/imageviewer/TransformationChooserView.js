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

			this.panel.append(this.renderSelect());
			this.panel.append(this.renderParameters());

			return this.panel;
		},

		renderSelect : function() {
			var thisView = this;
			var select = $("<select>").attr("id", "transformationChooserSelect").addClass("form-control");

			this.controller.getTransformations().forEach(function(transformation) {
				$("<option>").attr("value", transformation.code).text(transformation.label).appendTo(select);
			});

			select.val(this.controller.getSelectedTransformation());
			select.change(function() {
				thisView.controller.setSelectedTransformation(select.val());
			});

			return select;
		},

		renderParameters : function() {
			var thisView = this;

			var parametersPanel = $("<div>").addClass("transformationChooserParameters");
			var parameters = this.controller.getTransformationParameters();

			if (parameters.length > 0) {
				parameters.forEach(function(parameter) {
					var parameterPanel = $("<div>");
					parameterPanel.append(parameter.name);
					$("<input>").attr("name", parameter.name).val(parameter.value).change(function() {
						var parameterValues = [];
						parametersPanel.find("input").each(function() {
							parameterValues.push({
								name : $(this).attr("name"),
								value : $(this).val()
							});
						});
						thisView.controller.setTransformationParameters(parameterValues);
					}).appendTo(parameterPanel);
					parametersPanel.append(parameterPanel);
				});
			}

			return parametersPanel;
		},

		refresh : function() {
			this.panel.empty();
			this.render();
		}

	});

	return TransformationChooserView;

});