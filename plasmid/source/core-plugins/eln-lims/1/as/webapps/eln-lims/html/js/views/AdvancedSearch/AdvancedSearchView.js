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
		
		//Search Menu Panel
		this._$menuPanelContainer = $("<div>");
		this._paintMenuPanel(this._$menuPanelContainer);
		$mainPanel.append(this._$menuPanelContainer);
		
		//Search Criteria Panel
		//table to select field type, name, and value
		this._$searchCriteriaPanelContainer = $("<div>");
		this._paintCriteriaPanel(this._$searchCriteriaPanelContainer);
		$mainPanel.append(this._$searchCriteriaPanelContainer);
		
		//Search Results Panel
		//TODO
		
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

		var andOrOptions = [{value : "AND", label : "AND"}, {value : "OR", label : "OR"}];
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
		var $table = $("<table>", { class : "table"});
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
		$fieldValue.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].value = selectedValue; //Update model
		});
		var $newFieldNameContainer = $("<td>");
		
			$newRow.append($("<td>").append(this._getNewFieldTypeDropdownComponent($newFieldNameContainer)))
					.append($newFieldNameContainer)
					.append($("<td>").append($fieldValue))
					.append($("<td>").append(this._getMinusButtonComponentForRow(this._$tbody, $newRow)));
					
		this._$tbody.append($newRow);
	}
	
	//should make new objects every time. otherwise, using the same object will produce odd results!
	//how to make an on-select event??
	this._getNewFieldTypeDropdownComponent = function($newFieldNameContainer) {
		var _this = this;
		var fieldTypeOptions = [{value : "All", label : "All"}, {value : "Property", label : "Property"}, {value : "Attribute", label : "Attribute"}, {value : "Parent", label : "Parent"}, {value : "Children", label : "Children"}, {value : "Space", label : "Space"}];
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
				case "Space":
					//Do Nothing
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
				         { value : "ATTR.PROJECT_SPACE", label : "Space" }, 
				         { value : "ATTR.METAPROJECT", label : "Metaproject" }, 
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date" }, 
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date" }];
				break;
			case "SAMPLE":
				model = [{ value : "ATTR.CODE", label: "Code" },
				         { value : "ATTR.SAMPLE_TYPE", label: "Sample Type" },
				         { value : "ATTR.PERM_ID", label: "Perm Id" },
				         { value : "ATTR.SPACE", label: "Space" },
						 { value : "ATTR.PROJECT", label: "Project" },
				         { value : "ATTR.PROJECT_PERM_ID", label: "Project Perm Id" },
				         { value : "ATTR.METAPROJECT", label: "Metaproject" },
						 { value : "ATTR.REGISTRATION_DATE", label: "Registration Date" }, 
						 { value : "ATTR.MODIFICATION_DATE", label: "Modification Date" }];
				break;
			case "DATASET":
				model = [{ value : "ATTR.CODE", label : "Code" }, 
				         { value : "ATTR.DATA_SET_TYPE", label : "Data Set Type" }, 
				         { value : "ATTR.METAPROJECT", label : "Metaproject" }, 
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date" },
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date" }];
				break;
			default:
				//Do Nothing
		}
		return model;
	}
	
	this._getEntityTypeDropdown = function() {
		var $component = $("<select>", { class : 'form-control' } );
			$component.append($("<option>").attr('value', '').attr('disabled', '').attr('selected', '').text('Select Entity Type to search for'));
			$component.append($("<option>").attr('value', 'EXPERIMENT').text('Experiment'));
			$component.append($("<option>").attr('value', 'SAMPLE').text('Sample'));
			$component.append($("<option>").attr('value', 'DATASET').text('Dataset'));
			var _this = this;
			$component.change(function() {
				_this._advancedSearchModel.resetModel($(this).val()); //Restart model
				_this._paintCriteriaPanel(_this._$searchCriteriaPanelContainer); //Restart view
			});
			
		return $component;
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
	
}