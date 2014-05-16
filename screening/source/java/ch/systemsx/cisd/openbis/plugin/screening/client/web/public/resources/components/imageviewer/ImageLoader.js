define([ "jquery" ], function($) {

	//
	// IMAGE LOADER
	//

	function ImageLoader() {
		this.init();
	}

	$.extend(ImageLoader.prototype, {

		init : function() {
		},

		loadImage : function(imageData, callback) {
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

				// TODO duplicated constant (see TransformationChooserWidget.js)

				if ("$USER_DEFINED_RESCALING$" == imageData.transformation) {

					var channelToParametersMap = {};

					imageData.transformationParameters.forEach(function(parameter) {
						var channelParameters = channelToParametersMap[parameter.channel];

						if (!channelParameters) {
							channelParameters = {};
							channelToParametersMap[parameter.channel] = channelParameters;
						}

						channelParameters[parameter.name] = parameter;
					});

					var multipleChannels = Object.keys(channelToParametersMap).length > 1;

					for (channel in channelToParametersMap) {
						var blackPoint = channelToParametersMap[channel]["blackpoint"].value;
						var whitePoint = channelToParametersMap[channel]["whitepoint"].value;
						url += "&transformation";
						if (multipleChannels) {
							url += channel;
						}
						url += "=" + encodeURIComponent(imageData.transformation + "(" + blackPoint + "," + whitePoint + ")");
					}

				} else {
					url += "&transformation=" + encodeURIComponent(imageData.transformation);
				}
			}

			$("<img>").attr("src", url).load(function() {
				if (callback) {
					callback(this);
				}
			});
		}

	});

	return ImageLoader;

});