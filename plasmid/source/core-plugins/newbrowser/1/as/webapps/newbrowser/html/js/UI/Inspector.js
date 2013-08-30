function Inspector(containerId, profile) {
	this.containerId = containerId;
	this.profile = profile;
	this.inspectedSamples = new Array();
	
	this.repaint = function() {
		$("#"+containerId).empty();
		var allInspectors = ""
		allInspectors += "<a class='btn' href='javascript:inspector.printInspectors()'><i class='icon-print'></i></a>";
		allInspectors += "<div id='inspectorsContainer' class='inspectorsContainer'>";
		allInspectors += this.getAllInspectors(false, true, true, true);
		allInspectors += "</div>";
		
		$("#"+containerId).append(allInspectors);
	}
	
	this.containsSample = function(sampleId) {
		for(var i = 0; i < this.inspectedSamples.length; i++) {
			if(this.inspectedSamples[i].id === sampleId) {
				return i;
			}
		}
		return -1;
	}
	
	this.addInspectSampleIfNotFound = function(sampleToInspect) {
		var samplePosition = this.containsSample(sampleToInspect.id);
		
		if(samplePosition === -1) {
			this.toggleInspectSample(sampleToInspect);
			return true;
		}
		
		return false;
	}
	
	this.toggleInspectSample = function(sampleToInspect) {
		var isInspected = null;
		//Null Check
		if(sampleToInspect === null || sampleToInspect === undefined) {
			return;
		}
		
		//Already inspected check
		var samplePosition = this.containsSample(sampleToInspect.id);
		if(samplePosition !== -1) {
			this.inspectedSamples.splice(samplePosition, 1);
			isInspected = false;
		} else {
			this.inspectedSamples.push(sampleToInspect);
			isInspected = true;
		}
		
		$("#num-pins").empty();
		$("#num-pins").append(this.inspectedSamples.length);
		
		return isInspected;
	}
	
	this.getAllInspectors = function(withSeparator, withClose, withColors, withLinks) {
		var inspectorsContent = "";
		for(var i=0;i<this.inspectedSamples.length;i++) {
			inspectorsContent += this.getInspectorTable(this.inspectedSamples[i], withClose, withColors, withLinks);
			if(withSeparator) {
				inspectorsContent += "<hr>";
			}
		}
		return inspectorsContent;
	}
	
	this.closeNewInspector = function(sampleIdToDelete) {
		for(var i = 0; i < this.inspectedSamples.length; i++) {
			if(this.inspectedSamples[i].id === sampleIdToDelete) {
				this.inspectedSamples.splice(i, 1);
				break;
			}
		
		}
		
		$("#num-pins").empty();
		$("#num-pins").append(this.inspectedSamples.length);
		this.repaint();
	}
	
	this.printInspectors = function() {
		var newWindow = window.open(null,"print");
		
		var pageToPrint = "";
			pageToPrint += "<html>";
			pageToPrint += "<head>";
			pageToPrint += "</head>";
			pageToPrint += "<body stlye='font-family: '\"'Helvetica Neue\",Helvetica,Arial,sans-serif;'>";
			pageToPrint += this.getAllInspectors(true, false, false, false);
			pageToPrint += "</body>";
			pageToPrint += "</html>";
		
		$(newWindow.document.body).html(pageToPrint);
	}
	
	this.showSampleOnInspector = function(sampleTypeCode, sampleCode) {
		var localReference = this;
		
		//Clean glow effect in case was used already with that div
		var divID = sampleCode + "_INSPECTOR";
		$("#"+divID).removeClass("glow");
		
		Search.searchWithType(sampleTypeCode, sampleCode, function(data) {
			
			var isAdded = localReference.addInspectSampleIfNotFound(data[0]);
			if(isAdded) {
				var inspectorTable = localReference.getInspectorTable(data[0], true, true, true);
				$("#inspectorsContainer").append(inspectorTable);
			}
			
			//Move Scrollbar	
			var objDiv = document.getElementById(divID);
			var moveTo = moveTo = objDiv.offsetTop-50;
			$('html,body').animate({scrollTop:  moveTo}, 200, "swing");
			
			//Make it Glow
			$("#"+divID).addClass("glow");
		});
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
			var displayName = profile.getTypeForTypeCode(sampleType).description;
			allParentCodesAsText += displayName + ": ";
			var parents = allParentCodesByType[sampleType];
			for(var i = 0; i < parents.length; i++) {
				var parent = parents[i];
				if(withLinks) {
					allParentCodesAsText += "<a href=\"javascript:inspector.showSampleOnInspector('" + parent.sampleTypeCode + "','" + parent.code + "');\">" + parent.code + "</a> ";
				} else {
					allParentCodesAsText += parent.code + " ";
				}
			}
			allParentCodesAsText += "</br>";
		}
		
		return allParentCodesAsText;
	}
	
	this.getInspectorTable = function(entity, showClose, withColors, withLinks) {
		
		var defaultColor = "#ffc"
		var profileColor = this.profile.colorForInspectors[entity.sampleTypeCode];
		
		if(!withColors) {
			defaultColor = "#fff"
		} else if(profileColor !== null && profileColor !== undefined) {
			defaultColor = profileColor;
		}
		
		
		var inspector = "";
			inspector += "<div id='"+entity.code+"_INSPECTOR' class='inspector' style='background-color:" + defaultColor + ";' >";
			
			inspector += "<strong>" + entity.code + "</strong>";
			
			if(showClose) {
				inspector += "<span class='close' onclick='inspector.closeNewInspector(\""+entity.id+"\")'>x</span>";
			}
			
			inspector += "<table class='properties table'>"
			
			//Show Properties following the order given on openBIS
			var sampleTypePropertiesCode =  this.profile.getAllPropertiCodesForTypeCode(entity.sampleTypeCode);
			var sampleTypePropertiesDisplayName = this.profile.getPropertiesDisplayNamesForTypeCode(entity.sampleTypeCode, sampleTypePropertiesCode);
			
			for(var i = 0; i < sampleTypePropertiesCode.length; i++) {
				
				var propertyCode = sampleTypePropertiesCode[i];
				var propertyLabel = sampleTypePropertiesDisplayName[i];
				var propertyContent = entity.properties[propertyCode];
				propertyContent = Util.getEmptyIfNull(propertyContent);
				
				var isSingleColumn = false;
				if((propertyContent instanceof String) || (typeof propertyContent === "string")) {
					var transformerResult = this.profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
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
						inspector += "<td class='property'>"+propertyContent+"</td>";
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
						var transformerResult = this.profile.inspectorContentTransformer(entity, propertyCode, propertyContent);
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
							inspector += "<td class='property'>"+propertyContent+"</td>";
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
			
			var extraContainerId = entity.code+"_INSPECTOR_EXTRA";
			inspector += "<div id='"+ extraContainerId + "'>";
			inspector += this.profile.inspectorContentExtra(extraContainerId, entity);
			inspector += "</div>"
			
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
			inspector += "</div>"
			
		return inspector;
	}
}