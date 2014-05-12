define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ChannelStackMatrixChooserView",
		"components/imageviewer/ChannelStackSeriesChooserView", "components/imageviewer/ChannelStackManager" ], function($, AbstractWidget,
		ChannelStackMatrixChooserView, ChannelStackSeriesChooserView, ChannelStackManager) {

	//
	// CHANNEL STACK CHOOSER
	//

	function ChannelStackChooserWidget(channelStacks) {
		this.init(channelStacks);
	}

	$.extend(ChannelStackChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channelStacks) {
			this.channelStackManager = new ChannelStackManager(channelStacks);

			if (this.channelStackManager.isMatrix()) {
				AbstractWidget.prototype.init.call(this, new ChannelStackMatrixChooserView(this));
			} else {
				AbstractWidget.prototype.init.call(this, new ChannelStackSeriesChooserView(this));
			}

			if (channelStacks && channelStacks.length > 0) {
				this.setSelectedChannelStack(channelStacks[0]);
			}
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

		getChannelStackIndex : function(channelStackId) {
			return this.channelStackManager.getChannelStackIndex(channelStackId);
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
			if (timePoint != null && $.inArray(timePoint, this.getTimePoints()) == -1) {
				timePoint = this.getTimePoints().length > 0 ? this.getTimePoints()[0] : null;
			}

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
			if (depth != null && $.inArray(depth, this.getDepths()) == -1) {
				depth = this.getDepths().length > 0 ? this.getDepths()[0] : null;
			}

			if (depth != null && this.getSelectedTimePoint() != null) {
				var channelStack = this.channelStackManager.getChannelStackByTimePointAndDepth(this.getSelectedTimePoint(), depth);
				this.setSelectedChannelStack(channelStack);
			} else {
				this.setSelectedChannelStack(null);
			}
		}

	});

	return ChannelStackChooserWidget;

});
