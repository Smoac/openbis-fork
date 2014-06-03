define([ "jquery", "components/common/Logger", "components/common/CallbackManager", "components/imageviewer/AbstractWidget",
		"components/imageviewer/ImageViewerView", "components/imageviewer/DataSetChooserWidget", "components/imageviewer/ImageParametersWidget",
		"components/imageviewer/ImageWidget", "components/imageviewer/ImageLoader", "components/imageviewer/ImageData",
		"components/imageviewer/OpenbisFacade" ], function($, Logger, CallbackManager, AbstractWidget, ImageViewerView, DataSetChooserWidget,
		ImageParametersWidget, ImageWidget, ImageLoader, ImageData, OpenbisFacade) {

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
					var correctDataSetCodes = [];

					thisViewer.getDataSetCodes().forEach(function(dataSetCode) {
						var correct = true;

						if (!thisViewer.getDataStoreUrl(dataSetCode)) {
							Logger.log("Ignoring data set: " + dataSetCode + " - could not get data store url");
							correct = false;
						}
						if (!thisViewer.getImageInfo(dataSetCode)) {
							Logger.log("Ignoring data set: " + dataSetCode + " - could not get image info");
							correct = false;
						}
						if (!thisViewer.getImageResolutions(dataSetCode)) {
							Logger.log("Ignoring data set: " + dataSetCode + " - could not get image resolutions");
							correct = false;
						}

						if (correct) {
							correctDataSetCodes.push(dataSetCode);
						}
					});

					thisViewer.dataSetCodes = correctDataSetCodes;
					thisViewer.loaded = true;
					callback();
				});

				this.facade.getDataStoreBaseURLs(thisViewer.dataSetCodes, manager.registerCallback(function(response) {
					if (response.error) {
						alert("Could not load data store urls: " + JSON.stringify(response.error));
					} else {
						thisViewer.dataSetCodeToDataStoreUrlMap = response.result;
					}
				}));

				this.facade.getImageInfo(thisViewer.dataSetCodes, manager.registerCallback(function(response) {
					if (response.error) {
						alert("Could not load image info: " + JSON.stringify(response.error));
					} else {
						thisViewer.dataSetCodeToImageInfoMap = response.result;
					}
				}));

				this.facade.getImageResolutions(thisViewer.dataSetCodes, manager.registerCallback(function(response) {
					if (response.error) {
						alert("Could not load image resolution: " + JSON.stringify(response.error));
					} else {
						thisViewer.dataSetCodeToImageResolutionsMap = response.result;
					}
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
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().getSelectedChannel();
		},

		setSelectedChannel : function(channel) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().setSelectedChannel(channel);
		},

		getSelectedMergedChannels : function() {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().getSelectedMergedChannels();
		},

		setSelectedMergedChannels : function(channels) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getChannelChooserWidget().setSelectedMergedChannels(channels);
		},

		getSelectedResolution : function() {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getResolutionChooserWidget().getSelectedResolution();
		},

		setSelectedResolution : function(resolution) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getResolutionChooserWidget().setSelectedResolution(resolution);
		},

		getSelectedTransformation : function() {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getTransformationChooserWidget().getSelectedTransformation();
		},

		setSelectedTransformation : function(transformation) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getTransformationChooserWidget().setSelectedTransformation(transformation);
		},

		getUserDefinedTransformationParameters : function(channel) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getTransformationChooserWidget().getUserDefinedTransformationParameters(channel);
		},

		setUserDefinedTransformationParameters : function(channel, parameters) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getTransformationChooserWidget().setUserDefinedTransformationParameters(channel, parameters);
		},

		getUserDefinedTransformationParametersMap : function() {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getTransformationChooserWidget().getUserDefinedTransformationParametersMap();
		},

		setUserDefinedTransformationParametersMap : function(parametersMap) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getTransformationChooserWidget().setUserDefinedTransformationParametersMap(parametersMap);
		},

		getSelectedChannelStackId : function() {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
			return viewer.getChannelStackChooserWidget().getSelectedChannelStackId();
		},

		setSelectedChannelStackId : function(channelStackId) {
			var viewer = this.getImageParametersWidget(this.getSelectedDataSetCode());
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
			imageData.setTransformation(this.getSelectedTransformation());
			imageData.setUserDefinedTransformationParametersMap(this.getUserDefinedTransformationParametersMap());
			return imageData;
		},

		getDataSetChooserWidget : function() {
			var thisWidget = this;

			if (this.dataSetChooserWidget == null) {
				var widget = new DataSetChooserWidget(this.getDataSetCodes());

				widget.addChangeListener(function(event) {
					var oldWidget = thisWidget.getImageParametersWidget(event.getOldValue());
					var newWidget = thisWidget.getImageParametersWidget(event.getNewValue());

					newWidget.setState(oldWidget.getState());

					thisWidget.getImageWidget().setImageData(thisWidget.getSelectedImageData());
					thisWidget.refresh();
				});

				this.dataSetChooserWidget = widget;
			}

			return this.dataSetChooserWidget;
		},

		getImageParametersWidget : function(dataSetCode) {
			var thisWidget = this;

			if (!this.imageViewerMap) {
				this.imageViewerMap = {};
			}

			if (!this.imageViewerMap[dataSetCode]) {
				var widget = new ImageParametersWidget(this.getImageInfo(dataSetCode), this.getImageResolutions(dataSetCode));

				widget.getChannelStackChooserWidget().setChannelStackContentLoader(function(channelStack, callback) {
					var imageData = thisWidget.getSelectedImageData();
					imageData.setChannelStackId(channelStack.id);
					thisWidget.getImageLoader().loadImage(imageData, callback);
				});

				widget.addChangeListener(function() {
					thisWidget.getImageWidget().setImageData(thisWidget.getSelectedImageData());
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