define([ "support/stjs", "dto/fetchoptions/FetchOptions" ], function(stjs, FetchOptions) {
	var EmptyFetchOptions = function() {
		FetchOptions.call(this);
	};
	stjs.extend(EmptyFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.EmptyFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return EmptyFetchOptions;
})