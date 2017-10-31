define([ "stjs" ], function(stjs) {
	var AuthorizationGroupCreation = function() {
	};
	stjs.extend(AuthorizationGroupCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.authorizationGroup.create.AuthorizationGroupCreation';
		constructor.serialVersionUID = 1;
		prototype.code = null;
		prototype.description = null;
		prototype.experimentIds = null;
		prototype.users = null;

		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description = description;
		};
		prototype.getUsers = function() {
			return this.users;
		};
		prototype.setUsers = function(users) {
			this.users = users;
		};
	}, {
		users : {
			name : "List",
			arguments : [ "Object" ]
		}
	});
	return AuthorizationGroupCreation;
})