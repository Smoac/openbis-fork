define([ "jquery", "components/imageviewer/MovieButtonsView", "components/imageviewer/AbstractWidget" ],
		function($, MovieButtonsView, AbstractWidget) {

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
						this.getFrameContentLoader()(frame, callback);
						this.refresh();
						this.notifyChangeListeners();
					}
				},

				getFrameCount : function() {
					return this.frameCount;
				},

				setFrameContentLoader : function(frameContentLoader) {
					this.frameContentLoader = frameContentLoader;
				},

				getFrameContentLoader : function() {
					return this.frameContentLoader;
				}

			});

			return MovieButtonsWidget;

		});