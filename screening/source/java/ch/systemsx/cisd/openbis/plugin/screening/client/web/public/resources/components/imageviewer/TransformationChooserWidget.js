define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/TransformationChooserView" ], function($, AbstractWidget,
		TransformationChooserView) {

	//
	// TRANSFORMATION CHOOSER WIDGET
	//

	function TransformationChooserWidget(channels) {
		this.init(channels);
	}

	$.extend(TransformationChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channels) {
			AbstractWidget.prototype.init.call(this, new TransformationChooserView(this));
			this.setChannels(channels);
		},

		doGetState : function(state) {
			state.selectedChannels = this.getSelectedChannels();
			state.selectedTransformation = this.getSelectedTransformation();
		},

		doSetState : function(state) {
			this.setSelectedChannels(state.selectedChannels);
			this.setSelectedTransformation(state.selectedTransformation);
		},

		getSelectedChannels : function() {
			if (this.selectedChannels) {
				return this.selectedChannels;
			} else {
				return [];
			}
		},

		setSelectedChannels : function(channels) {
			var thisWidget = this;

			if (!channels) {
				channels = [];
			}

			channels = channels.filter(function(channel) {
				return $.inArray(channel, thisWidget.getChannelsCodes()) != -1;
			});

			if (this.getSelectedChannels().toString() != channels.toString()) {
				this.selectedChannels = channels;
				this.setSelectedTransformation(this.getSelectedTransformation());
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		getSelectedTransformation : function() {
			if (this.selectedTransformation) {
				return this.selectedTransformation;
			} else {
				return null;
			}
		},

		setSelectedTransformation : function(transformation) {
			var transformations = this.getTransformationsCodes();

			if (transformation == null || $.inArray(transformation, transformations) == -1) {
				transformation = transformations.length > 0 ? transformations[0] : null;
			}

			if (this.getSelectedTransformation() != transformation) {
				this.selectedTransformation = transformation;
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		getChannels : function() {
			if (this.channels) {
				return this.channels;
			} else {
				return [];
			}
		},

		getChannelsCodes : function() {
			return this.getChannels().map(function(channel) {
				return channel.code;
			});
		},

		setChannels : function(channels) {
			this.channels = channels;
		},

		getChannelsMap : function() {
			if (this.channelMap == null) {
				var map = {};
				this.getChannels().forEach(function(channel) {
					map[channel.code] = channel;
				});
				this.channelMap = map;
			}
			return this.channelMap;
		},

		getTransformations : function() {
			var selectedChannels = this.getSelectedChannels();

			if (selectedChannels.length == 0) {
				return [];
			} else if (selectedChannels.length == 1) {
				var list = [];

				list.push(this.getOptimalTransformation());
				list.push(this.getUserDefinedTransformation());

				var channel = this.getChannelsMap()[selectedChannels[0]];

				if (channel && channel.availableImageTransformations) {
					channel.availableImageTransformations.forEach(function(transformation) {
						list.push(transformation);
					});
				}

				return list;
			} else {
				return [ this.getOptimalTransformation(), this.getUserDefinedTransformation() ];
			}
		},

		getTransformationsCodes : function() {
			return this.getTransformations().map(function(transformation) {
				return transformation.code;
			});
		},

		getOptimalTransformation : function() {
			return {
				code : "$DEFAULT$",
				label : "Optimal (image)"
			}
		},

		getUserDefinedTransformation : function() {
			return {
				code : "$USER_DEFINED_RESCALING$",
				label : "User defined"
			}
		}

	});

	return TransformationChooserWidget;

});