define([ "jquery", "bootstrap", "bootstrap-slider", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget",
		"components/imageviewer/MovieButtonsWidget", "components/imageviewer/ChannelStackManager" ], function($, bootstrap, bootstrapSlider,
		AbstractView, AbstractWidget, MovieButtonsWidget, ChannelStackManager) {

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
			this.panel.append(this.createButtonsWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var channelStackId = this.controller.getSelectedChannelStackId();

			if (channelStackId != null) {
				var count = this.controller.getChannelStacks().length;
				var index = this.controller.getChannelStackIndex(channelStackId);

				var sliderLabel = this.panel.find(".sliderWidget label");
				sliderLabel.text("Channel Stack: " + index + " (" + (index + 1) + "/" + count + ")");

				var sliderInput = this.panel.find(".sliderWidget input");
				sliderInput.slider("setValue", index);

				this.buttons.setSelectedFrame(index);
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
		},

		createButtonsWidget : function() {
			var thisView = this;

			var buttons = new MovieButtonsWidget(this.controller.getChannelStacks().length);

			buttons.setFrameContentLoader(function(frameIndex, callback) {
				var channelStack = thisView.controller.getChannelStacks()[frameIndex];
				thisView.controller.loadChannelStackContent(channelStack, callback);
			});

			buttons.addChangeListener(function() {
				var channelStack = thisView.controller.getChannelStacks()[buttons.getSelectedFrame()];
				thisView.controller.setSelectedChannelStackId(channelStack.id);
			});

			this.buttons = buttons;
			return buttons.render();
		}

	});

	//
	// CHANNEL STACK SERIES CHOOSER
	//

	function ChannelStackSeriesChooserWidget(channelStacks) {
		this.init(channelStacks);
	}

	$.extend(ChannelStackSeriesChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channelStacks) {
			AbstractWidget.prototype.init.call(this, new ChannelStackSeriesChooserView(this));
			this.channelStackManager = new ChannelStackManager(channelStacks);
			this.setSelectedChannelStackId(channelStacks[0].id);
		},

		getChannelStacks : function() {
			return this.channelStackManager.getChannelStacks();
		},

		getChannelStackIndex : function(channelStackId) {
			return this.channelStackManager.getChannelStackIndex(channelStackId);
		},

		loadChannelStackContent : function(channelStack, callback) {
			this.getChannelStackContentLoader()(channelStack, callback);
		},

		getChannelStackContentLoader : function() {
			if (this.channelStackContentLoader) {
				return this.channelStackContentLoader;
			} else {
				return function(channelStack, callback) {
					callback();
				}
			}
		},

		setChannelStackContentLoader : function(channelStackContentLoader) {
			this.channelStackContentLoader = channelStackContentLoader;
		},

		getSelectedChannelStackId : function() {
			return this.selectedChannelStackId;
		},

		setSelectedChannelStackId : function(channelStackId) {
			if (this.selectedChannelStackId != channelStackId) {
				this.selectedChannelStackId = channelStackId;
				this.refresh();
				this.notifyChangeListeners();
			}
		}

	});

	return ChannelStackSeriesChooserWidget;

});
