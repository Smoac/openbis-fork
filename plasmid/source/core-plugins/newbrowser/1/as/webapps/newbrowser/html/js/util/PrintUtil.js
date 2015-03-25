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
		
		var pageToPrint = "";
			pageToPrint += "<html>";
			pageToPrint += "<head>";
			pageToPrint += "</head>";
			pageToPrint += "<body stlye='font-family: '\"'Helvetica Neue\",Helvetica,Arial,sans-serif;'>";
			pageToPrint += this.getInspectorTable(sample, false, false, false);
			pageToPrint += "</body>";
			pageToPrint += "</html>";
		
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this.getParentsChildrenText = function(parentsChildrenList, withLinks) {
		var allParentCodesByType = {};
		
		if(parentsChildrenList) {
			for(var i = 0; i < parentsChildrenList.length; i++) {
				var parent = parentsChildrenList[i];
				var parentsWithType = allParentCodesByType[parent.sampleTypeCode];
				if(parentsWithType === null || parentsWithType === undefined) {
					parentsWithType = new Array();
				}
				parentsWithType.push(parent);
				
				allParentCodesByType[parent.sampleTypeCode] = parentsWithType;
			}
		}
		
		var allParentCodesAsText = "";
		
		for(var sampleType in allParentCodesByType) {
			var displayName = profile.getSampleTypeForSampleTypeCode(sampleType).description;
			if(displayName === null) {
				displayName = sampleType;
			}
			allParentCodesAsText += displayName + ": ";
			var parents = allParentCodesByType[sampleType];
			for(var i = 0; i < parents.length; i++) {
				var parent = parents[i];
				if(withLinks) {
					allParentCodesAsText += "<a href=\"javascript:mainController.inspector.showSampleOnInspector('" + parent.permId + "');\">" + parent.code + "</a> ";
				} else {
					allParentCodesAsText += parent.code + " ";
				}
			}
			allParentCodesAsText += "</br>";
		}
		
		return allParentCodesAsText;
	}
	
	this.getTable = function(entity, showClose, withColors, withLinks, optionalTitle, isCondensed) {
		var defaultColor = null;
		
		if(!withColors) {
			defaultColor = "#ffffff"
		} else {
			defaultColor = profile.getColorForInspectors(entity.sampleTypeCode);
		} 

		var inspector = "";
			var divID = entity.sampleTypeCode + "_" + entity.code + "_INSPECTOR";
			
			var inspectorClass = null;
			if(isCondensed) {
				inspectorClass = 'inspectorCondensed';
			} else {
				inspectorClass = 'inspector';
			}
			
			inspector += "<div id='"+divID+"' class='" + inspectorClass + "' style='background-color:" + defaultColor + ";' >";
			
			if(showClose) {
				var removeButton = "<span class='btn inspectorToolbar btn-default' style='float:left; margin: 2px' onclick='mainController.inspector.closeNewInspector(\""+entity.id+"\")'><i class='glyphicon glyphicon-remove'></i></span>";
				inspector += removeButton;
			}
			
			if(withLinks) {
				var toogleButton = "<span class='btn inspectorToolbar btn-default' style='float:left; margin: 2px' onclick='mainController.inspector.toogleInspector(\""+entity.permId+"_TOOGLE\")'><i id='"+entity.permId+"_TOOGLE_ICON' class='glyphicon glyphicon-chevron-up'></i></span>";
				inspector += toogleButton;
			}
			
			if(optionalTitle) {
				inspector += optionalTitle;
			} else {
				inspector += "<strong>" + entity.code + "</strong>";
			}
			
			
			if(withLinks) {
				var printButton = "<span class='btn btn-default inspectorToolbar' style='float:right; margin: 2px;' onclick='javascript:mainController.inspector.printInspector(\""+entity.permId+"\")'><i class='glyphicon glyphicon-print'></i></span>";
				inspector += printButton;
				var viewButton = "<span class='btn btn-default inspectorToolbar' style='float:right; margin: 2px' onclick='javascript:mainController.changeView(\"showViewSamplePageFromPermId\",\""+entity.permId+"\")'><i class='glyphicon glyphicon-edit'></i></span>";
				inspector += viewButton;
				var hierarchyButton = "<span class='btn btn-default inspectorToolbar' style='float:right; margin: 2px' onclick=\"javascript:mainController.changeView('showSampleHierarchyPage','"+entity.permId+"');\"><img src='./img/hierarchy-icon.png' style='width:16px; height:17px;' /></span>";
				inspector += hierarchyButton;
			}
			
			inspector += "<table id='" + entity.permId +"_TOOGLE' class='properties table table-condensed'>"
			
			//Show Properties following the order given on openBIS
			var sampleTypePropertiesCode = profile.getAllPropertiCodesForTypeCode(entity.sampleTypeCode);
			var sampleTypePropertiesDisplayName = profile.getPropertiesDisplayNamesForTypeCode(entity.sampleTypeCode, sampleTypePropertiesCode);
			
			for(var i = 0; i < sampleTypePropertiesCode.length; i++) {
				
				var propertyCode = sampleTypePropertiesCode[i];
				var propertyLabel = sampleTypePropertiesDisplayName[i];
				var propertyContent = entity.properties[propertyCode];
				
				//
				// Fix to show vocabulary labels instead of codes
				//
				var sampleType = profile.getSampleTypeForSampleTypeCode(entity.sampleTypeCode);
				var propertyType = profile.getPropertyTypeFrom(sampleType, propertyCode);
				if(propertyType && propertyType.dataType === "CONTROLLEDVOCABULARY") {
					var vocabulary = null;
					if(isNaN(propertyType.vocabulary)) {
						vocabulary = profile.getVocabularyById(propertyType.vocabulary.id);
					} else {
						vocabulary = profile.getVocabularyById(propertyType.vocabulary);
					}
					
					if(vocabulary) {
						for(var j = 0; j < vocabulary.terms.length; j++) {
							if(vocabulary.terms[j].code === propertyContent) {
								propertyContent = vocabulary.terms[j].label;
								break;
							}
						}
					}
				}
				// End Fix
				
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
			
			//Show Properties not found on openBIS (TO-DO Clean duplicated code)
			for(propertyCode in entity.properties) {
				if($.inArray(propertyCode, sampleTypePropertiesCode) === -1) {
					var propertyLabel = propertyCode;
					var propertyContent = entity.properties[propertyCode];
					propertyContent = Util.getEmptyIfNull(propertyContent);
					
					var isSingleColumn = false;
					if((propertyContent instanceof String) || (typeof propertyContent === "string")) {
						var transformerResult = profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
						isSingleColumn = transformerResult["isSingleColumn"];
						propertyContent = transformerResult["content"];
						propertyContent = propertyContent.replace(/\n/g, "<br />");
					}
					
					if(propertyContent !== "") {
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
			}
			
			//Show Parent Codes
			var allParentCodesAsText = this.getParentsChildrenText(entity.parents, withLinks);
			if(allParentCodesAsText.length > 0) {
				inspector += "<tr>";
				inspector += "<td class='property'>Parents</td>";
				inspector += "<td class='property'>"+allParentCodesAsText+"</td>";
				inspector += "</tr>";
			}
			
			//Show Children Codes
			var allChildrenCodesAsText = this.getParentsChildrenText(entity.children, withLinks);
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
			
			var extraContainerId = entity.sampleTypeCode + "_" + entity.code+"_INSPECTOR_EXTRA";
			inspector += "<div class='inspectorExtra' id='"+ extraContainerId + "'></div>";
			profile.inspectorContentExtra(extraContainerId, entity);
			
			inspector += "</div>"
			
			
		return inspector;
	}
}