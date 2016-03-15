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

function AdvancedSearchView(advancedSearchController, advancedSearchModel) {
	this._advancedSearchController = advancedSearchController;
	this._advancedSearchModel = advancedSearchModel;
	this._$menuPanelContainer = null;
	this._$searchCriteriaPanelContainer = null;
	this._$tbody = null;
	this._$dataGridContainer = null;
	
	//
	// Main Repaint Method
	//
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		//Layout
		var $mainPanel = $("<form>", { 
			"class" : "form-inline", 
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		$mainPanel.append($("<h2>").append("Advanced Search"));
		
		
		//Search Menu Panel
		this._$menuPanelContainer = $("<div>");
		$mainPanel.append(this._$menuPanelContainer);
		this._paintMenuPanel(this._$menuPanelContainer);
		$mainPanel.append($("<br>"));
		
		//Search Criteria Panel
		//table to select field type, name, and value
		this._$searchCriteriaPanelContainer = $("<div>");
		this._paintCriteriaPanel(this._$searchCriteriaPanelContainer);
		$mainPanel.append(this._$searchCriteriaPanelContainer);
		
		//
		
		//Search Results Panel
		this._$dataGridContainer = $("<div>");
		$mainPanel.append(this._$dataGridContainer);
		//
		
		//Triggers Layout refresh
		$container.append($mainPanel);
		
	}
	
	//
	// Repaint Panels Methods
	//
	
	this._paintMenuPanel = function($menuPanelContainer) {
		$menuPanelContainer.empty();
		var $entityTypeDropdown = this._getEntityTypeDropdown();
		$menuPanelContainer.append(FormUtil.getFieldForComponentWithLabel($entityTypeDropdown, "Search For", null, true));

		var andOrOptions = [{value : "AND", label : "AND", selected : true}, {value : "OR", label : "OR"}];
		var $andOrDropdownComponent = FormUtil.getDropdown(andOrOptions, "Select logical operator");
		var _this = this;
		$andOrDropdownComponent.change(function() {
			_this._advancedSearchModel.criteria.logicalOperator = $(this).val();
		});
		$menuPanelContainer.append(FormUtil.getFieldForComponentWithLabel($andOrDropdownComponent, "Using", null, true));
		
		var $submitButton = FormUtil.getButtonWithIcon('glyphicon-search', function() {
			_this._advancedSearchController.search();
		});
		
		$menuPanelContainer.append($submitButton);
	}
	
	this._paintCriteriaPanel = function($searchCriteriaPanelContainer) {
		$searchCriteriaPanelContainer.empty();
		var _this = this;
		var $table = $("<table>", { class : "table table-bordered"});
		$thead = $("<thead>");
		this._$tbody = $("<tbody>");

		//todo there should be ONE add button at the top! (?)
		this._$addButton = FormUtil.getButtonWithIcon('glyphicon-plus', function() {
			_this._paintInputRow();
		});
		
		$table
			.append($thead)
			.append(this._$tbody);
			
		$thead
			.append($("<tr>")
						.append($("<th>").text("Field Type"))
						.append($("<th>").text("Field Name"))
						.append($("<th>").text("Field Value"))
						.append($("<th>").append(this._$addButton))
					);
		
		this._paintInputRow();
		
		$searchCriteriaPanelContainer.append($table);
	}
	
	//
	// Auxiliar Components Methods
	//
	
	this._paintInputRow = function() {
		var _this = this;
		var uuidValue = Util.guid();
		this._advancedSearchModel.criteria.rules[uuidValue] = { };
		
		var $newRow = $("<tr>", { id : uuidValue });
		var $fieldValue = $("<input>", { class : "form-control", type: "text"});
		
		if(this._advancedSearchModel.forceFreeTextSearch) {
			$fieldValue.val(this._advancedSearchModel.forceFreeTextSearch);
			this._advancedSearchModel.forceFreeTextSearch = undefined;
		}
		
		$fieldValue.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].value = selectedValue; //Update model
		});
		var $newFieldNameContainer = $("<td>");
		
			$newRow.append($("<td>").append(this._getNewFieldTypeDropdownComponent($newFieldNameContainer, this._advancedSearchModel.criteria.entityKind)))
					.append($newFieldNameContainer)
					.append($("<td>").append($fieldValue))
					.append($("<td>").append(this._getMinusButtonComponentForRow(this._$tbody, $newRow)));
					
		this._$tbody.append($newRow);
	}
	
	//should make new objects every time. otherwise, using the same object will produce odd results!
	//how to make an on-select event??
	this._getNewFieldTypeDropdownComponent = function($newFieldNameContainer, entityKind) {
		var _this = this;
		var fieldTypeOptions = null;
		switch(entityKind) {
			case "ALL":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }];
				break;
			case "SAMPLE":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }, 
				                    {value : "Property", label : "Property"}, 
				                    {value : "Attribute", label : "Attribute"}, 
				                    {value : "Parent", label : "Parent"}, 
				                    {value : "Children", label : "Children"}];
				break;
			case "EXPERIMENT":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }, 
				                    {value : "Property", label : "Property"}, 
				                    {value : "Attribute", label : "Attribute"}];
				break;
			case "DATASET":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }, 
				                    {value : "Property", label : "Property"}, 
				                    {value : "Attribute", label : "Attribute"},
