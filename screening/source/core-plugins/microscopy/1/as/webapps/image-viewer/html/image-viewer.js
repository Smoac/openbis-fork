//
// ABSTRACT VIEW
//

function AbstractView(controller) {
	this.init(controller);
}

$.extend(AbstractView.prototype, {

	init : function(controller) {
		this.controller = controller;
	},

	render : function() {
		return null;
	},

	refresh : function() {

	}

});

//
// ABSTRACT WIDGET
//

function AbstractWidget(view) {
	this.init(view);
}

$.extend(AbstractWidget.prototype, {
	init : function(view) {
		this.setView(view);
		this.listeners = new ListenerManager();
		this.panel = $("<div>").addClass("widget");
	},

	load : function(callback) {
		callback();
	},

	render : function() {
		if (this.rendered) {
			return this.panel;
		}

		var thisWidget = this;

		this.load(function() {
			if (thisWidget.getView()) {
				thisWidget.panel.append(thisWidget.getView().render());
				thisWidget.rendered = true;
			}
		});

		return this.panel;
	},

	refresh : function() {
		if (!this.rendered) {
			return;
		}

		if (this.getView()) {
			this.getView().refresh();
		}
	},

	getView : function() {
		return this.view;
	},

	setView : function(view) {
		this.view = view;
		this.refresh();
	},

	addChangeListener : function(listener) {
		this.listeners.addListener('change', listener);
	},

	notifyChangeListeners : function() {
		this.listeners.notifyListeners('change');
	}
});

// TODO do not pollute the global namespace and expose only ImageViewer

//
// IMAGE VIEWER CHOOSER VIEW
//

function ImageViewerChooserView(controller) {
	this.init(controller);
}

$.extend(ImageViewerChooserView.prototype, AbstractView.prototype, {

	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
		this.panel = $("<div>").addClass("imageViewerChooser");
	},

	render : function() {
		this.panel.append(this.createDataSetChooserWidget());
		this.panel.append(this.createImageViewerContainerWidget());

		this.refresh();

		return this.panel;
	},

	refresh : function() {
		var select = this.panel.find(".dataSetChooser").find("select");
		var container = this.panel.find(".imageViewerContainer");

		if (this.controller.getSelectedDataSetCode() != null) {
			select.val(this.controller.getSelectedDataSetCode());
			container.children().detach();
			container.append(this.createImageViewerWidget(this.controller.getSelectedDataSetCode()));
		}
	},

	createDataSetChooserWidget : function() {
		var thisView = this;
		var widget = $("<div>").addClass("dataSetChooser").addClass("form-group");

		$("<label>").text("Data set").attr("for", "dataSetChooserSelect").appendTo(widget);

		var select = $("<select>").attr("id", "dataSetChooserSelect").addClass("form-control").appendTo(widget);

		this.controller.getDataSetCodes().forEach(function(dataSetCode) {
			$("<option>").attr("value", dataSetCode).text(dataSetCode).appendTo(select);
		});

		select.change(function() {
			thisView.controller.setSelectedDataSetCode(select.val());
		});

		return widget;
	},

	createImageViewerContainerWidget : function() {
		return $("<div>").addClass("imageViewerContainer");
	},

	createImageViewerWidget : function(dataSetCode) {
		if (!this.imageViewerMap) {
			this.imageViewerMap = {};
		}

		if (!this.imageViewerMap[dataSetCode]) {
			this.imageViewerMap[dataSetCode] = new ImageViewerWidget(this.controller.getSessionToken(), dataSetCode, this.controller
					.getDataStoreUrl(dataSetCode), this.controller.getImageInfo(dataSetCode), this.controller.getImageResolutions(dataSetCode));
		}

		return this.imageViewerMap[dataSetCode].render();
	}

});

//
// IMAGE VIEWER CHOOSER
//

function ImageViewerChooserWidget(openbis, dataSetCodes) {
	this.init(openbis, dataSetCodes);
}

