define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget" ], function($, AbstractView, AbstractWidget) {

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

		doGetState : function(state) {
			state.selectedFrame = this.getSelectedFrame();
			state.selectedDelay = this.getSelectedDelay();
		},

		doSetState : function(state) {
			this.setSelectedFrame(state.selectedFrame);
			this.setSelectedDelay(state.selectedDelay);
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

	return MovieButtonsWidget;

});