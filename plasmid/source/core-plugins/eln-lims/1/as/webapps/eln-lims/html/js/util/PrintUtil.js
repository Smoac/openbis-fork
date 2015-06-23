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


var PrintUtil = new function() {

	this.printEntity = function(entity) {
		var newWindow = window.open(undefined,"print " + entity.permId);
		var pageToPrint = $("<html>")
							.append($("<head>"))
							.append($("<body>").append(this.getTable(entity)));
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this._getCodesFromSamples = function(samplesList) {
		var allSamplesByType = {};
		
		if(samplesList) {
			for(var i = 0; i < samplesList.length; i++) {
				var sample = samplesList[i];
				var samplesByType = allSamplesByType[sample.sampleTypeCode];
				if(samplesByType === null || samplesByType === undefined) {
					samplesByType = new Array();
				}
				samplesByType.push(sample);
				
				allSamplesByType[sample.sampleTypeCode] = samplesByType;
			}
		}
		
		var samplesListOfCodes = "";
		
		for(var sampleTypeCode in allSamplesByType) {
			samplesListOfCodes += sampleTypeCode + ": ";
			var samples = allSamplesByType[sampleTypeCode];
			for(var i = 0; i < samples.length; i++) {
				var sample = samples[i];
				samplesListOfCodes += sample.code + " ";
			}
			samplesListOfCodes += "</br>";
		}
		
		return samplesListOfCodes;
	}
	
	this.getTable = function(entity, isNotTransparent, optionalTitle, customClass, extraCustomId, extraContent) {
		var $newInspector = $("<div>");
		if(isNotTransparent) {
			$newInspector.css("background-color", "#FBFBFB");
		} else {
			$newInspector.css("background-color", "transparent");
		} 
		
		
		$newInspector.addClass("inspector");
		if(customClass) {
			$newInspector.addClass(customClass);
		}
			
		if(optionalTitle) {
			$newInspector.append(optionalTitle);
		} else {
			$newInspector.append($("<strong>").append(entity.code));
		}
		
		var $newInspectorTable = $("<table>", { "class" : "properties table table-condensed" });
		$newInspector.append($newInspectorTable);
		
		//Show Properties following the order given on openBIS
		var sampleTypePropertiesCode = profile.getAllPropertiCodesForTypeCode(entity.sampleTypeCode);
		var sampleTypePropertiesDisplayName = profile.getPropertiesDisplayNamesForTypeCode(entity.sampleTypeCode, sampleTypePropertiesCode);
			
		for(var i = 0; i < sampleTypePropertiesCode.length; i++) {
			var propertyCode = sampleTypePropertiesCode[i];
			var propertyLabel = sampleTypePropertiesDisplayName[i];
			var propertyContent = null;
			
			var propertyType = profile.getPropertyType(propertyCode);
			if(propertyType.dataType === "CONTROLLEDVOCABULARY") {
				propertyContent = FormUtil.getVocabularyLabelForTermCode(propertyType, entity.properties[propertyCode]);
			} else if(propertyType.dataType === "MATERIAL") {
				propertyContent = $("<a>").append(entity.properties[propertyCode]);
			} else {
				propertyContent = entity.properties[propertyCode];
				propertyContent = Util.getEmptyIfNull(propertyContent);
				propertyContent = Util.replaceURLWithHTMLLinks(propertyContent);
			}
				
			
			
			var isSingleColumn = false;
			if((propertyContent instanceof String) || (typeof propertyContent === "string")) {
				var transformerResult = profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
				isSingleColumn = transformerResult["isSingleColumn"];
				propertyContent = transformerResult["content"];
				propertyContent = propertyContent.replace(/\n/g, "<br />");
			}
			
			if(propertyContent !== "") {
				if(isSingleColumn) {
					$newInspectorTable
					.append($("<tr>")
								.append($("<td>", { "class" : "property", "colspan" : "2" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(propertyLabel + ":").append("<br>").append(propertyContent)))
							);
				} else {
					$newInspectorTable
					.append($("<tr>")
								.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLabel"}).append(propertyLabel + ":")))
								.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(propertyContent)))
							);
				}
			}
		}
		if(entity["@type"] === "Sample") {
			//Show Parent Codes
			var allParentCodesAsText = this._getCodesFromSamples(entity.parents);
			if(allParentCodesAsText.length > 0) {
				$newInspectorTable
					.append($("<tr>")
								.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLabel"}).append("Parents:")))
								.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(allParentCodesAsText)))
							);
			}
				
			//Show Children Codes
			var allChildrenCodesAsText = this._getCodesFromSamples(entity.children);
			if(allChildrenCodesAsText.length > 0) {
				$newInspectorTable
				.append($("<tr>")
							.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLabel"}).append("Children:")))
							.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(allChildrenCodesAsText)))
						);
			}
		}
		
		//Show Modification Date
		$newInspectorTable
		.append($("<tr>")
					.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLabel"}).append("Modification Date:")))
					.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(new Date(entity.registrationDetails["modificationDate"]))))
				);
		
		//Show Creation Date
		$newInspectorTable
		.append($("<tr>")
					.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLabel"}).append("Registration Date:")))
					.append($("<td>", { "class" : "property", "colspan" : "1" }).append($("<p>", { "class" : "inspectorLineBreak"}).append(new Date(entity.registrationDetails["registrationDate"]))))
				);
		
		if(extraCustomId && extraContent) {
			$newInspector.append($("<div>", { "class" : "inspectorExtra", "id" : extraCustomId}).append(extraContent));
		}
		
		return $newInspector;
	}
}