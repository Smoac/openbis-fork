/**
 * File format type perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/dataset/id/IFileFormatTypeId" ], function(stjs, ObjectPermId, IFileFormatTypeId) {
	/**
	 * @param permId
	 *            File format type perm id, e.g. "PROPRIETARY".
	 */
	var FileFormatTypePermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(FileFormatTypePermId, ObjectPermId, [ ObjectPermId, IFileFormatTypeId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.id.FileFormatTypePermId';
		constructor.serialVersionUID = 1;
	}, {});
	return FileFormatTypePermId;
})