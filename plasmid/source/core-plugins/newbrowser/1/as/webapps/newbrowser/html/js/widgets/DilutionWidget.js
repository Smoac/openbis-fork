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

function DilutionWidget(containerId, serverFacade, isEnabled) {
	this._containerId = containerId;
	this._serverFacade = serverFacade;
	this._isEnabled = isEnabled;
	this._predefinedMass = [ 139,141,142,143,144
	                        ,145,146,147,148,149
	                        ,150,151,152,153,154
	                        ,155,156,158,159,160
	                        ,161,162,163,164,165
	                        ,166,167,168,169,170
	                        ,171,172,173,174,175
	                        ,176];
	this._allProteins = null;
	this._widgetTableId = "dillution-widget-table";
	this._totalVolume = null;
		
	this.init = function() {
		$("#"+this._containerId).append("Loading data for Dilution Widget.");
		var _this = this;
		//Load all proteins
		this._serverFacade.searchWithTypeAndLinks("PROTEIN", null, function(data) {
			_this._allProteins = data;
			
			//First repaint after all initializations
			_this._repaint();
			
			//Disable field or give alert
			var stateField = $("#DILUTION_STATE");
			if(stateField.length === 0) {
				Util.showError("You need a property with code DILUTION_STATE on this entity to store the state of the dilution widget.");
			} else {
				//Hide State Field
				var fieldset = stateField.parent().parent().parent();
				fieldset.hide();
				
				//Update Values
				var stateFieldVar = stateField.val();
				var stateObj = null;
				if(stateFieldVar) {
					stateObj = JSON.parse(stateFieldVar);
				} else {
					stateObj = JSON.parse("{}");
					return; //No update
				}
				var tBody = $("#" + _this._widgetTableId).children()[1];
				for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
					var row = $(tBody.rows[rowNum]);
					var antibodyDropDown = $($(row.children()[2]).children()[0]);
					var antibody = stateObj[rowNum][0];
					if(antibody !== "") {
						antibodyDropDown.val(antibody);
						antibodyDropDown.change();
						var conjugatedCloneDropDown = $($(row.children()[3]).children()[0]);
						var conjugatedClone = stateObj[rowNum][1];
						if(conjugatedClone !== "") {
							conjugatedCloneDropDown.val(conjugatedClone);
							conjugatedCloneDropDown.change();
						}
					}
				}
				_this._updateCalculatedValues();
			}
		});
	}
	
	this._getProteinDropdown = function(rowNumber) {
		var $component = $("<select>");

		$component.css(
				{
					"margin-top": "1px",
					"margin-bottom": "1px",
					"border": "0px",
					"width": "100%"
				}
		);
		
		$component.attr('data-row-number', rowNumber);
		
		//Get proteins for row
		var proteins = [];
		var _this = this;
		this._allProteins.forEach(function(protein) {
			protein.children.forEach(function(clone) {
				clone.children.forEach(function(lot) { 
					lot.children.forEach(function(conjugatedClone) {
						var metalMass = conjugatedClone.properties["METAL_MASS"];
						var predefinedMass = _this._predefinedMass[rowNumber] + "";
						if(predefinedMass === metalMass && $.inArray(protein, proteins) === -1) {
							proteins.push(protein);
						}
					});
				});
			});
		});
		//
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < proteins.length; i++) {
			$component.append($("<option>").attr('value',proteins[i].permId).text(proteins[i].properties["PROTEIN_NAME"]));
		}
		
		var _this = this;
		$component.change(function() {
			var rowNumber = $(this).attr('data-row-number');
			var proteinPermId = $(this).val();
			
			//Clear row
			_this._updateCell(rowNumber,3, "");
			_this._updateCell(rowNumber,4, "");
			_this._updateCell(rowNumber,5, "");
			_this._updateCell(rowNumber,6, "");
			_this._updateCell(rowNumber,7, "");
			_this._updateCalculatedValues();
			//Update row
			if(proteinPermId !== "") {
				_this._updateConjugatedClone(rowNumber, proteinPermId);
			}
		});
		if(!_this._isEnabled) {
			$component.attr('disabled', true)
		}
		return $component;
	}
	
	this._updateCell = function(row, column, component) {
		var $widgetTableCell = $($("#" + this._widgetTableId).children()[1].rows[row].cells[column]);
		$widgetTableCell.empty();
		$widgetTableCell.append(component);
	}
	
	this._updateConjugatedClone = function(rowNumber, proteinPermId) {
		//Get valid conjugated clones
		var conjugatedClones = [];
			
		this._allProteins.forEach(function(protein) {
			if(protein.permId === proteinPermId) {
				protein.children.forEach(function(clone) {
					clone.children.forEach(function(lot) { 
						lot.children.forEach(function(conjugatedClone) { 
							conjugatedClones.push(
									{
										"clone" : clone,
										"lot" : lot,
										"conjugatedClone" : conjugatedClone
									});
							
						});
					});
				});
			}
		});
			
		//Build dropdown with conjugated clones
		var $component = $("<select>");
		$component.css(
				{
					"margin-top": "1px",
					"margin-bottom": "1px",
					"border": "0px",
					"width": "100%"
				}
		);
			
		$component.attr('data-row-number', rowNumber);
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < conjugatedClones.length; i++) {
			var conjugatedClone = conjugatedClones[i]["conjugatedClone"];
			var metalMass = conjugatedClone.properties["METAL_MASS"];
			var predefinedMass = this._predefinedMass[rowNumber] + "";
			if(predefinedMass === metalMass) {
				$component.append($("<option>").attr('value',conjugatedClone.permId).text(conjugatedClone.code));
			}
		}
			
		//Add dropdown to the DOM
		this._updateCell(rowNumber,3, $component);
		
		//Add change method to DOM
		var _this = this;
		var conjugatedCloneChange = function() {
			var conjugatedCloneSelected = $(this).val();
			var data = null;
			for(var i = 0; i < conjugatedClones.length; i++) {
				var conjugatedClone = conjugatedClones[i]["conjugatedClone"];
				if(conjugatedCloneSelected === conjugatedClone.permId) {
					data = conjugatedClones[i];
					break;
				}
			}
			var dilutionVolume = parseFloat(data["conjugatedClone"].properties["CYTOF_STAINING_CONC"]) / parseFloat(data["conjugatedClone"].properties["CYTOF_CONCENTRATION"]);

				if(conjugatedCloneSelected === "") {
					_this._updateCell(rowNumber,4, "");
					_this._updateCell(rowNumber,5, "");
					_this._updateCell(rowNumber,6, "");
					_this._updateCell(rowNumber,7, "");
				} else {
					_this._updateCell(rowNumber,4, data["clone"].properties["REACTIVITY"]);
					_this._updateCell(rowNumber,5, data["lot"].properties["SUPPLIER"]);
					_this._updateCell(rowNumber,6, dilutionVolume);

				}
			_this._updateCalculatedValues();
		}
		
		$component.change(conjugatedCloneChange);
		if(!this._isEnabled) {
			$component.attr('disabled', true)
		}
	}
	
	this._updateCalculatedValues = function() {
		var tBody = $("#" + this._widgetTableId).children()[1];
		var totalVolumeToAdd = 0;
		
		//Row Volume to add
		for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
			var row = $(tBody.rows[rowNum]);
			var concentration = row.children()[6].innerHTML;
			if(concentration !== "") {
				var volumeToAdd = this._totalVolume * parseFloat(concentration);
				totalVolumeToAdd += volumeToAdd;
				this._updateCell(rowNum,7, volumeToAdd);
			}
		}
		
		//Total Volume to add
		this._updateCell(tBody.rows.length - 1,7, totalVolumeToAdd);
		
		//Buffer Volume
		this._updateCell(tBody.rows.length - 2,7, this._totalVolume - totalVolumeToAdd);
		
		//Update State
		var state = $("#DILUTION_STATE");
		if(state.length === 1) {
			var state = {};
			for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
				var row = $(tBody.rows[rowNum]);
				var antibodyDropDown = $($(row.children()[2]).children()[0]);
				var antibody = "";
				if(antibodyDropDown.val()) {
					antibody = antibodyDropDown.val();
				}
				var conjugatedClone = "";
				
				if(antibody !== "") {
					var conjugatedCloneDropDown = $($(row.children()[3]).children()[0]);
					if(conjugatedCloneDropDown.val()) {
						conjugatedClone = conjugatedCloneDropDown.val();
					}
				}
				
				state[rowNum] = [antibody, conjugatedClone];
			}
			
			$("#DILUTION_STATE").val(JSON.stringify(state));
		}
	}
	
	this._repaint = function() {
		var _this = this;
		//
		$("#"+this._containerId).empty();

		//Top Title
		var $legend = $("<legend>");
		$legend.append("Dilution Calculator ");
		
		var $printButton = $("<a>", { class: "btn" }).append($("<i>", { class: "icon-print" }));
		$printButton.click(function() { 
			var tableWidget = $("#" + _this._widgetTableId);
			var clonedWidget = tableWidget.clone();
			
			//FIX Selected values
			var tBody = tableWidget.children()[1];
			var tBodyClone = clonedWidget.children()[1];
			for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
				var row = $(tBody.rows[rowNum]);
				var antibodyDropDown = $($(row.children()[2]).children()[0]);
				var antibody = antibodyDropDown.val();
				if(antibody !== "") {
					var rowClone = $(tBodyClone.rows[rowNum]);
					var antibodyDropDownClone = $($(rowClone.children()[2]).children()[0]);
					antibodyDropDownClone.val(antibody);

					var conjugatedCloneDropDown = $($(row.children()[3]).children()[0]);
					var conjugatedClone = conjugatedCloneDropDown.val();
					if(conjugatedClone !== "") {
						var conjugatedCloneDropDownClone = $($(rowClone.children()[3]).children()[0]);
						conjugatedCloneDropDownClone.val(conjugatedClone);
					}
				}
			}
			
			var newWindow = window.open(null,"print dilution table");
			$(newWindow.document.body).html(clonedWidget);
		} );
		
		$legend.append($printButton);
		//Defining containers
		var $wrapper = $("<div>");
		$wrapper.append($legend);
		
		var $table = $("<table>", { "class" : "table table-bordered table-condensed table-condensed-dilution", "id" : this._widgetTableId});
		
		$wrapper.append($table);
		var $tableHead = $("<thead>");
		var $tableBody = $("<tbody>");
		$table
			.append($tableHead)
			.append($tableBody);

		//Headers
		var $tableHeadTr = $("<tr>");
		$tableHeadTr
			.append("<th><center>Index</center></th>")
			.append("<th><center>Metal Mass</center></th>")
			.append("<th><center>Antibody</center></th>")
			.append("<th><center>Conjugated Clone</center></th>")
			.append("<th><center>Reactivity</center></th>")
			.append("<th><center>Supplier</center></th>")
			.append("<th><center>Dilution Factor</center></th>")
			.append("<th><center>Volume To Add</center></th>");
		$tableHead.append($tableHeadTr);

		for(var i = 0; i < this._predefinedMass.length; i++){
			var $tableRowTr = $("<tr>");
			
			var $proteinSelectionTD = $("<td>").append(this._getProteinDropdown(i));
			
			$tableRowTr
				.append("<td>" + (i+1) + "</td>")
				.append("<td>" + this._predefinedMass[i] +"</td>")
				.append($proteinSelectionTD)
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>");
			
			$tableBody.append($tableRowTr);
		}
		
		
		var $tableRowTrF1TextBox = $("<input>", {"type" : "number", "step" : "any"});
		$tableRowTrF1TextBox.css(
				{
					"margin-top": "0px",
					"margin-bottom": "0px",
					"border": "0px",
					"width": "100%",
					"height": "25px",
					"padding" : "0px 0px 0px 0px",
					"background-color" : "#EEEEEE",
					"text-align" : "center",
					"border-radius" : "0px 0px 0px 0px"
				});
		var $tableRowTrF1TextBoxLastTD = $("<td>");
		$tableRowTrF1TextBoxLastTD.css(
				{
					"margin-top": "0px",
					"margin-bottom": "0px",
					"padding" : "0px 0px 0px 0px"
				});
		$tableRowTrF1TextBox.val(400);
		
		var _this = this;
		this._totalVolume = 400;
		$tableRowTrF1TextBox.keyup(function() {
			_this._totalVolume = $(this).val();
			_this._updateCalculatedValues();
		});
		
		$tableRowTrF1TextBoxLastTD.append($tableRowTrF1TextBox);
		
				
		var $tableRowTrF1 = $("<tr>");
		$tableRowTrF1.append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>")
		.append("<td><b>Total Volume Needed</b></td>")
		.append($tableRowTrF1TextBoxLastTD);
		
		$tableBody.append($tableRowTrF1);
		var $tableRowTrF2 = $("<tr>");
		$tableRowTrF2.append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>")
		.append("<td><b>Buffer Volume</b></td>")
		.append("<td style='text-align : center;'>" + this._totalVolume + "</td>");
		$tableBody.append($tableRowTrF2);
		var $tableRowTrF3 = $("<tr>");
		$tableRowTrF3.append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>").append("<td></td>")
		.append("<td><b>Total Antibody</b></td>")
		.append("<td style='text-align : center;'>0</td>");
		$tableBody.append($tableRowTrF3);
		
		//
		$wrapper.append();
		$("#"+this._containerId).append($wrapper);
	}
}