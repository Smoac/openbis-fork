/**
 * Data set perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/dataset/id/IDataSetId" ], function(stjs, ObjectPermId, IDataSetId) {
	/**
	 * @param permId
	 *            Data set perm id, e.g. "201108050937246-1031".
	 */
	var DataSetPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(DataSetPermId, ObjectPermId, [ ObjectPermId, IDataSetId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.id.DataSetPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return DataSetPermId;
})