/**
 * @author pkupczyk
 */
define([ "stjs", "dto/fetchoptions/EmptyFetchOptions" ], function(stjs, EmptyFetchOptions) {
	var PropertyFetchOptions = function() {
	};
	stjs.extend(PropertyFetchOptions, EmptyFetchOptions, [ EmptyFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.property.PropertyFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyFetchOptions;
})