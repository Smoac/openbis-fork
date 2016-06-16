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

function ExportTreeView(exportTreeController, exportTreeView) {
	var exportTreeController = exportTreeController;
	var exportTreeView = exportTreeView;
	
	this.repaint = function($container) {
		$container.empty();
		
		var $form = $("<div>", { "class" : "form-horizontal row"});
		var $formColumn = $("<div>", { "class" : FormUtil.formColumClass });
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append("Select Entities to Export");
		
		$formColumn.append($formTitle);
		$formColumn.append("<br>");
		
		var $tree = $("<div>", { "id" : "tree" });
		$formColumn.append($tree);
		
		$container.append($form);
		
		//
		//
		//
		
		var treeModel = [{ title : "openBIS", key : "ROOT:OPENBIS", folder : true, lazy : true }];
		
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
    	    
    	    var results = [{ title : "openBIS New", key : "ROOT:OPENBIS", folder : true, lazy : true }];
			dfd.resolve(results);
    	};
    	
    	$tree.fancytree({
        	extensions: ["dnd", "edit", "glyph"], //, "wide"
        	glyph: glyph_opts,
        	source: treeModel,
        	lazyLoad : onLazyLoad
        });
	}
}