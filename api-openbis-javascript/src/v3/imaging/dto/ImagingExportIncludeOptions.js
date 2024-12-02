define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ImagingExportIncludeOptions = function() {
		Enum.call(this, [ "IMAGE", "RAW_DATA" ]);
	};
	stjs.extend(ImagingExportIncludeOptions, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ImagingExportIncludeOptions();
})