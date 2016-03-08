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
		$menuPanelContainer.append(FormUtil.getFieldForComponentWithLabel($andOrDropdownComponent, "Using", null, true));
		
		var $submitButton = FormUtil.getButtonWithIcon('glyphicon-search').append(" Search");
		
		$submitButton.click(function() {
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
		
		//initialize dropdowns & buttons
		var fieldTypeOptions = [{value : "Property", label : "Property"}, {value : "Attribute", label : "Attribute"}, {value : "Parent", label : "Parent"}, {value : "Children", label : "Children"}, {value : "Space", label : "Space"}];
		$fieldTypeDropdownComponent = FormUtil.getDropdown(fieldTypeOptions, "All");
		
		//todo there should be ONE add button at the top! (?)
		this._$addButton = $("<button>", { class : "btn btn-default", text: "+"});
		this._$addButton.click(function() {
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
		var $newRow = $("<tr>");
		
		var $newFieldNameContainer = $("<td>");
		
			$newRow.append($("<td>").append(this._getNewFieldTypeDropdownComponent($newFieldNameContainer)))
					.append($newFieldNameContainer)
					.append($("<td>").append($("<input>", { class : "form-control", type: "text"})))
					.append($("<td>").append(this._getMinusButtonComponentForRow(this._$tbody, $newRow)));
					
		this._$tbody.append($newRow);
	}
	
	//should make new objects every time. otherwise, using the same object will produce odd results!
	//how to make an on-select event??
	this._getNewFieldTypeDropdownComponent = function($newFieldNameContainer) {
		var _this = this;
		var fieldTypeOptions = [{value : "Property", label : "Property"}, {value : "Attribute", label : "Attribute"}, {value : "Parent", label : "Parent"}, {value : "Children", label : "Children"}, {value : "Space", label : "Space"}];
		var $fieldTypeComponent = FormUtil.getDropdown(fieldTypeOptions, "All");
		$fieldTypeComponent.change(function() {
			var selectedValue = $(this).val();
			$newFieldNameContainer.empty();
			switch(selectedValue) {
				case "Property":
					$newFieldNameContainer.append(_this._getNewPropertyDropdown());
					break;
				case "Attribute":
					$newFieldNameContainer.append(null);
					break;	
				case "Parent":
					$newFieldNameContainer.append(null);
					break;
				case "Children":
					$newFieldNameContainer.append(null);
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
	
	
	this._getNewPropertyDropdown = function() {
		var model = [];
		var allProp = profile.getPropertyTypes();
		for(var pIdx = 0; pIdx < allProp.length; pIdx++) {
			var prop = allProp[pIdx];
			model.push({ value : prop.code, label : prop.label });
		}
		var $dropdown = FormUtil.getDropdown(model, "Select a property");
		return $dropdown;
	}
	
	this._getNewAttributeDropdown = function(entityKind) {
		var model = null;
		switch(entityKind) {
			case "EXPERIMENT":
				model = [{ value : "CODE", label : "Code" }, 
				         { value : "EXPERIMENT_TYPE", label : "Experiment Type" }, 
				         { value : "PERM_ID", label : "Perm Id" }, 
				         { value : "PROJECT", label : "Project" }, 
				         { value : "PROJECT_PERM_ID", label : "Project Perm Id" }, 
				         { value : "PROJECT_SPACE", label : "Space" }, 
				         { value : "METAPROJECT", label : "Metaproject" }, 
				         { value : "REGISTRATION_DATE", label : "Registration Date" }, 
				         { value : "MODIFICATION_DATE", label : "Modification Date" }];
				break;
			case "SAMPLE":
				model = [{ value : "CODE", label: "Code" },
				         { value : "SAMPLE_TYPE", label: "Sample Type" },
				         { value : "PERM_ID", label: "Perm Id" },
				         { value : "SPACE", label: "Space" },
						 { value : "PROJECT", label: "Project" },
				         { value : "PROJECT_PERM_ID", label: "Project Perm Id" },
//						 { value : "PROJECT_SPACE", label: "Project Space" },
				         { value : "METAPROJECT", label: "Metaproject" },
						 { value : "REGISTRATION_DATE", label: "Registration Date" }, 
						 { value : "MODIFICATION_DATE", label: "Modification Date" }];
				break;
			case "DATASET":
				model = [{ value : "CODE", label : "Code" }, 
				         { value : "DATA_SET_TYPE", label : "Data Set Type" }, 
//				         { value : "FILE_TYPE", label : "File Type" },
//				         { value : "STORAGE_CONFIRMATION", label : "Storage Confirmation" },
				         { value : "METAPROJECT", label : "Metaproject" }, 
				         { value : "REGISTRATION_DATE", label : "Registration Date" },
				         { value : "MODIFICATION_DATE", label : "Modification Date" }];
				break;
			default:
				//Do Nothing
		}
		var $dropdown = FormUtil.getDropdown(model, "Select an attribute");
		return $dropdown;
	}
	
	this._getEntityTypeDropdown = function() {
		var $component = $("<select>", { class : 'form-control' } );
			$component.append($("<option>").attr('value', '').attr('disabled', '').attr('selected', '').text('Select Entity Type to search for'));
			$component.append($("<option>").attr('value', 'EXPERIMENT').text('Experiment'));
			$component.append($("<option>").attr('value', 'SAMPLE').text('Sample'));
			$component.append($("<option>").attr('value', 'DATASET').text('Dataset'));
			var _this = this;
			$component.change(function() {
				_this._advancedSearchModel.resetModel($(this).val());
				_this._paintCriteriaPanel(_this._$searchCriteriaPanelContainer);
			});
			
		return $component;
	}
	
	this._getMinusButtonComponentForRow = function($tbody, $row) {
		var $minusButton = $("<button>", { class : "btn btn-default", text: "-"});
		$minusButton.click(function() {
			if($tbody.children().length > 1) {
				$row.remove();
			} else {
				Util.showError("There must be at least one row of search criteria present.");
			}
		});
		return $minusButton;
	}
	
}