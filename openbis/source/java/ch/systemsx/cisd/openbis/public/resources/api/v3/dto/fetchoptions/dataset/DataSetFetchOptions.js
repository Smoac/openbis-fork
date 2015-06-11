/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/fetchoptions/tag/TagFetchOptions", "dto/fetchoptions/dataset/DataSetTypeFetchOptions", "dto/fetchoptions/person/PersonFetchOptions",
		"dto/fetchoptions/experiment/ExperimentFetchOptions", "dto/fetchoptions/sample/SampleFetchOptions", "dto/fetchoptions/property/PropertyFetchOptions",
		"dto/fetchoptions/dataset/ExternalDataFetchOptions", "dto/fetchoptions/history/HistoryEntryFetchOptions", "dto/fetchoptions/material/MaterialFetchOptions" ], function(stjs, TagFetchOptions,
		DataSetTypeFetchOptions, PersonFetchOptions, ExperimentFetchOptions, SampleFetchOptions, PropertyFetchOptions, ExternalDataFetchOptions, HistoryEntryFetchOptions, MaterialFetchOptions) {
	var DataSetFetchOptions = function() {
	};
	stjs.extend(DataSetFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.DataSetFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.parents = null;
		prototype.children = null;
		prototype.containers = null;
		prototype.contained = null;
		prototype.externalData = null;
		prototype.tags = null;
		prototype.type = null;
		prototype.history = null;
		prototype.modifier = null;
		prototype.registrator = null;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.properties = null;
		prototype.materialProperties = null;
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
		prototype.withExternalData = function() {
			if (this.externalData == null) {
				this.externalData = new ExternalDataFetchOptions();
			}
			return this.externalData;
		};
		prototype.withExternalDataUsing = function(fetchOptions) {
			return this.externalData = fetchOptions;
		};
		prototype.hasExternalData = function() {
			return this.externalData != null;
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
	}, {
		parents : "DataSetFetchOptions",
		children : "DataSetFetchOptions",
		containers : "DataSetFetchOptions",
		contained : "DataSetFetchOptions",
		externalData : "ExternalDataFetchOptions",
		tags : "TagFetchOptions",
		type : "DataSetTypeFetchOptions",
		history : "HistoryEntryFetchOptions",
		modifier : "PersonFetchOptions",
		registrator : "PersonFetchOptions",
		experiment : "ExperimentFetchOptions",
		sample : "SampleFetchOptions",
		properties : "PropertyFetchOptions",
		materialProperties : "MaterialFetchOptions"
	});
	return DataSetFetchOptions;
})