// ELN-UI don't support this yet
//				                    {value : "Parent", label : "Parent"}, 
//				                    {value : "Children", label : "Children"}
				                    ];
				break;
		}
		
		var $fieldTypeComponent = FormUtil.getDropdown(fieldTypeOptions, "Select Field Type");
		$fieldTypeComponent.change(function() {
			var $thisComponent = $(this);
			
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].type = selectedValue; //Update model
			
			$newFieldNameContainer.empty();
			switch(selectedValue) {
				case "All":
					//Do Nothing
				break;
				case "Property":
					$newFieldNameContainer.append(_this._getNewPropertyDropdown());
					break;
				case "Attribute":
					$newFieldNameContainer.append(_this._getNewAttributeDropdown(_this._advancedSearchModel.criteria.entityKind));
					break;	
				case "Parent":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "Parent"));
					break;
				case "Children":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "Children"));
					break;
				default:
					//Do Nothing
			}
		});
		return $fieldTypeComponent;
	}
	
	this._getNewMergedDropdown = function(entityKind, parentOrChildren) {
		var _this = this;
		var model = null;
		var attributesModel = this._getFieldNameAttributesByEntityKind(entityKind);
		attributesModel.push({ value : "", label : "-------------------------", disabled : true });
		var propertiesModel = this._getFieldNameProperties();
		model = attributesModel.concat(propertiesModel);
		var $dropdown = FormUtil.getDropdown(model, "Select a property or attribute");
		$dropdown.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].name = selectedValue; //Update model
			//alert("updated model! type is now " + _this._advancedSearchModel.criteria.rules[uuid].type + " and name is " + _this._advancedSearchModel.criteria.rules[uuid].name);
		});
		
		return $dropdown;
	}
	
	this._getNewPropertyDropdown = function() {
		var _this = this;
		var model = this._getFieldNameProperties();
		var $dropdown = FormUtil.getDropdown(model, "Select a property");
		$dropdown.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].name = selectedValue; //Update model
			//alert("updated model! type is now " + _this._advancedSearchModel.criteria.rules[uuid].type + " and name is " + _this._advancedSearchModel.criteria.rules[uuid].name);
		});
		return $dropdown;
	}
	
	this._getFieldNameProperties = function() {
		var model = [];
		var allProp = profile.getPropertyTypes();
		for(var pIdx = 0; pIdx < allProp.length; pIdx++) {
			var prop = allProp[pIdx];
			model.push({ value : "PROP." + prop.code, label : prop.label });
		}
		
		model.sort(function(propertyA, propertyB) {
			return propertyA.label.localeCompare(propertyB.label);
		});
		
		return model;
	}
	
	this._getNewAttributeDropdown = function(entityKind) {
		var _this = this;
		var model = this._getFieldNameAttributesByEntityKind(entityKind);
		var $dropdown = FormUtil.getDropdown(model, "Select an attribute");
		$dropdown.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].name = selectedValue; //Update model
			//alert("updated model! type is now " + _this._advancedSearchModel.criteria.rules[uuid].type + " and name is " + _this._advancedSearchModel.criteria.rules[uuid].name);

		});
		return $dropdown;
	}
	
	this._getFieldNameAttributesByEntityKind = function(entityKind) {
		var model = null;
		switch(entityKind) {
			case "EXPERIMENT":
				model = [{ value : "ATTR.CODE", label : "Code" }, 
				         { value : "ATTR.EXPERIMENT_TYPE", label : "Experiment Type" }, 
				         { value : "ATTR.PERM_ID", label : "Perm Id" }, 
				         { value : "ATTR.PROJECT", label : "Project" }, 
				         { value : "ATTR.PROJECT_PERM_ID", label : "Project Perm Id" }, 
				         { value : "ATTR.PROJECT_SPACE", label : "Project Space" }, 
//				         { value : "ATTR.METAPROJECT", label : "Tag" }, TO-DO Not supported by ELN yet
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date" }, 
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date" }];
				break;
			case "SAMPLE":
				model = [{ value : "ATTR.CODE", label: "Code" },
				         { value : "ATTR.SAMPLE_TYPE", label: "Sample Type" },
				         { value : "ATTR.PERM_ID", label: "Perm Id" },
				         { value : "ATTR.SPACE", label: "Space" },
//				         { value : "ATTR.METAPROJECT", label: "Tag" }, TO-DO Not supported by ELN yet
						 { value : "ATTR.REGISTRATION_DATE", label: "Registration Date" }, 
						 { value : "ATTR.MODIFICATION_DATE", label: "Modification Date" }];
				break;
			case "DATASET":
				model = [{ value : "ATTR.CODE", label : "Code" }, 
				         { value : "ATTR.DATA_SET_TYPE", label : "Data Set Type" }, 
//				         { value : "ATTR.METAPROJECT", label : "Tag" }, TO-DO Not supported by ELN yet
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date" },
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date" }];
				break;
		}
		return model;
	}
	
	this._getEntityTypeDropdown = function() {
		var _this = this;
		var model = [{ value : 'ALL', label : "All", selected : true },
		             { value : 'EXPERIMENT', label : "Experiment" },
		             { value : 'SAMPLE', label : "Sample" },
		             { value : 'DATASET', label : "Dataset" }];
		this._advancedSearchModel.resetModel('ALL');
		var $dropdown = FormUtil.getDropdown(model, 'Select Entity Type to search for');
		
		$dropdown.change(function() {
			_this._advancedSearchModel.resetModel($(this).val()); //Restart model
			_this._paintCriteriaPanel(_this._$searchCriteriaPanelContainer); //Restart view
		});
		
		return $dropdown;
	}
	
	this._getMinusButtonComponentForRow = function($tbody, $row) {
		var _this = this;
		var $minusButton = FormUtil.getButtonWithIcon('glyphicon-minus', function() {
			if($tbody.children().length > 1) {
				var uuid = $row.attr("id");
				delete _this._advancedSearchModel.criteria.rules[uuid];
				$row.remove();
			} else {
				Util.showError("There must be at least one row of search criteria present.");
			}
		});
		return $minusButton;
	}
	
	this.renderResults = function(results) {
		var dataGridController = this._getGridForResults(results);
		dataGridController.init(this._$dataGridContainer);
	}
	
	this._getGridForResults = function(results) {
			var columns = [ {
				label : 'Entity Kind',
				property : 'entityKind',
				sortable : true
			}, {
				label : 'Entity Type',
				property : 'entityType',
				sortable : true
			}, {
				label : 'Code',
				property : 'code',
				sortable : true
			}, {
				label : 'Identifier',
				property : 'identifier',
				sortable : true
			}];
			
			columns.push({
				label : 'Matched',
				property : 'matched',
				isExportable: false,
				sortable : true
			});
			
			columns.push({
				label : 'Score',
				property : 'score',
				isExportable: false,
				sortable : true
			});
			
			columns.push({
				label : '---------------',
				property : null,
				isExportable: false,
				sortable : false
			});
			
			//Add properties as columns dynamically depending on the results
			
			//1. Get properties with actual data
			var foundPropertyCodes = {};
			for(var rIdx = 0; rIdx < results.objects.length; rIdx++) {
				var entity = results.objects[rIdx];
				for(var propertyCode in entity.properties) {
					if(entity.properties[propertyCode]) {
						foundPropertyCodes[propertyCode] = true;
					}
				}
			}
			
			//2. Get columns
			var propertyColumnsToSort = [];
			for(var propertyCode in foundPropertyCodes) {
				propertyColumnsToSort.push({
					label : profile.getPropertyType(propertyCode).label,
					property : propertyCode,
					sortable : true
				});
			}
			
			//3. Sort column properties by label
			propertyColumnsToSort.sort(function(propertyA, propertyB) {
				return propertyA.label.localeCompare(propertyB.label);
			});
			columns = columns.concat(propertyColumnsToSort);
			
			//4. Add registration/modification date columns
			columns.push({
				label : '---------------',
				property : null,
				isExportable: false,
				sortable : false
			});
			
			columns.push({
				label : 'Registration Date',
				property : 'registrationDate',
				isExportable: false,
				sortable : true
			});
			
			columns.push({
				label : 'Modification Date',
				property : 'modificationDate',
				isExportable: false,
				sortable : true
			});
			
			var getDataRows = function(callback) {
				var rows = [];
				for(var rIdx = 0; rIdx < results.objects.length; rIdx++) {
					var entity = results.objects[rIdx];
					
					var rowData = {};
					
					if(entity["@type"] === "as.dto.global.GlobalSearchObject") {
						rowData.matched = entity.match;
						rowData.score = entity.score;
						
						switch(entity.objectKind) {
							case "SAMPLE":
								entity = entity.sample;
							break;
							case "EXPERIMENT":
								entity = entity.experiment;
							break;
							case "DATA_SET":
								entity = entity.dataSet;
							break;
						}
					}
					
					//properties
					rowData.entityKind = entity["@type"].substring(entity["@type"].lastIndexOf(".") + 1, entity["@type"].length);
					rowData.entityType = entity.type.code;
					rowData.code =  entity.code;
					rowData.permId = entity.permId.permId;
					rowData.registrationDate = (entity.registrator && entity.registrator.registrationDate)?Util.getFormatedDate(new Date(entity.registrator.registrationDate)):null;
					rowData.modificationDate = (entity.modifier && entity.modifier.registrationDate)?Util.getFormatedDate(new Date(entity.modifier.registrationDate)):null;
					rowData.entityObject = entity;
					
					if(entity.identifier) {
						rowData.identifier = entity.identifier.identifier;
					}
					
					for(var propertyCode in entity.properties) {
						rowData[propertyCode] = entity.properties[propertyCode];
					}
					
					//Add the row data
					rows.push(rowData);
				}
				callback(rows);
			};
			
			var rowClick = function(e) {
				switch(e.data.entityKind) {
					case "Sample":
						mainController.changeView('showViewSamplePageFromPermId', e.data.permId);
						break;
					case "Experiment":
						mainController.changeView('showExperimentPageFromIdentifier', e.data.identifier);
						break;
					case "DataSet":
						mainController.changeView('showViewDataSetPageFromPermId', e.data.permId);
						break;
				}
			}
			
			var dataGrid = new DataGridController("Search Results", columns, getDataRows, rowClick, false, "ADVANCED_SEARCH_OPENBIS");
			return dataGrid;
	}
	
}