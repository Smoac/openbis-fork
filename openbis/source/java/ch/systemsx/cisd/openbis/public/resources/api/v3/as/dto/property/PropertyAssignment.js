define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var PropertyAssignment = function() {
	};
	stjs.extend(PropertyAssignment, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.PropertyAssignment';
		constructor.serialVersionUID = 1;
		prototype.mandatory = null;
		prototype.propertyType = null;
		prototype.isMandatory = function() {
			return this.mandatory;
		};
		prototype.setMandatory = function(mandatory) {
			this.mandatory = mandatory;
		};
		prototype.getPropertyType = function() {
			return this.propertyType;
		};
		prototype.setPropertyType = function(propertyType) {
			this.propertyType = propertyType;
		};
	}, {
		propertyType : "PropertyType"
	});
	return PropertyAssignment;
})