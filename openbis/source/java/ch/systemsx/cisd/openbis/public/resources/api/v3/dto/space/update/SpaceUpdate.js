/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var SpaceUpdate = function() {
		this.description = new FieldUpdateValue();
	};
	stjs.extend(SpaceUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.space.update.SpaceUpdate';
		constructor.serialVersionUID = 1;
		prototype.spaceId = null;
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