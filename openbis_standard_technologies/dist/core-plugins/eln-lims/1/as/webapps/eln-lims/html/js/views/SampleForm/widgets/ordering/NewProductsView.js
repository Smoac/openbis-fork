function NewProductsView(newProductsController, newProductsModel) {
	this._newProductsController = newProductsController;
	this._newProductsModel = newProductsModel;
	
	this._$newProductsTableBody = $("<tbody>");
	
	this.repaint = function($container) {
		var _this = this;
		$container.empty();
		
		var $newProducts = $("<div>");
			$newProducts.append($("<legend>").append("Create and add new product"));
			
		var $newProductsTable = $("<table>", { class : "table table-bordered" });
		var $newProductsTableHead = $("<thead>");
		var $newProductsTableHeaders = $("<tr>")
											.append($("<th>").append("Name"))
											.append($("<th>").append("Catalog Num"))
											.append($("<th>").append("Price"))
											.append($("<th>").append("Currency"))
											.append($("<th>").append("Supplier"))
											.append($("<th>").append("Quantiy"))
											.append($("<th>").append(FormUtil.getButtonWithIcon("glyphicon-plus", function() {
												_this.addNewProduct(_this._$newProductsTableBody);
											})));
		
		$newProductsTable.append($newProductsTableHead.append($newProductsTableHeaders)).append(this._$newProductsTableBody);
		$newProducts.append($newProductsTable);
			
		$container.append($newProducts);
	}
	
	this.addNewProduct = function($newProductsTableBody) {
		mainController.serverFacade.searchWithType("SUPPLIER", null, false, function(suppliers){
			var supplierTerms = [];
			for(var sIdx = 0; sIdx < suppliers.length; sIdx++) {
				var name = suppliers[sIdx].properties["$NAME"];
				if(!name) {
					name = suppliers[sIdx].properties["NAME"];
				}
				supplierTerms.push({code : suppliers[sIdx].identifier, label : name });
			}
			var supplierDropdown = FormUtil.getDropDownForTerms(null, supplierTerms, "Select a supplier", true);
			
			var currencyVocabulary = profile.getVocabularyByCode("$PRODUCT.CURRENCY");
			var currencyDropdown = FormUtil.getDropDownForTerms(null, currencyVocabulary.terms, "Select a currency", false);
			
			var quantityField = FormUtil.getIntegerInputField(null, "Quantiy", true);
			quantityField.change(function() {
				var value = $(this).val();
				try {
					var valueParsed = parseInt(value);
					if("" + valueParsed === "NaN") {
						Util.showUserError("Please input a correct quantiy.");
						$(this).val("");
					} else {
						$(this).val(valueParsed);
					}
				} catch(err) {
					Util.showUserError("Please input a correct quantiy.");
					$(this).val("");
				}
			});
			
			var priceField = FormUtil.getRealInputField(null, "Price", false);
				priceField.change(function() {
					var value = $(this).val();
					if(value) {
						try {
							var valueParsed = parseFloat(value);
							if("" + valueParsed === "NaN") {
								Util.showUserError("Please input a correct price.");
								$(this).val("");
							} else {
								$(this).val(valueParsed);
							}
						} catch(err) {
							Util.showUserError("Please input a correct price.");
							$(this).val("");
						}
					}
				});
			
			var catalogNumField = FormUtil.getTextInputField(null, "Catalog Num", true);
			
			var nameField = FormUtil.getTextInputField(null, "Name", true);
			
			var $newProductsTableRow = $("<tr>")
			.append($("<td>").append(nameField))
			.append($("<td>").append(catalogNumField))
			.append($("<td>").append(priceField))
			.append($("<td>").append(currencyDropdown))
			.append($("<td>").append(supplierDropdown))
			.append($("<td>").append(quantityField))
			.append($("<td>").append(FormUtil.getButtonWithIcon("glyphicon-minus", function() {
				$(this).parent().parent().remove();
			})));
			
			$newProductsTableBody.append($newProductsTableRow);
		});
		
		
		
	}
	
}