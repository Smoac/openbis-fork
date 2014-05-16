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

			var selectContainer = $("<div>").addClass("transformationSelectContainer").appendTo(this.panel);
			var parametersContainer = $("<div>").addClass("transformationParametersContainer").appendTo(this.panel);

			this.renderOrRefreshSelect(selectContainer);
			this.renderOrRefreshParameters(parametersContainer);

			return this.panel;
		},

		refresh : function() {
			this.renderOrRefreshSelect(this.panel.find(".transformationSelectContainer"));
			this.renderOrRefreshParameters(this.panel.find(".transformationParametersContainer"));
		},

		renderOrRefreshSelect : function(container) {
			var transformations = this.controller.getTransformations().map(function(transformation) {
				return transformation.code;
			});

			if (this.currentTransformations != undefined) {
				if (this.currentTransformations.toString() != transformations.toString()) {
					container.empty().append(this.renderSelect());
				} else {
					this.refreshSelect();
				}
			} else {
				container.append(this.renderSelect());
			}

			this.currentTransformations = transformations;
		},

		renderSelect : function(container) {
			var thisView = this;
			var widget = $("<div>").addClass("transformationSelect").addClass("form-group");

			$("<label>").text("Filter").attr("for", "transformationSelect").appendTo(widget);

			var select = $("<select>").attr("id", "transformationSelect").addClass("form-control");

			$("<div>").append(select).appendTo(widget);

			this.controller.getTransformations().forEach(function(transformation) {
				$("<option>").attr("value", transformation.code).text(transformation.label).appendTo(select);
			});

			select.val(this.controller.getSelectedTransformation());

			select.change(function() {
				thisView.controller.setSelectedTransformation(select.val());
			});
			return widget;
		},

		refreshSelect : function() {
			var select = this.panel.find(".transformationSelect select");

			if (select.val() != this.controller.getSelectedTransformation()) {
				select.val(this.controller.getSelectedTransformation());
			}
		},

		renderOrRefreshParameters : function(container) {
			var transformationParameters = this.controller.getTransformationParameters().map(function(parameter) {
				return parameter.channel + " " + parameter.name;
			});

			if (this.currentTransformationParameters != undefined) {
				if (this.currentTransformationParameters.toString() != transformationParameters.toString()) {
					container.empty().append(this.renderParameters());
				} else {
					this.refreshParameters();
				}
			} else {
				container.append(this.renderParameters());
			}

			this.currentTransformationParameters = transformationParameters;
		},

		renderParameters : function() {
			var thisView = this;

			var panel = $("<div>").addClass("transformationParameters");
			var parameters = this.controller.getTransformationParameters();

			if (parameters.length > 0) {
				var channelToParametersMap = {};

				parameters.forEach(function(parameter) {
					var channelParameters = channelToParametersMap[parameter.channel];

					if (!channelParameters) {
						channelParameters = {};
						channelToParametersMap[parameter.channel] = channelParameters;
					}

					channelParameters[parameter.name] = parameter;
				});

				this.controller.getSelectedChannels().forEach(function(channel) {
					panel.append(thisView.renderChannelParameters(channel, channelToParametersMap[channel]));
				});
			}

			return panel;
		},

		renderChannelParameters : function(channel, parameters) {
			var thisView = this;
			var widget = $("<div>").addClass("transformationParameter").addClass("form-group");

			var channelObject = this.controller.getChannelsMap()[channel];
			var blackPoint = parameters["blackpoint"].value;
			var whitePoint = parameters["whitepoint"].value;

			$("<label>").text(channelObject.label + " [" + blackPoint + ", " + whitePoint + "]").appendTo(widget);

			var input = $("<input>").attr("type", "text").attr("channel", channel).addClass("form-control");

			$("<div>").append(input).appendTo(widget);

			input.slider({
				"min" : 0,
				"max" : 65535,
				"step" : 1,
				"tooltip" : "hide",
				"value" : [ blackPoint, whitePoint ]
			}).on("slide", function(event) {
				thisView.controller.setTransformationParameters(thisView.getParameters());
			});

			return widget;
		},

		refreshParameters : function() {
			var thisView = this;

			var parameters = this.controller.getTransformationParameters();
			var channelToParametersMap = {};

			parameters.forEach(function(parameter) {
				var channelParameters = channelToParametersMap[parameter.channel];

				if (!channelParameters) {
					channelParameters = {};
					channelToParametersMap[parameter.channel] = channelParameters;
				}

				channelParameters[parameter.name] = parameter;
			});

			this.panel.find(".transformationParameter").each(function() {
				var label = $(this).find("label");
				var input = $(this).find("input");

				var channel = input.attr("channel");
				var channelObject = thisView.controller.getChannelsMap()[channel];
				var channelParameters = channelToParametersMap[channel];
				var blackPoint = channelParameters["blackpoint"].value;
				var whitePoint = channelParameters["whitepoint"].value;

				label.text(channelObject.label + " [" + blackPoint + ", " + whitePoint + "]");
				input.slider("setValue", [ blackPoint, whitePoint ]);
			});
		},

		getParameters : function() {
			var parameters = [];

			this.panel.find(".transformationParameters input").each(function() {
				var input = $(this);
				var value = input.slider("getValue");

				parameters.push({
					"channel" : input.attr("channel"),
					"name" : "blackpoint",
					"value" : value[0]
				});

				parameters.push({
					"channel" : input.attr("channel"),
					"name" : "whitepoint",
					"value" : value[1]
				});
			});

			return parameters;
		}

	});

	return TransformationChooserView;

});