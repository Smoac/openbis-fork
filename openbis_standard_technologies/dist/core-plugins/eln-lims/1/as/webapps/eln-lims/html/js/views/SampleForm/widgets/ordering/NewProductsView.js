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
											.append($("<th>").append("Provider"))
											.append($("<th>").append("Quantiy"))
											.append($("<th>").append(FormUtil.getButtonWithIcon("glyphicon-plus", this.addNewProduct)));
		
		$newProductsTable.append($newProductsTableHead.append($newProductsTableHeaders)).append($newProductsTableBody);
		$newProducts.append($newProductsTable);
			
		$container.append($newProducts);
		
	}
	
	this.addNewProduct = function() {
		var currencyVocabulary = profile.getVocabularyByCode("CURRENCY");
		var currencyDropdown = FormUtil.getDropDownForTerms(null, currencyVocabulary.terms, "Currency", true);
		
		var $newProductsTableRow = $("<tr>")
		.append($("<td>").append(FormUtil.getTextInputField(null, "Name", true)))
		.append($("<td>").append(FormUtil.getTextInputField(null, "Code", true)))
		.append($("<td>").append(FormUtil.getRealInputField(null, "Price", true)))
		.append($("<td>").append(currencyDropdown))
		.append($("<td>").append(FormUtil.getIntegerInputField(null, "Provider Code", true)))
		.append($("<td>").append(FormUtil.getIntegerInputField(null, "Quantiy", true)))
		.append($("<td>").append(FormUtil.getButtonWithIcon("glyphicon-minus", function() {
			$(this).parent().parent().remove();
		})));
		
		$newProductsTableBody.append($newProductsTableRow);
	}
	
}