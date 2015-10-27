/**
 * @author pkupczyk
 */
define([ "stjs", "dto/entity/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var PhysicalDataUpdate = function() {
		this.fileFormatTypeId = new FieldUpdateValue();
	};
	stjs.extend(PhysicalDataUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.dataset.PhysicalDataUpdate';
		constructor.serialVersionUID = 1;

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