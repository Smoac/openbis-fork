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
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		//Layout
		var $formColumn = $("<form>", { 
			"class" : "form-inline", 
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		
		//Form Layout
		var $entityTypeDropdown = FormUtil.getEntityTypeDropdown();
		$formColumn.append(FormUtil.getFieldForComponentWithLabel($entityTypeDropdown, "Search For", null, true));

		var andOrOptions = [{value : "AND", label : "AND"}, {value : "OR", label : "OR"}];
		var $andOrDropdownComponent = FormUtil.getDropdown(andOrOptions, "Select logical operator");
		$formColumn.append(FormUtil.getFieldForComponentWithLabel($andOrDropdownComponent, "Using", null, true));
		
		var $submitButton = FormUtil.getButtonWithIcon('glyphicon-search').append(" Search");
		
		$submitButton.click(function() {
			_this._advancedSearchController.search();
		});
		$formColumn.append($submitButton);
		
		//table to select field type, name, and value
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
		
		//Triggers Layout refresh
		$container.append($formColumn).append($table);
		
	}
	
	this._paintInputRow = function() {
		var $newRow = $("<tr>");
			$newRow.append($("<td>").append(this._getNewFieldTypeDropdownComponent()))
					.append($("<td>").append(this._getNewFieldNameDropdownComponent()))
					.append($("<td>").append($("<input>", { class : "form-control", type: "text"})))
					.append($("<td>").append(this._getMinusButtonComponentForRow(this._$tbody, $newRow)));
					
		this._$tbody.append($newRow);
	}
	
	//should make new objects every time. otherwise, using the same object will produce odd results!
	//how to make an on-select event??
	this._getNewFieldTypeDropdownComponent = function() {
		var fieldTypeOptions = [{value : "Property", label : "Property"}, {value : "Attribute", label : "Attribute"}, {value : "Parent", label : "Parent"}, {value : "Children", label : "Children"}, {value : "Space", label : "Space"}];
		var $fieldTypeComponent = FormUtil.getDropdown(fieldTypeOptions, "All");
		$fieldTypeComponent.click(function() {
			alert("selecting something! now the field name box changes...");
		});
		return $fieldTypeComponent;
	}
	
	//todo generate the names when there is something selected in the method above
	this._getNewFieldNameDropdownComponent = function() {
		var fieldNameOptions = [{value : "name1", label : "name1"}, {value : "name2", label : "name2"}];
		var $fieldNameComponent = FormUtil.getDropdown(fieldNameOptions, "All");
		return $fieldNameComponent;
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