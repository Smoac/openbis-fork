/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var SpaceUpdate = function() {
		this.description = new FieldUpdateValue();
	};
	stjs.extend(SpaceUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.space.update.SpaceUpdate';
		constructor.serialVersionUID = 1;
		prototype.spaceId = null;
		prototype.description = null;

		prototype.getObjectId = function() {
			return this.getSpaceId();
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId = spaceId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
	}, {
		spaceId : "ISpaceId"
	});
	return SpaceUpdate;
})