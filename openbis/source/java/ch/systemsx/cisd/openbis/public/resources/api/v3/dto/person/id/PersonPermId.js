/**
 * Person perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/person/id/IPersonId" ], function(stjs, ObjectPermId, IPersonId) {

	/**
	 * @param permId
	 *            Person perm id, e.g. "admin".
	 */
	var PersonPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(PersonPermId, ObjectPermId, [ ObjectPermId, IPersonId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.person.id.PersonPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return PersonPermId;
})