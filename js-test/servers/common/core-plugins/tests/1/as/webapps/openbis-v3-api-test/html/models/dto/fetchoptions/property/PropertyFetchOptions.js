/**
 * @author pkupczyk
 */
define([ "support/stjs" ], function(stjs) {
	var PropertyFetchOptions = function() {
	};
	stjs.extend(PropertyFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.property.PropertyFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return PropertyFetchOptions;
})