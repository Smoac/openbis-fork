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
			// log("loadImage: " + imageData.channelStackId);

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

			$("<img>").attr("src", url).load(function() {
				if (callback) {
					callback(this);
				}
			});
		}

	});

	return ImageLoader;

});