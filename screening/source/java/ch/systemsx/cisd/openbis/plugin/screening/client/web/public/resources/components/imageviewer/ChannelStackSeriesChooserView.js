define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView" ], function($, bootstrap, bootstrapSlider, AbstractView) {

	//
	// CHANNEL STACK SERIES CHOOSER VIEW
	//

	function ChannelStackSeriesChooserView(controller) {
		this.init(controller);
	}

	$.extend(ChannelStackSeriesChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("channelStackChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			this.panel.append(this.createSliderWidget());
			this.panel.append(this.controller.getChannelStackButtonsWidget().render());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var channelStackId = this.controller.getSelectedChannelStackId();

			if (channelStackId != null) {
				var count = this.controller.getChannelStacks().length;
				var index = this.controller.getChannelStackIndex(channelStackId);
				var channelStack = this.controller.getChannelStacks()[index];

				var sliderWidget = this.panel.find(".sliderWidget");

				var sliderLabel = this.panel.find(".sliderWidget label");
				var labelText = "Series: " + channelStack.seriesNumberOrNull;
				if (channelStack.timePointOrNull != null) {
					labelText += ", Time: " + channelStack.timePointOrNull + " sec";
				}
				if (channelStack.depthOrNull != null) {
					labelText += ", Depth: " + channelStack.depthOrNull;
				}
				sliderLabel.text(labelText);

				var sliderInput = this.panel.find(".sliderWidget input");
				sliderInput.slider("setValue", index);

				var toggle = this.panel.find(".sliderWidget a");
				toggle.text(this.controller.getChannelStackButtonsWidget().isVisible() ? "Hide Buttons" : "Show Buttons");

				if (count <= 1) {
					sliderWidget.addClass("disabled");
					sliderInput.slider("disable");
				} else {
					sliderWidget.removeClass("disabled");
					sliderInput.slider("enable");
				}
			}
		},

		createSliderWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("sliderWidget").addClass("form-group");

			var label = $("<label>").attr("for", "sliderInput").appendTo(widget);
			var input = $("<input>").attr("id", "sliderInput").attr("type", "text").addClass("form-control");

			var labelContainer = $("<div>").addClass("labelContainer").append(label).appendTo(widget);
			var inputContainer = $("<div>").addClass("inputContainer").append(input).appendTo(widget);

			$("<a>").click(function() {
				if (thisView.controller.getChannelStacks().length > 1) {
					var buttonsWidget = thisView.controller.getChannelStackButtonsWidget();
					buttonsWidget.setVisible(!buttonsWidget.isVisible());
				}
			}).appendTo(labelContainer)

			input.slider({
				"min" : 0,
				"max" : this.controller.getChannelStacks().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var index = parseInt(event.value);
					var channelStack = thisView.controller.getChannelStacks()[index];
					thisView.controller.setSelectedChannelStackId(channelStack.id);
				}
			});

			return widget;
		}

	});

	return ChannelStackSeriesChooserView;

});
