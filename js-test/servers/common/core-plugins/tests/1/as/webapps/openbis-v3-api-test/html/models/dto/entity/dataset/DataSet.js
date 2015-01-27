/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs", "sys/exceptions"], function (stjs, exceptions) {
    var DataSet = function() {};
    stjs.extend(DataSet, null, [], function(constructor, prototype) {
        prototype['@type'] = 'DataSet';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;
        prototype.permId = null;
        prototype.code = null;
        prototype.accessDate = null;
        prototype.derived = null;
        prototype.placeholder = null;
        prototype.parents = null;
        prototype.children = null;
        prototype.containers = null;
        prototype.contained = null;
        prototype.tags = null;
        prototype.type = null;
        prototype.modificationDate = null;
        prototype.modifier = null;
        prototype.registrationDate = null;
        prototype.registrator = null;
        prototype.experiment = null;
        prototype.sample = null;
        prototype.properties = null;
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
        prototype.isDerived = function() {
            return this.derived;
        };
        prototype.setDerived = function(derived) {
            this.derived = derived;
        };
        prototype.isPlaceholder = function() {
            return this.placeholder;
        };
        prototype.setPlaceholder = function(placeholder) {
            this.placeholder = placeholder;
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
        prototype.getContained = function() {
            if (this.getFetchOptions().hasContained()) {
                return this.contained;
            } else {
                 throw new exceptions.NotFetchedException("Contained data sets has not been fetched.");
            }
        };
        prototype.setContained = function(contained) {
            this.contained = contained;
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
    }, {fetchOptions: "DataSetFetchOptions", permId: "DataSetPermId", accessDate: "Date", parents: {name: "List", arguments: ["DataSet"]}, children: {name: "List", arguments: ["DataSet"]}, containers: {name: "List", arguments: ["DataSet"]}, contained: {name: "List", arguments: ["DataSet"]}, tags: {name: "Set", arguments: ["Tag"]}, type: "DataSetType", modificationDate: "Date", modifier: "Person", registrationDate: "Date", registrator: "Person", experiment: "Experiment", sample: "Sample", properties: {name: "Map", arguments: [null, null]}});
    return DataSet;
})