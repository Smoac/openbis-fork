define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue) {
	var PersonUpdate = function() {
		this.spaceId = new FieldUpdateValue();
	};
	stjs.extend(PersonUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.update.PersonUpdate';
		constructor.serialVersionUID = 1;
		prototype.userId = null;
		prototype.spaceId = null;
		prototype.active = true;

		prototype.getObjectId = function() {
			return this.getUserId();
		};
		prototype.getUserId = function() {
			return this.userId;
		};
		prototype.setUserId = function(userId) {
			this.userId = userId;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId.setValue(spaceId);
		};
		prototype.isActive = function() {
			return this.active;
		};
		prototype.deactivate = function() {
			this.active = false;
		};
	}, {
		userId : "IPersonId",
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		}
	});
	return PersonUpdate;
})