$.extend(ImageViewerChooserWidget.prototype, AbstractWidget.prototype, {
	init : function(openbis, dataSetCodes) {
		AbstractWidget.prototype.init.call(this, new ImageViewerChooserView(this));
		this.facade = new OpenbisFacade(openbis);
		this.setDataSetCodes(dataSetCodes)
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
		if (this.dataSetCodes) {
			return this.dataSetCodes;
		} else {
			return [];
		}
	},

	setDataSetCodes : function(dataSetCodes) {
		if (!dataSetCodes) {
			dataSetCodes = [];
		}
		if (this.getDataSetCodes().toString() != dataSetCodes.toString()) {
			this.dataSetCodes = dataSetCodes;
			if (dataSetCodes.length > 0) {
				this.setSelectedDataSetCode(dataSetCodes[0]);
			}
			this.refresh();
		}
	},

	getSelectedDataSetCode : function() {
		if (this.selectedDataSetCode != null) {
			return this.selectedDataSetCode;
		} else {
			return null;
		}
	},

	setSelectedDataSetCode : function(dataSetCode) {
		if (this.getSelectedDataSetCode() != dataSetCode) {
			this.selectedDataSetCode = dataSetCode;
			this.refresh();
		}
	}

});

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

//
// CHANNEL CHOOSER VIEW
//

function ChannelChooserView(controller) {
	this.init(controller);
}

$.extend(ChannelChooserView.prototype, AbstractView.prototype, {

	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
		this.panel = $("<div>");
	},

	render : function() {
		this.panel.append(this.createChannelWidget());
		this.panel.append(this.createMergedChannelsWidget());

		this.refresh();

		return this.panel;
	},

	refresh : function() {
		var thisView = this;

		var select = this.panel.find("select");
		var mergedChannels = this.panel.find(".mergedChannelsWidget");

		if (this.controller.getSelectedChannel() != null) {
			select.val(this.controller.getSelectedChannel());
			mergedChannels.hide();
		} else {
			select.val("");
			mergedChannels.find("input").each(function() {
				var checkbox = $(this);
				checkbox.prop("checked", thisView.controller.isMergedChannelSelected(checkbox.val()));
				checkbox.prop("disabled", !thisView.controller.isMergedChannelEnabled(checkbox.val()));
			});
			mergedChannels.show();
		}
	},

	createChannelWidget : function() {
		var thisView = this;
		var widget = $("<div>").addClass("channelWidget").addClass("form-group");

		$("<label>").text("Channel").attr("for", "channelChooserSelect").appendTo(widget);

		var select = $("<select>").attr("id", "channelChooserSelect").addClass("form-control").appendTo(widget);
		$("<option>").attr("value", "").text("Merged Channels").appendTo(select);

		this.controller.getChannels().forEach(function(channel) {
			$("<option>").attr("value", channel.code).text(channel.label).appendTo(select);
		});

		select.change(function() {
			if (select.val() == "") {
				thisView.controller.setSelectedChannel(null);
			} else {
				thisView.controller.setSelectedChannel(select.val());
			}
		});

		return widget;
	},

	createMergedChannelsWidget : function() {
		var thisView = this;
		var widget = $("<div>").addClass("mergedChannelsWidget").addClass("form-group");

		$("<label>").text("Merged Channels").appendTo(widget);

		var checkboxes = $("<div>").appendTo(widget);

		this.controller.getChannels().forEach(function(channel) {
			var checkbox = $("<label>").addClass("checkbox-inline").appendTo(checkboxes);
			$("<input>").attr("type", "checkbox").attr("value", channel.code).appendTo(checkbox);
			checkbox.append(channel.label);
		});

		widget.find("input").change(function() {
			var channels = []
			widget.find("input:checked").each(function() {
				channels.push($(this).val());
			});
			thisView.controller.setSelectedMergedChannels(channels);
		});

		return widget;
	}

});

//
// CHANNEL CHOOSER
//

function ChannelChooserWidget(channels) {
	this.init(channels);
}

