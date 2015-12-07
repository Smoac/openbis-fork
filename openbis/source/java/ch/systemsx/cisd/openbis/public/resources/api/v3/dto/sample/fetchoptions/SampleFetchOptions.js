/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", 'dto/sample/fetchoptions/SampleTypeFetchOptions', 'dto/space/fetchoptions/SpaceFetchOptions',
		'dto/project/fetchoptions/ProjectFetchOptions', 'dto/experiment/fetchoptions/ExperimentFetchOptions', 'dto/property/fetchoptions/PropertyFetchOptions', 'dto/tag/fetchoptions/TagFetchOptions',
		'dto/person/fetchoptions/PersonFetchOptions', 'dto/attachment/fetchoptions/AttachmentFetchOptions', 'dto/material/fetchoptions/MaterialFetchOptions',
		'dto/dataset/fetchoptions/DataSetFetchOptions', 'dto/history/fetchoptions/HistoryEntryFetchOptions', 'dto/sample/fetchoptions/SampleSortOptions' ], function(require, stjs, FetchOptions) {
	var SampleFetchOptions = function() {
	};
	stjs.extend(SampleFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.fetchoptions.SampleFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.type = null;
		prototype.project = null;
		prototype.space = null;
		prototype.experiment = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.parents = null;
		prototype.children = null;
		prototype.container = null;
		prototype.components = null;
		prototype.dataSets = null;
		prototype.history = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
		prototype.sort = null;
		prototype.withType = function() {
			if (this.type == null) {
				var SampleTypeFetchOptions = require("dto/sample/fetchoptions/SampleTypeFetchOptions");
				this.type = new SampleTypeFetchOptions();
			}
			return this.type;
		};
		prototype.withTypeUsing = function(fetchOptions) {
			return this.type = fetchOptions;
		};
		prototype.hasType = function() {
			return this.type != null;
		};
		prototype.withProject = function() {
			if (this.project == null) {
				var ProjectFetchOptions = require("dto/project/fetchoptions/ProjectFetchOptions");
				this.project = new ProjectFetchOptions();
			}
			return this.project;
		};
		prototype.withProjectUsing = function(fetchOptions) {
			return this.project = fetchOptions;
		};
		prototype.hasProject = function() {
			return this.project != null;
		};
		prototype.withSpace = function() {
			if (this.space == null) {
				var SpaceFetchOptions = require("dto/space/fetchoptions/SpaceFetchOptions");
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
		prototype.withExperiment = function() {
			if (this.experiment == null) {
				var ExperimentFetchOptions = require("dto/experiment/fetchoptions/ExperimentFetchOptions");
				this.experiment = new ExperimentFetchOptions();
			}
			return this.experiment;
		};
		prototype.withExperimentUsing = function(fetchOptions) {
			return this.experiment = fetchOptions;
		};
		prototype.hasExperiment = function() {
			return this.experiment != null;
		};
		prototype.withProperties = function() {
			if (this.properties == null) {
				var PropertyFetchOptions = require("dto/property/fetchoptions/PropertyFetchOptions");
				this.properties = new PropertyFetchOptions();
			}
			return this.properties;
		};
		prototype.withPropertiesUsing = function(fetchOptions) {
			return this.properties = fetchOptions;
		};
		prototype.hasProperties = function() {
			return this.properties != null;
		};
		prototype.withMaterialProperties = function() {
			if (this.materialProperties == null) {
				var MaterialFetchOptions = require("dto/material/fetchoptions/MaterialFetchOptions");
				this.materialProperties = new MaterialFetchOptions();
			}
			return this.materialProperties;
		};
		prototype.withMaterialPropertiesUsing = function(fetchOptions) {
			return this.materialProperties = fetchOptions;
		};
		prototype.hasMaterialProperties = function() {
			return this.materialProperties != null;
		};
		prototype.withParents = function() {
			if (this.parents == null) {
				this.parents = new SampleFetchOptions();
			}
			return this.parents;
		};
		prototype.withParentsUsing = function(fetchOptions) {
			return this.parents = fetchOptions;
		};
		prototype.hasParents = function() {
			return this.parents != null;
		};
		prototype.withChildren = function() {
			if (this.children == null) {
				this.children = new SampleFetchOptions();
			}
			return this.children;
		};
		prototype.withChildrenUsing = function(fetchOptions) {
			return this.children = fetchOptions;
		};
		prototype.hasChildren = function() {
			return this.children != null;
		};
		prototype.withContainer = function() {
			if (this.container == null) {
				this.container = new SampleFetchOptions();
			}
			return this.container;
		};
		prototype.withContainerUsing = function(fetchOptions) {
			return this.container = fetchOptions;
		};
		prototype.hasContainer = function() {
			return this.container != null;
		};
		prototype.withComponents = function() {
			if (this.components == null) {
				this.components = new SampleFetchOptions();
			}
			return this.components;
		};
		prototype.withComponentsUsing = function(fetchOptions) {
			return this.components = fetchOptions;
		};
		prototype.hasComponents = function() {
			return this.components != null;
		};
		prototype.withDataSets = function() {
			if (this.dataSets == null) {
				var DataSetFetchOptions = require("dto/dataset/fetchoptions/DataSetFetchOptions");
				this.dataSets = new DataSetFetchOptions();
			}
			return this.dataSets;
		};
		prototype.withDataSetsUsing = function(fetchOptions) {
			return this.dataSets = fetchOptions;
		};
		prototype.hasDataSets = function() {
			return this.dataSets != null;
		};
		prototype.withHistory = function() {
			if (this.history == null) {
				var HistoryEntryFetchOptions = require("dto/history/fetchoptions/HistoryEntryFetchOptions");
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
		prototype.withTags = function() {
			if (this.tags == null) {
				var TagFetchOptions = require("dto/tag/fetchoptions/TagFetchOptions");
				this.tags = new TagFetchOptions();
			}
			return this.tags;
		};
		prototype.withTagsUsing = function(fetchOptions) {
			return this.tags = fetchOptions;
		};
		prototype.hasTags = function() {
			return this.tags != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("dto/person/fetchoptions/PersonFetchOptions");
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
				var PersonFetchOptions = require("dto/person/fetchoptions/PersonFetchOptions");
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
		prototype.withAttachments = function() {
			if (this.attachments == null) {
				var AttachmentFetchOptions = require("dto/attachment/fetchoptions/AttachmentFetchOptions");
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SampleSortOptions = require("dto/sample/fetchoptions/SampleSortOptions");
				this.sort = new SampleSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		type : "SampleTypeFetchOptions",
		project : "ProjectFetchOptions",
		space : "SpaceFetchOptions",
		experiment : "ExperimentFetchOptions",
		properties : "PropertyFetchOptions",
		parents : "SampleFetchOptions",
		children : "SampleFetchOptions",
		container : "SampleFetchOptions",
		components : "SampleFetchOptions",
		tags : "TagFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions",
		sort : "SampleSortOptions"
	});
	return SampleFetchOptions;
})