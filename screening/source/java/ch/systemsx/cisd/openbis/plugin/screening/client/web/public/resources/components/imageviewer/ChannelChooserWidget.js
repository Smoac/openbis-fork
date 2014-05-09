define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget" ], function($, AbstractView, AbstractWidget) {

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

	return ChannelChooserWidget;

});