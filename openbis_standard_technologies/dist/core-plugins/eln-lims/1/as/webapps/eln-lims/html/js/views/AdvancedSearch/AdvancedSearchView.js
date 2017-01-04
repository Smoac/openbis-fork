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
	this._$andOrDropdownComponent = null;
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
		this._$andOrDropdownComponent = FormUtil.getDropdown(andOrOptions, "Select logical operator");
		var _this = this;
		this._$andOrDropdownComponent.change(function() {
			_this._advancedSearchModel.criteria.logicalOperator = $(this).val();
		});
		$menuPanelContainer.append(FormUtil.getFieldForComponentWithLabel(this._$andOrDropdownComponent, "Using", null, true));
		
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
			this._advancedSearchModel.criteria.rules[uuidValue].value = this._advancedSearchModel.forceFreeTextSearch; //Update model
			this._advancedSearchModel.forceFreeTextSearch = undefined;
		}
		
		$fieldValue.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].value = selectedValue; //Update model
		});
		
		$fieldValue.keypress(function (e) {
       	 var key = e.which;
       	 if(key == 13)  // the enter key code
       	  {
       		 _this._advancedSearchController.search();
       	    return false;  
       	  }
       });
        
		var $newFieldNameContainer = $("<td>");
		
			$newRow.append($("<td>").append(this._getNewFieldTypeDropdownComponent($newFieldNameContainer, this._advancedSearchModel.criteria.entityKind, uuidValue)))
					.append($newFieldNameContainer)
					.append($("<td>").append($fieldValue))
					.append($("<td>").append(this._getMinusButtonComponentForRow(this._$tbody, $newRow)));
					
		this._$tbody.append($newRow);
	}
	
	//should make new objects every time. otherwise, using the same object will produce odd results!
	//how to make an on-select event??
	this._getNewFieldTypeDropdownComponent = function($newFieldNameContainer, entityKind, uuid) {
		//Update dropdown component
		this._$andOrDropdownComponent.val("AND");
		this._advancedSearchModel.criteria.logicalOperator = "AND";
		this._$andOrDropdownComponent.removeAttr("disabled");
		//
		var _this = this;
		var fieldTypeOptions = null;
		switch(entityKind) {
			case "ALL":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }];
				this._$andOrDropdownComponent.val("OR");
				this._advancedSearchModel.criteria.logicalOperator = "OR";
				this._$andOrDropdownComponent.attr("disabled", "");
				break;
			case "SAMPLE":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }, 
				                    {value : "Property/Attribute", label : "Property"},
				                    {value : "Experiment", label : ELNDictionary.ExperimentELN + "/" + ELNDictionary.ExperimentInventory}, 
				                    {value : "Parent", label : "Parent"}, 
				                    {value : "Children", label : "Children"}];
				break;
			case "EXPERIMENT":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }, 
				                    {value : "Property/Attribute", label : "Property"}];
				break;
			case "DATASET":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }, 
				                    {value : "Property/Attribute", label : "Property"},
				                    {value : "Sample", label : "" + ELNDictionary.Sample + ""},
