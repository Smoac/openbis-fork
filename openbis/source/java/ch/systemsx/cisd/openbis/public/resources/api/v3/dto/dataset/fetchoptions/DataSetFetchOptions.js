/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/tag/fetchoptions/TagFetchOptions", "dto/dataset/fetchoptions/DataSetTypeFetchOptions",
		"dto/person/fetchoptions/PersonFetchOptions", "dto/experiment/fetchoptions/ExperimentFetchOptions", "dto/sample/fetchoptions/SampleFetchOptions",
		"dto/property/fetchoptions/PropertyFetchOptions", "dto/dataset/fetchoptions/PhysicalDataFetchOptions", "dto/dataset/fetchoptions/LinkedDataFetchOptions",
		"dto/history/fetchoptions/HistoryEntryFetchOptions", "dto/material/fetchoptions/MaterialFetchOptions", "dto/datastore/fetchoptions/DataStoreFetchOptions",
		"dto/dataset/fetchoptions/DataSetSortOptions" ], function(require, stjs, FetchOptions) {
	var DataSetFetchOptions = function() {
	};
	stjs.extend(DataSetFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.fetchoptions.DataSetFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.parents = null;
		prototype.children = null;
		prototype.containers = null;
		prototype.components = null;
		prototype.physicalData = null;
		prototype.linkedData = null;
		prototype.tags = null;
		prototype.type = null;
		prototype.dataStore = null;
		prototype.history = null;
		prototype.modifier = null;
		prototype.registrator = null;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.sort = null;
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
		prototype.withComponents = function() {
			if (this.components == null) {
				this.components = new DataSetFetchOptions();
			}
			return this.components;
		};
		prototype.withComponentsUsing = function(fetchOptions) {
			return this.components = fetchOptions;
		};
		prototype.hasComponents = function() {
			return this.components != null;
		};
		prototype.withPhysicalData = function() {
			if (this.physicalData == null) {
				var PhysicalDataFetchOptions = require("dto/dataset/fetchoptions/PhysicalDataFetchOptions");
				this.physicalData = new PhysicalDataFetchOptions();
			}
			return this.physicalData;
		};
		prototype.withPhysicalDataUsing = function(fetchOptions) {
			return this.physicalData = fetchOptions;
		};
		prototype.hasPhysicalData = function() {
			return this.physicalData != null;
		};
		prototype.withLinkedData = function() {
			if (this.linkedData == null) {
				var LinkedDataFetchOptions = require("dto/dataset/fetchoptions/LinkedDataFetchOptions");
				this.linkedData = new LinkedDataFetchOptions();
			}
			return this.linkedData;
		};
		prototype.withLinkedDataUsing = function(fetchOptions) {
			return this.linkedData = fetchOptions;
		};
		prototype.hasLinkedData = function() {
			return this.linkedData != null;
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
		prototype.withType = function() {
			if (this.type == null) {
				var DataSetTypeFetchOptions = require("dto/dataset/fetchoptions/DataSetTypeFetchOptions");
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
		prototype.withDataStore = function() {
			if (this.dataStore == null) {
				var DataStoreFetchOptions = require("dto/datastore/fetchoptions/DataStoreFetchOptions");
				this.dataStore = new DataStoreFetchOptions();
			}
			return this.dataStore;
		};
		prototype.withDataStoreUsing = function(fetchOptions) {
			return this.dataStore = fetchOptions;
		};
		prototype.hasDataStore = function() {
			return this.dataStore != null;
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
		prototype.withSample = function() {
			if (this.sample == null) {
				var SampleFetchOptions = require("dto/sample/fetchoptions/SampleFetchOptions");
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var DataSetSortOptions = require("dto/dataset/fetchoptions/DataSetSortOptions");
				this.sort = new DataSetSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		parents : "DataSetFetchOptions",
		children : "DataSetFetchOptions",
		containers : "DataSetFetchOptions",
		components : "DataSetFetchOptions",
		physicalData : "PhysicalDataFetchOptions",
		tags : "TagFetchOptions",
		type : "DataSetTypeFetchOptions",
		dataStore : "DataStoreFetchOptions",
		history : "HistoryEntryFetchOptions",
		modifier : "PersonFetchOptions",
		registrator : "PersonFetchOptions",
		experiment : "ExperimentFetchOptions",
		sample : "SampleFetchOptions",
		properties : "PropertyFetchOptions",
		materialProperties : "MaterialFetchOptions",
		sort : "DataSetSortOptions"
	});
	return DataSetFetchOptions;
})