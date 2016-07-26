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

function OrdersView(ordersController, ordersModel) {
	var ordersController = ordersController;
	var ordersModel = ordersModel;
	
	this.repaint = function($container) {
		$container.empty();
		
		var $form = $("<div>", { "class" : "form-horizontal row"});
		var $formColumn = $("<div>", { "class" : FormUtil.formColumClass });
			
		$form.append($formColumn);
		
		var $formTitle = $("<h2>").append("Orders");
		
		//
		// Toolbar
		//
		var toolbarModel = [];
		
		var $request = FormUtil.getButtonWithIcon("glyphicon-plus", function() {}, "Request");
		toolbarModel.push({ component : $request, tooltip: "Create Request" });
		var $order = FormUtil.getButtonWithIcon("glyphicon-plus", function() {}, "Order");
		toolbarModel.push({ component : $order, tooltip: "Create Order" });
		
		
		$formColumn.append($formTitle);
		$formColumn.append(FormUtil.getToolbar(toolbarModel));
		$formColumn.append("<br>");
		
		$container.append($form);
	}
}