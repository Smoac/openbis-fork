
function StandardProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(StandardProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		
		this.hideCodes = true;
		
		this.storagesConfiguration = {
				"isEnabled" : true,
				"storageSpaceLowWarning" : 0.8, //Storage goes over 80%
				"boxSpaceLowWarning" : 0.8, //Box goes over 80%
				/*
				 * Should be the same across all storages, if not correct behaviour is not guaranteed.
				 */
				"STORAGE_PROPERTIES": [{
					"STORAGE_PROPERTY_GROUP" : "Physical Storage", //Where the storage will be painted.
					"STORAGE_GROUP_DISPLAY_NAME" : "Physical Storage", //Storage Group Name
					"NAME_PROPERTY" : 		"STORAGE_NAMES", //Should be a Vocabulary.
					"ROW_PROPERTY" : 		"STORAGE_ROW", //Should be an integer.
					"COLUMN_PROPERTY" : 	"STORAGE_COLUMN",  //Should be an integer.
					"BOX_PROPERTY" : 		"STORAGE_BOX_NAME", //Should be text.
					"BOX_SIZE_PROPERTY" : 	"STORAGE_BOX_SIZE", //Should be Vocabulary.
					"USER_PROPERTY" : 		"STORAGE_USER", //Should be text.
					"POSITION_PROPERTY" : 	"STORAGE_POSITION" //Should be text.
				}],
				/*
				 * Storages map, can hold configurations for several storages.
				 */
				"STORAGE_CONFIGS": {
					"BENCH" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.BOX_POSITION, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 1 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					},
					"DEFAULT_STORAGE" : { //Freezer name given by the NAME_PROPERTY
						"VALIDATION_LEVEL" : ValidationLevel.BOX_POSITION, //When non present it defaults to BOX_POSITION
						"ROW_NUM" : 1, //Number of rows
						"COLUMN_NUM" : 1, //Number of columns
						"BOX_NUM" : 9999 //Boxes on each rack, used for validation, to avoid validation increase the number to 9999 for example
					}
				}
			};
	
		/* New Sample definition tests*/
		this.sampleTypeDefinitionsExtension = {
				"MEDIA" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																								
												],
				},

				"SOLUTION_BUFFER" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																								
												],
				},

				"GENERAL_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "General protocol",
														"TYPE": "GENERAL_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																												
												],
				},

				"PCR_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},																								
												],
				},

				"WESTERN_BLOTTING_PROTOCOL" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : false }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},																								
												],
				},

				"PLASMID" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																		
												],
				},

				"BACTERIA" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Bacteria parents",
														"TYPE": "BACTERIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																				
												],
				},

				"YEAST" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Yeast parents",
														"TYPE": "YEAST",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																					
												],
					"SAMPLE_LINKS_HINT" : [
												{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false },{"TYPE" : "CONTAINED", "MANDATORY" : false }]
												}
										]
				},

				"CELL_LINE" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Parental cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},		
					                             	{
														"LABEL" : "Parental fly",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																									
												],
				},

				"FLY" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Fly parents",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																					
												],
				},
				"REQUEST" : {
					"SAMPLE_PARENTS_TITLE" : "Products",
					"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Products",
						"TYPE": "PRODUCT",
						"MIN_COUNT" : 1,
						"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY", "MANDATORY" : true }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
					}]
				},
				"ORDER" : {
					"SAMPLE_PARENTS_TITLE" : "Requests",
					"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Requests",
						"TYPE": "REQUEST",
						"MIN_COUNT" : 1,
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"SUPPLIER" : {
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_DISABLED" : true,
				},
				"PRODUCT" : {
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_TITLE" : "Suppliers",
					"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Suppliers",
						"TYPE": "SUPPLIER",
						"MIN_COUNT" : 1,
						"MAX_COUNT" : 1,
						"ANNOTATION_PROPERTIES" : []
					}]
				},
				"EXPERIMENTAL_STEP" : {
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [ {"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Bacteria",
														"TYPE": "BACTERIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Fly",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Oligo",
														"TYPE": "OLIGO",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "RNA",
														"TYPE": "RNA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Yeast",
														"TYPE": "YEAST",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "General protocol",
														"TYPE": "GENERAL_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "PCR protocol",
														"TYPE": "PCR_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Western blotting protocol",
														"TYPE": "WESTERN_BLOTTING_PROTOCOL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "COMMENTS", "MANDATORY" : false }]
													}																																			
												],
				}
		
		} 
		
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "EXPERIMENTAL_STEP") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				var freeFormTableController = new FreeFormTableController(sample, isEnabled);
				freeFormTableController.init($("#" + containerId));
			} else if(sampleTypeCode === "ORDER") {
				var isExisting = mainController.currentView._sampleFormModel.mode === FormMode.VIEW;
				if(isExisting) {
					var order = mainController.currentView._sampleFormModel.sample;
					var requests = order.parents;
					var providerByPermId = {};
					var productsByProviderPermId = {};
					var quantityByProductPermId = {};
					var printOrder = FormUtil.getButtonWithIcon("glyphicon-print",function() {
						
						//1. Group products from all requests by provider and obtain their quantities from the annotations
						for(var rIdx = 0; rIdx < requests.length; rIdx++) {
							var request = requests[rIdx];
							var requestProductsAnnotations = FormUtil.getAnnotationsFromSample(request);
							var requestProducts = request.parents;
							for(var pIdx = 0; pIdx < requestProducts.length; pIdx++) {
								var requestProduct = requestProducts[pIdx];
								var requestProductAnnotations = requestProductsAnnotations[requestProduct.permId];
								
								if(requestProduct.parents.length === 0) {
									Util.showError("Product " + requestProduct.code + " don't have a provider, FIX IT!.");
									return;
								}
								var provider = requestProduct.parents[0];
								var providerProducts = productsByProviderPermId[provider.permId];
								if(!providerProducts) {
									providerProducts = [];
									productsByProviderPermId[provider.permId] = providerProducts;
									providerByPermId[provider.permId] = provider;
								}
								var quantity = quantityByProductPermId[requestProduct.permId];
								if(!quantity) {
									quantity = 0;
								}
								quantity += parseInt(requestProductAnnotations["QUANTITY"]);
								if(!quantity) {
									Util.showError("Product " + requestProduct.code + " from request " +  request.code + " don't have a quantity, FIX IT!.");
									return;
								}
								quantityByProductPermId[requestProduct.permId] = quantity;
								providerProducts.push(requestProduct);
							}
						}
						
						//2. Create an order page for each provider
						var orderPages = [];
						for(var providerPermId in productsByProviderPermId) {
							var provider = providerByPermId[providerPermId];
							var providerProducts = productsByProviderPermId[providerPermId];
							var page  = "Order for supplier: " + provider.properties["COMPANY_NAME"];
								page += "\n";
								page += "Contact Info:";
								page += "\n";
								page += "- Supplier address: " + provider.properties["COMPANY_ADDRESS"];
								page += "\n";
								page += "- Supplier fax: " + provider.properties["COMPANY_FAX"];
								page += "\n";
								page += "- Supplier phone: " + provider.properties["COMPANY_PHONE"];
								page += "\n";
								page += "- Supplier email: " + provider.properties["COMPANY_EMAIL"];
								page += "\n";
								page += "Other Info:";
								page += "\n";
								page += "- Account No: " + provider.properties["ACCOUNT_NUMBER"];
								page += "\n";
								page += "- Preferred Language: " + provider.properties["COMPANY_LANGUAGE"];
								page += "\n";
								page += "- Preferred Order Method: " + provider.properties["PREFERRED_ORDER_METHOD"];
								page += "\n";
								page += "\n";
								page += "Requested Products:";
								page += "\n";
								page += "Name\tCode\tQuantity\tUnit Price\tCurrency";
								page += "\n";
								var totalByCurrency = {};
								for(var pIdx = 0; pIdx < providerProducts.length; pIdx++) {
									var product = providerProducts[pIdx];
									var quantity = quantityByProductPermId[product.permId];
									var unitPrice = parseFloat(product.properties["PRICE_PER_UNIT"]);
									page += product.properties["PRODUCT_MAIN_NAME"] + "\t" + product.properties["CATALOG_CODE"] + "\t" + quantity + "\t" + product.properties["PRICE_PER_UNIT"] + "\t" + product.properties["CURRENCY"];
									page += "\n";
									var totalForCurrency = totalByCurrency[product.properties["CURRENCY"]];
									if(!totalForCurrency) {
										totalForCurrency = 0;
									}
									totalForCurrency += unitPrice * quantity;
									totalByCurrency[product.properties["CURRENCY"]] = totalForCurrency;
								}
								page += "\n";
								page += "Price Totals by currency:"
								page += "\n";
								for(var currency in totalByCurrency) {
									page += totalByCurrency[currency] + " " + currency;
									page += "\n";
								}
								page += "-------------------------------------------------------------------";
								page += "\n";
							orderPages.push(page);
						}
						
						//3. Print Pages
						var completeOrder = "";
						for(var pageIdx = 0; pageIdx < orderPages.length; pageIdx++) {
							completeOrder += orderPages[pageIdx];
						}
						Util.downloadTextFile(completeOrder, "order.txt");
					}, "Print Order");
					$("#" + containerId).append(printOrder);
				}
			}
		}
		
		this.getDataSetTypeForFileName = function(allDatasetFiles, fileName) {
			if(fileName.endsWith("fasta")) {
				return "SEQ_FILE";
			} else {
				return null;
			}
		}
}
});