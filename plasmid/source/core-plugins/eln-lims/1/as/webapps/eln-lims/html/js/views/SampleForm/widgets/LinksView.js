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

function LinksView(linksController, linksModel) {
	var linksController = linksController;
	var linksModel = linksModel;
	var sampleTablesByTypeContainers = {};
	var samplesByTypeCache = {};
	
	var $samplePicker = $("<div>");
	
	this.repaint = function($container) {
		$container.empty();
		$container.append($("<legend>").append(linksModel.title));
		$container.append($samplePicker);
		$container.append(this._getAddAnyBtn());
	}
	
	this._showSamplePicker = function(sampleType) {
		$samplePicker.empty();
		
		//Close Button
		var $closeBtn = FormUtil.getButtonWithIcon("glyphicon-remove", function() {
			$samplePicker.empty();
		});
		var $closeBtnContainer = $("<div>").append($closeBtn).css({"text-align" : "right", "padding-right" : "2px"});
		$samplePicker.append($closeBtnContainer);
		
		//Grid Contaienr
		var $gridContainer = $("<div>");
		$samplePicker.append($gridContainer);
		
		//Show Table Logic
		var showTableFunction = function(samples) {
			samplesByTypeCache[sampleType.code] = samples;
			
			var rowClick = function(e) {
				linksController.addSample(e.data["$object"]);
				$samplePicker.empty();
			}
			
			var dataGrid = SampleDataGridUtil.getSampleDataGrid(sampleType.code, samples, rowClick);
			dataGrid.init($gridContainer);
		}
		
		//Check Cache and Show Table
		var sampleTypeCache = samplesByTypeCache[sampleType.code];
		if(sampleTypeCache) {
			showTableFunction(sampleTypeCache);
		} else {
			mainController.serverFacade.searchWithType(sampleType.code, null, false, showTableFunction);
		}
	}
			
	this._getAddAnyBtn = function() {
		var _this = this;
		
		var $addBtn = FormUtil.getButtonWithIcon("glyphicon-plus", function() {
			var $sampleTypesDropdown = FormUtil.getSampleTypeDropdown("sampleTypeSelector", true);
			Util.blockUI("Select type: <br><br>" + $sampleTypesDropdown[0].outerHTML + "<br> or <a class='btn btn-default' id='sampleTypeSelectorCancel'>Cancel</a>");
			
			$("#sampleTypeSelector").on("change", function(event) {
				var sampleTypeCode = $(this).val();
				var sampleType = profile.getSampleTypeForSampleTypeCode(sampleTypeCode);
				
				_this._showSamplePicker(sampleType);
				Util.unblockUI();
			});
			
			$("#sampleTypeSelectorCancel").on("click", function(event) { 
				Util.unblockUI();
			});
		});
		
		return $addBtn;
	}
}