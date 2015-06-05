/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/entity/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var ExternalDataUpdate = function() {
		this.fileFormatTypeId = new FieldUpdateValue();
	};
	stjs.extend(ExternalDataUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.dataset.ExternalDataUpdate';
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
	return ExternalDataUpdate;
})