// ELN-UI don't support this yet
//				                    {value : "Parent", label : "Parent"}, 
//				                    {value : "Children", label : "Children"}
				                    ];
				break;
		}
		
		this._advancedSearchModel.criteria.rules[uuid].type = "All"; //Update model with defaults
		
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
				case "Property/Attribute":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "OWN"));
					break;
				case "Sample":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "SAMPLE"));
					break;
				case "Experiment":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "EXPERIMENT"));
					break;
				case "Parent":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "PARENT"));
					break;
				case "Children":
					$newFieldNameContainer.append(_this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, "CHILDREN"));
					break;
				default:
					//Do Nothing
			}
		});
		return $fieldTypeComponent;
	}
	
	this._getNewMergedDropdown = function(entityKind, parentOrChildrenOrExperimentOrSample) {
		var _this = this;
		var model = null;
		var attributesModel = null;
		if(parentOrChildrenOrExperimentOrSample === "EXPERIMENT") {
			attributesModel = this._getFieldNameAttributesByEntityKind("EXPERIMENT");
		} else if(parentOrChildrenOrExperimentOrSample === "SAMPLE") {
			attributesModel = this._getFieldNameAttributesByEntityKind("SAMPLE");
		} else {
			attributesModel = this._getFieldNameAttributesByEntityKind(entityKind);
		}
		attributesModel.push({ value : "", label : "-------------------------", disabled : true });
		var propertiesModel = this._getFieldNameProperties();
		model = attributesModel.concat(propertiesModel);
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
	
	this._getFieldNameAttributesByEntityKind = function(entityKind) {
		var model = null;
		switch(entityKind) {
			case "EXPERIMENT":
				model = [{ value : "ATTR.CODE", label : "Code" }, 
				         { value : "ATTR.EXPERIMENT_TYPE", label :  ELNDictionary.ExperimentELN + "/" + ELNDictionary.ExperimentInventory + " Type" }, 
				         { value : "ATTR.PERM_ID", label : "Perm Id" }, 
				         { value : "ATTR.PROJECT", label : "Project" }, 
				         { value : "ATTR.PROJECT_PERM_ID", label : "Project Perm Id" }, 
				         { value : "ATTR.PROJECT_SPACE", label : "Project Space" }, 
//				         { value : "ATTR.METAPROJECT", label : "Tag" }, TO-DO Not supported by ELN yet
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date" }, 
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date" }];
				break;
			case "SAMPLE":
				model = [];
				model.push({ value : "ATTR.CODE", label: "Code" });
				if(!this._advancedSearchModel.isSampleTypeForced) {
					model.push({ value : "ATTR.SAMPLE_TYPE", label: "" + ELNDictionary.Sample + " Type" });
				}
				model.push({ value : "ATTR.PERM_ID", label: "Perm Id" });
				model.push({ value : "ATTR.SPACE", label: "Space" });
//				model.push({ value : "ATTR.METAPROJECT", label: "Tag" }); //TO-DO Not supported by ELN yet
				model.push({ value : "ATTR.REGISTRATION_DATE", label: "Registration Date" });
				model.push({ value : "ATTR.MODIFICATION_DATE", label: "Modification Date" });
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
		var model = [];
			model.push({ value : 'ALL', label : "All", selected : true });
			model.push({ value : 'EXPERIMENT', label : ELNDictionary.ExperimentELN + "/" + ELNDictionary.ExperimentInventory });
			model.push({ value : 'SAMPLE', label : "" + ELNDictionary.Sample + "" });			
			model.push({ value : 'DATASET', label : "Dataset" });
			model.push({ value : '', label : "--------------", disabled : true });
			var sampleTypes = profile.getAllSampleTypes();
			for(var tIdx = 0; tIdx < sampleTypes.length; tIdx++) {
				var sampleType = sampleTypes[tIdx];
				model.push({ value : 'SAMPLE$' + sampleType.code, label : Util.getDisplayNameFromCode(sampleType.code) });
			}
		
		this._advancedSearchModel.resetModel('ALL');
		var $dropdown = FormUtil.getDropdown(model, 'Select Entity Type to search for');
		
		$dropdown.change(function() {
			var kindAndType = $(this).val().split("$");
			
			if(_this._advancedSearchModel.isAllRules()) {
				//1. update the entity type only in the model
				_this._advancedSearchModel.setEntityKind(kindAndType[0]);
				//2. change the field type dropdowns in the view
				var rows = _this._$tbody.children();
				for(var rIdx = 0; rIdx < rows.length; rIdx++) {
					var $row = $(rows[rIdx]);
					var tds = $row.children();
					var $newFieldTypeComponent = _this._getNewFieldTypeDropdownComponent($(tds[1]), _this._advancedSearchModel.criteria.entityKind, $row.attr("id"));
					$(tds[0]).empty();
					$(tds[0]).append($newFieldTypeComponent);
				}				
			} else {
				_this._advancedSearchModel.resetModel(kindAndType[0]); //Restart model
				_this._paintCriteriaPanel(_this._$searchCriteriaPanelContainer); //Restart view	
			}
			
			if(kindAndType.length === 2) {
				var uuidValue = Util.guid();
				_this._advancedSearchModel.criteria.rules[uuidValue] = { };
				_this._advancedSearchModel.criteria.rules[uuidValue].type = 'Attribute';
				_this._advancedSearchModel.criteria.rules[uuidValue].name = 'ATTR.SAMPLE_TYPE';
				_this._advancedSearchModel.criteria.rules[uuidValue].value = kindAndType[1];
				_this._advancedSearchModel.isSampleTypeForced = true;
			} else {
				_this._advancedSearchModel.isSampleTypeForced = false;
			}
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
	
	this.renderResults = function(criteria) {
		var isGlobalSearch = this._advancedSearchModel.criteria.entityKind === "ALL";
		var dataGridController = this._getGridForResults(criteria, isGlobalSearch);
		dataGridController.init(this._$dataGridContainer);
	}
	
	this._getGridForResults = function(criteria, isGlobalSearch) {
		
			var getLinkOnClick = function(code, data) {
				switch(data.entityKind) {
					case "Experiment":
						return FormUtil.getFormLink(code, data.entityKind, data.identifier);
						break;
					default:
						return FormUtil.getFormLink(code, data.entityKind, data.permId);
						break;
				}
			}
		
			var columns = [ {
				label : 'Entity Kind',
				property : 'entityKind',
				isExportable: true,
				sortable : false,
				render : function(data) {
					if(data.entityKind === "Sample") {
						return ELNDictionary.Sample;
					} else if(data.entityKind === "Experiment") {
						return ELNDictionary.getExperimentKindName(data.identifier);
					} else {
						return data.entityKind;
					}
				}
			}, {
				label : 'Entity Type',
				property : 'entityType',
				isExportable: true,
				sortable : !isGlobalSearch
			}, {
				label : 'Name',
				property : 'NAME',
				isExportable: true,
				sortable : !isGlobalSearch,
				render : function(data) {
					if(data.NAME) {
						return getLinkOnClick(data.NAME, data);
					} else {
						return "";
					}
				}
			}, {
				label : 'Code',
				property : 'code',
				isExportable: true,
				sortable : !isGlobalSearch,
				render : function(data) {
					return getLinkOnClick(data.code, data);
				}
			}, {
				label : 'Identifier',
				property : 'identifier',
				isExportable: true,
				sortable : !isGlobalSearch,
				render : function(data) {
					return getLinkOnClick(data.identifier, data);
				}
			}, {
				label : ELNDictionary.ExperimentELN + '/' + ELNDictionary.ExperimentInventory,
				property : 'experiment',
				isExportable: false,
				sortable : false
			}];
			
			if(isGlobalSearch) {
				columns.push({
					label : 'Matched',
					property : 'matched',
					isExportable: true,
					sortable : false
				});
				
				columns.push({
					label : 'Score',
					property : 'score',
					isExportable: true,
					sortable : false
				});
			}
			
			columns.push({
				label : '---------------',
				property : null,
				isExportable: false,
				sortable : false
			});
			
			//Add properties as columns dynamically depending on the results
			var dynamicColumnsFunc = function(entities) {
				//1. Get properties with actual data
				var foundPropertyCodes = {};
				for(var rIdx = 0; rIdx < entities.length; rIdx++) {
					var entity = entities[rIdx].entityObject;
					if(isGlobalSearch) {
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
					
					if(!entity) {
						continue;
					}
					for(var propertyCode in entity.properties) {
						if(entity.properties[propertyCode]) {
							foundPropertyCodes[propertyCode] = true;
						}
					}
				}
				
				//2. Get columns
				var propertyColumnsToSort = [];
				for(var propertyCode in foundPropertyCodes) {
					var propertiesToSkip = ["NAME", "XMLCOMMENTS", "ANNOTATIONS_STATE"];
					if($.inArray(propertyCode, propertiesToSkip) !== -1) {
						continue;
					}
					
					propertyColumnsToSort.push({
						label : profile.getPropertyType(propertyCode).label,
						property : propertyCode,
						sortable : !isGlobalSearch
					});
				}
				
				//3. Sort column properties by label
				propertyColumnsToSort.sort(function(propertyA, propertyB) {
					return propertyA.label.localeCompare(propertyB.label);
				});
				
				return propertyColumnsToSort;
			}
			
			var columnsLast = [];
			//4. Add registration/modification date columns
			columnsLast.push({
				label : '---------------',
				property : null,
				isExportable: false,
				sortable : false
			});
			
			columnsLast.push({
				label : 'Registration Date',
				property : 'registrationDate',
				isExportable: true,
				sortable : !isGlobalSearch
			});
			
			columnsLast.push({
				label : 'Modification Date',
				property : 'modificationDate',
				isExportable: true,
				sortable : !isGlobalSearch
			});
			
			var getDataRows = this._advancedSearchController.searchWithPagination(criteria, isGlobalSearch);
			
			var dataGrid = new DataGridController("Search Results", columns, columnsLast, dynamicColumnsFunc, getDataRows, null, false, "ADVANCED_SEARCH_OPENBIS_" + this._advancedSearchModel.criteria.entityKind);
			return dataGrid;
	}
	
}