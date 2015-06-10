/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "support/stjs", 'dto/fetchoptions/experiment/ExperimentTypeFetchOptions', 'dto/fetchoptions/project/ProjectFetchOptions', 'dto/fetchoptions/property/PropertyFetchOptions',
		'dto/fetchoptions/tag/TagFetchOptions', 'dto/fetchoptions/person/PersonFetchOptions', 'dto/fetchoptions/attachment/AttachmentFetchOptions', 'dto/fetchoptions/dataset/DataSetFetchOptions',
		'dto/fetchoptions/sample/SampleFetchOptions', 'dto/fetchoptions/history/HistoryEntryFetchOptions', 'dto/fetchoptions/material/MaterialFetchOptions' ], function(stjs,
		ExperimentTypeFetchOptions, ProjectFetchOptions, PropertyFetchOptions, TagFetchOptions, PersonFetchOptions, AttachmentFetchOptions, DataSetFetchOptions, SampleFetchOptions,
		HistoryEntryFetchOptions, MaterialFetchOptions) {
	var ExperimentFetchOptions = function() {
	};
	stjs.extend(ExperimentFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.experiment.ExperimentFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.type = null;
		prototype.project = null;
		prototype.dataSets = null;
		prototype.samples = null;
		prototype.history = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
		prototype.withType = function() {
			if (this.type == null) {
				this.type = new ExperimentTypeFetchOptions();
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
		prototype.withDataSets = function() {
			if (this.dataSets == null) {
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
		prototype.withSamples = function() {
			if (this.samples == null) {
				this.samples = new SampleFetchOptions();
			}
			return this.samples;
		};
		prototype.withSamplesUsing = function(fetchOptions) {
			return this.samples = fetchOptions;
		};
		prototype.hasSamples = function() {
			return this.samples != null;
		};
		prototype.withHistory = function() {
			if (this.history == null) {
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
		prototype.withProperties = function() {
			if (this.properties == null) {
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
		prototype.withTags = function() {
			if (this.tags == null) {
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
		type : "ExperimentTypeFetchOptions",
		project : "ProjectFetchOptions",
		dataSets : "DataSetFetchOptions",
		samples : "SampleFetchOptions",
		history : "HistoryEntryFetchOptions",
		properties : "PropertyFetchOptions",
		materialProperties : "MaterialFetchOptions",
		tags : "TagFetchOptions",
		registrator : "PersonFetchOptions",
		modifier : "PersonFetchOptions",
		attachments : "AttachmentFetchOptions"
	});
	return ExperimentFetchOptions;
})