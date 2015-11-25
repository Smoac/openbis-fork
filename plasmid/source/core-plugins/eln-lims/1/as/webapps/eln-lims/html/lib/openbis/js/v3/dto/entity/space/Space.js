/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Space = function() {
	};
	stjs.extend(Space, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.space.Space';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.description = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.registrator = null;
		prototype.samples = null;
		prototype.projects = null;

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
		prototype.getSamples = function() {
			if (this.getFetchOptions().hasSamples()) {
				return this.samples;
			} else {
				throw new exceptions.NotFetchedException("Samples have not been fetched.");
			}
		};
		prototype.setSamples = function(samples) {
			this.samples = samples;
		};
		prototype.getProjects = function() {
			if (this.getFetchOptions().hasProjects()) {
				return this.projects;
			} else {
				throw new exceptions.NotFetchedException("Projects have not been fetched.");
			}
		};
		prototype.setProjects = function(projects) {
			this.projects = projects;
		};
	}, {
		fetchOptions : "SpaceFetchOptions",
		permId : "SpacePermId",
		registrationDate : "Date",
		registrator : "Person",
		samples : {
			name : "List",
			arguments : [ "Sample" ]
		},
		projects : {
			name : "List",
			arguments : [ "Project" ]
		}
	});
	return Space;
})