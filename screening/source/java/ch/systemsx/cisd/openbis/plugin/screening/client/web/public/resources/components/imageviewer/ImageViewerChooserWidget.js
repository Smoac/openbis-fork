define([ "jquery", "components/common/CallbackManager", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget",
		"components/imageviewer/ImageViewerWidget", "components/imageviewer/OpenbisFacade" ], function($, CallbackManager, AbstractView,
		AbstractWidget, ImageViewerWidget, OpenbisFacade) {

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

	return ImageViewerChooserWidget;

});