/**
 * Relative location perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/dataset/id/LocatorTypePermId" ], function(stjs, LocatorTypePermId) {
	var RelativeLocationLocatorTypePermId = function() {
		LocatorTypePermId.call(this, "RELATIVE_LOCATION");
	};
	stjs.extend(RelativeLocationLocatorTypePermId, LocatorTypePermId, [ LocatorTypePermId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.id.RelativeLocationLocatorTypePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return RelativeLocationLocatorTypePermId;
})