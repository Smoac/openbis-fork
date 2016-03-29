/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var DataSet = function() {
	};
	stjs.extend(DataSet, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.DataSet';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.code = null;
		prototype.accessDate = null;
		prototype.measured = null;
		prototype.postRegistered = null;
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
		prototype.modificationDate = null;
		prototype.modifier = null;
		prototype.registrationDate = null;
		prototype.registrator = null;
		prototype.experiment = null;
		prototype.sample = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.dataProducer = null;
		prototype.dataProductionDate = null;

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
		prototype.getAccessDate = function() {
			return this.accessDate;
		};
		prototype.setAccessDate = function(accessDate) {
			this.accessDate = accessDate;
		};
		prototype.isMeasured = function() {
			return this.measured;
		};
		prototype.setMeasured = function(measured) {
			this.measured = measured;
		};
		prototype.isPostRegistered = function() {
			return this.postRegistered;
		};
		prototype.setPostRegistered = function(postRegistered) {
			this.postRegistered = postRegistered;
		};
		prototype.getParents = function() {
			if (this.getFetchOptions().hasParents()) {
				return this.parents;
			} else {
				throw new exceptions.NotFetchedException("Parents has not been fetched.");
			}
		};
		prototype.setParents = function(parents) {
			this.parents = parents;
		};
		prototype.getChildren = function() {
			if (this.getFetchOptions().hasChildren()) {
				return this.children;
			} else {
				throw new exceptions.NotFetchedException("Children has not been fetched.");
			}
		};
		prototype.setChildren = function(children) {
			this.children = children;
		};
		prototype.getContainers = function() {
			if (this.getFetchOptions().hasContainers()) {
				return this.containers;
			} else {
				throw new exceptions.NotFetchedException("Container data sets has not been fetched.");
			}
		};
		prototype.setContainers = function(containers) {
			this.containers = containers;
		};
		prototype.getComponents = function() {
			if (this.getFetchOptions().hasComponents()) {
				return this.components;
			} else {
				throw new exceptions.NotFetchedException("Component data sets has not been fetched.");
			}
		};
		prototype.setComponents = function(components) {
			this.components = components;
		};
		prototype.getPhysicalData = function() {
			if (this.getFetchOptions().hasPhysicalData()) {
				return this.physicalData;
			} else {
				throw new exceptions.NotFetchedException("Physical data has not been fetched.");
			}
		};
		prototype.setPhysicalData = function(physicalData) {
			this.physicalData = physicalData;
		};
		prototype.getLinkedData = function() {
			if (this.getFetchOptions().hasLinkedData()) {
				return this.linkedData;
			} else {
				throw new exceptions.NotFetchedException("Linked data has not been fetched.");
			}
		};
		prototype.setLinkedData = function(linkedData) {
			this.linkedData = linkedData;
		};
		prototype.getTags = function() {
			if (this.getFetchOptions().hasTags()) {
				return this.tags;
			} else {
				throw new exceptions.NotFetchedException("Tags has not been fetched.");
			}
		};
		prototype.setTags = function(tags) {
			this.tags = tags;
		};
		prototype.getType = function() {
			if (this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Data Set type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getDataStore = function() {
			if (this.getFetchOptions().hasDataStore()) {
				return this.dataStore;
			} else {
				throw new exceptions.NotFetchedException("Data store has not been fetched.");
			}
		};
		prototype.setDataStore = function(dataStore) {
			this.dataStore = dataStore;
		};
		prototype.getHistory = function() {
			if (this.getFetchOptions().hasHistory()) {
				return this.history;
			} else {
				throw new exceptions.NotFetchedException("History has not been fetched.");
			}
		};
		prototype.setHistory = function(history) {
			this.history = history;
		};
		prototype.getModificationDate = function() {
			return this.modificationDate;
		};
		prototype.setModificationDate = function(modificationDate) {
			this.modificationDate = modificationDate;
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
		prototype.getRegistrationDate = function() {
			return this.registrationDate;
		};
		prototype.setRegistrationDate = function(registrationDate) {
			this.registrationDate = registrationDate;
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
		prototype.getExperiment = function() {
			if (this.getFetchOptions().hasExperiment()) {
				return this.experiment;
			} else {
				throw new exceptions.NotFetchedException("Experiment has not been fetched.");
			}
		};
		prototype.setExperiment = function(experiment) {
			this.experiment = experiment;
		};
		prototype.getSample = function() {
			if (this.getFetchOptions().hasSample()) {
				return this.sample;
			} else {
				throw new exceptions.NotFetchedException("Sample has not been fetched.");
			}
		};
		prototype.setSample = function(sample) {
			this.sample = sample;
		};
		prototype.getProperty = function(propertyName) {
			var properties = this.getProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.getProperties = function() {
			if (this.getFetchOptions().hasProperties()) {
				return this.properties;
			} else {
				throw new exceptions.NotFetchedException("Properties has not been fetched.");
			}
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getMaterialProperty = function(propertyName) {
			var properties = this.getMaterialProperties();
			return properties ? properties[propertyName] : null;
		};
		prototype.getMaterialProperties = function() {
			if (this.getFetchOptions().hasMaterialProperties()) {
				return this.materialProperties;
			} else {
				throw new exceptions.NotFetchedException("Material properties has not been fetched.");
			}
		};
		prototype.setMaterialProperties = function(materialProperties) {
			this.materialProperties = materialProperties;
		};
		prototype.getDataProducer = function() {
			return this.dataProducer;
		};
		prototype.setDataProducer = function(dataProducer) {
			this.dataProducer = dataProducer;
		};
		prototype.getDataProductionDate = function() {
			return this.dataProductionDate;
		};
		prototype.setDataProductionDate = function(dataProductionDate) {
			this.dataProductionDate = dataProductionDate;
		};
	}, {
		fetchOptions : "DataSetFetchOptions",
		permId : "DataSetPermId",
		accessDate : "Date",
		parents : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		children : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		containers : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		components : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		physicalData : "PhysicalData",
		linkedData : "LinkedData",
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		},
		type : "DataSetType",
		dataStore : "DataStore",
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		modificationDate : "Date",
		modifier : "Person",
		registrationDate : "Date",
		registrator : "Person",
		experiment : "Experiment",
		sample : "Sample",
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		materialProperties : {
			name : "Map",
			arguments : [ null, "Material" ]
		},
		dataProductionDate : "Date"
	});
	return DataSet;
})