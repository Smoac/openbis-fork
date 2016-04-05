define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyType = function() {
	};
	stjs.extend(PropertyType, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyType';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.label = null;
		prototype.description = null;
		prototype.dataTypeCode = null;
		prototype.internalNameSpace = null;
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label = label;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getDataTypeCode = function() {
			return this.dataTypeCode;
		};
		prototype.setDataTypeCode = function(dataTypeCode) {
			this.dataTypeCode = dataTypeCode;
		};
		prototype.isInternalNameSpace = function() {
			return this.internalNameSpace;
		};
		prototype.setInternalNameSpace = function(internalNameSpace) {
			this.internalNameSpace = internalNameSpace;
		};
	}, {
		dataTypeCode : "DataTypeCode"
	});
	return PropertyType;
})