$.extend(ChannelChooserWidget.prototype, AbstractWidget.prototype, {

	init : function(channels) {
		AbstractWidget.prototype.init.call(this, new ChannelChooserView(this));
		this.setChannels(channels);
	},

	getSelectedChannel : function() {
		if (this.selectedChannel) {
			return this.selectedChannel;
		} else {
			return null;
		}
	},

	setSelectedChannel : function(channel) {
		if (this.getSelectedChannel() != channel) {
			this.selectedChannel = channel;
			this.refresh();
			this.notifyChangeListeners();
		}
	},

	getSelectedMergedChannels : function() {
		if (this.selectedMergedChannels) {
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
			this.notifyChangeListeners();
		}
	},

	isMergedChannelSelected : function(channel) {
		return $.inArray(channel, this.getSelectedMergedChannels()) != -1;
	},

	isMergedChannelEnabled : function(channel) {
		if (this.getSelectedMergedChannels().length == 1) {
			return !this.isMergedChannelSelected(channel);
		} else {
			return true;
		}
	},

	getChannels : function() {
		if (this.channels) {
			return this.channels;
		} else {
			return [];
		}
	},

	setChannels : function(channels) {
		if (!channels) {
			channels = [];
		}

		if (this.getChannels().toString() != channels.toString()) {
			this.setSelectedChannel(null);
			this.setSelectedMergedChannels(channels.map(function(channel) {
				return channel.code;
			}));
			this.channels = channels;
			this.refresh();
		}
	}

});

//
// RESOLUTION CHOOSER VIEW
//

function ResolutionChooserView(controller) {
	this.init(controller);
}

$.extend(ResolutionChooserView.prototype, AbstractView.prototype, {

	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
		this.panel = $("<div>").addClass("resolutionChooserWidget").addClass("form-group");
	},

	render : function() {
		var thisView = this;

		$("<label>").text("Resolution").attr("for", "resolutionChooserSelect").appendTo(this.panel);

		var select = $("<select>").attr("id", "resolutionChooserSelect").addClass("form-control").appendTo(this.panel);
		$("<option>").attr("value", "").text("Default").appendTo(select);

		this.controller.getResolutions().forEach(function(resolution) {
			var value = resolution.width + "x" + resolution.height;
			$("<option>").attr("value", value).text(value).appendTo(select);
		});

		select.change(function() {
			if (select.val() == "") {
				thisView.controller.setSelectedResolution(null);
			} else {
				thisView.controller.setSelectedResolution(select.val());
			}
		});

		this.refresh();

		return this.panel;
	},

	refresh : function() {
		var select = this.panel.find("select");

		if (this.controller.getSelectedResolution() != null) {
			select.val(this.controller.getSelectedResolution());
		} else {
			select.val("");
		}
	}

});

//
// RESOLUTION CHOOSER
//

function ResolutionChooserWidget(resolutions) {
	this.init(resolutions);
}

$.extend(ResolutionChooserWidget.prototype, AbstractWidget.prototype, {

	init : function(resolutions) {
		AbstractWidget.prototype.init.call(this, new ResolutionChooserView(this));
		this.setResolutions(resolutions);
	},

	getSelectedResolution : function() {
		return this.selectedResolution;
	},

	setSelectedResolution : function(resolution) {
		if (this.selectedResolution != resolution) {
			this.selectedResolution = resolution;
			this.refresh();
			this.notifyChangeListeners();
		}
	},

	getResolutions : function() {
		if (this.resolutions) {
			return this.resolutions;
		} else {
			return [];
		}
	},

	setResolutions : function(resolutions) {
		if (!resolutions) {
			resolutions = [];
		}

		if (this.getResolutions().toString() != resolutions.toString()) {
			this.resolutions = resolutions;
			this.refresh();
			this.notifyChangeListeners();
		}
	}

});

//
// CHANNEL STACK CHOOSER
//

function ChannelStackChooserWidget(channelStacks) {
	this.init(channelStacks);
}

