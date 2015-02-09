/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "support/stjs", "sys/exceptions" ], function(stjs, exceptions) {
	var Project = function() {
	};
	stjs.extend(Project, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.project.Project';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.identifier = null;
		prototype.code = null;
		prototype.description = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.space = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getPermId = function() {
			return this.permId;
		};
		prototype.setPermId = function(permId) {
			this.permId = permId;
		};
		prototype.getIdentifier = function() {
			return this.identifier;
		};
		prototype.setIdentifier = function(identifier) {
			this.identifier = identifier;
		};
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
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
		};
		prototype.getSpace = function() {
			if (this.getFetchOptions().hasSpace()) {
				return this.space;
			} else {
				throw new exceptions.NotFetchedException("Space has not been fetched.");
			}
		};
		prototype.setSpace = function(space) {
			this.space = space;
		};
		prototype.getRegistrator = function() {
			if (this.getFetchOptions().hasRegistrator()) {
				return this.registrator;
			} else {
				throw new exceptions.NotFetchedException("Registrator has not been fetched.");
			}
		};
		prototype.setRegistrator = function(registrator) {
			this.registrator = registrator;
		};
		prototype.getModifier = function() {
			if (this.getFetchOptions().hasModifier()) {
				return this.modifier;
			} else {
				throw new exceptions.NotFetchedException("Modifier has not been fetched.");
			}
		};
		prototype.setModifier = function(modifier) {
			this.modifier = modifier;
		};
	}, {
		fetchOptions : "ProjectFetchOptions",
		permId : "ProjectPermId",
		identifier : "ProjectIdentifier",
		registrationDate : "Date",
		modificationDate : "Date",
		space : "Space",
		registrator : "Person",
		modifier : "Person"
	});
	return Project;
})