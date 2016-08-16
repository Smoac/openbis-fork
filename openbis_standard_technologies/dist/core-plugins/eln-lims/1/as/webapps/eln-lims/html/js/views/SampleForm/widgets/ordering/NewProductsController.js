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

function NewProductsController() {
	this._newProductsModel = new NewProductsModel();
	this._newProductsView = new NewProductsView(this, this._newProductsModel);
	
	this.init = function($container) {
		this._newProductsView.repaint($container);
	}
	
	this.createAndAddToForm = function(sample, action) {
		//TO-DO
		//1. Create Product
		
		//2. Add to sample parents widget with the quantity annotation
		
		//3. Add to the attribute sample.parents
		
		//When done for all new products, execute the action to submit the form
		action();
	}
}