/*
 * Copyright 2016 ETH Zuerich, Scientific IT Services
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

function ExportTreeView(exportTreeController, exportTreeModel) {
	var exportTreeController = exportTreeController;
	var exportTreeModel = exportTreeModel;
	
	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		
		var $form = $("<div>");
		
		var $formColumn = $("<form>", {
			'role' : "form",
			'action' : 'javascript:void(0);',
			'onsubmit' : 'mainController.currentView.exportSelected();'
		});
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append("Export Builder");
		
		$header.append($formTitle);
		var $infoBox = FormUtil.getInfoBox("You can select any parts of the accesible openBIS structure to export:", [
		                                   "If you select a tree node and do not expand it, everything below this node will be exported by default.",
		                                   "To export selectively only parts of a tree, open the nodes and select what to export."
		]);
		$infoBox.css("border", "none");
		$header.append($infoBox);
		
		var $tree = $("<div>", { "id" : "exportsTree" });
		$formColumn.append($("<br>"));
		$formColumn.append(FormUtil.getBox().append($tree));
		
		$container.append($form);
		
		//
		//
		//
		
		var treeModel = [{ title : "/", entityType: "ROOT", key : "/", folder : true, lazy : true }];
		
		var glyph_opts = {
        	    map: {
        	      doc: "glyphicon glyphicon-file",
        	      docOpen: "glyphicon glyphicon-file",
        	      checkbox: "glyphicon glyphicon-unchecked",
        	      checkboxSelected: "glyphicon glyphicon-check",
        	      checkboxUnknown: "glyphicon glyphicon-share",
        	      dragHelper: "glyphicon glyphicon-play",
        	      dropMarker: "glyphicon glyphicon-arrow-right",
        	      error: "glyphicon glyphicon-warning-sign",
        	      expanderClosed: "glyphicon glyphicon-plus-sign",
        	      expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
        	      expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
        	      folder: "glyphicon glyphicon-folder-close",
        	      folderOpen: "glyphicon glyphicon-folder-open",
        	      loading: "glyphicon glyphicon-refresh"
        	    }
        };
    	
    	var onLazyLoad = function(event, data) {
    		var dfd = new $.Deferred();
    	    data.result = dfd.promise();
    	    var type = data.node.data.entityType;
    	    var permId = data.node.key;
    	    
    	    switch(type) {
    	    	case "ROOT":
    	    		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
    	    			var results = [];
    	                var spaces = searchResult.objects;
    	                for (var i = 0; i < spaces.length; i++) {
    	                    var space = spaces[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(space), entityType: "SPACE", key : space.code, folder : true, lazy : true });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "SPACE":
    	    		var projectRules = { "UUIDv4" : { type : "Attribute", name : "SPACE", value : permId } };
    	    		mainController.serverFacade.searchForProjectsAdvanced({ entityKind : "PROJECT", logicalOperator : "AND", rules : projectRules }, null, function(searchResult) {
    	    			var results = [];
    	                var projects = searchResult.objects;
    	                for (var i = 0; i < projects.length; i++) {
    	                    var project = projects[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(project), entityType: "PROJECT", key : project.permId, folder : true, lazy : true });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "PROJECT":
    	    		var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PROJECT_PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForExperimentsAdvanced({ entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules }, null, function(searchResult) {
    	    			var results = [];
    	                var experiments = searchResult.objects;
    	                for (var i = 0; i < experiments.length; i++) {
    	                    var experiment = experiments[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(experiment), entityType: "EXPERIMENT", key : experiment.permId, folder : true, lazy : true });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "EXPERIMENT":
    	    		var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, null, function(searchResult) {
    	    			var results = [];
    	                var samples = searchResult.objects;
    	                for (var i = 0; i < samples.length; i++) {
    	                    var sample = samples[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(sample), entityType: "SAMPLE", key : sample.permId, folder : true, lazy : true });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "SAMPLE":
    	    		var datasetRules = { "UUIDv4" : { type : "Sample", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
    	    			var results = [];
    	                var datasets = searchResult.objects;
    	                for (var i = 0; i < datasets.length; i++) {
    	                    var dataset = datasets[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(dataset), entityType: "DATASET", key : dataset.permId, folder : false, lazy : false });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "DATASET":
    	    		break;
    	    }
    	};
    	
    	exportTreeModel.tree = $tree.fancytree({
        	extensions: ["dnd", "edit", "glyph"], //, "wide"
        	checkbox: true,
        	selectMode: 2, // 1:single, 2:multi, 3:multi-hier
        	glyph: glyph_opts,
        	source: treeModel,
        	lazyLoad : onLazyLoad
        });
    	
    	var $exportButton = $("<input>", { "type": "submit", "class" : "btn btn-primary", 'value' : 'Export Selected' });
		$formColumn.append($("<br>"));
		$formColumn.append($exportButton);
	}
}