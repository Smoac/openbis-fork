define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var PropertyTypeUpdate = function() {
		this.label = new FieldUpdateValue();
		this.description = new FieldUpdateValue();
		this.schema = new FieldUpdateValue();
		this.transformation = new FieldUpdateValue();
	};
	stjs.extend(PropertyTypeUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.property.update.PropertyTypeUpdate';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.label = null;
		prototype.description = null;
		prototype.schema = null;
		prototype.transformation = null;

		prototype.getObjectId = function() {
			return this.getTypeId();
		};
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.getLabel = function() {
			return this.label;
		};
		prototype.setLabel = function(label) {
			this.label.setValue(label);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getSchema = function() {
			return this.schema;
		};
		prototype.setSchema = function(schema) {
			this.schema.setValue(schema);
		};
		prototype.getTransformation = function() {
			return this.transformation;
		};
		prototype.setTransformation = function(transformation) {
			this.transformation.setValue(transformation);
		};
	}, {
		typeId : "IPropertyTypeId"
	});
	return PropertyTypeUpdate;
})
