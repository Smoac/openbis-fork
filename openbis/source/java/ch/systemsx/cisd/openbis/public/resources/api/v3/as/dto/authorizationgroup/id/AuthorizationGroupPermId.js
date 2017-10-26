define([ "stjs", "as/dto/common/id/ObjectPermId", "as/dto/authorizationgroup/id/IAuthorizationGroupId" ], function(stjs, ObjectPermId, IAuthorizationGroupId) {
	/**
	 * @param permId
	 *            Tag perm id, e.g. "/MY_USER/MY_TAG".
	 */
	var AuthorizationGroupPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(AuthorizationGroupPermId, ObjectPermId, [ ObjectPermId, IAuthorizationGroupId ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationgroup.id.AuthorizationGroupPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return AuthorizationGroupPermId;
})