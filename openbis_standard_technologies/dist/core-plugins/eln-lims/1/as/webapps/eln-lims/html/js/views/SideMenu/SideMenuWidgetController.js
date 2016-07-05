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

/**
 * Creates an instance of SideMenuWidget.
 *
 * @constructor
 * @this {SideMenuWidgetController}
 * @param {MainController} mainController Used to control view changes.
 */
function SideMenuWidgetController(mainController) {
    this._mainController = mainController;
    this._sideMenuWidgetModel = new SideMenuWidgetModel();
    this._sideMenuWidgetView = new SideMenuWidgetView(this, this._sideMenuWidgetModel);
    
    //
    // External API for real time updates
    //
    
    this.hideSideMenu = function() {
        this._sideMenuWidgetView.hideSideMenu();
    };

    this.showSideMenu = function() {
        this._sideMenuWidgetView.showSideMenu();
    };
    
    this.getCurrentNodeId = function() {
    	return (this._sideMenuWidgetModel.selectedNodeData)?this._sideMenuWidgetModel.selectedNodeData.key:null;
    };
    
    this.deleteNodeByEntityPermId = function(key, isMoveToParent) {
    	var node = $(this._sideMenuWidgetModel.tree).fancytree('getTree').getNodeByKey(key);
    	if(isMoveToParent) {
    		var parent = node.getParent();
    		this._showNodeView(parent);
    	}
    	node.remove();
    };
    
    this.refreshCurrentNode = function() {
    	this._refreshNode(this.getCurrentNodeId());
    }
    
    this.refreshNodeParent = function(key) {
    	var node = $(this._sideMenuWidgetModel.tree).fancytree('getTree').getNodeByKey(key);
    	if(node) {
    		var parent = node.getParent();
    		if(parent) {
    			this._refreshNode(parent.key);
    		}
    	}
    }
    
    this.moveToNodeId = function(uniqueId) {
    	// Can't be implemented initially, it will be required to keep the whole menu path to know all parents that need to be loaded 
    }
    
    //
    // Init method that builds the menu object hierarchy
    //
    this.init = function($container, initCallback) {
    	this._sideMenuWidgetModel.$container = $container;
        var _this = this;
        
        _this._sideMenuWidgetView.repaint($container);
		
        $(window).scroll(function(event) {
            var sideMenuWidth = $("#sideMenu").width();
            var windowWidth = $(window).width();
            var ratio = sideMenuWidth / windowWidth;
            if (ratio < 0.9) { //For small screens where the menu takes all the screen, we don't move it
                var $element = $("#sideMenu");
                var scrollTop = $(document).scrollTop();
                $element.css('top', scrollTop + "px");
            }
        });

        $(window).resize(function(event) {
            //Use all height
            var $element = $("#sideMenu");
            var windowHeight = $(window).height();
            $element.css('min-height', windowHeight + "px");

            //Set the children of the menu scrollable
            var $elementHead = $("#sideMenuHeader");
            var sideMenuHeaderHeight = $elementHead.height();
            var $elementBody = $("#sideMenuBody");
            $elementBody.css('overflow-y', 'auto');
            $elementBody.css('overflow-x', 'hidden');
            //TO-DO 17px is a hack to be able to scroll properly to the last item on the iPad
            $elementBody.css('max-height', (windowHeight - sideMenuHeaderHeight - 17) + "px");
        });
        
        initCallback();
    }
    
    this._showNodeView = function(node) {
		if(node.data.view) {
			var viewData =  node.data.viewData;
			if(!viewData) {
				viewData = null;
			}
			mainController.changeView(node.data.view, viewData);
			this._sideMenuWidgetModel.selectedNodeData = {
				key : node.key,
				view : node.data.view,
				viewData : node.data.viewData
			};
		}
    }
    
    this._refreshNode = function(key) {
    	var node = $(this._sideMenuWidgetModel.tree).fancytree('getTree').getNodeByKey(key);
    	if(node) {
    		node.removeChildren();
        	node.resetLazy();
        	node.setExpanded(true);
    	}
    }
}

function SideMenuWidgetComponent(isSelectable, isTitle, displayName, uniqueId, parent, newMenuIfSelected, newViewIfSelected, newViewIfSelectedData, contextTitle) {
    this.isSelectable = isSelectable;
    this.isTitle = isTitle;
    this.displayName = displayName;
    this.uniqueId = uniqueId;
    this.contextTitle = contextTitle;
    this.parent = parent;
    this.newMenuIfSelected = newMenuIfSelected;
    this.newViewIfSelected = newViewIfSelected;
    this.newViewIfSelectedData = newViewIfSelectedData;
}

var naturalSortSideMenuWidgetComponent = function(componentA, componentB){
  	return naturalSort(componentA.displayName, componentB.displayName);
}