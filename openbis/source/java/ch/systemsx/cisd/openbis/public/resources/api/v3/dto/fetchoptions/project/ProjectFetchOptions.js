/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", 'dto/fetchoptions/person/PersonFetchOptions', 'dto/fetchoptions/space/SpaceFetchOptions', 'dto/fetchoptions/experiment/ExperimentFetchOptions',
		'dto/fetchoptions/attachment/AttachmentFetchOptions' ], function(stjs, PersonFetchOptions, SpaceFetchOptions, ExperimentFetchOptions, AttachmentFetchOptions) {
	var ProjectFetchOptions = function() {
	};
	stjs.extend(ProjectFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.project.ProjectFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.experiments = null;
		prototype.space = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.leader = null;
		prototype.attachments = null;
		prototype.withExperiments = function() {
			if (this.experiments == null) {
				this.experiments = new ExperimentFetchOptions();
			}
			return this.experiments;
		};
		prototype.withExperimentsUsing = function(fetchOptions) {
			return this.experiments = fetchOptions;
		};
		prototype.hasExperiments = function() {
			return this.experiments != null;
		};
		prototype.withSpace = function() {
			if (this.space == null) {
				this.space = new SpaceFetchOptions();
			}
			return this.space;
		};
		prototype.withSpaceUsing = function(fetchOptions) {
			return this.space = fetchOptions;
		};
		prototype.hasSpace = function() {
			return this.space != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.withModifier = function() {
			if (this.modifier == null) {
				this.modifier = new PersonFetchOptions();
			}
			return this.modifier;
		};
		prototype.withModifierUsing = function(fetchOptions) {
			return this.modifier = fetchOptions;
		};
		prototype.hasModifier = function() {
			return this.modifier != null;
		};
		prototype.withLeader = function() {
			if (this.leader == null) {
				this.leader = new PersonFetchOptions();
			}
			return this.leader;
		};
		prototype.withLeaderUsing = function(fetchOptions) {
			return this.leader = fetchOptions;
		};
		prototype.hasLeader = function() {
			return this.leader != null;
		};
		prototype.withAttachments = function() {
			if (this.attachments == null) {
				this.attachments = new AttachmentFetchOptions();
			}
			return this.attachments;
		};
		prototype.withAttachmentsUsing = function(fetchOptions) {
			return this.attachments = fetchOptions;
		};
		prototype.hasAttachments = function() {
			return this.attachments != null;
		};
	}, {
		experiments : "ExperimentFetchOptions",
		space : "SpaceFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		leader : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions"
	});
	return ProjectFetchOptions;
})