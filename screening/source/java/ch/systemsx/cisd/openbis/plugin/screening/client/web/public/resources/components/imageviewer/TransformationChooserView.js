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
			var thisView = this;

			if (this.controller.isUserDefinedTransformation()) {
				var channels = this.controller.getSelectedChannels();
				var scales = [];

				channels.forEach(function(channel) {
					var parameters = thisView.controller.getUserDefinedTransformationParameters(channel);
					scales.push(parameters.min + "_" + parameters.max);
				});

				var channelsChanned = !this.currentChannels || this.currentChannels.toString() != channels.toString();
				var scalesChanned = !this.currentScales || this.currentScales.toString() != scales.toString();

				if (channelsChanned || scalesChanned) {
					container.empty().append(this.renderParameters());
					this.currentChannels = channels;
					this.currentScales = scales;
				} else {
					this.refreshParameters();
				}
			} else {
				container.empty();
				this.currentChannels = null;
				this.currentScales = null;
			}
		},

		renderParameters : function() {
			var thisView = this;

			var panel = $("<div>").addClass("transformationParameters");

			this.controller.getSelectedChannels().forEach(function(channel) {
				var parameters = thisView.controller.getUserDefinedTransformationParameters(channel);
				panel.append(thisView.renderChannelParameters(channel, parameters));
			});

			return panel;
		},

		renderChannelParameters : function(channel, parameters) {
			var thisView = this;
			var widget = $("<div>").addClass("transformationParameter").addClass("form-group");

			var channelObject = this.controller.getChannelsMap()[channel];

			var label = $("<label>").text(channelObject.label + " [" + parameters.blackpoint + ", " + parameters.whitepoint + "]");
			var input = $("<input>").attr("type", "text").attr("channel", channel).addClass("form-control");

			var labelContainer = $("<div>").addClass("labelContainer").append(label).appendTo(widget);
			var inputContainer = $("<div>").addClass("inputContainer").append(input).appendTo(widget);

			input.slider({
				"min" : parameters.min,
				"max" : parameters.max,
				"step" : 1,
				"tooltip" : "hide",
				"value" : [ parameters.blackpoint, parameters.whitepoint ]
			}).on("slide", function(event) {
				var value = input.slider("getValue");

				thisView.controller.setUserDefinedTransformationParameters(channel, {
					"min" : parameters.min,
					"max" : parameters.max,
					"blackpoint" : value[0],
					"whitepoint" : value[1],
				});
			});

			var rescale = $("<a>").text("Rescale").click(function() {
				var value = input.slider("getValue");

				thisView.controller.setUserDefinedTransformationParameters(channel, {
					"min" : value[0],
					"max" : value[1],
					"blackpoint" : value[0],
					"whitepoint" : value[1],
				});
			}).appendTo(labelContainer);

			var reset = $("<a>").text("Reset").click(function() {
				var value = input.slider("getValue");

				thisView.controller.setUserDefinedTransformationParameters(channel, {
					"min" : 0,
					"max" : 65535,
					"blackpoint" : 0,
					"whitepoint" : 65535,
				});
			}).appendTo(labelContainer);

			return widget;
		},

		refreshParameters : function() {
			var thisView = this;

			this.panel.find(".transformationParameter").each(function() {
				var label = $(this).find("label");
				var input = $(this).find("input");

				var channel = input.attr("channel");
				var channelObject = thisView.controller.getChannelsMap()[channel];
				var channelParameters = thisView.controller.getUserDefinedTransformationParameters(channel);

				label.text(channelObject.label + " [" + channelParameters.blackpoint + ", " + channelParameters.whitepoint + "]");
				input.slider("setValue", [ channelParameters.blackpoint, channelParameters.whitepoint ]);
			});
		}

	});

	return TransformationChooserView;

});