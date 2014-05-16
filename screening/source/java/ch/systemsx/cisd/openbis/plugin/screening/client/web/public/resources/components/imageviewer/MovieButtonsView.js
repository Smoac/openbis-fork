define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

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
			var disabled = this.controller.getFrameCount() <= 1;

			var play = this.panel.find("button.play");
			play.prop("disabled", this.controller.isPlaying() || disabled);

			var stop = this.panel.find("button.stop");
			stop.prop("disabled", this.controller.isStopped() || disabled);

			var prev = this.panel.find("button.prev");
			prev.prop("disabled", this.controller.isFirstFrameSelected() || disabled);

			var next = this.panel.find("button.next");
			next.prop("disabled", this.controller.isLastFrameSelected() || disabled);

			var delay = this.panel.find("input.delay");
			delay.prop("disabled", disabled)
			delay.val(this.controller.getSelectedDelay());
		}

	});

	return MovieButtonsView;

});