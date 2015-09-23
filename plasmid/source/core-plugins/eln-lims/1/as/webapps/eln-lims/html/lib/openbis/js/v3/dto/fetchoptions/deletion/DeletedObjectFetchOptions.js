/**
 * @author pkupczyk
 */
define([ "stjs", "dto/fetchoptions/EmptyFetchOptions" ], function(stjs, EmptyFetchOptions) {
	var DeletedObjectFetchOptions = function() {
	};
	stjs.extend(DeletedObjectFetchOptions, EmptyFetchOptions, [ EmptyFetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.deletion.DeletedObjectFetchOptions';
		constructor.serialVersionUID = 1;
	}, {});
	return DeletedObjectFetchOptions;
})