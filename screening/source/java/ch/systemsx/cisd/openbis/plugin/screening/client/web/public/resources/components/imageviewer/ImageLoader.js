define([ "jquery" ], function($) {

	//
	// IMAGE LOADER
	//

	function ImageLoader() {
		this.init();
	}

	$.extend(ImageLoader.prototype, {

		init : function() {
			this.callbacks = [];
		},

		loadImage : function(imageData, callback) {
			var thisLoader = this;

			var url = imageData.dataStoreUrl + "/datastore_server_screening";
			url += "?sessionID=" + imageData.sessionToken;
			url += "&dataset=" + imageData.dataSetCode;
			url += "&channelStackId=" + imageData.channelStackId;

			imageData.channels.forEach(function(channel) {
				url += "&channel=" + channel;
			});

			if (imageData.resolution) {
				url += "&mode=thumbnail" + imageData.resolution;
			} else {
				url += "&mode=thumbnail480x480";
			}

			if (imageData.transformation) {

				if ("$USER_DEFINED_RESCALING$" == imageData.transformation) {
					var parametersMap = imageData.userDefinedTransformationParametersMap;
					var multipleChannels = imageData.channels.length > 1;

					for (channel in parametersMap) {
						if ($.inArray(channel, imageData.channels) != -1) {
							var blackPoint = parametersMap[channel].blackpoint;
							var whitePoint = parametersMap[channel].whitepoint;
							url += "&transformation";
							if (multipleChannels) {
								url += channel;
							}
							url += "=" + encodeURIComponent(imageData.transformation + "(" + blackPoint + "," + whitePoint + ")");
						}
					}

				} else {
					url += "&transformation=" + encodeURIComponent(imageData.transformation);
				}
			}

			if (callback) {
				this.callbacks.push(callback);
			}

			if (this.timeoutConfig) {
				clearTimeout(this.timeoutConfig.id);
			}

			var timeoutConfig = {};
			var timeout = function() {

				$("<img>").attr("src", timeoutConfig.url).load(function() {
					if (thisLoader.timeoutConfig && thisLoader.timeoutConfig.id === timeoutConfig.id) {
						var thisImage = this;

						thisLoader.callbacks.forEach(function(callback) {
							callback(thisImage);
						});

						thisLoader.timeoutConfig = null;
						thisLoader.callbacks = [];
					}
				});
			};

			timeoutConfig.id = setTimeout(timeout, 50);
			timeoutConfig.url = url;
			this.timeoutConfig = timeoutConfig;
		}

	});

	return ImageLoader;

});