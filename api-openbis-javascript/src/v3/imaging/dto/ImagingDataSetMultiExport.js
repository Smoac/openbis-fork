define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetMultiExport = function() {
	};
	stjs.extend(ImagingDataSetMultiExport, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetMultiExport';
		constructor.serialVersionUID = 1;
		prototype.permId = null;
        prototype.imageIndex = null;
        prototype.previewIndex = null;
		prototype.config = null;
		prototype.metadata = null;

        prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getImageIndex = function() {
            return this.imageIndex;
        };
        prototype.setImageIndex = function(imageIndex) {
            this.imageIndex = imageIndex;
        };
        prototype.getPreviewIndex = function() {
            return this.previewIndex;
        };
        prototype.setPreviewIndex = function(previewIndex) {
            this.previewIndex = previewIndex;
        };
		prototype.getConfig = function() {
			return this.config;
		};
		prototype.setConfig = function(config) {
			this.config = config;
		};
		prototype.getMetadata = function() {
			return this.metadata;
		};
		prototype.setMetadata = function(metadata) {
			this.metadata = metadata;
		};
		prototype.toString = function() {
            return "ImagingDataSetExport: " + this.config;
        };

	}, {
        metadata : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        }
	});
	return ImagingDataSetMultiExport;
})