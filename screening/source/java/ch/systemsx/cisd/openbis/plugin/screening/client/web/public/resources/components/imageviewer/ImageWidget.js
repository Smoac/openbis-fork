define([ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget" ], function($, AbstractView, AbstractWidget) {

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
			$("<div>").addClass("imageContainer").appendTo(this.panel);
			$("<div>").addClass("loadingContainer").appendTo(this.panel);
			this.refresh();
			return this.panel;
		},

		refresh : function() {
			var thisView = this;

			if (this.controller.getImageData()) {
				var loadingContainer = this.panel.find(".loadingContainer");
				var imageContainer = this.panel.find(".imageContainer");

				loadingContainer.text("loading...");

				this.controller.loadImage(this.controller.getImageData(), function(image) {
					loadingContainer.empty();
					imageContainer.empty().append(image);
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

	return ImageWidget;

});