define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/entitytype/update/PropertyAssignmentListUpdateValue" ], function(stjs, FieldUpdateValue, 
		PropertyAssignmentListUpdateValue) {
	var DataSetTypeUpdate = function() {
		this.description = new FieldUpdateValue();
		this.mainDataSetPattern = new FieldUpdateValue();
		this.mainDataSetPath = new FieldUpdateValue();
		this.disallowDeletion = new FieldUpdateValue();
		this.validationPluginId = new FieldUpdateValue();
		this.propertyAssignments = new PropertyAssignmentListUpdateValue();
	};
	stjs.extend(DataSetTypeUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.update.DataSetTypeUpdate';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.description = null;
		prototype.mainDataSetPattern = null;
		prototype.mainDataSetPath = null;
		prototype.disallowDeletion = null;
		prototype.validationPluginId = null;
		prototype.propertyAssignments = null;

		prototype.getObjectId = function() {
			return this.getTypeId();
		};
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setMainDataSetPattern = function(mainDataSetPattern) {
			this.mainDataSetPattern.setValue(mainDataSetPattern);
		};
		prototype.getMainDataSetPattern = function() {
			return this.mainDataSetPattern;
		};
		prototype.setMainDataSetPath = function(mainDataSetPath) {
			this.mainDataSetPath.setValue(mainDataSetPath);
		};
		prototype.getMainDataSetPath = function() {
			return this.mainDataSetPath;
		};
		prototype.setDisallowDeletion = function(disallowDeletion) {
			this.disallowDeletion.setValue(disallowDeletion);
		};
		prototype.isDisallowDeletion = function() {
			return this.disallowDeletion;
		};
		prototype.setValidationPluginId = function(validationPluginId) {
			this.validationPluginId.setValue(validationPluginId);
		};
		prototype.getValidationPluginId = function() {
			return this.validationPluginId;
		};
		prototype.getPropertyAssignments = function() {
			return this.propertyAssignments;
		};
		prototype.setPropertyAssignmentActions = function(actions) {
			this.propertyAssignments.setActions(actions);
		};
	}, {
		typeId : "IEntityTypeId",
		description : {
			name : "FieldUpdateValue",
			arguments : [ null ]
		},
		mainDataSetPattern : {
			name : "FieldUpdateValue",
			arguments : [ null ]
		},
		mainDataSetPath : {
			name : "FieldUpdateValue",
			arguments : [ null ]
		},
		disallowDeletion : {
			name : "FieldUpdateValue",
			arguments : [ null ]
		},
		validationPluginId : {
			name : "FieldUpdateValue",
			arguments : [ "IPluginId" ]
		},
		propertyAssignments : "PropertyAssignmentListUpdateValue"
	});
	return DataSetTypeUpdate;
})