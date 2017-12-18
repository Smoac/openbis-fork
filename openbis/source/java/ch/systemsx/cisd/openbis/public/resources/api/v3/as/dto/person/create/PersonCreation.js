define([ "stjs" ], function(stjs) {
	var PersonCreation = function() {
	};
	stjs.extend(PersonCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.person.create.PersonCreation';
		constructor.serialVersionUID = 1;
		prototype.userId = null;
		prototype.homeSpaceId = null;

		prototype.getUserId = function() {
			return this.userId;
		};
		prototype.setUserId = function(userId) {
			this.userId = userId;
		};
		prototype.getHomeSpaceId = function() {
			return this.homeSpaceId;
		};
		prototype.setHomeSpaceId = function(spaceId) {
			this.homeSpaceId = spaceId;
		};
	}, {
	});
	return PersonCreation;
})