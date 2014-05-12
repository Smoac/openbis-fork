define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView", "components/imageviewer/MovieButtonsWidget" ], function($,
		bootstrap, bootstrapSlider, AbstractView, MovieButtonsWidget) {

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

			var slidersRow = $("<div>").addClass("row").appendTo(this.panel);
			$("<div>").addClass("col-md-6").append(this.createTimePointWidget()).appendTo(slidersRow);
			$("<div>").addClass("col-md-6").append(this.createDepthWidget()).appendTo(slidersRow);

			var buttonsRow = $("<div>").appendTo(this.panel);
			buttonsRow.append(this.createTimePointButtonsWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var time = this.controller.getSelectedTimePoint();
			var timeLabel = this.panel.find(".timePointWidget label");
			var timeInput = this.panel.find(".timePointWidget input");

			if (time != null) {
				var timeCount = this.controller.getTimePoints().length;
				var timeIndex = this.controller.getTimePoints().indexOf(time);

				timeLabel.text("Time: " + time + " sec (" + (timeIndex + 1) + "/" + timeCount + ")");
				timeInput.slider("setValue", time);

				this.timePointButtons.setSelectedFrame(timeIndex);
			}

			var depth = this.controller.getSelectedDepth();
			var depthLabel = this.panel.find(".depthWidget label");
			var depthInput = this.panel.find(".depthWidget input");

			if (depth != null) {
				var depthCount = this.controller.getDepths().length;
				var depthIndex = this.controller.getDepths().indexOf(depth);

				depthLabel.text("Depth: " + depth + " (" + (depthIndex + 1) + "/" + depthCount + ")");
				depthInput.slider("setValue", depthIndex);
			}
		},

		createTimePointWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("timePointWidget").addClass("form-group");

			$("<label>").attr("for", "timePointInput").appendTo(widget);

			var timeInput = $("<input>").attr("id", "timePointInput").attr("type", "text").addClass("form-control");

			$("<div>").append(timeInput).appendTo(widget);

			timeInput.slider({
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

			$("<label>").attr("for", "depthInput").appendTo(widget);

			var depthInput = $("<input>").attr("id", "depthInput").attr("type", "text").addClass("form-control");

			$("<div>").append(depthInput).appendTo(widget);

			depthInput.slider({
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
		},

		createTimePointButtonsWidget : function() {
			var thisView = this;

			var buttons = new MovieButtonsWidget(this.controller.getTimePoints().length);

			buttons.setFrameContentLoader(function(frameIndex, callback) {
				var timePoint = thisView.controller.getTimePoints()[frameIndex];
				var depth = thisView.controller.getSelectedDepth();
				var channelStack = thisView.controller.getChannelStackByTimePointAndDepth(timePoint, depth);
				thisView.controller.loadChannelStackContent(channelStack, callback);
			});

			buttons.addChangeListener(function() {
				var timePoint = thisView.controller.getTimePoints()[buttons.getSelectedFrame()];
				thisView.controller.setSelectedTimePoint(timePoint);
			});

			this.timePointButtons = buttons;
			return buttons.render();
		}

	});

	return ChannelStackMatrixChooserView;

});
