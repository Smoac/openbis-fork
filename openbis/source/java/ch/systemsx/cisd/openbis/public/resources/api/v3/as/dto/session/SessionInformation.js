/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs" ], function(stjs) {
	var SessionInformation = function() {
	};
	stjs.extend(SessionInformation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.session.SessionInformation';
		constructor.serialVersionUID = 1;
		prototype.homeGroupCode = null;
		prototype.userName = null;
		prototype.person = null;
		prototype.creatorPerson = null;

		prototype.getHomeGroupCode = function() {
			return this.homeGroupCode;
		};
		prototype.setHomeGroupCode = function(homeGroupCode) {
			this.homeGroupCode = homeGroupCode;
		};
		
		prototype.getUserName = function() {
			return this.userName;
		};
		prototype.setUserName = function(userName) {
			this.userName = userName;
		};
		
		prototype.getPerson = function() {
			return this.person;
		};
		prototype.setPerson = function(person) {
			this.person = person;
		};
		
		prototype.getCreatorPerson = function() {
			return this.creatorPerson;
		};
		prototype.setCreatorPerson = function(creatorPerson) {
			this.creatorPerson = creatorPerson;
		};
	}, {
		person : "Person",
		creatorPerson : "Person"
	});
	return SessionInformation;
})