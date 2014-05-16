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

				if (count <= 1) {
					sliderInput.slider("disable");
				} else {
					sliderInput.slider("enable");
				}
			}
		},

		createSliderWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("sliderWidget").addClass("form-group");

			$("<label>").attr("for", "sliderInput").appendTo(widget);

			var sliderInput = $("<input>").attr("id", "sliderInput").attr("type", "text").addClass("form-control");

			$("<div>").append(sliderInput).appendTo(widget);

			sliderInput.slider({
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
