define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetImage = function() {
	};
	stjs.extend(ImagingDataSetImage, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetImage';
		constructor.serialVersionUID = 1;
		prototype.previews = null;
		prototype.config = null;
		prototype.metaData = null;

		prototype.getPreviews = function() {
			return this.previews;
		};
		prototype.setPreviews = function(previews) {
			this.previews = previews;
		};
		prototype.getConfig = function() {
            return this.config;
        };
        prototype.setConfig = function(config) {
            this.config = config;
        };
		prototype.getMetaData = function() {
			return this.metaData;
		};
		prototype.setMetaData = function(metaData) {
			this.metaData = metaData;
		};
		prototype.toString = function() {
            return "ImagingDataSetImage: " + this.previews;
        };

	}, {
		previews : {
            name : "List",
            arguments : [ "ImagingDataSetPreview"]
        },
        config : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        },
        metaData : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        }
	});
	return ImagingDataSetImage;
})