define([ "jquery", "components/common/ChangeEvent", "components/imageviewer/AbstractView", "components/imageviewer/AbstractWidget" ], function($,
		ChangeEvent, AbstractView, AbstractWidget) {

	//
	// DATA SET CHOOSER VIEW
	//

	function DataSetChooserView(controller) {
		this.init(controller);
	}

	$.extend(DataSetChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("dataSetChooser");
		},

		render : function() {
			var thisView = this;
			var widget = $("<div>").addClass("form-group");

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

		refresh : function() {
			var select = this.panel.find("select");

			if (this.controller.getSelectedDataSetCode() != null) {
				select.val(this.controller.getSelectedDataSetCode());
			}
		}

	});

	//
	// DATA SET CHOOSER
	//

	function DataSetChooserWidget(dataSetCodes) {
		this.init(dataSetCodes);
	}

	$.extend(DataSetChooserWidget.prototype, AbstractWidget.prototype, {
		init : function(dataSetCodes) {
			AbstractWidget.prototype.init.call(this, new DataSetChooserView(this));
			this.setDataSetCodes(dataSetCodes)
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
				var event = new ChangeEvent(this.getSelectedDataSetCode(), dataSetCode);
				this.selectedDataSetCode = dataSetCode;
				this.refresh();
				this.notifyChangeListeners(event);
			}
		}

	});

	return DataSetChooserWidget;

});