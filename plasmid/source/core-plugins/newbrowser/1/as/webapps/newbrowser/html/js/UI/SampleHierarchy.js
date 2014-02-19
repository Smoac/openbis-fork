/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function SampleHierarchy(serverFacade, inspector, containerId, profile, sample) {
	this.serverFacade = serverFacade;
	this.inspector = inspector;
	this.containerId = containerId;
	this.profile = profile;
	this.sample = sample;

	this.init = function() {
		this.repaint();
	}
	
	this._getSampleTypes = function(sample) {
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
	
	this._getMaxChildrenDepth = function(sample) {
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
		
		var result = getMaxChildrenDepthWithQueueRecurion(sample, 0);
		return result;
	}
	
	this._getMaxParentsDepth = function(sample) {
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
		
		var result = getMaxParentsDepthWithQueueRecurion(sample, 0);
		return result;
	}
	
	this.repaint = function() {
		var localInstance = this;
		$('#'+this.containerId).empty();
		
		var $filtersForm = $('<form>' , { class : 'form-inline'});
		$filtersForm.submit(function(event) {localInstance._filterSampleAndUpdate(); event.preventDefault();});
		
		var maxChildren = this._getMaxChildrenDepth(this.sample);
		var	$filtersFormSliderChildren = null;
		if(maxChildren > 0) {
			$filtersFormSliderChildren = $('<input>' , { 'id' : 'childrenLimit' , 'type' : 'text' , 'class' : 'span2', 'value' : '' , 'data-slider-max' : maxChildren , 'data-slider-value' : maxChildren});
		} else {
			$filtersFormSliderChildren = 'No Children';
		}
		
		var maxParents = this._getMaxParentsDepth(this.sample);
		var	$filtersFormSliderParents = null;
		if(maxParents > 0) {
			$filtersFormSliderParents = $('<input>' , { 'id' : 'parentsLimit' , 'type' : 'text' , 'class' : 'span2', 'value' : '' , 'data-slider-max' : maxParents , 'data-slider-value' : maxParents});
		} else {
			$filtersFormSliderParents = 'No Parents';
		}
		
		var sampleTypes = this._getSampleTypes(this.sample);
		var	$filtersFormSampleTypes = $('<select>', { 'id' : 'sampleTypesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
		for (var sampleType in sampleTypes) {
			$filtersFormSampleTypes.append($('<option>', { 'value' : sampleType , 'selected' : ''}).html(sampleType));
		}
			
		
//		var $submitButton = $('<input>', { class : 'btn btn-primary', 'type' : 'submit', 'value' : 'Filter'});
		
		$filtersForm
			.append('<b>Filters</b>')
			.append("<span style='padding-right:15px;'></span>")
			.append('Children: ')
			.append($filtersFormSliderChildren)
			.append("<span style='padding-right:15px;'></span>")
			.append(' Parents: ')
			.append($filtersFormSliderParents)
			.append("<span style='padding-right:15px;'></span>")
			.append(' Types: ')
			.append($filtersFormSampleTypes);
//			.append('<span style='padding-right:15px;'></span>')
//			.append($submitButton);
		
		$('#'+this.containerId).append($filtersForm);
		$('#'+this.containerId).append($('<div>', { 'id' : 'graphContainer' }));
		
		$('#childrenLimit').slider();
		$('#childrenLimit').slider().on('slideStop', function(event){
			localInstance._filterSampleAndUpdate();
		});
		
		$('#parentsLimit').slider();
		$('#parentsLimit').slider().on('slideStop', function(event){
			localInstance._filterSampleAndUpdate();
		});
		
		$('#sampleTypesSelector').multiselect();
		$('#sampleTypesSelector').change(function(event){
			localInstance._filterSampleAndUpdate();
		});
		
		this._filterSampleAndUpdate();
	}
	
	
	
	this._filterSampleAndUpdate = function() {
		var newSample = jQuery.extend(true, {}, this.sample);
		
		var selectedSampleTypes = $('#sampleTypesSelector').val();
		if(selectedSampleTypes === null) {
			selectedSampleTypes = [];
		}
		var inArray = function(value, array) {
			for(var i = 0; i < array.length; i++) {
				if(array[i] === value) {
					return true;
				}
			}
			return false;
		}
		
		var selectedSampleTypesFilter = function(sample, selectedSampleTypes) {
			if(sample.parents) {
				var newParentsArray = [];
				for(var i = 0; i < sample.parents.length; i++) {
					if(inArray(sample.parents[i].sampleTypeCode, selectedSampleTypes)) {
						newParentsArray.push(sample.parents[i]);
						selectedSampleTypesFilter(sample.parents[i], selectedSampleTypes);
					}
				}
				sample.parents = newParentsArray;
			}
			if(sample.children) {
				var newChildrenArray = [];
				for(var i = 0; i < sample.children.length; i++) {
					if(inArray(sample.children[i].sampleTypeCode, selectedSampleTypes)) {
						newChildrenArray.push(sample.children[i]);
						selectedSampleTypesFilter(sample.children[i], selectedSampleTypes);
					}
				}
				sample.children = newChildrenArray;
			}
		}
		selectedSampleTypesFilter(newSample, selectedSampleTypes);
		
		var parentsLimit =  ($('#parentsLimit').length > 0)?$('#parentsLimit').data('slider').getValue():0;
		var parentsLimitFilter = function(sample, depthLimit) {
			if(sample.parents) {
				if(depthLimit === 0) {
					sample.parents = null;
				} else {
					for(var i = 0; i < sample.parents.length; i++) {
						parentsLimitFilter(sample.parents[i], (depthLimit - 1));
					}
				}
			}
		}
		parentsLimitFilter(newSample, parentsLimit);
		
		var childrenLimit = ($('#childrenLimit').length > 0)?$('#childrenLimit').data('slider').getValue():0;
		var childrenLimitFilter = function(sample, depthLimit) {
			if(sample.children) {
				if(depthLimit === 0) {
					sample.children = null;
				} else {
					for(var i = 0; i < sample.children.length; i++) {
						childrenLimitFilter(sample.children[i], (depthLimit - 1));
					}
				}
			}
		}
		childrenLimitFilter(newSample, childrenLimit);
		
		this._repaintGraph(newSample);
	}
	
	this._repaintGraph = function(sample) {
		$('#graphContainer').empty();
		$('#graphContainer').append("<svg><g transform='translate(20,20)'/></svg>");
		
		// Create a new directed graph
		var g = new dagreD3.Digraph();
		
		//Fill graph
		var NODES = {};
		function addSampleNodes(sample, permId) {
			if(!NODES[sample.permId]) {
				var sampleLink = "<a href=\"javascript:mainController.showViewSamplePageFromPermId('" + sample.permId + "');\">" + sample.code + "</a>";
				
				if(sample.permId === permId) {
					g.addNode(sample.permId, { label: "<div style='padding:10px; background-color:lightgreen;'>" + sample.sampleTypeCode + ':' + sampleLink + "</div>"});
				} else {
					g.addNode(sample.permId, { label: "<div style='padding:10px;'>" + sample.sampleTypeCode + ':' + sampleLink + "</div>"});
				}
				
				
				NODES[sample.permId] = true;
			}
			
			if(sample.parents) {
				sample.parents.forEach(addSampleNodes, permId);
			}
			if(sample.children) {
				sample.children.forEach(addSampleNodes, permId);
			}
		}
		
		var EDGES = {};
		
		function addSampleEdges(sample) {
			if(sample.parents) {
				for(var i=0; i < sample.parents.length; i++) {
					if(!EDGES[sample.parents[i].permId + ' -> ' + sample.permId]) {
						g.addEdge(null, sample.parents[i].permId, sample.permId);
						EDGES[sample.parents[i].permId + ' -> ' + sample.permId] = true;
					}
				}
				sample.parents.forEach(addSampleEdges);
			}
			if(sample.children) {
				for(var i=0; i < sample.children.length; i++) {
					if(!EDGES[sample.permId + ' -> ' + sample.children[i].permId]) {
						g.addEdge(null, sample.permId, sample.children[i].permId);
						EDGES[sample.permId + ' -> ' + sample.children[i].permId] = true;
					}
				}
				
				sample.children.forEach(addSampleEdges);
			}
		}
		
		addSampleNodes(sample, sample.permId);
		addSampleEdges(sample);
		
		// Render the directed graph
		var svg = d3.select('svg');
		var renderer = new dagreD3.Renderer();
		
		// Custom transition function
		function transition(selection) {
			return selection.transition().duration(500);
		}
		
		renderer.transition(transition);
		var layout = renderer.run(g, svg.select('g'));
		transition(d3.select('svg'))
			.attr('width', layout.graph().width + 40)
			.attr('height', layout.graph().height + 40)
			
		d3.select('svg')
		.call(d3.behavior.zoom().on('zoom', function() {
			var ev = d3.event;
			svg.select('g').attr('transform', 'translate(' + ev.translate + ') scale(' + ev.scale + ')');
		}));
	}
}
