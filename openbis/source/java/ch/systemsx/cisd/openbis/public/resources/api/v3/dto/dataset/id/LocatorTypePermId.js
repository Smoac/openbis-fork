/**
 * Locator type perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/dataset/id/ILocatorTypeId" ], function(stjs, ObjectPermId, ILocatorTypeId) {
	/**
	 * @param permId
	 *            Locator type perm id, e.g. "RELATIVE_LOCATION".
	 */
	var LocatorTypePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(LocatorTypePermId, ObjectPermId, [ ObjectPermId, ILocatorTypeId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.id.LocatorTypePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return LocatorTypePermId;
})