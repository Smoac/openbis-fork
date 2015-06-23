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

	this.printSample = function(sample) {
		var newWindow = window.open(undefined,"print " + sample.permId);
		var pageToPrint = $("<html>")
							.append($("<head>"))
							.append($("<body>").append(this.getTable(sample)));
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this.getParentsChildrenText = function(samplesList) {
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
		var defaultColor = null;
		
		if(isNotTransparent) {
			defaultColor = "#FBFBFB";
		} else {
			defaultColor = "transparent"
		} 
		
		var inspector = "";
			
		var inspectorClass = 'inspector';
		if(customClass) {
			inspectorClass += ' ' + customClass;
		}
		inspector += "<div class='" + inspectorClass + "' style='background-color:" + defaultColor + ";' >";
			
		if(optionalTitle) {
			inspector += optionalTitle;
		} else {
			inspector += "<strong>" + entity.code + "</strong>";
		}
		
		inspector += "<table id='" + entity.permId +"_TOOGLE' class='properties table table-condensed'>"
		
		//Show Properties following the order given on openBIS
		var sampleTypePropertiesCode = profile.getAllPropertiCodesForTypeCode(entity.sampleTypeCode);
		var sampleTypePropertiesDisplayName = profile.getPropertiesDisplayNamesForTypeCode(entity.sampleTypeCode, sampleTypePropertiesCode);
			
		for(var i = 0; i < sampleTypePropertiesCode.length; i++) {
			var propertyCode = sampleTypePropertiesCode[i];
			var propertyLabel = sampleTypePropertiesDisplayName[i];
			var propertyContent = null;
			
			var propertyType = profile.getPropertyType(propertyCode);
			if(propertyType && propertyType.dataType === "CONTROLLEDVOCABULARY") {
				propertyContent = FormUtil.getVocabularyLabelForTermCode(propertyType, entity.properties[propertyCode]);
			} else {
				propertyContent = entity.properties[propertyCode];
			}
				
			propertyContent = Util.getEmptyIfNull(propertyContent);
			
			var isSingleColumn = false;
			if((propertyContent instanceof String) || (typeof propertyContent === "string")) {
				var transformerResult = profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
				isSingleColumn = transformerResult["isSingleColumn"];
				propertyContent = transformerResult["content"];
				propertyContent = propertyContent.replace(/\n/g, "<br />");
			}
			
			if(propertyContent !== "") {
				propertyContent = Util.replaceURLWithHTMLLinks(propertyContent);
				inspector += "<tr>";
					
				if(isSingleColumn) {
					inspector += "<td class='property' colspan='2'>"+propertyLabel+"<br />"+propertyContent+"</td>";
				} else {
					inspector += "<td class='property'>"+propertyLabel+"</td>";
					inspector += "<td class='property'><p class='inspectorLineBreak'>"+propertyContent+"</p></td>";
				}
				
				inspector += "</tr>";
			}
		}
			
		//Show Parent Codes
		var allParentCodesAsText = this.getParentsChildrenText(entity.parents);
		if(allParentCodesAsText.length > 0) {
			inspector += "<tr>";
			inspector += "<td class='property'>Parents</td>";
			inspector += "<td class='property'>"+allParentCodesAsText+"</td>";
			inspector += "</tr>";
		}
			
		//Show Children Codes
		var allChildrenCodesAsText = this.getParentsChildrenText(entity.children);
		if(allChildrenCodesAsText.length > 0) {
			inspector += "<tr>";
			inspector += "<td class='property'>Children</td>";
			inspector += "<td class='property'>"+allChildrenCodesAsText+"</td>";
			inspector += "</tr>";
		}
			
		//Show Modification Date
		inspector += "<tr>";
		inspector += "<td class='property'>Modification Date</td>";
		inspector += "<td class='property'>"+new Date(entity.registrationDetails["modificationDate"])+"</td>";
		inspector += "</tr>";
		
		//Show Creation Date
		inspector += "<tr>";
		inspector += "<td class='property'>Registration Date</td>";
		inspector += "<td class='property'>"+new Date(entity.registrationDetails["registrationDate"])+"</td>";
		inspector += "</tr>";
		
		inspector += "</table>"
		
		if(extraCustomId && extraContent) {
			inspector += "<div class='inspectorExtra' id='"+ extraCustomId + "'>" + extraContent + "</div>";
		}
		
		inspector += "</div>"
			
		return inspector;
	}
}