$.extend(ChannelStackChooserWidget.prototype, {

	init : function(channelStacks) {
		var manager = new ChannelStackManager(channelStacks);

		if (manager.isMatrix()) {
			this.widget = new ChannelStackMatrixChooserWidget(channelStacks);
		} else {
			this.widget = new ChannelStackSeriesChooserWidget(channelStacks);
		}
	},

	render : function() {
		return this.widget.render();
	},

	getSelectedChannelStackId : function() {
		return this.widget.getSelectedChannelStackId();
	},

	setSelectedChannelStackId : function(channelStackId) {
		this.widget.setSelectedChannelStackId(channelStackId);
	},

	getChannelStackContentLoader : function() {
		return this.widget.getChannelStackContentLoader();
	},

	setChannelStackContentLoader : function(channelStackContentLoader) {
		return this.widget.setChannelStackContentLoader(channelStackContentLoader);
	},

	addChangeListener : function(listener) {
		this.widget.addChangeListener(listener);
	},

	notifyChangeListeners : function() {
		this.widget.notifyChangeListeners();
	}

});

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

//
// CHANNEL STACK MATRIX CHOOSER
//

function ChannelStackMatrixChooserWidget(channelStacks) {
	this.init(channelStacks);
}

$.extend(ChannelStackMatrixChooserWidget.prototype, AbstractWidget.prototype, {

	init : function(channelStacks) {
		AbstractWidget.prototype.init.call(this, new ChannelStackMatrixChooserView(this));
		this.channelStackManager = new ChannelStackManager(channelStacks);
	},

	getTimePoints : function() {
		return this.channelStackManager.getTimePoints();
	},

	getDepths : function() {
		return this.channelStackManager.getDepths();
	},

	getChannelStacks : function() {
		return this.channelStackManager.getChannelStacks();
	},

	getChannelStackById : function(channelStackId) {
		return this.channelStackManager.getChannelStackById(channelStackId);
	},

	getChannelStackByTimePointAndDepth : function(timePoint, depth) {
		return this.channelStackManager.getChannelStackByTimePointAndDepth(timePoint, depth);
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
	},

	getSelectedChannelStack : function() {
		var channelStackId = this.getSelectedChannelStackId();

		if (channelStackId != null) {
			return this.channelStackManager.getChannelStackById(channelStackId);
		} else {
			return null;
		}
	},

	setSelectedChannelStack : function(channelStack) {
		if (channelStack != null) {
			this.setSelectedChannelStackId(channelStack.id);
		} else {
			this.setSelectedChannelStackId(null);
		}
	},

	getSelectedTimePoint : function() {
		var channelStack = this.getSelectedChannelStack();
		if (channelStack != null) {
			return channelStack.timePointOrNull;
		} else {
			return null;
		}
	},

	setSelectedTimePoint : function(timePoint) {
		if (timePoint != null && this.getSelectedDepth() != null) {
			var channelStack = this.channelStackManager.getChannelStackByTimePointAndDepth(timePoint, this.getSelectedDepth());
			this.setSelectedChannelStack(channelStack);
		} else {
			this.setSelectedChannelStack(null);
		}
	},

	getSelectedDepth : function() {
		var channelStack = this.getSelectedChannelStack();
		if (channelStack != null) {
			return channelStack.depthOrNull;
		} else {
			return null;
		}
	},

	setSelectedDepth : function(depth) {
		if (depth != null && this.getSelectedTimePoint() != null) {
			var channelStack = this.channelStackManager.getChannelStackByTimePointAndDepth(this.getSelectedTimePoint(), depth);
			this.setSelectedChannelStack(channelStack);
		} else {
			this.setSelectedChannelStack(null);
		}
	}

});

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

//
// MOVIE BUTTONS VIEW
//

function MovieButtonsView(controller) {
	this.init(controller);
}

