define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ImageParametersView",
		"components/imageviewer/ChannelChooserWidget", "components/imageviewer/ResolutionChooserWidget",
		"components/imageviewer/ChannelStackChooserWidget" ], function($, AbstractWidget, ImageParametersView, ChannelChooserWidget,
		ResolutionChooserWidget, ChannelStackChooserWidget) {

	//
	// IMAGE PARAMETERS WIDGET
	//

	function ImageParametersWidget(imageInfo, imageResolutions) {
		this.init(imageInfo, imageResolutions);
	}

	$.extend(ImageParametersWidget.prototype, AbstractWidget.prototype, {
		init : function(imageInfo, imageResolutions, imageLoader) {
			AbstractWidget.prototype.init.call(this, new ImageParametersView(this));
			this.imageInfo = imageInfo;
			this.imageResolutions = imageResolutions;
		},

		getState : function() {
			var state = {};
			state.channelChooserState = this.getChannelChooserWidget().getState();
			state.resolutionChooserState = this.getResolutionChooserWidget().getState();
			state.channelStackChooserState = this.getChannelStackChooserWidget().getState();
			return state;
		},

		setState : function(state) {
			if (state) {
				this.getChannelChooserWidget().setState(state.channelChooserState);
				this.getResolutionChooserWidget().setState(state.resolutionChooserState);
				this.getChannelStackChooserWidget().setState(state.channelStackChooserState);
			}
		},

		getChannelChooserWidget : function() {
			if (this.channelChooserWidget == null) {
				var thisWidget = this;
				this.channelChooserWidget = new ChannelChooserWidget(this.imageInfo.imageDataset.imageDataset.imageParameters.channels);
				this.channelChooserWidget.addChangeListener(function() {
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
			}
			return this.channelChooserWidget;
		},

		getResolutionChooserWidget : function() {
			if (this.resolutionChooserWidget == null) {
				var thisWidget = this;
				this.resolutionChooserWidget = new ResolutionChooserWidget(this.imageResolutions);
				this.resolutionChooserWidget.addChangeListener(function() {
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
			}
			return this.resolutionChooserWidget;
		},

		getChannelStackChooserWidget : function() {
			if (this.channelStackChooserWidget == null) {
				var thisWidget = this;
				this.channelStackChooserWidget = new ChannelStackChooserWidget(this.imageInfo.channelStacks);
				this.channelStackChooserWidget.addChangeListener(function() {
					thisWidget.refresh();
					thisWidget.notifyChangeListeners();
				});
			}
			return this.channelStackChooserWidget;
		}

	});

	return ImageParametersWidget;

});