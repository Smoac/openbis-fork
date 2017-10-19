
function StandardProfile(serverFacade) {
	this.init(serverFacade);
}

$.extend(StandardProfile.prototype, DefaultProfile.prototype, {
	init: function(serverFacade){
		DefaultProfile.prototype.init.call(this, serverFacade);
		
		this.hideCodes = true;
		
		this.storagesConfiguration = { "isEnabled" : false };
	
		/* New Sample definition tests*/
		this.sampleTypeDefinitionsExtension = {
				"ANTIBODY" : {
					"ENABLE_STORAGE" : true,
				},
				
				"CHEMICAL" : {
					"ENABLE_STORAGE" : true,
				},
				
				"ENZYME" : {
					"ENABLE_STORAGE" : true,
				},
				
				"OLIGO" : {
					"ENABLE_STORAGE" : true,
				},
				
				"RNA" : {
					"ENABLE_STORAGE" : true,
				},
				
				"MEDIA" : {
					"ENABLE_STORAGE" : true,
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
					"ENABLE_STORAGE" : true,
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
					"USE_AS_PROTOCOL" : true,
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
					"USE_AS_PROTOCOL" : true,
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
					"USE_AS_PROTOCOL" : true,
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
					"ENABLE_STORAGE" : true,
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
					"ENABLE_STORAGE" : true,
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
					"ENABLE_STORAGE" : true,
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
				},

				"CELL_LINE" : {
					"ENABLE_STORAGE" : true,
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
					"ENABLE_STORAGE" : true,
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
				"STORAGE" : {
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_DISABLED" : true,
				},
				"STORAGE_POSITION" : {
					"SAMPLE_CHILDREN_DISABLED" : true
				},
				"REQUEST" : {
					"SAMPLE_PARENTS_TITLE" : "Products from Catalog",
					"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
					"SAMPLE_CHILDREN_DISABLED" : true,
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Products",
						"TYPE": "PRODUCT",
						"MIN_COUNT" : 1,
						"MAX_COUNT" : 1,
						"ANNOTATION_PROPERTIES" : [{"TYPE" : "QUANTITY_OF_ITEMS", "MANDATORY" : true }, {"TYPE" : "COMMENTS", "MANDATORY" : false }]
					}]
				},
				"ORDER" : {
					"SAMPLE_PARENTS_TITLE" : "Requests",
					"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
					"SAMPLE_CHILDREN_DISABLED" : true,
					showParents : function(sample) { 
						var orderStatus = sample.properties["ORDER_STATUS"];
						var orderSummary = sample.properties["ORDER_STATE"];
						return orderStatus !== "ORDERED" && orderStatus !== "DELIVERED" && orderStatus !== "PAID" && !orderSummary;
					},
					"SAMPLE_PARENTS_HINT" : [{
						"LABEL" : "Requests",
						"TYPE": "REQUEST",
						"MIN_COUNT" : 0,
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
		
		this.sampleFormOnSubmit = function(sample, action) {
			if(sample.sampleTypeCode === "ORDER") {
				var orderStatus = sample.properties["ORDER_STATUS"];
				var samplesToDelete = null;
				var changesToDo = null;
				if((orderStatus === "ORDERED" || orderStatus === "DELIVERED" || orderStatus === "PAID") && !sample.properties["ORDER_STATE"]) {
					//Set property
					sample.properties["ORDER_STATE"] = window.btoa(unescape(encodeURIComponent(JSON.stringify(sample))));
					//Update order state on the requests
					changesToDo = [];
					var requests = sample.parents;
					if(requests) {
						for(var rIdx = 0; rIdx < requests.length; rIdx++) {
							changesToDo.push({ "permId" : requests[rIdx].permId, "identifier" : requests[rIdx].identifier, "properties" : {"ORDER_STATUS" : orderStatus } });
						}
					}
				}
				action(sample, null, samplesToDelete, changesToDo);
			} else if(sample.sampleTypeCode === "REQUEST") {
				mainController.currentView._newProductsController.createAndAddToForm(sample, action);
			} else if(action) {
				action(sample);
			}
		}
		
		this.sampleFormContentExtra = function(sampleTypeCode, sample, containerId) {
			if(sampleTypeCode === "EXPERIMENTAL_STEP") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				var freeFormTableController = new FreeFormTableController(sample, isEnabled);
				freeFormTableController.init($("#" + containerId));
			} else if(sampleTypeCode === "ORDER") {
				var isExisting = mainController.currentView._sampleFormModel.mode === FormMode.VIEW;
				var isFromState = false;
				if(isExisting) {
					//
					// Data Structures to Help the reports functionality
					//
					var order = mainController.currentView._sampleFormModel.sample;
					if(order.properties["ORDER_STATE"]) {
						isFromState = true;
						order = JSON.parse(decodeURIComponent(escape(window.atob(order.properties["ORDER_STATE"]))));
					}
					
					var requests = order.parents;
					var providerByPermId = {};
					var productsByProviderPermId = {};
					var quantityByProductPermId = {};
					var absoluteTotalByCurrency = {};
					
					//
					// Fills data structures
					//
					for(var rIdx = 0; rIdx < requests.length; rIdx++) {
						var request = requests[rIdx];
						var requestProductsAnnotations = FormUtil.getAnnotationsFromSample(request);
						var requestProducts = request.parents;
						for(var pIdx = 0; pIdx < requestProducts.length; pIdx++) {
							var requestProduct = requestProducts[pIdx];
							var requestProductAnnotations = requestProductsAnnotations[requestProduct.permId];
							
							if(requestProduct.parents.length === 0) {
								Util.showError("Product " + requestProduct.code + " does not have a provider, FIX IT!.");
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
							quantity += parseInt(requestProductAnnotations["QUANTITY_OF_ITEMS"]);
							if(!quantity) {
								Util.showError("Product " + requestProduct.code + " from request " +  request.code + " does not have a quantity, FIX IT!.");
								return;
							}
							var currencyCode = requestProduct.properties["CURRENCY"];
							if(!currencyCode) {
								currencyCode = "N/A";
							}
							
							var absoluteTotalForCurrency = absoluteTotalByCurrency[currencyCode];
							if(!absoluteTotalForCurrency) {
								absoluteTotalForCurrency = 0;
							}
							
							if(requestProduct.properties["PRICE_PER_UNIT"]) {
								absoluteTotalForCurrency += parseFloat(requestProduct.properties["PRICE_PER_UNIT"]) * quantity;
							}
							
							absoluteTotalByCurrency[currencyCode] = absoluteTotalForCurrency;
							
							quantityByProductPermId[requestProduct.permId] = quantity;
							providerProducts.push(requestProduct);
						}
					}
					
					//
					// Button that prints an order report
					//
					var printOrder = FormUtil.getButtonWithIcon("glyphicon-print",function() {
						//Create an order page for each provider
						var orderPages = [];
						for(var providerPermId in productsByProviderPermId) {
							var provider = providerByPermId[providerPermId];
							var preferredSupplierLanguage = provider.properties["COMPANY_LANGUAGE"];
							
							var languageLabels = profile.orderLanguage[preferredSupplierLanguage];
							if(!languageLabels) {
								languageLabels = profile.orderLanguage["ENGLISH"];
							}
							
							var providerProducts = productsByProviderPermId[providerPermId];
							
							var registrationDate = null;
							if(order.registrationDetails && order.registrationDetails.modificationDate) {
								registrationDate = order.registrationDetails.modificationDate;
							} else if(mainController.currentView._sampleFormModel.sample.registrationDetails && 
									mainController.currentView._sampleFormModel.sample.registrationDetails.modificationDate) {
								registrationDate = mainController.currentView._sampleFormModel.sample.registrationDetails.modificationDate;
							}
							
							var page = languageLabels["ORDER_FORM"];
								page += "\n";
								page += "\n";
								page += languageLabels["ORDER_INFORMATION"];
								page += "\n";
								page += "- " + languageLabels["ORDER_DATE"] + ": " + Util.getFormatedDate(new Date(registrationDate));
								page += "\n";
								page += "- " + languageLabels["ORDER_STATUS"] + ": " + order.properties["ORDER_STATUS"];
								page += "\n";
								page += "- " + languageLabels["ORDER_CODE"] + ": " + order.code;
								page += "\n";
								page += "\n";
								page += "\n";
								page += languageLabels["COSTUMER_INFORMATION"];
								page += "\n";
								if(order.properties["SHIP_TO"]) {
									page += "- " + languageLabels["SHIP_TO"] + ": " + order.properties["SHIP_TO"];
									page += "\n";
								}
								if(order.properties["BILL_TO"]) {
									page += "- " + languageLabels["BILL_TO"] + ": " + order.properties["BILL_TO"];
									page += "\n";
								}
								if(order.properties["SHIP_ADDRESS"]) {
									page += "- " + languageLabels["SHIP_ADDRESS"] + ": " + order.properties["SHIP_ADDRESS"];
									page += "\n";
								}
								if(order.properties["CONTACT_PHONE"]) {
									page += "- " + languageLabels["PHONE"] + ": " + order.properties["CONTACT_PHONE"];
									page += "\n";
								}
								if(order.properties["CONTACT_FAX"]) {
									page += "- " + languageLabels["FAX"] + ": " + order.properties["CONTACT_FAX"];
									page += "\n";
								}
								page += "\n";
								page += "\n";
								page += languageLabels["SUPPLIER_INFORMATION"];
								page += "\n";
								if(provider.properties["NAME"]) {
									page += "- " + languageLabels["SUPPLIER"] + ": " + provider.properties["NAME"];
									page += "\n";
								}
								if(provider.properties["COMPANY_ADDRESS_LINE_1"]) {
									page += "- " + languageLabels["SUPPLIER_ADDRESS_LINE_1"] + ": " + provider.properties["COMPANY_ADDRESS_LINE_1"]
									page += "\n";
								}
								if(provider.properties["COMPANY_ADDRESS_LINE_2"]) {
									page += "  " + languageLabels["SUPPLIER_ADDRESS_LINE_2"] + "  " + provider.properties["COMPANY_ADDRESS_LINE_2"]
									page += "\n";
								}
								if(provider.properties["COMPANY_PHONE"]) {
									page += "- " + languageLabels["SUPPLIER_PHONE"] + ": " + provider.properties["COMPANY_PHONE"];
									page += "\n";
								}
								if(provider.properties["COMPANY_FAX"]) {
									page += "- " + languageLabels["SUPPLIER_FAX"] + ": " + provider.properties["COMPANY_FAX"];
									page += "\n";
								}
								if(provider.properties["COMPANY_EMAIL"]) {
									page += "- " + languageLabels["SUPPLIER_EMAIL"] + ": " + provider.properties["COMPANY_EMAIL"];
									page += "\n";
								}
								if(provider.properties["CUSTOMER_NUMBER"]) {
									page += "- " + languageLabels["CUSTOMER_NUMBER"] + ": " + provider.properties["CUSTOMER_NUMBER"];
									page += "\n";
								}
								page += "\n";
								page += "\n";
								page += languageLabels["REQUESTED_PRODUCTS_LABEL"];
								page += "\n";
								page += languageLabels["PRODUCTS_COLUMN_NAMES_LABEL"];
								page += "\n";
								var providerTotalByCurrency = {};
								for(var pIdx = 0; pIdx < providerProducts.length; pIdx++) {
									var product = providerProducts[pIdx];
									var quantity = quantityByProductPermId[product.permId];
									var unitPriceAsString = product.properties["PRICE_PER_UNIT"];
									var unitPrice = "N/A";
									if(unitPriceAsString) {
										unitPrice = parseFloat(unitPriceAsString);
									}
									var currencyCode = product.properties["CURRENCY"];
									if(!currencyCode) {
										currencyCode = "N/A";
									}
									page += quantity + "\t\t" + product.properties["NAME"] + "\t\t" + product.properties["CATALOG_NUM"] + "\t\t" + unitPrice + " " + currencyCode;
									page += "\n";
									
									if(unitPriceAsString) {
										var totalForCurrency = providerTotalByCurrency[product.properties["CURRENCY"]];
										if(!totalForCurrency) {
											totalForCurrency = 0;
										}
										totalForCurrency += unitPrice * quantity;
										
										providerTotalByCurrency[product.properties["CURRENCY"]] = totalForCurrency;
									} else {
										providerTotalByCurrency[product.properties["CURRENCY"]] = "N/A";
									}
								}
								page += "\n";
								page += "\n";
								page += "\n";
								
								var showTotals = false;
								for(var currency in providerTotalByCurrency) {
									if(providerTotalByCurrency[currency] > 0) {
										showTotals = true;
									}
								}
								if(showTotals) {
									page += languageLabels["PRICE_TOTALS_LABEL"] + ":";
									page += "\n";
									for(var currency in providerTotalByCurrency) {
										page += providerTotalByCurrency[currency] + " " + currency;
										page += "\n";
									}
									page += "\n";
									page += "\n";
									page += "\n";
								}
								page += languageLabels["ADDITIONAL_INFO_LABEL"];
								page += "\n";
								var additionalInfo = order.properties["ADDITIONAL_INFORMATION"];
								if(!additionalInfo) {
									additionalInfo = "N/A";
								}
								page += additionalInfo;
							orderPages.push(page);
						}
						
						//Print Pages
						for(var pageIdx = 0; pageIdx < orderPages.length; pageIdx++) {
							Util.downloadTextFile(orderPages[pageIdx], "order_" + sample.code + "_p" + pageIdx + ".txt");
						}
						
					}, "Print Order");
					
					//
					// Order Summary Grid
					//
					var columns = [ {
						label : 'Catalog Num',
						property : 'catalogNum',
						isExportable: true,
						sortable : true,
						render : function(data) {
							return FormUtil.getFormLink(data.catalogNum, "Sample", data.permId);
						}
					},{
						label : 'Supplier',
						property : 'supplier',
						isExportable: true,
						sortable : true
					}, {
						label : 'Name',
						property : 'name',
						isExportable: true,
						sortable : true
					}, {
						label : 'Quantity',
						property : 'quantity',
						isExportable: true,
						sortable : true,
					}, {
						label : 'Unit Price',
						property : 'unitPrice',
						isExportable: true,
						sortable : true
					}, {
						label : 'Total Product Cost',
						property : 'totalProductCost',
						isExportable: true,
						sortable : true
					}, {
						label : 'Currency',
						property : 'currency',
						isExportable: true,
						sortable : true
					}];
					
					var getDataRows = function(callback) {
						var rows = [];
						for(var providerPermId in productsByProviderPermId) {
							var provider = providerByPermId[providerPermId];
							var providerProducts = productsByProviderPermId[providerPermId];
							for(var pIdx = 0; pIdx < providerProducts.length; pIdx++) {
								var product = providerProducts[pIdx];
								var quantity = quantityByProductPermId[product.permId];
								var unitPriceAsString = product.properties["PRICE_PER_UNIT"];
								var unitPrice = "N/A";
								if(unitPriceAsString) {
									unitPrice = parseFloat(unitPriceAsString);
								}
								var rowData = {};
								rowData.permId = product.permId;
								rowData.supplier = provider.properties["NAME"];
								rowData.name = product.properties["NAME"];
								rowData.catalogNum =  product.properties["CATALOG_NUM"];
								rowData.quantity = quantity;
								rowData.unitPrice = unitPrice;
								if(unitPrice !== "N/A") {
									rowData.totalProductCost = rowData.quantity * rowData.unitPrice;
								} else {
									rowData.totalProductCost = "N/A";
								}
								var currencyCode = product.properties["CURRENCY"];
								if(!currencyCode) {
									currencyCode = "N/A";
								}
								rowData.currency = currencyCode;
								rows.push(rowData);
							}
							
						}
						callback(rows);
					};
					
					var orderSummaryContainer = $("<div>");
					var repTitle = "Order Summary";
					if(isFromState) {
						repTitle += " (as saved when ordered)"
					}
					
					var orderSummary = new DataGridController(repTitle, columns, [], null, getDataRows, null, false, "ORDER_SUMMARY");
					orderSummary.init(orderSummaryContainer);
					
					var totalsByCurrencyContainer = $("<div>").append($("<br>")).append($("<legend>").append("Total:"));
					for(var currency in absoluteTotalByCurrency) {
						totalsByCurrencyContainer.append(absoluteTotalByCurrency[currency] + " " + currency).append($("<br>"));
					}
					$("#" + containerId).append(orderSummaryContainer).append(totalsByCurrencyContainer).append($("<br>")).append(printOrder);
				}
			} else if(sampleTypeCode === "REQUEST") {
				var isEnabled = mainController.currentView._sampleFormModel.mode !== FormMode.VIEW;
				if(isEnabled) {
					var $newProductsController = new NewProductsController();
						$newProductsController.init($("#" + containerId));
						mainController.currentView._newProductsController = $newProductsController;
				}
			}
		}
		

		this.dataSetTypeForFileNameMap = [
				{ fileNameExtension : "fasta", dataSetType : "SEQ_FILE" },
		];

}
});