$.extend(MovieButtonsView.prototype, AbstractView.prototype, {

	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
		this.panel = $("<div>").addClass("movieButtonsWidget").addClass("form-group");
	},

	render : function() {
		var thisView = this;

		var row = $("<div>").addClass("row").appendTo(this.panel);

		var buttonsRow = $("<div>").addClass("buttons").addClass("row").appendTo(this.panel);
		var delayRow = $("<div>").addClass("delay").addClass("form-inline").appendTo(this.panel);

		$("<div>").addClass("col-md-6").append(buttonsRow).appendTo(row);
		$("<div>").addClass("col-md-6").append(delayRow).appendTo(row);

		var play = $("<button>").addClass("play").addClass("btn").addClass("btn-primary");
		$("<span>").addClass("glyphicon").addClass("glyphicon-play").appendTo(play);
		$("<div>").addClass("col-md-3").append(play).appendTo(buttonsRow);

		play.click(function() {
			thisView.controller.play();
		});

		var stop = $("<button>").addClass("stop").addClass("btn").addClass("btn-primary");
		$("<span>").addClass("glyphicon").addClass("glyphicon-stop").appendTo(stop);
		$("<div>").addClass("col-md-3").append(stop).appendTo(buttonsRow);

		stop.click(function() {
			thisView.controller.stop();
		});

		var prev = $("<button>").addClass("prev").addClass("btn").addClass("btn-default");
		$("<span>").addClass("glyphicon").addClass("glyphicon-backward").appendTo(prev);
		$("<div>").addClass("col-md-3").append(prev).appendTo(buttonsRow);

		prev.click(function() {
			thisView.controller.prev();
		});

		var next = $("<button>").addClass("next").addClass("btn").addClass("btn-default");
		$("<span>").addClass("glyphicon").addClass("glyphicon-forward").appendTo(next);
		$("<div>").addClass("col-md-3").append(next).appendTo(buttonsRow);

		next.click(function() {
			thisView.controller.next();
		});

		var delayTable = $("<table>").appendTo(delayRow);
		var delayTr = $("<tr>").appendTo(delayTable);

		$("<td>").append($("<span>").addClass("delayLabel").text("delay:").attr("for", "delayInput")).appendTo(delayTr);

		var delay = $("<input>").attr("id", "delayInput").attr("type", "text").addClass("delay").addClass("form-control");
		delay.change(function() {
			thisView.controller.setSelectedDelay(delay.val());
		});
		$("<td>").attr("width", "100%").append(delay).appendTo(delayTr);

		$("<td>").append($("<span>").addClass("delayUnit").text("ms")).appendTo(delayTr);

		this.refresh();

		return this.panel;
	},

	refresh : function() {
		var play = this.panel.find("button.play");
		play.prop("disabled", this.controller.isPlaying());

		var stop = this.panel.find("button.stop");
		stop.prop("disabled", this.controller.isStopped());

		var prev = this.panel.find("button.prev");
		prev.prop("disabled", this.controller.isFirstFrameSelected());

		var next = this.panel.find("button.next");
		next.prop("disabled", this.controller.isLastFrameSelected());

		var delay = this.panel.find("input.delay");
		delay.val(this.controller.getSelectedDelay());
	}

});

//
// MOVIE BUTTONS WIDGET
//

function MovieButtonsWidget(frameCount) {
	this.init(frameCount);
}

$.extend(MovieButtonsWidget.prototype, AbstractWidget.prototype, {

	init : function(frameCount) {
		AbstractWidget.prototype.init.call(this, new MovieButtonsView(this));
		this.frameCount = frameCount;
		this.frameContentLoader = function(frameIndex, callback) {
			callback();
		};
		this.frameAction = null;
		this.selectedDelay = 100;
		this.selectedFrame = 0;
	},

	play : function() {
		if (this.frameAction) {
			return;
		}

		if (this.getSelectedFrame() == this.frameCount - 1) {
			this.setSelectedFrame(0);
		}

		var thisButtons = this;

		this.frameAction = function() {
			if (thisButtons.getSelectedFrame() < thisButtons.frameCount - 1) {
				var frame = thisButtons.getSelectedFrame() + 1;
				var startTime = Date.now();

				thisButtons.setSelectedFrame(frame, function() {
					var prefferedDelay = thisButtons.selectedDelay;
					var actualDelay = Date.now() - startTime;

					setTimeout(function() {
						if (thisButtons.frameAction) {
							thisButtons.frameAction();
						}
					}, Math.max(1, prefferedDelay - actualDelay));
				});
			} else {
				thisButtons.stop();
				thisButtons.setSelectedFrame(0);
			}
		};

		this.frameAction();
		this.refresh();
	},

	stop : function() {
		if (this.frameAction) {
			this.frameAction = null;
			this.refresh();
		}
	},

	prev : function() {
		this.setSelectedFrame(this.getSelectedFrame() - 1);
	},

	next : function() {
		this.setSelectedFrame(this.getSelectedFrame() + 1);
	},

	isPlaying : function() {
		return this.frameAction != null;
	},

	isStopped : function() {
		return this.frameAction == null;
	},

	isFirstFrameSelected : function() {
		return this.getSelectedFrame() == 0;
	},

	isLastFrameSelected : function() {
		return this.getSelectedFrame() == (this.frameCount - 1)
	},

	getSelectedDelay : function() {
		return this.selectedDelay;
	},

	setSelectedDelay : function(delay) {
		if (this.selectedDelay != delay) {
			this.selectedDelay = delay;
			this.refresh();
		}
	},

	getSelectedFrame : function() {
		return this.selectedFrame;
	},

	setSelectedFrame : function(frame, callback) {
		frame = Math.min(Math.max(0, frame), this.frameCount - 1);

		if (this.selectedFrame != frame) {
			log("Selected frame: " + frame);
			this.selectedFrame = frame;
			this.frameContentLoader(frame, callback);
			this.refresh();
			this.notifyChangeListeners();
		}
	},

	setFrameContentLoader : function(frameContentLoader) {
		this.frameContentLoader = frameContentLoader;
	},

	getFrameContentLoader : function(frameContentLoader) {
		return this.frameContentLoader;
	}

});

