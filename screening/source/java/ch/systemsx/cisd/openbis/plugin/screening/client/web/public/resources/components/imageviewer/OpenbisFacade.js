define([ "jquery" ], function($) {

	//
	// FACADE
	//

	function OpenbisFacade(openbis) {
		this.init(openbis);
	}

	$.extend(OpenbisFacade.prototype, {
		init : function(openbis) {
			this.openbis = openbis;
		},

		getSession : function() {
			return this.openbis.getSession();
		},

		getDataStoreBaseURLs : function(dataSetCodes, action) {
			this.openbis.getDataStoreBaseURLs(dataSetCodes, function(response) {
				var dataSetCodeToUrlMap = {};

				if (response.result) {
					response.result.forEach(function(urlForDataSets) {
						urlForDataSets.dataSetCodes.forEach(function(dataSetCode) {
							dataSetCodeToUrlMap[dataSetCode] = urlForDataSets.dataStoreURL;
						});
					});
					response.result = dataSetCodeToUrlMap;
				}

				action(response);
			});
		},

		getImageInfo : function(dataSetCodes, callback) {
			this.openbis.getImageInfo(dataSetCodes, callback);
		},

		getImageResolutions : function(dataSetCodes, callback) {
			this.openbis.getImageResolutions(dataSetCodes, callback);
		}
	});

	return OpenbisFacade;

});