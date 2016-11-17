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
		var $tbody = this._newProductsView._$newProductsTableBody;
		var $trList = $tbody.children();
		var sampleType = profile.getSampleTypeForSampleTypeCode("PRODUCT");
		mainController.serverFacade.generateCode(sampleType, function(nextCode) {
			var codePrefix = sampleType.codePrefix;
			var nextCodeNumber = parseInt(nextCode.substring(codePrefix.length));
			
			var products = [];
			for(var trIdx = 0; trIdx < $trList.length; trIdx++) {
				var $productRow = $($trList[trIdx]);
				var $productProperties = $($productRow.children());
				var newProduct = {
						permId : "PERM_ID_PLACEHOLDER_FOR/STOCK_CATALOG/" + codePrefix + nextCodeNumber,
						sampleTypeCode : "PRODUCT",
						experimentIdentifierOrNull : "/STOCK_CATALOG/PRODUCTS/PRODUCT_COLLECTION",
						identifier : "/STOCK_CATALOG/" + codePrefix + nextCodeNumber, 
						code : codePrefix + nextCodeNumber,
						parentsIdentifiers : [$($($productProperties[4]).children()[0]).val()],
						properties : {
							NAME : $($($productProperties[0]).children()[0]).val(),
							PRICE_PER_UNIT : $($($productProperties[2]).children()[0]).val(),
							CURRENCY : $($($productProperties[3]).children()[0]).val(),
							CATALOG_NUM : $($($productProperties[1]).children()[0]).val()
						},
						annotations : {
							QUANTITY_OF_ITEMS : $($($productProperties[5]).children()[0]).val()
						}
				}
				products.push(newProduct);
				nextCodeNumber++;
			}
			
			//When done for all new products, execute the action to submit the form
			action(sample, products);
		});
	}
}