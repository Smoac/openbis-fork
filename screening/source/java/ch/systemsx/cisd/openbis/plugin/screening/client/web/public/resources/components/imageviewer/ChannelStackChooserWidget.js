define([ "jquery", "components/imageviewer/ChannelStackManager", "components/imageviewer/ChannelStackMatrixChooserWidget",
		"components/imageviewer/ChannelStackSeriesChooserWidget" ], function($, ChannelStackManager, ChannelStackMatrixChooserWidget,
		ChannelStackSeriesChooserWidget) {

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

	return ChannelStackChooserWidget;

});