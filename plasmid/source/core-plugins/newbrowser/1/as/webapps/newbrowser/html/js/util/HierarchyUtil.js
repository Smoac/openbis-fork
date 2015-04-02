/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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
var HierarchyUtil = new function() {
	var getSampleTypes = function(sample) {
		var sampleTypes = {};
		
		var getSampleTypesWithQueueRecursion = function(sample, sampleTypes) {
			if(!sampleTypes[sample.sampleTypeCode]) {
				sampleTypes[sample.sampleTypeCode] = true;
			}
			
			if(sample.parents) {
				for(var i = 0; i < sample.parents.length; i++) {
					getSampleTypesWithQueueRecursion(sample.parents[i], sampleTypes);
				}
			}
			
			if(sample.children) {
				for(var i = 0; i < sample.children.length; i++) {
					getSampleTypesWithQueueRecursion(sample.children[i], sampleTypes);
				}
			}
		}
		
		getSampleTypesWithQueueRecursion(sample, sampleTypes);
		return sampleTypes;
	}
	
	var getMaxChildrenDepth = function(sample) {
		var getMaxChildrenDepthWithQueueRecurion = function(sample, max) {
			if(sample.children) {
				var posibleNextMax = [];
				
				for(var i = 0; i < sample.children.length; i++) {
					var nextMax = getMaxChildrenDepthWithQueueRecurion(sample.children[i], (max + 1));
					posibleNextMax.push(nextMax);
				}
				
				for(var i = 0; i < posibleNextMax.length; i++) {
					if(posibleNextMax[i] > max) {
						max = posibleNextMax[i];
					}
				}
			}
			
			return max;
		}
		
		return getMaxChildrenDepthWithQueueRecurion(sample, 0);
	}
	
	var getMaxParentsDepth = function(sample) {
		var getMaxParentsDepthWithQueueRecurion = function(sample, max) {
			if(sample.parents) {
				var posibleNextMax = [];
				
				for(var i = 0; i < sample.parents.length; i++) {
					var nextMax = getMaxParentsDepthWithQueueRecurion(sample.parents[i], (max + 1));
					posibleNextMax.push(nextMax);
				}
				
				for(var i = 0; i < posibleNextMax.length; i++) {
					if(posibleNextMax[i] > max) {
						max = posibleNextMax[i];
					}
				}
			}
			
			return max;
		}
		
		return getMaxParentsDepthWithQueueRecurion(sample, 0);
	}
	
	
	var getSliderValue = function(id) {
		var element = $('#' + id)
		return  element.length > 0 ? element.data('slider').getValue() : 0;
	}
	
	this.getParentsLimit = function() {
		return getSliderValue("parentsLimit");
	}
	
	this.getChildrenLimit = function() {
		return getSliderValue("childrenLimit");
	}
	
	this.getSelectedSampleTypes = function() {
		var selectedSampleTypes = $('#sampleTypesSelector').val();
		if(selectedSampleTypes === null) {
			selectedSampleTypes = [];
		}
		return selectedSampleTypes;
	}
	
	/*
	 * Creates a hierarchy filter widget for the specified sample and adds it to the specified container.
	 * The specified updater object should have a function filterSampleAndUpdate().
	 */
	this.addHierarchyFilterWidget = function(container, sample, updater) {
		var $filtersForm = $('<form>' , { class : 'form-inline'});
		container.append($filtersForm);
		$filtersForm.submit(function(event) {updater.filterSampleAndUpdate(); event.preventDefault();});
		
		var maxChildren = getMaxChildrenDepth(sample);
		var $filtersFormSliderChildren = null;
		if(maxChildren > 0) {
			$filtersFormSliderChildren = $('<input>' , { 'id' : 'childrenLimit' , 'type' : 'text' , 'class' : 'span2', 'value' : '' , 'data-slider-max' : maxChildren , 'data-slider-value' : maxChildren});
		} else {
			$filtersFormSliderChildren = 'No Children';
		}
		
		var maxParents = getMaxParentsDepth(sample);
		var $filtersFormSliderParents = null;
		if(maxParents > 0) {
			$filtersFormSliderParents = $('<input>' , { 'id' : 'parentsLimit' , 'type' : 'text' , 'class' : 'span2', 'value' : '' , 'data-slider-max' : maxParents , 'data-slider-value' : maxParents});
		} else {
			$filtersFormSliderParents = 'No Parents';
		}
		
		var sampleTypes = getSampleTypes(sample);
		var $filtersFormSampleTypes = $('<select>', { 'id' : 'sampleTypesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
		for (var sampleType in sampleTypes) {
			$filtersFormSampleTypes.append($('<option>', { 'value' : sampleType , 'selected' : ''}).html(sampleType));
		}
		
		$filtersForm
			.append('<b>Filters</b>')
			.append("<span style='padding-right:15px;'></span>")
			.append('Children: ')
			.append($filtersFormSliderChildren)
			.append("<span style='padding-right:15px;'></span>")
			.append(' Parents: ')
			.append($filtersFormSliderParents)
			.append("<span style='padding-right:15px;'></span>")
			.append(' Show Types: ')
			.append($filtersFormSampleTypes)
			.append("<span style='position:absolute; left:30px; top:80px;'><svg height='100' width='100'><g id='svgControls'/></svg></span>");

		$('#childrenLimit').slider().on('slideStop', function(event){
			updater.filterSampleAndUpdate();
		});
		
		$('#parentsLimit').slider().on('slideStop', function(event){
			updater.filterSampleAndUpdate();
		});
		
		$('#sampleTypesSelector').multiselect();
		$('#sampleTypesSelector').change(function(event){
			updater.filterSampleAndUpdate();
		});
	}
	
	/*
	 * Creates a map (sample identifiers as keys) for all ancestors and descendants of the specified sample.
	 * The values of the map are the children and parents of the sample specified by the key..
	 */
	this.createRelationShipsMap = function(sample) {
		var relationShipsMap = {};
		createRelationShipEntry(sample, relationShipsMap);
		traverseAncestors(sample, relationShipsMap);
		traverseDescendants(sample, relationShipsMap);
		return relationShipsMap;
	}
	
	var traverseAncestors = function(sample, relationShipsMap) {
		if (sample.parents) {
			for (var i = 0; i < sample.parents.length; i++) {
				var parent = sample.parents[i];
				addRelationShip(parent, sample, relationShipsMap);
				traverseAncestors(parent, relationShipsMap);
			}
		}
	}
	
	var traverseDescendants = function(sample, relationShipsMap) {
		if (sample.children) {
			for (var i = 0; i < sample.children.length; i++) {
				var child = sample.children[i];
				addRelationShip(sample, child, relationShipsMap);
				traverseDescendants(child, relationShipsMap);
			}
		}
	}
	
	var addRelationShip = function(parent, child, relationShipsMap) {
		getRelationShips(child, relationShipsMap).parents.push(parent);
		getRelationShips(parent, relationShipsMap).children.push(child);
	}
	
	var getRelationShips = function(sample, relationShipsMap) {
		var relationShips = relationShipsMap[sample.identifier];
		if (typeof relationShips === 'undefined') {
			relationShips = createRelationShipEntry(sample, relationShipsMap);
		}
		return relationShips;
	}
	
	var createRelationShipEntry = function(sample, relationShipsMap) {
		var relationShips = {parents: [], children: []};
		relationShipsMap[sample.identifier] = relationShips;
		return relationShips;
	}
	
}
