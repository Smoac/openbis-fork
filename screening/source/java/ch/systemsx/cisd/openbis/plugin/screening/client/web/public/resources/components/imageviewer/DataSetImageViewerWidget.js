define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/DataSetImageViewerView",
		"components/imageviewer/ChannelChooserWidget", "components/imageviewer/ResolutionChooserWidget",
		"components/imageviewer/ChannelStackChooserWidget" ], function($, AbstractWidget, DataSetImageViewerView, ChannelChooserWidget,
		ResolutionChooserWidget, ChannelStackChooserWidget) {

	//
	// DATA SET IMAGE VIEWER WIDGET
	//

	function DataSetImageViewerWidget(imageInfo, imageResolutions) {
		this.init(imageInfo, imageResolutions);
	}

	$.extend(DataSetImageViewerWidget.prototype, AbstractWidget.prototype, {
		init : function(imageInfo, imageResolutions, imageLoader) {
			AbstractWidget.prototype.init.call(this, new DataSetImageViewerView(this));
			this.imageInfo = imageInfo;
			this.imageResolutions = imageResolutions;
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

	return DataSetImageViewerWidget;

});