//
// IMAGE VIEW
//

function ImageView(controller) {
	this.init(controller);
}

$.extend(ImageView.prototype, AbstractView.prototype, {

	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
		this.panel = $("<div>").addClass("imageWidget");
	},

	render : function() {
		this.refresh();
		return this.panel;
	},

	refresh : function() {
		var thisView = this;

		if (this.controller.getImageData()) {
			this.controller.loadImage(this.controller.getImageData(), function(image) {
				thisView.panel.empty();
				thisView.panel.append(image);
			});
		} else {
			this.panel.empty();
		}
	}

});

//
// IMAGE WIDGET
//

function ImageWidget(imageLoader) {
	this.init(imageLoader);
}

$.extend(ImageWidget.prototype, AbstractWidget.prototype, {

	init : function(imageLoader) {
		AbstractWidget.prototype.init.call(this, new ImageView(this));
		this.imageLoader = imageLoader;
	},

	loadImage : function(imageData, callback) {
		this.imageLoader.loadImage(imageData, callback);
	},

	getImageData : function(imageData) {
		return this.imageData;
	},

	setImageData : function(imageData) {
		this.imageData = imageData;
		this.refresh();
	}

});

//
// FACADE
//

function OpenbisFacade(openbis) {
	this.init(openbis);
}

$.extend(OpenbisFacade.prototype, {
	init : function(openbis) {
		this.openbis = openbis;
	},

	getSession : function() {
		return this.openbis.getSession();
	},

	getDataStoreBaseURLs : function(dataSetCodes, action) {
		this.openbis.getDataStoreBaseURLs(dataSetCodes, function(response) {
			var dataSetCodeToUrlMap = {};

			if (response.result) {
				response.result.forEach(function(urlForDataSets) {
					urlForDataSets.dataSetCodes.forEach(function(dataSetCode) {
						dataSetCodeToUrlMap[dataSetCode] = urlForDataSets.dataStoreURL;
					});
				});
				response.result = dataSetCodeToUrlMap;
			}

			action(response);
		});
	},

	getImageInfo : function(dataSetCodes, callback) {
		this.openbis.getImageInfo(dataSetCodes, callback);
	},

	getImageResolutions : function(dataSetCodes, callback) {
		this.openbis.getImageResolutions(dataSetCodes, callback);
	}
});

//
// CHANNEL STACK MANAGER
//

function ChannelStackManager(channelStacks) {
	this.init(channelStacks);
}

