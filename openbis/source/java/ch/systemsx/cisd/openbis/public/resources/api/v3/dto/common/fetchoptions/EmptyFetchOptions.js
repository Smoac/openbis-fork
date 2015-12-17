define([ "stjs", "dto/common/fetchoptions/FetchOptions" ], function(stjs, FetchOptions) {
	var EmptyFetchOptions = function() {
		FetchOptions.call(this);
	};
	stjs.extend(EmptyFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.fetchoptions.EmptyFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sortBy = function() {
			return null;
		};
		prototype.getSortBy = function() {
			return null;
		};
	}, {});
	return EmptyFetchOptions;
})