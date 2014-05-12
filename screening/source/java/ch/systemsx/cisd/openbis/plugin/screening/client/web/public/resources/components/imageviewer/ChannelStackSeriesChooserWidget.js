define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ChannelStackSeriesChooserView",
		"components/imageviewer/MovieButtonsWidget", "components/imageviewer/ChannelStackManager" ], function($, AbstractWidget,
		ChannelStackSeriesChooserView, MovieButtonsWidget, ChannelStackManager) {

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

			if (channelStacks && channelStacks.length > 0) {
				this.setSelectedChannelStackId(channelStacks[0].id);
			}
		},

		getState : function() {
			var state = {};
			state.buttonsWidget = this.getButtonsWidget().getState();
			state.selectedChannelStackIndex = this.getSelectedChannelStackIndex();
			return state;
		},

		setState : function(state) {
			if (state) {
				this.getButtonsWidget().setState(state.buttonsWidget);
				this.setSelectedChannelStackIndex(state.selectedChannelStackIndex);
			}
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

		getSelectedChannelStackIndex : function() {
			return this.channelStackManager.getChannelStackIndex(this.getSelectedChannelStackId());
		},

		setSelectedChannelStackIndex : function(index) {
			index = Math.min(Math.max(0, index), this.channelStackManager.getChannelStacks().length - 1);
			if (index > 0) {
				var channelStack = this.channelStackManager.getChannelStacks()[index];
				this.setSelectedChannelStackId(channelStack.id);
			} else {
				this.setSelectedChannelStackId(null);
			}
		},

		getSelectedChannelStackId : function() {
			if (this.selectedChannelStackId != undefined) {
				return this.selectedChannelStackId;
			} else {
				return null;
			}
		},

		setSelectedChannelStackId : function(channelStackId) {
			var channelStack = this.channelStackManager.getChannelStackById(channelStackId);
			var channelStackId = null;

			if (channelStack == null) {
				channelStackId = this.getChannelStacks().length > 0 ? this.getChannelStacks()[0].id : null;
			} else {
				channelStackId = channelStack.id;
			}

			if (this.getSelectedChannelStackId() != channelStackId) {
				this.selectedChannelStackId = channelStackId;
				this.getButtonsWidget().setSelectedFrame(this.getSelectedChannelStackIndex());
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		getButtonsWidget : function() {
			if (this.buttonsWidget == null) {
				var thisWidget = this;

				var widget = new MovieButtonsWidget(this.getChannelStacks().length);

				widget.setFrameContentLoader(function(frameIndex, callback) {
					var channelStack = thisWidget.getChannelStacks()[frameIndex];
					thisWidget.loadChannelStackContent(channelStack, callback);
				});

				widget.addChangeListener(function() {
					var channelStack = thisWidget.getChannelStacks()[widget.getSelectedFrame()];
					thisWidget.setSelectedChannelStackId(channelStack.id);
				});

				this.buttonsWidget = widget;
			}

			return this.buttonsWidget;
		}

	});

	return ChannelStackSeriesChooserWidget;

});
