define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var ImagingDataSetExport = function() {
	};
	stjs.extend(ImagingDataSetExport, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dss.dto.imaging.ImagingDataSetExport';
		constructor.serialVersionUID = 1;
		prototype.config = null;
		prototype.metaData = null;

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
            return "ImagingDataSetExport: " + this.config;
        };

	}, {
		config : {
            name : "Map",
            arguments : [ "String", "Serializable" ]
        },
        metaData : {
            name : "Map",
            arguments : [ "String", "String" ]
        }
	});
	return ImagingDataSetExport;
})