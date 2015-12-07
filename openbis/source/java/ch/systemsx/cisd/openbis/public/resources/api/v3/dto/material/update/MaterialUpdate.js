/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/update/IdListUpdateValue" ], function(stjs, IdListUpdateValue) {
	var MaterialUpdate = function() {
		this.properties = {};
		this.tagIds = new IdListUpdateValue();
	};
	stjs.extend(MaterialUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.material.update.MaterialUpdate';
		constructor.serialVersionUID = 1;
		prototype.materialId = null;
		prototype.getMaterialId = function() {
			return this.materialId;
		};
		prototype.setMaterialId = function(materialId) {
			this.materialId = materialId;
		};
		prototype.setProperty = function(key, value) {
			this.properties[key] = value;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
	}, {
		materialId : "IMaterialId",
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		}
	});
	return MaterialUpdate;
})