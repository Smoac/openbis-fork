/*
 * Copyright 2013 ETH Zuerich, CISD
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

function SampleHierarchi(serverFacade, inspector, containerId, profile, sample) {
	this.serverFacade = serverFacade;
	this.inspector = inspector;
	this.containerId = containerId;
	this.profile = profile;
	this.sample = sample;

	this.init = function() {
		this.repaint();
	}
	
	this.repaint = function() {
		$("#"+this.containerId).empty();
		$("#"+this.containerId).append("<svg><g transform='translate(20,20)'/></svg>");
		
		// Create a new directed graph
		var g = new dagreD3.Digraph();
		
		//Fill graph
		var NODES = {};
		function addSampleNodes(sample) {
			if(!NODES[sample.permId]) {
				g.addNode(sample.permId,    { label: sample.identifier });
				NODES[sample.permId] = true;
			}
			
			if(sample.parents) {
				sample.parents.forEach(addSampleNodes);
			}
			if(sample.children) {
				sample.children.forEach(addSampleNodes);
			}
		}
		
		var EDGES = {};
		function addSampleEdges(sample) {
			if(sample.parents) {
				for(var i=0; i < sample.parents.length; i++) {
					if(!EDGES[sample.permId + " -> " + sample.parents[i].permId]) {
						g.addEdge(null, sample.permId, sample.parents[i].permId);
						EDGES[sample.permId + " -> " + sample.parents[i].permId] = true;
					}
				}
				sample.parents.forEach(addSampleEdges);
			}
			if(sample.children) {
				for(var i=0; i < sample.children.length; i++) {
					if(!EDGES[sample.permId + " -> " + sample.children[i].permId]) {
						g.addEdge(null, sample.permId, sample.children[i].permId);
						EDGES[sample.permId + " -> " + sample.children[i].permId] = true;
					}
				}
				
				sample.children.forEach(addSampleEdges);
			}
		}
		
		addSampleNodes(this.sample);
		addSampleEdges(this.sample);
		
		// Render the directed graph
		var svg = d3.select("svg");
		var renderer = new dagreD3.Renderer();
		
		// Custom transition function
		function transition(selection) {
			return selection.transition().duration(500);
		}
		
		renderer.transition(transition);
		var layout = renderer.run(g, svg.select("g"));
		transition(d3.select("svg"))
			.attr("width", layout.graph().width + 40)
			.attr("height", layout.graph().height + 40)
			
		d3.select("svg")
		.call(d3.behavior.zoom().on("zoom", function() {
			var ev = d3.event;
			svg.select("g").attr("transform", "translate(" + ev.translate + ") scale(" + ev.scale + ")");
		}));
	}
}
