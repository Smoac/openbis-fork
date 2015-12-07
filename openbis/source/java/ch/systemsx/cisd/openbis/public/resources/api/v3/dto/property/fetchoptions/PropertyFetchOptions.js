/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/fetchoptions/EmptyFetchOptions" ], function(stjs, EmptyFetchOptions) {
	var PropertyFetchOptions = function() {
	};
	stjs.extend(PropertyFetchOptions, EmptyFetchOptions, [ EmptyFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.property.fetchoptions.PropertyFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyFetchOptions;
})