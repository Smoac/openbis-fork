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
	this._sampleTypeDefinitionsTableModels = {}; // key: sample type; value: table model

	this.repaint = function(views) {

		var $container = views.content;

		Util.blockUI(null, null, true);
		// delay painting just a bit so blockUI can be shown
		window.setTimeout((function($container) {

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

			this._paintGeneralSection($formColumn);
			// this._paintStorageSection($formColumn);
			this._paintDataSetTypesForFileNamesSection($formColumn);
			this._paintSampleTypesDefinition($formColumn);

			$container.append($form);

			Util.unblockUI();

		}).bind(this, $container), 20);

	}

	this._getSettings = function() {
		return {
			dataSetTypeForFileNameMap : this._datasetTypesTableModel.getValues(),
			mainMenu : this._mainMenuItemsTableModel.getValues(),
			forcedDisableRTF : this._forcedDisableRTFTableModel.getValues(),
			forceMonospaceFont : this._forcedMonospaceTableModel.getValues(),
			inventorySpaces : this._inventorySpacesTableModel.getValues(),
			sampleTypeProtocols : this._sampleTypeProtocolsTableModel.getValues(),
			sampleTypeDefinitionsExtension : this._getSampleTypeDefinitionsSettings(),
		};
	}

	this._getSampleTypeDefinitionsSettings = function() {
		var sampleTypeDefinitionsSettings = {};
		var sampleTypes = Object.keys(this._sampleTypeDefinitionsTableModels);
		for (sampleType of sampleTypes) {
			var tableModel = this._sampleTypeDefinitionsTableModels[sampleType];
			var values = tableModel.getValues();
			if (Object.keys(values).length !== 0) {
				sampleTypeDefinitionsSettings[sampleType] = tableModel.getValues();
			}
		}
		return sampleTypeDefinitionsSettings;
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
		tableModel.columns = [{ label : "Main Menu Item"}, { label : "enabled"}];
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
	// this._paintStorageSection = function($container) {
	// 	var $fieldset = this._getFieldset($container, "Storage", "settings-section-storage");

	// 	// enabled
	// 	var $checkbox = $("<input>", { type : "checkbox" });
	// 	$checkbox.attr("checked", true); // TODO get value
	// 	$fieldset.append(this._getFormGroup($checkbox, "Enabled:"));

	// 	this._appendSlider({
	// 		$container : $fieldset,
	// 		value : profile.storagesConfiguration.storageSpaceLowWarning,
	// 		labelText : "Low storage space warning:",
	// 		id : "settings-slider-low-storage",
	// 	});

	// 	this._appendSlider({
	// 		$container : $fieldset,
	// 		value : profile.storagesConfiguration.boxSpaceLowWarning,
	// 		labelText : "Low box space warning:",
	// 		id : "settings-slider-low-box",
	// 	});

	// 	var storageProperties = this._getStorageProperties();

	// 	var $storagePropertySection = $("<input>", { type : "text", class : "form-control" });
	// 	$storagePropertySection.val(storageProperties.STORAGE_PROPERTY_GROUP); // TODO get value
	// 	$fieldset.append(this._getFormGroup($storagePropertySection, "Storage property section:"));

	// 	var $storagePropertySection = $("<input>", { type : "text", class : "form-control" });
	// 	$storagePropertySection.val(storageProperties.STORAGE_GROUP_DISPLAY_NAME); // TODO get value
	// 	$fieldset.append(this._getFormGroup($storagePropertySection, "Storage section display name:"));
	// }

	// this._getStorageProperties = function() {
	// 	var storageProperties = storagesConfiguration.STORAGE_PROPERTIES;
	// 	if (storageProperties && storageProperties === Array && storageProperties.legendText > 0) {
	// 		return storagesConfiguration.STORAGE_PROPERTIES[0];
	// 	}
	// 	return {};
	// }

	// this._appendSlider = function(params) {
	// 	var $slider = $("<input>", {
	// 		class : "span2",
	// 		type : "text",
	// 		"data-slider-min" :"0",
	// 		"data-slider-max" : "100",
	// 		"data-slider-step" : "1",
	// 		"data-slider-value" : params.value * 100,
	// 		"data-slider-id" : params.id,
	// 	});
	// 	params.$container.append(this._getFormGroup($slider, params.labelText));

	// 	$slider.slider({
	// 		// tooltip : "always"
	// 	});

	// 	// TODO get value
	// }

	// this._getFormGroup = function($input, labelText) {
	// 	var $formGroup = $("<div>", { class : "form-group" });
	// 	$formGroup.append($("<label>", { class : "control-label" }).text(labelText));
	// 	var $controls = $("<div>", { class : "controls" });
	// 	$formGroup.append($controls);
	// 	$controls.append($input);
	// 	return $formGroup;
	// }

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
		tableModel.columns = [{ label : "Filename extension" }, { label : "Dataset type" }];
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
	// sample type definitions
	//
	this._paintSampleTypesDefinition = function($container) {
		var $fieldset = this._getFieldset($container, "Sample type definitions", "settings-section-sampletype-definitions");
		for (var sampleType of profile.getAllSampleTypes()) {
			// layout
			var $div = $("<div>").css("padding-left", "15px");
			var $sampleTypeFieldset = this._getFieldset($div, sampleType.code, "settings-section-sampletype-" + sampleType.code);
			$fieldset.append($div);
			// table for sample type
			var sampleTypeSettings = profile.sampleTypeDefinitionsExtension[sampleType.code];
			var tableModel = this._getSampleTypeDefinitionTableModel(sampleTypeSettings);
			$sampleTypeFieldset.append(this._getTable(tableModel));
			this._sampleTypeDefinitionsTableModels[sampleType.code] = tableModel;
		}
	}

	this._getSampleTypeDefinitionTableModel = function(sampleTypeSettings) {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [
			{ label : "Hints for", width : "140px" },
			{ label : "Label" },
			{ label : "Type" },
			{ label : "Min", width : "80px" },
			{ label : "Max", width : "80px" }
		];
		tableModel.rowBuilders = {
			"Hints for" : (function(rowData) {
				var options = ["Children", "Parents"];
				return FormUtil.getDropdown(options.map(function(option) {
					return {
						label : option,
						value : option,
						selected : rowData.hintType && option === rowData.hintType,
					};
				}), "select children/parents");
			}).bind(this),
			"Label" :  function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.LABEL);
			},
			"Type" : (function(rowData) {
				var options = this._settingsFormController.getSampleTypeOptions();
				return FormUtil.getDropdown(options.map(function(option) {
					return {
						label : option,
						value : option,
						selected : option === rowData.TYPE,
					};
				}), "select sample type");
			}).bind(this),
			"Min" : function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.MIN_COUNT);
			},
			"Max" : function(rowData) {
				return $("<input>", { type : "text", class : "form-control" }).val(rowData.MAX_COUNT);
			},
		};
		// extra
		tableModel.rowExtraBuilder = (function(rowData) {
			var annotationPropertiesTableModel = this._getAnnotationPropertiesTableModel();
			if (rowData.ANNOTATION_PROPERTIES) {
				for (var annotationProperty of rowData.ANNOTATION_PROPERTIES) {
					annotationPropertiesTableModel.addRow(annotationProperty);
				}
			}
			tableModel.rowExtraModels.push(annotationPropertiesTableModel);
			return this._getTable(annotationPropertiesTableModel);
		}).bind(this);
		// add data
		var hintTypesMap = new Map();
		hintTypesMap.set("Children", "SAMPLE_CHILDREN_HINT");
		hintTypesMap.set("Parents", "SAMPLE_PARENTS_HINT");
		for (var [hintTypeLabel, hintTypeField] of hintTypesMap) {
			if (sampleTypeSettings && sampleTypeSettings[hintTypeField]) {
				for (var hintTypeSettings of sampleTypeSettings[hintTypeField]) {
					var hintTypeSettingsCopy = $.extend(true, {}, hintTypeSettings);
					hintTypeSettingsCopy.hintType = hintTypeLabel;
					tableModel.addRow(hintTypeSettingsCopy);
				}
			}
		}
		// transform output
		tableModel.valuesTransformer = function(values) {
			var definitionsExtension = {};
			for (var rowValues of values) {
				var hintTypeField = hintTypesMap.get(rowValues["Hints for"]);
				if ( ! definitionsExtension[hintTypeField]) {
					definitionsExtension[hintTypeField] = [];
				}
				var hints = {};
				hints.LABEL = rowValues["Label"];
				hints.TYPE = rowValues["Type"];
				if (rowValues["Min"]) { hints.MIN_COUNT = Number(rowValues["Min"]); };
				if (rowValues["Max"]) { hints.MAX_COUNT = Number(rowValues["Max"]); };
				hints.ANNOTATION_PROPERTIES = rowValues.extraValues;
				definitionsExtension[hintTypeField].push(hints);				
			}
			return definitionsExtension;
		}
		return tableModel;
	}

	this._getAnnotationPropertiesTableModel = function() {
		var tableModel = this._getTableModel();
		tableModel.dynamicRows = true;
		// define columns
		tableModel.columns = [{ label : "Annotation property type" }, { label : "Mandatory", width : "160px" }];
		tableModel.rowBuilders = {
			"Annotation property type" : (function(rowData) {
				var options = this._settingsFormController.getAnnotationPropertyTypeOptions();
				return FormUtil.getDropdown(options.map(function(option) {
					return {
						label : option,
						value : option,
						selected : option === rowData.TYPE,
					};
				}), "select property type");
			}).bind(this),
			"Mandatory" : function(rowData) {
				var $checkbox = $("<input>", { type : "checkbox" });
				if (rowData.MANDATORY) {
					$checkbox.attr("checked", true);
				}
				return $checkbox;
			},
		}
		// add data - done by caller
		// transform output
		tableModel.valuesTransformer = function(values) {
			return values.map(function(rowValues) {
				return {
					TYPE : rowValues["Annotation property type"],
					MANDATORY : rowValues["Mandatory"],
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
		tableModel.columns = [{ label : params.columnName }];
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
		tableModel.columns = []; // array of elements with label and optional width
		tableModel.rowBuilders = {}; // key (column name); value (function to build widget)
		tableModel.rows = []; // array of maps with key (column name); value (widget)
		tableModel.rowExtraBuilder = null; // optional builder for expandable component per row
		tableModel.rowExtras = []; // array of extras corresponding to the rows
		tableModel.rowExtraModels = [] // row extra models can be placed here. models need getValues() function
		tableModel.dynamicRows = false; // allows adding / removing rows
		tableModel.valuesTransformer = function(values) { return values }; // optional transformer
		tableModel.getValues = (function() {
			var values = [];
			for (var i of Object.keys(tableModel.rows)) {
				var row = tableModel.rows[i];
				var rowValues = {};
				for (var column of tableModel.columns) {
					var $widget = row[column.label];
					var value = this._getWidgetValue($widget);
					rowValues[column.label] = value;
				}
				if (tableModel.rowExtraModels.length === tableModel.rows.length) {
					rowValues.extraValues = tableModel.rowExtraModels[i].getValues();
				}
				values.push(rowValues);
			}
			return tableModel.valuesTransformer(values);
		}).bind(this);
		tableModel.addRow = function(rowData) {
			var rowWidgets = {};
			for (var column of tableModel.columns) {
				var rowBuilder = tableModel.rowBuilders[column.label];
				rowWidgets[column.label] = rowBuilder(rowData);
			}
			tableModel.rows.push(rowWidgets);
			if (tableModel.rowExtraBuilder) {
				tableModel.rowExtras.push(tableModel.rowExtraBuilder(rowData));
			}
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
		if (tableModel.dynamicRows != true) {
			$table.css("width", "initial");
		}
		// head
		var $thead = $("<thead>");
        var $trHead = $("<tr>");
		if (tableModel.rowExtraBuilder) {
			$trHead.append($("<th>").css("width", "30px"));
		}
        for (var column of tableModel.columns) {
			var $th = $("<th>").css("vertical-align", "middle").text(column.label);
			if (column.width) {
				$th.css("width", column.width);
			}
            $trHead.append($th);
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
					if (tableModel.rowExtraBuilder) {
						var $extra = tableModel.rowExtras[tableModel.rowExtras.length-1];
						this._addRow($tbody, tableModel, rowWidgets, $extra);
					} else {
						this._addRow($tbody, tableModel, rowWidgets);
					}
				}).bind(this))
			}
			$trHead.append($("<th>").css("width", "80px").append($addButton));
		}
		$thead.append($trHead);
		$table.append($thead);
		// body
        var $tbody = $("<tbody>");
		for (var i of Object.keys(tableModel.rows)) {
			var row = tableModel.rows[i];

			if (tableModel.rowExtraBuilder) {
				// add extra as row after actual row
				var $extra = tableModel.rowExtras[i];
				this._addRow($tbody, tableModel, row, $extra);
			} else {
				this._addRow($tbody, tableModel, row);				
			}
		}
		$table.append($tbody);
		return $table
	}

	this._addRow = function($tbody, tableModel, tableModelRow, $extra) {
		var $tr = $("<tr>");
		$tbody.append($tr);
		var $extraRow = null;

		// add expand / collapse for extra
		if ($extra) {
			// create extra row
			var colspan = tableModel.columns.length + 1;
			if (tableModel.dynamicRows) {
				colspan++;
			}
			$extraRow = $("<tr>")
				.append($("<td>").css({"padding-left" : "50px", "padding-right" : "50px"}).attr("colspan", colspan)
					.append($extra));
			// hiding / showing extra row
			$extraRow.hide();
			var $td = $("<td>");
			var $expandCollapse = $("<div>", { class : "glyphicon glyphicon-plus-sign" }).css("vertical-align", "middle");
			$expandCollapse.on("click", (function($extraRow, $expandCollapse) {
				$extraRow.toggle();
				if ($extraRow.is(":visible")) {
					$expandCollapse.removeClass("glyphicon-plus-sign").addClass("glyphicon-minus-sign");
				} else {
					$expandCollapse.removeClass("glyphicon-minus-sign").addClass("glyphicon-plus-sign");
				}
			}).bind(this, $extraRow, $expandCollapse));
			$tr.append($td);
			$td.append($expandCollapse);
		}

		for (var column of tableModel.columns) {
			var $td = $("<td>");
			$tr.append($td);
			var $widget = tableModelRow[column.label];
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
					if ($extraRow) {
						$extraRow.remove();
					}
					var rowIndex = tableModel.rows.indexOf(tableModelRow);
					tableModel.rows.splice(rowIndex, 1);
				});
			}
			$tr.append($("<td>").append($removeButton));
		}
		// add extra row
		if ($extraRow) {
			$tbody.append($extraRow);
		}
	}

	this._styleWidget = function($widget) {
		if ($widget.is("select")) {
			$widget.select2({ width: '100%', theme: "bootstrap" })
		}
	}

	this._getFieldset = function($container, legendText, key) {
		var $fieldsetOwner = $("<div>");
		// hide and show after setting has been loaded by getShowHideButton to avoid flickering
		$fieldsetOwner.hide();
		var $fieldset = $("<div>");
		var $legend = $("<legend>").text(legendText);
		$legend.prepend(FormUtil.getShowHideButton($fieldset, key, (function($fieldsetOwner) {
			$fieldsetOwner.show();
		}).bind(this, $fieldsetOwner)));
		$fieldsetOwner.append($legend).append($fieldset);
		$container.append($fieldsetOwner);
		return $fieldset;
	}

}