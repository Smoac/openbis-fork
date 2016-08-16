function NewProductsView(newProductsController, newProductsModel) {
	this._newProductsController = newProductsController;
	this._newProductsModel = newProductsModel;
	
	var $newProductsTableBody = $("<tbody>");
	
	this.repaint = function($container) {
		$container.empty();
		
		var $newProducts = $("<div>");
			$newProducts.append($("<legend>").append("New Products"));
			
		var $newProductsTable = $("<table>", { class : "table table-bordered" });
		var $newProductsTableHead = $("<thead>");
		var $newProductsTableHeaders = $("<tr>")
											.append($("<th>").append("Name"))
											.append($("<th>").append("Code"))
											.append($("<th>").append("Price"))
											.append($("<th>").append("Currency"))
											.append($("<th>").append("Supplier"))
											.append($("<th>").append("Quantiy"))
											.append($("<th>").append(FormUtil.getButtonWithIcon("glyphicon-plus", this.addNewProduct)));
		
		$newProductsTable.append($newProductsTableHead.append($newProductsTableHeaders)).append($newProductsTableBody);
		$newProducts.append($newProductsTable);
			
		$container.append($newProducts);
		
	}
	
	this.addNewProduct = function() {

		
		mainController.serverFacade.searchWithType("SUPPLIER", null, false, function(suppliers){
			var supplierTerms = [];
			for(var sIdx = 0; sIdx < suppliers.length; sIdx++) {
				supplierTerms.push({code : suppliers[sIdx].code, label : suppliers[sIdx].properties["NAME"]});
			}
			var supplierDropdown = FormUtil.getDropDownForTerms(null, supplierTerms, "Select a supplier", true);
			
			var currencyVocabulary = profile.getVocabularyByCode("CURRENCY");
			var currencyDropdown = FormUtil.getDropDownForTerms(null, currencyVocabulary.terms, "Select a currency", true);
			
			var quantityField = FormUtil.getIntegerInputField(null, "Quantiy", true);
			quantityField.change(function() {
				var value = $(this).val();
				try {
					var valueParsed = parseInt(value);
					if("" + valueParsed === "NaN") {
						Util.showError("Please input a correct quantiy.");
						$(this).val("");
					} else {
						$(this).val(valueParsed);
					}
				} catch(err) {
					Util.showError("Please input a correct quantiy.");
					$(this).val("");
				}
			});
			
			var priceField = FormUtil.getRealInputField(null, "Price", true);
				priceField.change(function() {
					var value = $(this).val();
					try {
						var valueParsed = parseFloat(value);
						if("" + valueParsed === "NaN") {
							Util.showError("Please input a correct price.");
							$(this).val("");
						} else {
							$(this).val(valueParsed);
						}
					} catch(err) {
						Util.showError("Please input a correct price.");
						$(this).val("");
					}
				});
			
			var codeField = FormUtil.getTextInputField(null, "Code", true);
			
			var nameField = FormUtil.getTextInputField(null, "Name", true);
				nameField.change(function() {
					codeField.val($(this).val().toUpperCase().replace(" ","_"));
				});
			var $newProductsTableRow = $("<tr>")
			.append($("<td>").append(nameField))
			.append($("<td>").append(codeField))
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