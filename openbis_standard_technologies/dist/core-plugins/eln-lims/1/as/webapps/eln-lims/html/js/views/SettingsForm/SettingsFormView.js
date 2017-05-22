/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function SettingsFormView(settingsFormController, settingsFormModel) {
	this._settingsFormController = settingsFormController;
	this._settingsFormModel = settingsFormModel;

	this._datasetTypesTableModel = null;

	this._mainMenuItemsTableModel = null;
	this._forcedDisableRTFTableModel = null;
	this._forcedMonospaceTableModel = null;
	this._inventorySpacesTableModel = null;
	this._sampleTypeProtocolsTableModel = null;

	this.repaint = function(views) {

		var $container = views.content;

		var $form = $("<div>");
		var $formColumn = $("<div>");
		$form.append($formColumn);

		var typeTitle = "Settings";

		var $formTitle = $("<h2>").append(typeTitle);

		//
		// Toolbar
		//
		var toolbarModel = [];		

		if(this._settingsFormModel.mode === FormMode.VIEW) {
			//Edit
			var $editButton = FormUtil.getButtonWithIcon("glyphicon-edit", function () {
				mainController.changeView("showEditSettingsPage");
			});
			toolbarModel.push({ component : $editButton, tooltip: "Edit" });
		} else { //Create and Edit
			//Save
			var $saveBtn = FormUtil.getButtonWithIcon("glyphicon-floppy-disk", (function() {
				this._settingsFormController.save(this._getSettings());
			}).bind(this), "Save");
			$saveBtn.removeClass("btn-default");
			$saveBtn.addClass("btn-primary");
			toolbarModel.push({ component : $saveBtn, tooltip: "Save" });
		}
		
		var $header = views.header;
		$header.append($formTitle);
		$header.append(FormUtil.getToolbar(toolbarModel));

		$container.append($form);

		this._paintGeneralSection($formColumn);
		this._paintStorageSection($formColumn);
		this._paintDataSetTypesForFileNamesSection($formColumn);

	}

	this._getSettings = function() {
		return {
			dataSetTypeForFileNameMap : this._datasetTypesTableModel.getValues(),
			mainMenu : this._mainMenuItemsTableModel.getValues(),
			forcedDisableRTF : this._forcedDisableRTFTableModel.getValues(),
			forceMonospaceFont : this._forcedMonospaceTableModel.getValues(),
			inventorySpaces : this._inventorySpacesTableModel.getValues(),
			sampleTypeProtocols : this._sampleTypeProtocolsTableModel.getValues(),
		};
	}

	//
	// general
	//
	this._paintGeneralSection = function($container) {
		var $fieldset = this._getFieldset($container, "General", "settings-section-general");

		this._mainMenuItemsTableModel = this._getMainMenuItemsTableModel();
		$fieldset.append(this._getTable(this._mainMenuItemsTableModel));

		this._forcedDisableRTFTableModel = this._getForcedDisableRTFTableModel();
		$fieldset.append(this._getTable(this._forcedDisableRTFTableModel));

		this._forcedMonospaceTableModel = this._getForcedMonospaceTableModel();
		$fieldset.append(this._getTable(this._forcedMonospaceTableModel));

		this._inventorySpacesTableModel = this._getInventorySpacesTableModel();
		$fieldset.append(this._getTable(this._inventorySpacesTableModel));

		this._sampleTypeProtocolsTableModel = this._getSampleTypeProtocolsTableModel();
		$fieldset.append(this._getTable(this._sampleTypeProtocolsTableModel));
	}

	this._getMainMenuItemsTableModel = function() {
		var tableModel = this._getTableModel();
		// define columns
		tableModel.columnNames = ["Main Menu Item", "enabled"];
		tableModel.rowBuilders = {
			"Main Menu Item" : function(rowData) {
				return $("<span>").text(rowData.menuItemName);
			},
			"enabled" : function(rowData) {
				var $checkbox = $("<input>", { type : "checkbox", name : "cb" });
				if (rowData.enabled) {
					$checkbox.attr("checked", true);
				}
				return $checkbox;
			}
		};
		// add data
		for (var menuItemName of Object.keys(profile.mainMenu)) {
			tableModel.addRow({
				menuItemName : menuItemName,
				enabled : profile.mainMenu[menuItemName] });
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			var mainMenu = {};
			for (var value of values) {
				mainMenu[value["Main Menu Item"]] = value["enabled"];
			}
			return mainMenu;
		};
		return tableModel;
	}

	this._getForcedDisableRTFTableModel = function() {
		return this._getSingleColumnDropdownTableModel({
			columnName : "Forced Disable RTF",
			placeholder : "select property type",
			options : this._settingsFormController.getForcedDisableRTFOptions(),
			initialValues : profile.forcedDisableRTF,
		});
	}

	this._getForcedMonospaceTableModel = function() {
		return this._getSingleColumnDropdownTableModel({
			columnName : "Forced Monospace Font",
			placeholder : "select property type",
			options : this._settingsFormController.getForcedMonospaceFontOptions(),
			initialValues : profile.forceMonospaceFont,
		});
	}

	this._getInventorySpacesTableModel = function() {
		return this._getSingleColumnDropdownTableModel({
			columnName : "Inventory Spaces",
			placeholder : "select space",
			options : this._settingsFormController.getInventorySpacesOptions(),
			initialValues : profile.inventorySpaces,
		});
	}

	this._getSampleTypeProtocolsTableModel = function() {
		return this._getSingleColumnDropdownTableModel({
			columnName : "Sample Type Protocols",
			placeholder : "select protocol",
			options : this._settingsFormController.getSampleTypeProtocolsOptions(),
			initialValues : profile.sampleTypeProtocols,
		});
	}

	//
	// Storage
	//
	this._paintStorageSection = function($container) {
		var $fieldset = this._getFieldset($container, "Storage", "settings-section-storage");

		// enabled
		var $checkbox = $("<input>", { type : "checkbox" });
		$checkbox.attr("checked", true); // TODO get value
		$fieldset.append(this._getFormGroup($checkbox, "Enabled:"));

		this._appendSlider({
			$container : $fieldset,
			value : profile.storagesConfiguration.storageSpaceLowWarning,
			labelText : "Low storage space warning:",
			id : "settings-slider-low-storage",
		});

		this._appendSlider({
			$container : $fieldset,
			value : profile.storagesConfiguration.boxSpaceLowWarning,
			labelText : "Low box space warning:",
			id : "settings-slider-low-box",
		});

		// var storageProperties = this._getStorageProperties();

		// var $storagePropertySection = $("<input>", { type : "text", class : "form-control" });
		// $storagePropertySection.val(storageProperties.STORAGE_PROPERTY_GROUP); // TODO get value
		// $fieldset.append(this._getFormGroup($storagePropertySection, "Storage property section:"));

		// var $storagePropertySection = $("<input>", { type : "text", class : "form-control" });
		// $storagePropertySection.val(storageProperties.STORAGE_GROUP_DISPLAY_NAME); // TODO get value
		// $fieldset.append(this._getFormGroup($storagePropertySection, "Storage section display name:"));
	}

	this._getStorageProperties = function() {
		var storageProperties = storagesConfiguration.STORAGE_PROPERTIES;
		if (storageProperties && storageProperties === Array && storageProperties.legendText > 0) {
			return storagesConfiguration.STORAGE_PROPERTIES[0];
		}
		return {};
	}

	this._appendSlider = function(params) {
		var $slider = $("<input>", {
			class : "span2",
			type : "text",
			"data-slider-min" :"0",
			"data-slider-max" : "100",
			"data-slider-step" : "1",
			"data-slider-value" : params.value * 100,
			"data-slider-id" : params.id,
		});
		params.$container.append(this._getFormGroup($slider, params.labelText));

		$slider.slider({
			// tooltip : "always"
		});

		// TODO get value
	}

	this._getFormGroup = function($input, labelText) {
		var $formGroup = $("<div>", { class : "form-group" });
		$formGroup.append($("<label>", { class : "control-label" }).text(labelText));
		var $controls = $("<div>", { class : "controls" });
		$formGroup.append($controls);
		$controls.append($input);
		return $formGroup;
	}

	//
	// dataset types for filenames
	//
	this._paintDataSetTypesForFileNamesSection = function($container) {
		var $fieldset = this._getFieldset($container, "Dataset types for filenames", "settings-section-dataset-types");
		this._datasetTypesTableModel = this._getDatasetTypesTableModel();
		$fieldset.append(this._getTable(this._datasetTypesTableModel));
	}

	this._getDatasetTypesTableModel = function() {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columnNames = ["Filename extension", "Dataset type"];
		tableModel.rowBuilders = {
			"Filename extension" : function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.fileNameExtension);
			},
			"Dataset type" : (function(rowData) {
				var allDatasetTypeCodes = this._settingsFormController.getAllDatasetTypeCodeOptions();
				return FormUtil.getDropdown(allDatasetTypeCodes.map(function(option) {
					return {
						label : option,
						value : option,
						selected : option === rowData.dataSetType,
					};
				}), "select Dataset type");
			}).bind(this),
		};
		// add data
		for (var dataSetTypeForFileName of profile.dataSetTypeForFileNameMap) {
			tableModel.addRow(dataSetTypeForFileName);
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			return values.map(function(value) {
				return {
					fileNameExtension : value["Filename extension"],
					dataSetType : value["Dataset type"],
				};
			});
		}
		return tableModel;
	}

	//
	// general
	//

	this._getSingleColumnDropdownTableModel = function(params) {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columnNames = [params.columnName];
		tableModel.rowBuilders[params.columnName] = function(rowData) {
			return FormUtil.getDropdown(params.options.map(function(option) {
				return {
					label : option,
					value : option,
					selected : option === rowData,
				};
			}), params.placeholder);
		}
		// add data
		for (var item of params.initialValues) {
			tableModel.addRow(item);
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			return values.map(function(value) {
				return value[params.columnName];
			});
		}
		return tableModel;
	}

	this._getTableModel = function() {
		var tableModel = {};
		tableModel.columnNames = []; // array of strings
		tableModel.rowBuilders = {}; // key (column name); value (function to build widget)
		tableModel.rows = []; // array of maps with key (column name); value (widget)
		tableModel.dynamicRows = false; // allows adding / removing rows
		tableModel.valuesTransformer = function(values) { return values }; // optional transformer
		tableModel.getValues = (function() {
			var values = [];
			for (var row of tableModel.rows) {
				var rowValues = {};
				for (var columnName of tableModel.columnNames) {
					var $widget = row[columnName];
					var value = this._getWidgetValue($widget);
					rowValues[columnName] = value;
				}
				values.push(rowValues);
			}
			return tableModel.valuesTransformer(values);
		}).bind(this);
		tableModel.addRow = function(rowData) {
			var rowWidgets = {};
			for (var columnName of tableModel.columnNames) {
				var rowBuilder = tableModel.rowBuilders[columnName];
				rowWidgets[columnName] = rowBuilder(rowData);
			}
			tableModel.rows.push(rowWidgets);
			return rowWidgets;
		};
		return tableModel;
	}

	this._getWidgetValue = function($widget) {
		if ($widget.is("span")) {
			return $widget.text();
		} else if ($widget.is("input") && $widget.attr("type") === "checkbox") {
			return $widget.is(":checked");
		} else {
			return $widget.val();
		}
	}

	this._getTable = function(tableModel) {
		var $table = $("<table>", { class : "table borderless table-compact" });
		if ( ! tableModel.dynamicRows) {
			$table.css("width", "initial");
		}
		// head		
		var $thead = $("<thead>");
        var $trHead = $("<tr>");
        for (var columnName of tableModel.columnNames) {
            $trHead.append($("<th>").css("vertical-align", "middle").text(columnName));
        }
		// add row button
		if (tableModel.dynamicRows) {
			var $addButton = $("<a>", { class : "btn btn-default" })
						.append($("<span>", { class : "glyphicon glyphicon-plus" } ));
			if (this._settingsFormModel.mode === FormMode.VIEW) {
				$addButton.addClass("disabled");
			} else {
				$addButton.on("click", (function() {
					var rowWidgets = tableModel.addRow({});
					this._addRow($tbody, tableModel, rowWidgets);				
				}).bind(this))
			}
			$trHead.append($("<th>").append($addButton));
		}
		$thead.append($trHead);
		$table.append($thead);
		// body
        var $tbody = $("<tbody>");
		for (var row of tableModel.rows) {
			this._addRow($tbody, tableModel, row);
		}
		$table.append($tbody);
		return $table
	}

	this._addRow = function($tbody, tableModel, tableModelRow) {
		var $tr = $("<tr>");
		$tbody.append($tr);
		for (var columnName of tableModel.columnNames) {
			var $td = $("<td>");
			$tr.append($td);
			var $widget = tableModelRow[columnName];
			$td.append($widget);
			// disbale widget if in view mode
			if (this._settingsFormModel.mode === FormMode.VIEW) {
				$widget.prop("disabled", true);
			}
			this._styleWidget($widget);
		}
		// remove row button if in edit mode
		if (tableModel.dynamicRows) {
			$removeButton = $("<a>", { class : "btn btn-default" })
						.append($("<span>", { class : "glyphicon glyphicon-minus" }));
			if (this._settingsFormModel.mode === FormMode.VIEW) {
				$removeButton.addClass("disabled");
			} else {
				$removeButton.on("click", function() {
					$tr.remove();
					var rowIndex = tableModel.rows.indexOf(tableModelRow);
					tableModel.rows.splice(rowIndex, 1);
				});
			}
			$tr.append($("<td>").append($removeButton));
		}
	}

	this._styleWidget = function($widget) {
		if ($widget.is("select")) {
			$widget.select2({ width: '100%', theme: "bootstrap" })
		} else if ($widget.is("input") && $widget.attr("type") === "checkbox") {
			// TODO avoid flickering
			// $widget.bootstrapSwitch();
		}
	}

	this._getFieldset = function($container, legendText, key) {
		var $fieldsetOwner = $("<div>");
		var $fieldset = $("<div>");
		var $legend = $("<legend>").text(legendText);
		$legend.prepend(FormUtil.getShowHideButton($fieldset, key));
		$fieldsetOwner.append($legend).append($fieldset);
		$container.append($fieldsetOwner);
		return $fieldset;
	}

}