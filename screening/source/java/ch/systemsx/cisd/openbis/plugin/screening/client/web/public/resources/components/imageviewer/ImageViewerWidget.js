define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget", "components/imageviewer/ChannelChooserWidget",
		"components/imageviewer/ResolutionChooserWidget", "components/imageviewer/ChannelStackChooserWidget",
		"components/imageviewer/ChannelStackManager", "components/imageviewer/ImageLoader", "components/imageviewer/ImageWidget",
		"components/imageviewer/ImageData", "components/imageviewer/OpenbisFacade" ], function($, AbstractView, AbstractWidget, ChannelChooserWidget,
		ResolutionChooserWidget, ChannelStackChooserWidget, ChannelStackManager, ImageLoader, ImageWidget, ImageData, OpenbisFacade) {

	//
	// IMAGE VIEWER VIEW
	//

	function ImageViewerView(controller) {
		this.init(controller);
	}

	$.extend(ImageViewerView.prototype, AbstractView.prototype, {
		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.imageLoader = new ImageLoader();
			this.panel = $("<div>").addClass("imageViewer");
		},

		render : function() {
			this.panel.append(this.createChannelWidget());
			this.panel.append(this.createResolutionWidget());
			this.panel.append(this.createChannelStackWidget());
			this.panel.append(this.createImageWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			this.channelWidget.setSelectedChannel(this.controller.getSelectedChannel());
			this.channelWidget.setSelectedMergedChannels(this.controller.getSelectedMergedChannels());
			this.resolutionWidget.setSelectedResolution(this.controller.getSelectedResolution());
			this.channelStackWidget.setSelectedChannelStackId(this.controller.getSelectedChannelStackId());
			this.imageWidget.setImageData(this.controller.getSelectedImageData());
		},

		createChannelWidget : function() {
			var thisView = this;

			var widget = new ChannelChooserWidget(this.controller.getChannels());
			widget.addChangeListener(function() {
				thisView.controller.setSelectedChannel(thisView.channelWidget.getSelectedChannel());
				thisView.controller.setSelectedMergedChannels(thisView.channelWidget.getSelectedMergedChannels());
			});

			this.channelWidget = widget;
			return widget.render();
		},

		createResolutionWidget : function() {
			var thisView = this;

			var widget = new ResolutionChooserWidget(this.controller.getResolutions());
			widget.addChangeListener(function() {
				thisView.controller.setSelectedResolution(thisView.resolutionWidget.getSelectedResolution());
			});

			this.resolutionWidget = widget;
			return widget.render();
		},

		createChannelStackWidget : function() {
			var thisView = this;

			var widget = new ChannelStackChooserWidget(this.controller.getChannelStacks());

			widget.setChannelStackContentLoader(function(channelStack, callback) {
				var imageData = thisView.controller.getSelectedImageData();
				imageData.setChannelStackId(channelStack.id);
				thisView.imageLoader.loadImage(imageData, callback);
			});

			widget.addChangeListener(function() {
				thisView.controller.setSelectedChannelStackId(thisView.channelStackWidget.getSelectedChannelStackId());
			});

			this.channelStackWidget = widget;
			return widget.render();
		},

		createImageWidget : function() {
			this.imageWidget = new ImageWidget(this.imageLoader);
			return this.imageWidget.render();
		}

	});

	//
	// IMAGE VIEWER
	//

	function ImageViewerWidget(sessionToken, dataSetCode, dataStoreUrl, imageInfo, imageResolutions) {
		this.init(sessionToken, dataSetCode, dataStoreUrl, imageInfo, imageResolutions);
	}

	$.extend(ImageViewerWidget.prototype, AbstractWidget.prototype, {
		init : function(sessionToken, dataSetCode, dataStoreUrl, imageInfo, imageResolutions) {
			AbstractWidget.prototype.init.call(this, new ImageViewerView(this));
			this.facade = new OpenbisFacade(openbis);
			this.sessionToken = sessionToken;
			this.dataSetCode = dataSetCode;
			this.dataStoreUrl = dataStoreUrl;
			this.imageInfo = imageInfo;
			this.imageResolutions = imageResolutions;

			var channels = this.getChannels();
			if (channels && channels.length > 0) {
				this.setSelectedMergedChannels(channels.map(function(channel) {
					return channel.code
				}));
			}

			var channelStacks = this.getChannelStacks();
			if (channelStacks && channelStacks.length > 0) {
				this.setSelectedChannelStackId(channelStacks[0].id);
			}
		},

		getSelectedChannel : function() {
			if (this.selectedChannel != null) {
				return this.selectedChannel;
			} else {
				return null;
			}
		},

		setSelectedChannel : function(channel) {
			if (this.getSelectedChannel() != channel) {
				this.selectedChannel = channel;
				this.refresh();
			}
		},

		getSelectedMergedChannels : function() {
			if (this.selectedMergedChannels != null) {
				return this.selectedMergedChannels;
			} else {
				return [];
			}
		},

		setSelectedMergedChannels : function(channels) {
			if (!channels) {
				channels = [];
			}

			if (this.getSelectedMergedChannels().toString() != channels.toString()) {
				this.selectedMergedChannels = channels;
				this.refresh();
			}
		},

		getSelectedChannelStackId : function() {
			if (this.selectedChannelStackId != null) {
				return this.selectedChannelStackId;
			} else {
				return null;
			}
		},

		setSelectedChannelStackId : function(channelStackId) {
			if (this.getSelectedChannelStackId() != channelStackId) {
				this.selectedChannelStackId = channelStackId;
				this.refresh();
			}
		},

		getSelectedResolution : function() {
			if (this.selectedResolution != null) {
				return this.selectedResolution;
			} else {
				return null;
			}
		},

		setSelectedResolution : function(resolution) {
			if (this.getSelectedResolution() != resolution) {
				this.selectedResolution = resolution;
				this.refresh();
			}
		},

		getSelectedImageData : function() {
			var imageData = new ImageData();
			imageData.setDataStoreUrl(this.dataStoreUrl);
			imageData.setSessionToken(this.sessionToken);
			imageData.setDataSetCode(this.dataSetCode);
			imageData.setChannelStackId(this.getSelectedChannelStackId());

			if (this.getSelectedChannel()) {
				imageData.setChannels([ this.getSelectedChannel() ]);
			} else {
				imageData.setChannels(this.getSelectedMergedChannels());
			}
			imageData.setResolution(this.getSelectedResolution());
			return imageData;
		},

		getChannels : function() {
			return this.imageInfo.imageDataset.imageDataset.imageParameters.channels;
		},

		getChannelStacks : function() {
			if (this.channelStackManager == null) {
				this.channelStackManager = new ChannelStackManager(this.imageInfo.channelStacks);
			}
			return this.channelStackManager.getChannelStacks();
		},

		getResolutions : function() {
			return this.imageResolutions;
		}

	// TODO add listeners for channel, resoluton, channel stack

	});

	return ImageViewerWidget;

});