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
			.append($filtersFormSampleTypes);
		
		$('#'+this.containerId).append($filtersForm);
		$('#'+this.containerId).append($('<div>', { 'id' : 'graphContainer' }));
		$('#graphContainer').append("<svg><g transform='translate(20,20)'/></svg>");
		
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
		
		//
		// Used to remove the type label when rendering
		//
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
				for(var i = 0; i < sample.parents.length; i++) {
					sample.parents[i].showLabel = inArray(sample.parents[i].sampleTypeCode, selectedSampleTypes);
					selectedSampleTypesFilter(sample.parents[i], selectedSampleTypes);
				}
				
			}
			if(sample.children) {
				for(var i = 0; i < sample.children.length; i++) {
					sample.children[i].showLabel = inArray(sample.children[i].sampleTypeCode, selectedSampleTypes);
					selectedSampleTypesFilter(sample.children[i], selectedSampleTypes);
				}
			}
		}
		selectedSampleTypesFilter(newSample, selectedSampleTypes);
		
		//
		// Used to cut the tree
		//
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
		
		//
		// Used to cut the tree
		//
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
	
	this._updateDataFor = function(show, permId) {
		var searchAndUpdateData = function(show, permId, sample) {
			if(sample.permId === permId) {
				sample.showDataOnGraph = show;
			} else {
				if(sample.parents) {
					for(var i = 0; i < sample.parents.length; i++) {
						searchAndUpdateData(show, permId, sample.parents[i]);
					}
				}
				
				if(sample.children) {
					for(var i = 0; i < sample.children.length; i++) {
						searchAndUpdateData(show, permId, sample.children[i]);
					}
				}
			}
		}
		
		searchAndUpdateData(show, permId, this.sample);
		this._filterSampleAndUpdate();
	}
	
	this._updateDisplayabilityFor = function(hide, permId) {
		var searchAndUpdateDisplayability = function(hide, permId, sample) {
			if(sample.permId === permId) {
				sample.hideGraphConnections = hide;
			} else {
				if(sample.parents) {
					for(var i = 0; i < sample.parents.length; i++) {
						searchAndUpdateDisplayability(hide, permId, sample.parents[i]);
					}
				}
				
				if(sample.children) {
					for(var i = 0; i < sample.children.length; i++) {
						searchAndUpdateDisplayability(hide, permId, sample.children[i]);
					}
				}
			}
		}
		
		searchAndUpdateDisplayability(hide, permId, this.sample);
		this._filterSampleAndUpdate();
	}
	
	this._repaintGraph = function(sample) {
		var g = new dagreD3.Digraph();
		
		//Fill graph
		var NODES = {};
		
		var _this = this;
		function addSampleNodes(sample, rootPermId) {
			if(!NODES[sample.permId]) {
				var $nodeContent = $('<div>');
				$nodeContent.css({
					'white-space' :'nowrap',
					'padding' : '10px',
					'background-color' : (sample.permId === rootPermId)?'lightgreen':'transparent',
					'border-radius' : '90px'
				});
				
				var $hideLink = $('<a>', {
					'href' : "javascript:mainController.sampleHierarchy._updateDisplayabilityFor(" + ((sample.hideGraphConnections)?false:true) + ",'" + sample.permId + "');"
				}).append(
					$('<i>', { 
						'class' : (sample.hideGraphConnections)?'icon-eye-close':'icon-eye-open',
						'style' : 'cursor:pointer',
					}));
				
				var $dataLink = $('<a>', {
					'href' : "javascript:mainController.sampleHierarchy._updateDataFor(" + ((sample.showDataOnGraph)?false:true) + ",'" + sample.permId + "');"
				}).append(
					$('<i>', {
						'class' : (sample.showDataOnGraph)?'icon-minus-sign':'icon-plus-sign',
						'style' : 'cursor:pointer',
					}));
				
				var $sampleLink = $('<a>', { 'href' : "javascript:mainController.showViewSamplePageFromPermId('" + sample.permId + "')"}).html(sample.code);
				
				
				
				if(sample.showLabel || (sample.permId === rootPermId)) {
					$nodeContent
						.append($hideLink)
						.append($dataLink)
						.append(sample.sampleTypeCode + ':')
						.append($sampleLink);
				
				if(sample.showDataOnGraph) {
					var optionalInspectorTitle = $nodeContent[0].outerHTML;
					var $inspector = _this.inspector.getInspectorTable(sample, false, true, false, optionalInspectorTitle, true);
					
					$nodeContent.empty();
					$nodeContent.css({
						'background-color' : 'transparent'
					});
					$nodeContent.append($inspector);
				}
					
				} else {
					$nodeContent.append('---');
				}
				
				g.addNode(sample.permId, { label: $nodeContent[0].outerHTML});
				
				NODES[sample.permId] = true;
			}
			
			if(sample.parents && !sample.hideGraphConnections) {
				sample.parents.forEach(addSampleNodes, rootPermId);
			}
			if(sample.children && !sample.hideGraphConnections) {
				sample.children.forEach(addSampleNodes, rootPermId);
			}
		}
		
		var EDGES = {};
		
		function addSampleEdges(sample) {
			if(sample.parents && !sample.hideGraphConnections) {
				for(var i=0; i < sample.parents.length; i++) {
					if(!EDGES[sample.parents[i].permId + ' -> ' + sample.permId]) {
						g.addEdge(null, sample.parents[i].permId, sample.permId);
						EDGES[sample.parents[i].permId + ' -> ' + sample.permId] = true;
					}
				}
				sample.parents.forEach(addSampleEdges);
			}
			if(sample.children && !sample.hideGraphConnections) {
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
		
		var layout = dagreD3.layout()
							.nodeSep(20)
							.rankDir("TB");
		
		var layout = renderer.layout(layout).run(g, svg.select('g'));
		transition(d3.select('svg'))
			.attr('width', $(document).width() - 30)
			.attr('height', $(document).height() - 120)
		
		d3.select('svg')
		.call(d3.behavior.zoom().on('zoom', function() {
			var ev = d3.event;
			svg.select('g').attr('transform', 'translate(' + ev.translate + ') scale(' + ev.scale + ')');
		}));
	}
}
