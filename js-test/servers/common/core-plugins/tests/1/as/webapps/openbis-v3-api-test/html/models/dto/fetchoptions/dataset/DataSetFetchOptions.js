/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "support/stjs", "dto/fetchoptions/tag/TagFetchOptions", "dto/fetchoptions/dataset/DataSetTypeFetchOptions", "dto/fetchoptions/person/PersonFetchOptions",
		"dto/fetchoptions/experiment/ExperimentFetchOptions", "dto/fetchoptions/sample/SampleFetchOptions", "dto/fetchoptions/property/PropertyFetchOptions" ], function(stjs, TagFetchOptions,
		DataSetTypeFetchOptions, PersonFetchOptions, ExperimentFetchOptions, SampleFetchOptions, PropertyFetchOptions) {
	var DataSetFetchOptions = function() {
	};
	stjs.extend(DataSetFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.DataSetFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.parents = null;
		prototype.children = null;
		prototype.containers = null;
		prototype.contained = null;
		prototype.tags = null;
		prototype.type = null;
		prototype.modifier = null;
		prototype.registrator = null;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.properties = null;
		prototype.withParents = function() {
			if (this.parents == null) {
				this.parents = new DataSetFetchOptions();
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
				this.children = new DataSetFetchOptions();
			}
			return this.children;
		};
		prototype.withChildrenUsing = function(fetchOptions) {
			return this.children = fetchOptions;
		};
		prototype.hasChildren = function() {
			return this.children != null;
		};
		prototype.withContainers = function() {
			if (this.containers == null) {
				this.containers = new DataSetFetchOptions();
			}
			return this.containers;
		};
		prototype.withContainersUsing = function(fetchOptions) {
			return this.containers = fetchOptions;
		};
		prototype.hasContainers = function() {
			return this.containers != null;
		};
		prototype.withContained = function() {
			if (this.contained == null) {
				this.contained = new DataSetFetchOptions();
			}
			return this.contained;
		};
		prototype.withContainedUsing = function(fetchOptions) {
			return this.contained = fetchOptions;
		};
		prototype.hasContained = function() {
			return this.contained != null;
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
		prototype.withType = function() {
			if (this.type == null) {
				this.type = new DataSetTypeFetchOptions();
			}
			return this.type;
		};
		prototype.withTypeUsing = function(fetchOptions) {
			return this.type = fetchOptions;
		};
		prototype.hasType = function() {
			return this.type != null;
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
		prototype.withExperiment = function() {
			if (this.experiment == null) {
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
		prototype.withSample = function() {
			if (this.sample == null) {
				this.sample = new SampleFetchOptions();
			}
			return this.sample;
		};
		prototype.withSampleUsing = function(fetchOptions) {
			return this.sample = fetchOptions;
		};
		prototype.hasSample = function() {
			return this.sample != null;
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
	}, {
		parents : "DataSetFetchOptions",
		children : "DataSetFetchOptions",
		containers : "DataSetFetchOptions",
		contained : "DataSetFetchOptions",
		tags : "TagFetchOptions",
		type : "DataSetTypeFetchOptions",
		modifier : "PersonFetchOptions",
		registrator : "PersonFetchOptions",
		experiment : "ExperimentFetchOptions",
		sample : "SampleFetchOptions",
		properties : "PropertyFetchOptions"
	});
	return DataSetFetchOptions;
})