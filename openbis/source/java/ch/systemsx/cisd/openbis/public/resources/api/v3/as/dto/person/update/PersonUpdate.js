define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue) {
	var PersonUpdate = function() {
		this.homeSpaceId = new FieldUpdateValue();
	};
	stjs.extend(PersonUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.update.PersonUpdate';
		constructor.serialVersionUID = 1;
		prototype.personId = null;
		prototype.homeSpaceId = null;
		prototype.active = true;

		prototype.getObjectId = function() {
			return this.getPersonId();
		};
		prototype.getPersonId = function() {
			return this.personId;
		};
		prototype.setPersonId = function(personId) {
			this.personId = personId;
		};
		prototype.getHomeSpaceId = function() {
			return this.spaceId;
		};
		prototype.setHomeSpaceId = function(spaceId) {
			this.homeSpaceId.setValue(spaceId);
		};
		prototype.isActive = function() {
			return this.active;
		};
		prototype.deactivate = function() {
			this.active = false;
		};
	}, {
		PersonId : "IPersonId",
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		}
	});
	return PersonUpdate;
})