$.extend(ChannelStackManager.prototype, {

	init : function(channelStacks) {
		this.channelStacks = channelStacks;
		this.channelStacks.sort(function(o1, o2) {
			var s1 = o1.seriesNumberOrNull;
			var s2 = o2.seriesNumberOrNull;
			var t1 = o1.timePointOrNull;
			var t2 = o2.timePointOrNull;
			var d1 = o1.depthOrNull;
			var d2 = o2.depthOrNull;

			var compare = function(v1, v2) {
				if (v1 == null) {
					if (v2 == null) {
						return 0;
					} else {
						return -1;
					}
				} else if (v2 == null) {
					return 1;
				} else {
					if (v1 > v2) {
						return 1;
					} else if (v1 < v2) {
						return -1;
					} else {
						return 0;
					}
				}
			}

			return compare(s1, s2) * 100 + compare(t1, t2) * 10 + compare(d1, d2);
		});
	},

	isMatrix : function() {
		/*
		 * TODO return (!this.isSeriesNumberPresent() ||
		 * this.getSeriesNumbers().length == 1) && !this.isTimePointMissing() &&
		 * !this.isDepthMissing() && this.isDepthConsistent();
		 */
		return !this.isSeriesNumberPresent() && !this.isTimePointMissing() && !this.isDepthMissing() && this.isDepthConsistent();
	},

	isSeriesNumberPresent : function() {
		return this.channelStacks.some(function(channelStack) {
			return channelStack.seriesNumberOrNull;
		});
	},

	isTimePointMissing : function() {
		return this.channelStacks.some(function(channelStack) {
			return channelStack.timePointOrNull == null;
		});
	},

	isDepthMissing : function() {
		return this.channelStacks.some(function(channelStack) {
			return channelStack.depthOrNull == null;
		});
	},

	isDepthConsistent : function() {
		var map = this.getChannelStackByTimePointAndDepthMap();
		var depthCounts = {};

		for (timePoint in map) {
			var entry = map[timePoint];
			var depthCount = Object.keys(entry).length;
			depthCounts[depthCount] = true;
		}

		return Object.keys(depthCounts).length == 1;
	},

	getSeriesNumbers : function() {
		if (!this.seriesNumbers) {
			var seriesNumbers = {};

			this.channelStacks.forEach(function(channelStack) {
				if (channelStack.seriesNumberOrNull != null) {
					seriesNumbers[channelStack.seriesNumberOrNull] = true;
				}
			});

			this.seriesNumbers = Object.keys(seriesNumbers).map(function(seriesNumber) {
				return parseInt(seriesNumber);
			}).sort();
		}
		return this.seriesNumbers;
	},

	getSeriesNumber : function(index) {
		return this.getSeriesNumbers()[index];
	},

	getTimePoints : function() {
		if (!this.timePoints) {
			var timePoints = {};

			this.channelStacks.forEach(function(channelStack) {
				if (channelStack.timePointOrNull != null) {
					timePoints[channelStack.timePointOrNull] = true;
				}
			});

			this.timePoints = Object.keys(timePoints).map(function(timePoint) {
				return parseInt(timePoint);
			}).sort();
		}
		return this.timePoints;
	},

	getTimePoint : function(index) {
		return this.getTimePoints()[index];
	},

	getTimePointIndex : function(timePoint) {
		if (!this.timePointsMap) {
			var map = {};

			this.getTimePoints().forEach(function(timePoint, index) {
				map[timePoint] = index;
			});

			this.timePointsMap = map;
		}

		return this.timePointsMap[timePoint];
	},

	getDepths : function() {
		if (!this.depths) {
			var depths = {};

			this.channelStacks.forEach(function(channelStack) {
				if (channelStack.depthOrNull != null) {
					depths[channelStack.depthOrNull] = true;
				}
			});

			this.depths = Object.keys(depths).map(function(depth) {
				return parseInt(depth);
			}).sort();
		}
		return this.depths;
	},

	getDepth : function(index) {
		return this.getDepths()[index];
	},

	getDepthIndex : function(depth) {
		if (!this.depthsMap) {
			var map = {};

			this.getDepths().forEach(function(depth, index) {
				map[depth] = index;
			});

			this.depthsMap = map;
		}

		return this.depthsMap[depth];
	},

	getChannelStackIndex : function(channelStackId) {
		if (!this.channelStackMap) {
			var map = {};

			this.getChannelStacks().forEach(function(channelStack, index) {
				map[channelStack.id] = index;
			});

			this.channelStackMap = map;
		}

		return this.channelStackMap[channelStackId];
	},

	getChannelStackById : function(channelStackId) {
		if (!this.channelStackByIdMap) {
			var map = {};
			this.channelStacks.forEach(function(channelStack) {
				map[channelStack.id] = channelStack;
			});
			this.channelStackByIdMap = map;
		}
		return this.channelStackByIdMap[channelStackId];
	},

	getChannelStackByTimePointAndDepth : function(timePoint, depth) {
		var map = this.getChannelStackByTimePointAndDepthMap();
		var entry = map[timePoint];

		if (entry) {
			return entry[depth];
		} else {
			return null;
		}
	},

	getChannelStackByTimePointAndDepthMap : function() {
		if (!this.channelStackByTimePointAndDepthMap) {
			var map = {};
			this.channelStacks.forEach(function(channelStack) {
				if (channelStack.timePointOrNull != null && channelStack.depthOrNull != null) {
					var entry = map[channelStack.timePointOrNull];
					if (!entry) {
						entry = {};
						map[channelStack.timePointOrNull] = entry;
					}
					entry[channelStack.depthOrNull] = channelStack;
				}
			});
			this.channelStackByTimePointAndDepthMap = map;
		}
		return this.channelStackByTimePointAndDepthMap;
	},

	getChannelStacks : function() {
		return this.channelStacks;
	}

});

