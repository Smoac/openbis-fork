/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define(["support/stjs", "sys/exceptions"], function (stjs, exceptions) {
    var Experiment = function() {};
    stjs.extend(Experiment, null, [], function(constructor, prototype) {
        prototype['@type'] = 'Experiment';
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
                 throw new exceptions.NotFetchedException("Experiment type has not been fetched.");
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
            return "Experiment " + this.permId;
        };
    }, {fetchOptions: "ExperimentFetchOptions", permId: "ExperimentPermId", identifier: "ExperimentIdentifier", registrationDate: "Date", modificationDate: "Date", type: "ExperimentType", project: "Project", properties: {name: "Map", arguments: [null, null]}, tags: {name: "Set", arguments: ["Tag"]}, registrator: "Person", modifier: "Person", attachments: {name: "List", arguments: ["Attachment"]}});
    return Experiment;
})