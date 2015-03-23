function PublishFormService(openbis) {
	this.init(openbis);
}

$.extend(PublishFormService.prototype, {
	init : function(openbis) {
		this.facade = openbis;
	},

	handleResponse : function(callback) {
		return function(response) {
			var operationResult = new OperationResult();

			if (response.error) {
				var message = null;

				if (_.isString(response.error)) {
					message = response.error;
				} else if (_.isObject(response.error)) {
					message = JSON.stringify(response.error);
				}

				operationResult.setSuccessful(false);
				operationResult.addMessage("error", message);
			} else {
				operationResult.setSuccessful(true);
				operationResult.setResult(response.result);
			}

			callback(operationResult);
		}
	},

	loadDataStoreCode : function(callback) {
		if (this.dataStoreCode) {
			var loadResult = new OperationResult();
			loadResult.setSuccessful(true);
			loadResult.setResult(this.dataStoreCode);
			callback(loadResult);
			return;
		}

		var thisModel = this;

		var listCallback = function(listResult) {
			if (listResult.isSuccessful()) {
				var services = listResult.getResult();
				var dataStoreCodes = [];

				if (services && services.length > 0) {
					services.forEach(function(service) {
						if (service.serviceKey == "publish-logic") {
							dataStoreCodes.push(service.dataStoreCode);
						}
					});
				}

				var loadResult = new OperationResult();

				if (dataStoreCodes.length == 0) {
					loadResult.setSuccessful(false);
					loadResult.addMessage("error", "Reporting plugin 'publish-logic' hasn't been found. "
							+ "Please check whether the data store server is running and the core plugin has been properly configured.");
				} else if (dataStoreCodes.length == 1) {
					loadResult.setSuccessful(true);
					loadResult.setResult(dataStoreCodes[0])
				} else {
					loadResult.setSuccessful(true);
					loadResult.setMessage("warning", "Reporting plugin 'publish-logic' has been configured at multiple data stores. " + "Plugin from '" + dataStoreCodes[0]
							+ "' data store will be used.");
					loadResult.setResult(dataStoreCodes[0]);
				}

				if (loadResult.isSuccessful()) {
					thisModel.dataStoreCode = loadResult.getResult();
				}

				callback(loadResult);
			} else {
				listResult.addMessage("error", "Couldn't get a list of aggregation services");
				callback(listResult);
			}
		};

		this.getFacade().listAggregationServices(thisModel.handleResponse(listCallback));
	},

	executeOnDataStore : function(method, methodParameters, callback) {
		var thisModel = this;

		var parameters = {}
		parameters["method"] = method;
		parameters["methodParameters"] = methodParameters;

		var createCallback = function(createResult) {
			if (createResult.isSuccessful()) {
				var result = createResult.getResult();
				var reportResult = result.rows[0][0].value;

				if (reportResult) {
					try {
						reportResult = JSON.parse(reportResult);
					} catch (err) {
						// do nothing
					}
				}

				var executeResult = new OperationResult();
				executeResult.setSuccessful(true);
				executeResult.setResult(reportResult);
				callback(executeResult);

			} else {
				callback(createResult);
			}
		};

		var loadCallback = function(loadResult) {
			if (loadResult.isSuccessful()) {
				var dataStoreCode = loadResult.getResult();
				var facade = thisModel.getFacade();

				// call DSS directly (not via AS) to have
				// an HTTP session in the ingestion service
				facade._internal.getDataStoreApiUrlForDataStoreCode(dataStoreCode, function(dataStoreApiUrl) {
					facade._internal.ajaxRequest({
						url : dataStoreApiUrl,

						// necessary for a cookie received from a DSS CORS
						// response to be sent back in a request
						xhrFields : {
							withCredentials : true
						},
						data : {
							"method" : "createReportFromAggregationService",
							params : [ facade.getSession(), "publish-logic", parameters ]
						},
						success : thisModel.handleResponse(createCallback)
					});
				});
			} else {
				callback(loadResult);
			}
		};

		this.loadDataStoreCode(loadCallback);
	},

	getSpaces : function(callback) {
		this.executeOnDataStore("getSpaces", {}, function(operationResult) {
			if (!operationResult.isSuccessful()) {
				operationResult.addMessage("error", "Couldn't load spaces.");
			}
			callback(operationResult);
		});
	},

	getLicenses : function(callback) {
		var thisModel = this;

		var listCallback = function(listResult) {
			if (listResult.isSuccessful()) {
				var vocabularies = listResult.getResult();
				var vocabulariesMap = {};

				vocabularies.forEach(function(vocabulary) {
					vocabulariesMap[vocabulary.code] = vocabulary;
				});

				var getResult = new OperationResult();
				var licenses = vocabulariesMap["LICENSE"];

				if (licenses) {
					getResult.setSuccessful(true);
					getResult.setResult(licenses);
					callback(getResult);
				} else {
					getResult.setSuccessful(false);
					getResult.addMessage("error", "No 'LICENSE' controlled vocabulary found.");
					callback(getResult);
				}
			} else {
				listResult.addMessage("error", "Could't load licenses.");
				callback(listResult);
			}
		};

		this.getFacade().listVocabularies(thisModel.handleResponse(listCallback));
	},

	getMeshTermChildren : function(parent, callback) {
		var parameters = {
			"parent" : parent
		};
		this.executeOnDataStore("getMeshTermChildren", parameters, function(operationResult) {
			if (operationResult.isSuccessful()) {
				var meshTerms = operationResult.getResult();

				meshTerms.forEach(function(meshTerm) {
					// create jquery friendly id
					meshTerm.id = meshTerm.identifier.replace(/\W+/g, "_");
				});
			} else {
				operationResult.addMessage("error", "Couldn't load mesh terms .");
			}
			callback(operationResult);
		});
	},

	publish : function(data, callback) {
		this.executeOnDataStore("publish", data, function(executeResult) {
			if (executeResult.isSuccessful()) {
				executeResult.addMessage("success", executeResult.getResult());
			} else {
				executeResult.addMessage("error", "Publication failed.");
			}
			callback(executeResult);
		});
	},

	getFacade : function() {
		return this.facade;
	}

});
