/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var LinkedDataUpdate = function() {
		this.externalCode = new FieldUpdateValue();
		this.externalDmsId = new FieldUpdateValue();
	};
	stjs.extend(LinkedDataUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.update.LinkedDataUpdate';
		constructor.serialVersionUID = 1;

		prototype.externalCode = null;
		prototype.externalDmsId = null;

		prototype.getExternalCode = function() {
			return this.externalCode;
		};
		prototype.setExternalCode = function(externalCode) {
			this.externalCode.setValue(externalCode);
		};
		prototype.getExternalDmsId = function() {
			return this.externalDmsId;
		};
		prototype.setExternalDmsId = function(externalDmsId) {
			this.externalDmsId.setValue(externalDmsId);
		};
	}, {
		externalCode : {
			name : "FieldUpdateValue",
			arguments : [ "String" ]
		},
		externalDmsId : {
			name : "FieldUpdateValue",
			arguments : [ "IExternalDmsId" ]
		}
	});
	return LinkedDataUpdate;
})