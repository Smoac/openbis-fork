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
			var thisFacade = this;

			this.openbis.getDatasetIdentifiers(dataSetCodes, function(response) {
				if (response.error) {
					callback(response);
				} else {
					thisFacade.openbis.listAvailableImageRepresentationFormats(response.result, function(response) {
						if (response.error) {
							callback(response);
						} else {
							var map = {};

							response.result.forEach(function(result) {
								var formats = result.imageRepresentationFormats;

								thisFacade._maybeAddThumbnailRepresentationFormat(formats);
								thisFacade._removeDuplicatedRepresentationFormats(formats);
								thisFacade._sortRepresentationFormats(formats);

								map[result.dataset.datasetCode] = formats;
							});

							callback({
								"error" : null,
								"result" : map
							});
						}
					});
				}
			});
		},

		_maybeAddThumbnailRepresentationFormat : function(formats) {
			var hasThumbnails = formats.some(function(format) {
				return false == format.original;
			});

			if (false == hasThumbnails) {
				var originals = formats.filter(function(format) {
					return format.original;
				});

				if (originals && originals.length > 0) {
					var original = originals[0];

					if (original.width && original.height) {
						formats.push({
							"original" : false,
							"width" : Math.round(original.width / 4),
							"height" : Math.round(original.height / 4)
						});
					}
				}
			}
		},

		_removeDuplicatedRepresentationFormats : function(formats) {
			var resolutions = {};
			var newFormats = [];

			formats.forEach(function(format, index) {
				var resolution = format.width + "x" + format.height;
				if (!resolutions[resolution]) {
					resolutions[resolution] = format;
					newFormats.push(format);
				}
			});

			formats.splice(0, formats.length);
			Array.prototype.push.apply(formats, newFormats);
		},

		_sortRepresentationFormats : function(formats) {
			formats.sort(function(o1, o2) {
				var compare = function(v1, v2) {
					if (v1 > v2) {
						return 1;
					} else if (v1 < v2) {
						return -1;
					} else {
						return 0;
					}
				}

				return compare(o1.width, o2.width) * 10 + compare(o1.height, o2.height);
			});
		}

	});

	return OpenbisFacade;

});