/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var Sample = function() {
	};
	stjs.extend(Sample, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.sample.Sample';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.permId = null;
		prototype.identifier = null;
		prototype.code = null;
		prototype.registrationDate = null;
		prototype.modificationDate = null;
		prototype.type = null;
		prototype.project = null;
		prototype.space = null;
		prototype.experiment = null;
		prototype.properties = null;
		prototype.materialProperties = null;
		prototype.parents = null;
		prototype.children = null;
		prototype.container = null;
		prototype.contained = null;
		prototype.dataSets = null;
		prototype.history = null;
		prototype.tags = null;
		prototype.registrator = null;
		prototype.modifier = null;
		prototype.attachments = null;
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
		prototype.getType = function() {
			if (this.getFetchOptions().hasType()) {
				return this.type;
			} else {
				throw new exceptions.NotFetchedException("Sample type has not been fetched.");
			}
		};
		prototype.setType = function(type) {
			this.type = type;
		};
		prototype.getProject = function() {
			if (this.getFetchOptions().hasProject()) {
				return this.project;
			} else {
				throw new exceptions.NotFetchedException("Project has not been fetched.");
			}
		};
		prototype.setProject = function(project) {
			this.project = project;
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
		prototype.getProperties = function() {
			if (this.getFetchOptions().hasProperties()) {
				return this.properties;
			} else {
				throw new exceptions.NotFetchedException("Properties have not been fetched.");
			}
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.getMaterialProperties = function() {
			if (this.getFetchOptions().hasMaterialProperties()) {
				return this.materialProperties;
			} else {
				throw new exceptions.NotFetchedException("Material properties have not been fetched.");
			}
		};
		prototype.setMaterialProperties = function(materialProperties) {
			this.materialProperties = materialProperties;
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
		prototype.getContainer = function() {
			if (this.getFetchOptions().hasContainer()) {
				return this.container;
			} else {
				throw new exceptions.NotFetchedException("Container sample has not been fetched.");
			}
		};
		prototype.setContainer = function(container) {
			this.container = container;
		};
		prototype.getContained = function() {
			if (this.getFetchOptions().hasContained()) {
				return this.contained;
			} else {
				throw new exceptions.NotFetchedException("Contained samples has not been fetched.");
			}
		};
		prototype.setContained = function(contained) {
			this.contained = contained;
		};
		prototype.getDataSets = function() {
			if (this.getFetchOptions().hasDataSets()) {
				return this.dataSets;
			} else {
				throw new exceptions.NotFetchedException("Data sets have not been fetched.");
			}
		};
		prototype.setDataSets = function(dataSets) {
			this.dataSets = dataSets;
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
		prototype.getAttachments = function() {
			if (this.getFetchOptions().hasAttachments()) {
				return this.attachments;
			} else {
				throw new exceptions.NotFetchedException("Attachments has not been fetched.");
			}
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
		};
		prototype.toString = function() {
			return "Sample " + this.permId;
		};
	}, {
		fetchOptions : "SampleFetchOptions",
		permId : "SamplePermId",
		identifier : "SampleIdentifier",
		registrationDate : "Date",
		modificationDate : "Date",
		type : "SampleType",
		project : "Project",
		space : "Space",
		experiment : "Experiment",
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		materialProperties : {
			name : "Map",
			arguments : [ null, "Material" ]
		},
		parents : {
			name : "List",
			arguments : [ "Sample" ]
		},
		children : {
			name : "List",
			arguments : [ "Sample" ]
		},
		container : "Sample",
		contained : {
			name : "List",
			arguments : [ "Sample" ]
		},
		dataSets : {
			name : "List",
			arguments : [ "DataSet" ]
		},
		history : {
			name : "List",
			arguments : [ "HistoryEntry" ]
		},
		tags : {
			name : "Set",
			arguments : [ "Tag" ]
		},
		registrator : "Person",
		modifier : "Person",
		attachments : {
			name : "List",
			arguments : [ "Attachment" ]
		}
	});
	return Sample;
})