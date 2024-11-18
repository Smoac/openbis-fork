define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetPropertyConfig = function() {
	};
	stjs.extend(ImagingDataSetPropertyConfig, null, [], function(constructor, prototype) {
		prototype['@type'] = 'imaging.dto.ImagingDataSetPropertyConfig';
		constructor.serialVersionUID = 1;
		prototype.images = null;

		prototype.getImages = function() {
            return this.images;
        };
        prototype.setSection = function(images) {
            this.images = images;
        };

		prototype.toString = function() {
            return "ImagingDataSetPropertyConfig: " + this.label;
        };

	}, {
		images : {
            name : "List",
            arguments : [ "ImagingDataSetImage"]
        }
	});
	return ImagingDataSetPropertyConfig;
})