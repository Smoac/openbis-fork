/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var DataSet = function() {
	this['@type'] = 'DataSet';
};

stjs.extend(DataSet, null, [Serializable], function(constructor, prototype) {
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
    prototype.properties = null;
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getFetchOptions = function() {
        return this.fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setFetchOptions = function(fetchOptions) {
        this.fetchOptions = fetchOptions;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getPermId = function() {
        return this.permId;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setPermId = function(permId) {
        this.permId = permId;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getCode = function() {
        return this.code;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setCode = function(code) {
        this.code = code;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getAccessDate = function() {
        return this.accessDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setAccessDate = function(accessDate) {
        this.accessDate = accessDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.isDerived = function() {
        return this.derived;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setDerived = function(derived) {
        this.derived = derived;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.isPlaceholder = function() {
        return this.placeholder;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setPlaceholder = function(placeholder) {
        this.placeholder = placeholder;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getParents = function() {
        if (this.getFetchOptions().hasParents()) {
            return this.parents;
        } else {
             throw new NotFetchedException("Parents has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setParents = function(parents) {
        this.parents = parents;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getChildren = function() {
        if (this.getFetchOptions().hasChildren()) {
            return this.children;
        } else {
             throw new NotFetchedException("Children has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setChildren = function(children) {
        this.children = children;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getContainers = function() {
        if (this.getFetchOptions().hasContainers()) {
            return this.containers;
        } else {
             throw new NotFetchedException("Container data sets has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setContainers = function(containers) {
        this.containers = containers;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getContained = function() {
        if (this.getFetchOptions().hasContained()) {
            return this.contained;
        } else {
             throw new NotFetchedException("Contained data sets has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setContained = function(contained) {
        this.contained = contained;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getTags = function() {
        if (this.getFetchOptions().hasTags()) {
            return this.tags;
        } else {
             throw new NotFetchedException("Tags has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setTags = function(tags) {
        this.tags = tags;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getType = function() {
        if (this.getFetchOptions().hasType()) {
            return this.type;
        } else {
             throw new NotFetchedException("Sample type has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setType = function(type) {
        this.type = type;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getModificationDate = function() {
        return this.modificationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setModificationDate = function(modificationDate) {
        this.modificationDate = modificationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getModifier = function() {
        if (this.getFetchOptions().hasModifier()) {
            return this.modifier;
        } else {
             throw new NotFetchedException("Modifier has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setModifier = function(modifier) {
        this.modifier = modifier;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getRegistrationDate = function() {
        return this.registrationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setRegistrationDate = function(registrationDate) {
        this.registrationDate = registrationDate;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getRegistrator = function() {
        if (this.getFetchOptions().hasRegistrator()) {
            return this.registrator;
        } else {
             throw new NotFetchedException("Registrator has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setRegistrator = function(registrator) {
        this.registrator = registrator;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getExperiment = function() {
        if (this.getFetchOptions().hasExperiment()) {
            return this.experiment;
        } else {
             throw new NotFetchedException("Experiment has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setExperiment = function(experiment) {
        this.experiment = experiment;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.getProperties = function() {
        if (this.getFetchOptions().hasProperties()) {
            return this.properties;
        } else {
             throw new NotFetchedException("Properties has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setProperties = function(properties) {
        this.properties = properties;
    };
}, {fetchOptions: "DataSetFetchOptions", permId: "DataSetPermId", accessDate: "Date", parents: {name: "List", arguments: ["DataSet"]}, children: {name: "List", arguments: ["DataSet"]}, containers: {name: "List", arguments: ["DataSet"]}, contained: {name: "List", arguments: ["DataSet"]}, tags: {name: "Set", arguments: ["Tag"]}, type: "DataSetType", modificationDate: "Date", modifier: "Person", registrationDate: "Date", registrator: "Person", experiment: "Experiment", properties: {name: "Map", arguments: [null, null]}});
