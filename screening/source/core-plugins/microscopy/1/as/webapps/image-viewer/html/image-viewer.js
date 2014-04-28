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
// IMAGE VIEWER VIEW
//

function ImageViewerView(controller) {
	this.init(controller);
}

$.extend(ImageViewerView.prototype, AbstractView.prototype, {
	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
		this.imageLoader = new ImageLoader();
		this.panel = $("<div>");
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

		var widget = new ChannelChooserWidget(this.controller.getAllChannels());
		widget.addChangeListener(function() {
			thisView.controller.setSelectedChannel(thisView.channelWidget.getSelectedChannel());
			thisView.controller.setSelectedMergedChannels(thisView.channelWidget.getSelectedMergedChannels());
		});

		this.channelWidget = widget;
		return widget.render();
	},

	createResolutionWidget : function() {
		var thisView = this;

		var widget = new ResolutionChooserWidget(this.controller.getAllResolutions());
		widget.addChangeListener(function() {
			thisView.controller.setSelectedResolution(thisView.resolutionWidget.getSelectedResolution());
		});

		this.resolutionWidget = widget;
		return widget.render();
	},

	createChannelStackWidget : function() {
		var thisView = this;

		var widget = new ChannelStackChooserWidget(this.controller.getAllChannelStacks());

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

function ImageViewerWidget(openbis, dataSetCode) {
	this.init(openbis, dataSetCode);
}

$.extend(ImageViewerWidget.prototype, AbstractWidget.prototype, {
	init : function(openbis, dataSetCode) {
		AbstractWidget.prototype.init.call(this, new ImageViewerView(this));
		this.facade = new OpenbisFacade(openbis);
		this.dataSetCode = dataSetCode;
	},

	load : function(callback) {
		if (this.loaded) {
			callback();
		} else {
			var thisViewer = this;

			var manager = new CallbackManager(function() {
				thisViewer.loaded = true;

				var channels = thisViewer.getAllChannels();

				if (channels && channels.length > 0) {
					thisViewer.setSelectedMergedChannels(channels.map(function(channel) {
						return channel.code
					}));
				}

				var channelStacks = thisViewer.getAllChannelStacks();

				if (channelStacks && channelStacks.length > 0) {
					thisViewer.setSelectedChannelStackId(channelStacks[0].id);
				}

				callback();
			});

			this.facade.tryGetDataStoreBaseURL(thisViewer.dataSetCode, manager.registerCallback(function(response) {
				thisViewer.dataStoreUrl = response.result;
			}));

			this.facade.getImageInfo(thisViewer.dataSetCode, manager.registerCallback(function(response) {
				thisViewer.imageInfo = response.result;
			}));

			this.facade.getImageResolutions(thisViewer.dataSetCode, manager.registerCallback(function(response) {
				thisViewer.imageResolutions = response.result;
			}));
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
		imageData.setSessionToken(this.facade.getSession());
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

	getAllChannels : function() {
		return this.imageInfo.imageDataset.imageDataset.imageParameters.channels;
	},

	getAllChannelStacks : function() {
		return this.imageInfo.channelStacks.sort(function(o1, o2) {
			var t1 = o1.timePointOrNull;
			var t2 = o2.timePointOrNull;
			var d1 = o1.depthOrNull;
			var d2 = o2.depthOrNull;

			var compare = function(v1, v2) {
				if (v1 > v2) {
					return 1;
				} else if (v1 < v2) {
					return -1;
				} else {
					return 0;
				}
			}

			return compare(t1, t2) * 10 + compare(d1, d2);
		});
	},

	getAllResolutions : function() {
		return this.imageResolutions;
	}

// TODO add listeners for channel, resoluton, channel stack

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

	tryGetDataStoreBaseURL : function(dataSetCode, action) {
		this.openbis.tryGetDataStoreBaseURL(dataSetCode, action);
	},

	getImageInfo : function(dataSetCode, callback) {
		this.openbis.getImageInfo(dataSetCode, null, callback);
	},

	getImageResolutions : function(dataSetCode, callback) {
		this.openbis.getImageResolutions(dataSetCode, callback);
	}
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
		var widget = $("<div>").addClass("channelWidget");

		$("<div>").text("Channel:").appendTo(widget);

		var select = $("<select>").appendTo(widget);

		$("<option>").attr("value", "").text("Merged Channels").appendTo(select);

		this.controller.getAllChannels().forEach(function(channel) {
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
		var widget = $("<div>").addClass("mergedChannelsWidget");

		this.controller.getAllChannels().forEach(function(channel) {
			var mergedChannel = $("<span>").addClass("mergedChannel").appendTo(widget);
			$("<input>").attr("type", "checkbox").attr("value", channel.code).appendTo(mergedChannel);
			mergedChannel.append(channel.label);
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
		this.setAllChannels(channels);
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
		}
	},

	getAllChannels : function() {
		if (this.channels) {
			return this.channels;
		} else {
			return [];
		}
	},

	setAllChannels : function(channels) {
		if (!channels) {
			channels = [];
		}

		if (this.getAllChannels().toString() != channels.toString()) {
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
		this.panel = $("<div>");
	},

	render : function() {
		var thisView = this;

		$("<div>").text("Resolution:").appendTo(this.panel);

		var select = $("<select>").appendTo(this.panel);

		$("<option>").attr("value", "").text("Default").appendTo(select);

		this.controller.getAllResolutions().forEach(function(resolution) {
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
		this.setAllResolutions(resolutions);
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

	getAllResolutions : function() {
		if (this.resolutions) {
			return this.resolutions;
		} else {
			return [];
		}
	},

	setAllResolutions : function(resolutions) {
		if (!resolutions) {
			resolutions = [];
		}

		if (this.getAllResolutions().toString() != resolutions.toString()) {
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
			this.widget = new ChannelStackDefaultChooserWidget(channelStacks);
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
		this.panel = $("<div>");
	},

	render : function() {
		var thisView = this;

		$("<div>").text("Channel Stack:").appendTo(this.panel);
		this.panel.append(this.createTimePointWidget());
		this.panel.append(this.createDepthWidget());
		this.panel.append(this.createButtonsWidget());

		this.refresh();

		return this.panel;
	},

	refresh : function() {
		var timeSelect = this.panel.find("select.timeChooser");

		if (this.controller.getSelectedTimePoint() != null) {
			timeSelect.val(this.controller.getSelectedTimePoint());
			this.buttons.setSelectedFrame(this.controller.getTimePoints().indexOf(this.controller.getSelectedTimePoint()));
		}

		var depthSelect = this.panel.find("select.depthChooser");

		if (this.controller.getSelectedDepth() != null) {
			depthSelect.val(this.controller.getSelectedDepth());
		}
	},

	createTimePointWidget : function() {
		var thisView = this;
		var widget = $("<span>");

		$("<span>").text("T:").appendTo(widget);

		var timeSelect = $("<select>").addClass("timeChooser").appendTo(widget);

		this.controller.getTimePoints().forEach(function(timePoint) {
			$("<option>").attr("value", timePoint).text(timePoint).appendTo(timeSelect);
		});

		timeSelect.change(function() {
			thisView.controller.setSelectedTimePoint(timeSelect.val());
		});

		return widget;
	},

	createDepthWidget : function() {
		var thisView = this;
		var widget = $("<span>");

		$("<span>").text("D:").appendTo(widget);

		var depthSelect = $("<select>").addClass("depthChooser").appendTo(widget);

		this.controller.getDepths().forEach(function(depth) {
			$("<option>").attr("value", depth).text(depth).appendTo(depthSelect);
		});

		depthSelect.change(function() {
			thisView.controller.setSelectedDepth(depthSelect.val());
		});

		return widget;
	},

	createButtonsWidget : function() {
		var thisView = this;

		buttons = new MovieButtonsWidget(this.controller.getTimePoints().length);

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

		this.buttons = buttons;
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
// CHANNEL STACK DEFAULT CHOOSER VIEW
//

function ChannelStackDefaultChooserView(controller) {
	this.init(controller);
}

$.extend(ChannelStackDefaultChooserView.prototype, AbstractView.prototype, {

	init : function(controller) {
		AbstractView.prototype.init.call(this, controller);
	}

});

//
// CHANNEL STACK DEFAULT CHOOSER
//

function ChannelStackDefaultChooserWidget(channelStacks) {
	this.init(channelStacks);
}

$.extend(ChannelStackDefaultChooserWidget.prototype, AbstractWidget.prototype, {

	init : function(channelStacks) {
		AbstractWidget.prototype.init.call(this, new ChannelStackDefaultChooserView(this));
		this.channelStackManager = new ChannelStackManager(channelStacks);
	},

	getSelectedChannelStackId : function() {
		return null;
	},

	setSelectedChannelStackId : function(channelStackId) {
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
		this.panel = $("<div>");
	},

	render : function() {
		var thisView = this;

		var play = $("<button>").addClass("play").text("Play").appendTo(this.panel);

		play.click(function() {
			thisView.controller.play();
		});

		var stop = $("<button>").addClass("stop").text("Stop").appendTo(this.panel);

		stop.click(function() {
			thisView.controller.stop();
		});

		var prev = $("<button>").addClass("prev").text("<<").appendTo(this.panel);

		prev.click(function() {
			thisView.controller.prev();
		});

		var next = $("<button>").addClass("next").text(">>").appendTo(this.panel);

		next.click(function() {
			thisView.controller.next();
		});

		var delay = $("<input>").attr("type", "text").addClass("delay").appendTo(this.panel);

		delay.change(function() {
			thisView.controller.setSelectedDelay(parseInt(delay.val()));
		});

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
		this.selectedDelay = 500;
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
		this.panel = $("<div>")
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
// CHANNEL STACK MANAGER
//

function ChannelStackManager(channelStacks) {
	this.init(channelStacks);
}

$.extend(ChannelStackManager.prototype, {

	init : function(channelStacks) {
		this.channelStacks = channelStacks;
	},

	isMatrix : function() {
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
			});
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
			});
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

	getChannelStackByTimePointAndDepth : function(timePoint, depth) {
		var map = this.getChannelStackByTimePointAndDepthMap();
		var entry = map[timePoint];

		if (entry) {
			return entry[depth];
		} else {
			return null;
		}
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

	getChannelStacks : function() {
		return this.channelStacks;
	},

	getChannelStackByTimePointAndDepthMap : function() {
		if (!this.channelStackMap) {
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
			this.channelStackMap = map;
		}
		return this.channelStackMap;
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
