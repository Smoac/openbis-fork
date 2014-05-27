define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView" ], function($, bootstrap, bootstrapSlider, AbstractView) {

	//
	// CHANNEL STACK MATRIX CHOOSER VIEW
	//

	function ChannelStackMatrixChooserView(controller) {
		this.init(controller);
	}

	$.extend(ChannelStackMatrixChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("channelStackChooserWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			this.panel.append(this.createTimePointWidget());
			this.panel.append(this.controller.getTimePointButtonsWidget().render());
			this.panel.append(this.createDepthWidget());
			this.panel.append(this.controller.getDepthButtonsWidget().render());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var time = this.controller.getSelectedTimePoint();
			var timeWidget = this.panel.find(".timePointWidget");
			var timeLabel = this.panel.find(".timePointWidget label");
			var timeInput = this.panel.find(".timePointWidget input");
			var timeToggle = this.panel.find(".timePointWidget a")

			if (time != null) {
				var timeCount = this.controller.getTimePoints().length;
				var timeIndex = this.controller.getTimePoints().indexOf(time);

				timeLabel.text("Time: " + time + " sec (" + (timeIndex + 1) + "/" + timeCount + ")");
				timeToggle.text(this.controller.getTimePointButtonsWidget().isVisible() ? "Hide Buttons" : "Show Buttons");
				timeInput.slider("setValue", time);

				if (timeCount <= 1) {
					timeWidget.addClass("disabled");
					timeInput.slider("disable");
				} else {
					timeWidget.removeClass("disabled");
					timeInput.slider("enable");
				}
			}

			var depth = this.controller.getSelectedDepth();
			var depthWidget = this.panel.find(".depthWidget");
			var depthLabel = this.panel.find(".depthWidget label");
			var depthInput = this.panel.find(".depthWidget input");
			var depthToggle = this.panel.find(".depthWidget a");

			if (depth != null) {
				var depthCount = this.controller.getDepths().length;
				var depthIndex = this.controller.getDepths().indexOf(depth);

				depthLabel.text("Depth: " + depth + " (" + (depthIndex + 1) + "/" + depthCount + ")");
				depthToggle.text(this.controller.getDepthButtonsWidget().isVisible() ? "Hide Buttons" : "Show Buttons");
				depthInput.slider("setValue", depthIndex);

				if (depthCount <= 1) {
					depthWidget.addClass("disabled");
					depthInput.slider("disable");
				} else {
					depthWidget.removeClass("disabled");
					depthInput.slider("enable");
				}
			}
		},

		createTimePointWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("timePointWidget").addClass("form-group");

			var label = $("<label>").attr("for", "timePointInput");
			var input = $("<input>").attr("id", "timePointInput").attr("type", "text").addClass("form-control");

			var labelContainer = $("<div>").addClass("labelContainer").append(label).appendTo(widget);
			var inputContainer = $("<div>").addClass("inputContainer").append(input).appendTo(widget);

			$("<a>").click(function() {
				if (thisView.controller.getTimePoints().length > 1) {
					var buttonsWidget = thisView.controller.getTimePointButtonsWidget();
					buttonsWidget.setVisible(!buttonsWidget.isVisible());
				}
			}).appendTo(labelContainer)

			input.slider({
				"min" : 0,
				"max" : this.controller.getTimePoints().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var timeIndex = parseInt(event.value);
					var time = thisView.controller.getTimePoints()[timeIndex];
					thisView.controller.setSelectedTimePoint(time);
				}
			});

			return widget;
		},

		createDepthWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("depthWidget").addClass("form-group");

			var label = $("<label>").attr("for", "depthInput");
			var input = $("<input>").attr("id", "depthInput").attr("type", "text").addClass("form-control");

			var labelContainer = $("<div>").addClass("labelContainer").append(label).appendTo(widget);
			var inputContainer = $("<div>").addClass("inputContainer").append(input).appendTo(widget);

			$("<a>").click(function() {
				if (thisView.controller.getDepths().length > 1) {
					var buttonsWidget = thisView.controller.getDepthButtonsWidget();
					buttonsWidget.setVisible(!buttonsWidget.isVisible());
				}
			}).appendTo(labelContainer)

			input.slider({
				"min" : 0,
				"max" : this.controller.getDepths().length - 1,
				"step" : 1,
				"tooltip" : "hide"
			}).on("slide", function(event) {
				if (!$.isArray(event.value) && !isNaN(event.value)) {
					var depthIndex = parseInt(event.value);
					var depth = thisView.controller.getDepths()[depthIndex];
					thisView.controller.setSelectedDepth(depth);
				}
			});

			return widget;
		}

	});

	return ChannelStackMatrixChooserView;

});
