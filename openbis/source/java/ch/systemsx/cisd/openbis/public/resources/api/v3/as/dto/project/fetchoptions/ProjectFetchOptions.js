/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", 'as/dto/person/fetchoptions/PersonFetchOptions', 'as/dto/space/fetchoptions/SpaceFetchOptions',
		'as/dto/experiment/fetchoptions/ExperimentFetchOptions', 'as/dto/attachment/fetchoptions/AttachmentFetchOptions',
		'as/dto/project/fetchoptions/ProjectSortOptions', 'as/dto/history/fetchoptions/HistoryEntryFetchOptions' ], function(require, stjs, FetchOptions) {
	var ProjectFetchOptions = function() {
	};
	stjs.extend(ProjectFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.fetchoptions.ProjectFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.experiments = null;
		prototype.space = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.leader = null;
		prototype.attachments = null;
		prototype.history = null;
		prototype.sort = null;
		prototype.withExperiments = function() {
			if (this.experiments == null) {
				var ExperimentFetchOptions = require("as/dto/experiment/fetchoptions/ExperimentFetchOptions");
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
				var SpaceFetchOptions = require("as/dto/space/fetchoptions/SpaceFetchOptions");
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
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
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
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
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
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
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
				var AttachmentFetchOptions = require("as/dto/attachment/fetchoptions/AttachmentFetchOptions");
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
		prototype.withHistory = function() {
			if (this.history == null) {
				var HistoryEntryFetchOptions = require("as/dto/history/fetchoptions/HistoryEntryFetchOptions");
				this.history = new HistoryEntryFetchOptions();
			}
			return this.history;
		};
		prototype.withHistoryUsing = function(fetchOptions) {
			return this.history = fetchOptions;
		};
		prototype.hasHistory = function() {
			return this.history != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ProjectSortOptions = require("as/dto/project/fetchoptions/ProjectSortOptions");
				this.sort = new ProjectSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		experiments : "ExperimentFetchOptions",
		space : "SpaceFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		leader : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions",
		history : "HistoryEntryFetchOptions",
		sort : "ProjectSortOptions"
	});
	return ProjectFetchOptions;
})