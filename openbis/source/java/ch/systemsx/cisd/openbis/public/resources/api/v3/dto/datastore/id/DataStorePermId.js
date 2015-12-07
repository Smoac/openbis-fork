/**
 * Data store perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/datastore/id/IDataStoreId" ], function(stjs, ObjectPermId, IDataStoreId) {

	/**
	 * @param permId
	 *            Data store perm id, e.g. "DSS1".
	 */
	var DataStorePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(DataStorePermId, ObjectPermId, [ ObjectPermId, IDataStoreId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.datastore.id.DataStorePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return DataStorePermId;
})