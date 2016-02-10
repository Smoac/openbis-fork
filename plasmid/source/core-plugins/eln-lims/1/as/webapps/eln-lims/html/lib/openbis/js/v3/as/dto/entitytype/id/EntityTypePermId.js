/**
 * Entity type perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/entitytype/id/IEntityTypeId" ], function(stjs, ObjectPermId, IEntityTypeId) {
	/**
	 * @param permId
	 *            Entity type perm id, e.g. "MY_ENTITY_TYPE".
	 */
	var EntityTypePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(EntityTypePermId, ObjectPermId, [ ObjectPermId, IEntityTypeId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.entitytype.id.EntityTypePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return EntityTypePermId;
})