//
// IMAGE DATA
//

function ImageData() {
	this.init();
}

$.extend(ImageData.prototype, {

	init : function() {
	},

	setDataStoreUrl : function(dataStoreUrl) {
		this.dataStoreUrl = dataStoreUrl;
	},

	setSessionToken : function(sessionToken) {
		this.sessionToken = sessionToken;
	},

	setDataSetCode : function(dataSetCode) {
		this.dataSetCode = dataSetCode;
	},

	setChannelStackId : function(channelStackId) {
		this.channelStackId = channelStackId;
	},

	setChannels : function(channels) {
		this.channels = channels;
	},

	setResolution : function(resolution) {
		this.resolution = resolution;
	}

});

//
// IMAGE LOADER
//

function ImageLoader() {
	this.init();
}

$.extend(ImageLoader.prototype, {

	init : function() {
	},

	loadImage : function(imageData, callback) {
		log("loadImage: " + imageData.channelStackId);

		var url = imageData.dataStoreUrl + "/datastore_server_screening";
		url += "?sessionID=" + imageData.sessionToken;
		url += "&dataset=" + imageData.dataSetCode;
		url += "&channelStackId=" + imageData.channelStackId;

		imageData.channels.forEach(function(channel) {
			url += "&channel=" + channel;
		});

		if (imageData.resolution) {
			url += "&mode=thumbnail" + imageData.resolution;
		} else {
			url += "&mode=thumbnail480x480";
		}

		$("<img>").attr("src", url).load(function() {
			if (callback) {
				callback(this);
			}
		});
	}

});

//
// CALLBACK MANAGER
//

function CallbackManager(callback) {
	this.init(callback);
}

$.extend(CallbackManager.prototype, {

	init : function(callback) {
		this.callback = callback;
		this.callbacks = {};
	},

	registerCallback : function(callback) {
		var manager = this;

		var wrapper = function() {
			callback.apply(this, arguments);

			delete manager.callbacks[callback]

			for (c in manager.callbacks) {
				return;
			}

			manager.callback();
		}

		this.callbacks[callback] = callback;
		return wrapper;
	}
});

//
// LISTENER MANAGER
//

function ListenerManager() {
	this.init();
}

$.extend(ListenerManager.prototype, {

	init : function() {
		this.listeners = {};
	},

	addListener : function(eventType, listener) {
		if (!this.listeners[eventType]) {
			this.listeners[eventType] = []
		}
		this.listeners[eventType].push(listener);
	},

	notifyListeners : function(eventType) {
		if (this.listeners[eventType]) {
			this.listeners[eventType].forEach(function(listener) {
				listener();
			});
		}
	}
});

function log(msg) {
	if (console) {
		var date = new Date();
		console.log(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "." + date.getMilliseconds() + " - " + msg);
	}
}
