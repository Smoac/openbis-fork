define([ "jquery", "components/common/CallbackManager", "components/imageviewer/AbstractWidget", "components/imageviewer/ImageViewerView",
		"components/imageviewer/DataSetChooserWidget", "components/imageviewer/DataSetImageViewerWidget", "components/imageviewer/ImageWidget",
		"components/imageviewer/ImageLoader", "components/imageviewer/ImageData", "components/imageviewer/OpenbisFacade" ], function($,
		CallbackManager, AbstractWidget, ImageViewerView, DataSetChooserWidget, DataSetImageViewerWidget, ImageWidget, ImageLoader, ImageData,
		OpenbisFacade) {

	//
	// IMAGE VIEWER WIDGET
	//

	function ImageViewerWidget(openbis, dataSetCodes) {
		this.init(openbis, dataSetCodes);
	}

	$.extend(ImageViewerWidget.prototype, AbstractWidget.prototype, {
		init : function(openbis, dataSetCodes) {
			AbstractWidget.prototype.init.call(this, new ImageViewerView(this));
			this.facade = new OpenbisFacade(openbis);
			this.dataSetCodes = dataSetCodes;
		},

		load : function(callback) {
			if (this.loaded) {
				callback();
			} else {
				var thisViewer = this;

				var manager = new CallbackManager(function() {
					thisViewer.loaded = true;
					callback();
				});

				this.facade.getDataStoreBaseURLs(thisViewer.dataSetCodes, manager.registerCallback(function(response) {
					thisViewer.dataSetCodeToDataStoreUrlMap = response.result;
				}));

				this.facade.getImageInfo(thisViewer.dataSetCodes, manager.registerCallback(function(response) {
					thisViewer.dataSetCodeToImageInfoMap = response.result;
				}));

				this.facade.getImageResolutions(thisViewer.dataSetCodes, manager.registerCallback(function(response) {
					thisViewer.dataSetCodeToImageResolutionsMap = response.result;
				}));
			}
		},

		getSessionToken : function() {
			return this.facade.getSession();
		},

		getImageLoader : function() {
			if (this.imageLoader == null) {
				this.imageLoader = new ImageLoader();
			}
			return this.imageLoader;
		},

		getImageInfo : function(dataSetCode) {
			return this.dataSetCodeToImageInfoMap[dataSetCode];
		},

		getImageResolutions : function(dataSetCode) {
			return this.dataSetCodeToImageResolutionsMap[dataSetCode];
		},

		getDataStoreUrl : function(dataSetCode) {
			return this.dataSetCodeToDataStoreUrlMap[dataSetCode];
		},

		getDataSetCodes : function() {
			return this.dataSetCodes;
		},

		getSelectedDataSetCode : function() {
			return this.getDataSetChooserWidget().getSelectedDataSetCode();
		},

		setSelectedDataSetCode : function(dataSetCode) {
			this.getDataSetChooserWidget().setSelectedDataSetCode(dataSetCode);
		},

		getSelectedChannel : function() {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().getSelectedChannel();
		},

		getSelectedMergedChannels : function() {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().getSelectedMergedChannels();
		},

		setSelectedMergedChannels : function(channels) {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().setSelectedMergedChannels(channels);
		},

		getSelectedResolution : function() {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getResolutionChooserWidget().getSelectedResolution();
		},

		setSelectedResolution : function(resolution) {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getResolutionChooserWidget().setSelectedResolution(resolution);
		},

		getSelectedChannelStackId : function() {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getChannelStackChooserWidget().getSelectedChannelStackId();
		},

		setSelectedChannelStackId : function(channelStackId) {
			var viewer = this.getDataSetImageViewerWidget(this.getSelectedDataSetCode());
			return viewer.getChannelStackChooserWidget().setSelectedChannelStackId(channelStackId);
		},

		getSelectedImageData : function() {
			var imageData = new ImageData();
			imageData.setDataStoreUrl(this.getDataStoreUrl(this.getSelectedDataSetCode()));
			imageData.setSessionToken(this.getSessionToken());
			imageData.setDataSetCode(this.getSelectedDataSetCode());
			imageData.setChannelStackId(this.getSelectedChannelStackId());

			if (this.getSelectedChannel()) {
				imageData.setChannels([ this.getSelectedChannel() ]);
			} else {
				imageData.setChannels(this.getSelectedMergedChannels());
			}
			imageData.setResolution(this.getSelectedResolution());
			return imageData;
		},

		getDataSetChooserWidget : function() {
			var thisWidget = this;

			if (this.dataSetChooserWidget == null) {
				var widget = new DataSetChooserWidget(this.getDataSetCodes());

				widget.addChangeListener(function(event) {
					var oldViewer = thisWidget.getDataSetImageViewerWidget(event.getOldValue());
					var newViewer = thisWidget.getDataSetImageViewerWidget(event.getNewValue());

					newViewer.getChannelChooserWidget().setSelectedChannel(oldViewer.getChannelChooserWidget().getSelectedChannel());
					newViewer.getChannelChooserWidget().setSelectedMergedChannels(oldViewer.getChannelChooserWidget().getSelectedMergedChannels());
					newViewer.getResolutionChooserWidget().setSelectedResolution(oldViewer.getResolutionChooserWidget().getSelectedResolution());
					newViewer.getChannelStackChooserWidget().setSelectedTimePoint(oldViewer.getChannelStackChooserWidget().getSelectedTimePoint());
					newViewer.getChannelStackChooserWidget().setSelectedDepth(oldViewer.getChannelStackChooserWidget().getSelectedDepth());

					thisWidget.getImageWidget().setImageData(thisWidget.getSelectedImageData());
					thisWidget.refresh();
				});

				this.dataSetChooserWidget = widget;
			}

			return this.dataSetChooserWidget;
		},

		getDataSetImageViewerWidget : function(dataSetCode) {
			var thisWidget = this;

			if (!this.imageViewerMap) {
				this.imageViewerMap = {};
			}

			if (!this.imageViewerMap[dataSetCode]) {
				var widget = new DataSetImageViewerWidget(this.getImageInfo(dataSetCode), this.getImageResolutions(dataSetCode));

				widget.getChannelStackChooserWidget().setChannelStackContentLoader(function(channelStack, callback) {
					var imageData = thisWidget.getSelectedImageData();
					imageData.setChannelStackId(channelStack.id);
					thisWidget.getImageLoader().loadImage(imageData, callback);
				});

				widget.addChangeListener(function() {
					thisWidget.getImageWidget().setImageData(thisWidget.getSelectedImageData());
					thisWidget.refresh();
				});

				this.imageViewerMap[dataSetCode] = widget;
			}

			return this.imageViewerMap[dataSetCode];
		},

		getImageWidget : function() {
			if (this.imageWidget == null) {
				var widget = new ImageWidget(this.getImageLoader())
				widget.setImageData(this.getSelectedImageData());
				this.imageWidget = widget;
			}
			return this.imageWidget;
		}

	});

	return ImageViewerWidget;

});