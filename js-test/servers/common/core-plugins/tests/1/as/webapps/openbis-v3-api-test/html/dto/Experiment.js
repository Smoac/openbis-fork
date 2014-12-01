/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
var Experiment = function() {
	this['@type'] = 'Experiment';
};

stjs.extend(Experiment, null, [Serializable], function(constructor, prototype) {
    constructor.serialVersionUID = 1;
    prototype.fetchOptions = null;
    prototype.permId = null;
    prototype.identifier = null;
    prototype.code = null;
    prototype.registrationDate = null;
    prototype.modificationDate = null;
    prototype.type = null;
    prototype.project = null;
    prototype.properties = null;
    prototype.tags = null;
    prototype.registrator = null;
    prototype.modifier = null;
    prototype.attachments = null;
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
    prototype.getIdentifier = function() {
        return this.identifier;
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setIdentifier = function(identifier) {
        this.identifier = identifier;
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
    prototype.getType = function() {
        if (this.getFetchOptions().hasType()) {
            return this.type;
        } else {
             throw new NotFetchedException("Experiment type has not been fetched.");
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
    prototype.getProject = function() {
        if (this.getFetchOptions().hasProject()) {
            return this.project;
        } else {
             throw new NotFetchedException("Project has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setProject = function(project) {
        this.project = project;
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
    prototype.getAttachments = function() {
        if (this.getFetchOptions().hasAttachments()) {
            return this.attachments;
        } else {
             throw new NotFetchedException("Attachments has not been fetched.");
        }
    };
    /**
     *  Method automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
     */
    prototype.setAttachments = function(attachments) {
        this.attachments = attachments;
    };
    prototype.toString = function() {
        return "Experiment " + this.permId;
    };
}, {fetchOptions: "ExperimentFetchOptions", permId: "ExperimentPermId", identifier: "ExperimentIdentifier", registrationDate: "Date", modificationDate: "Date", type: "ExperimentType", project: "Project", properties: {name: "Map", arguments: [null, null]}, tags: {name: "Set", arguments: ["Tag"]}, registrator: "Person", modifier: "Person", attachments: {name: "List", arguments: ["Attachment"]}});