/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var PhysicalDataUpdate = function() {
		this.fileFormatTypeId = new FieldUpdateValue();
	};
	stjs.extend(PhysicalDataUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.PhysicalDataUpdate';
		constructor.serialVersionUID = 1;

		prototype.fileFormatTypeId = null;

		prototype.getFileFormatTypeId = function() {
			return this.fileFormatTypeId;
		};
		prototype.setFileFormatTypeId = function(fileFormatTypeId) {
			this.fileFormatTypeId.setValue(fileFormatTypeId);
		};
	}, {
		fileFormatTypeId : {
			name : "FieldUpdateValue",
			arguments : [ "IFileFormatTypeId" ]
		}
	});
	return PhysicalDataUpdate;
})