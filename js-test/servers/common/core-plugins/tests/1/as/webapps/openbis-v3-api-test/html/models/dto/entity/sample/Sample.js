/**
 *  Class automatically generated with {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([], function () {
    var Sample = function() {};
    stjs.extend(Sample, null, [], function(constructor, prototype) {
        prototype['@type'] = 'Sample';
        constructor.serialVersionUID = 1;
        prototype.fetchOptions = null;
        prototype.permId = null;
        prototype.identifier = null;
        prototype.code = null;
        prototype.registrationDate = null;
        prototype.modificationDate = null;
        prototype.type = null;
        prototype.space = null;
        prototype.experiment = null;
        prototype.properties = null;
        prototype.parents = null;
        prototype.children = null;
        prototype.container = null;
        prototype.contained = null;
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
                 throw new NotFetchedException("Sample type has not been fetched.");
            }
        };
        prototype.setType = function(type) {
            this.type = type;
        };
        prototype.getSpace = function() {
            if (this.getFetchOptions().hasSpace()) {
                return this.space;
            } else {
                 throw new NotFetchedException("Space has not been fetched.");
            }
        };
        prototype.setSpace = function(space) {
            this.space = space;
        };
        prototype.getExperiment = function() {
            if (this.getFetchOptions().hasExperiment()) {
                return this.experiment;
            } else {
                 throw new NotFetchedException("Experiment has not been fetched.");
            }
        };
        prototype.setExperiment = function(experiment) {
            this.experiment = experiment;
        };
        prototype.getProperties = function() {
            if (this.getFetchOptions().hasProperties()) {
                return this.properties;
            } else {
                 throw new NotFetchedException("Properties has not been fetched.");
            }
        };
        prototype.setProperties = function(properties) {
            this.properties = properties;
        };
        prototype.getParents = function() {
            if (this.getFetchOptions().hasParents()) {
                return this.parents;
            } else {
                 throw new NotFetchedException("Parents has not been fetched.");
            }
        };
        prototype.setParents = function(parents) {
            this.parents = parents;
        };
        prototype.getChildren = function() {
            if (this.getFetchOptions().hasChildren()) {
                return this.children;
            } else {
                 throw new NotFetchedException("Children has not been fetched.");
            }
        };
        prototype.setChildren = function(children) {
            this.children = children;
        };
        prototype.getContainer = function() {
            if (this.getFetchOptions().hasContainer()) {
                return this.container;
            } else {
                 throw new NotFetchedException("Container sample has not been fetched.");
            }
        };
        prototype.setContainer = function(container) {
            this.container = container;
        };
        prototype.getContained = function() {
            if (this.getFetchOptions().hasContained()) {
                return this.contained;
            } else {
                 throw new NotFetchedException("Contained samples has not been fetched.");
            }
        };
        prototype.setContained = function(contained) {
            this.contained = contained;
        };
        prototype.getTags = function() {
            if (this.getFetchOptions().hasTags()) {
                return this.tags;
            } else {
                 throw new NotFetchedException("Tags has not been fetched.");
            }
        };
        prototype.setTags = function(tags) {
            this.tags = tags;
        };
        prototype.getRegistrator = function() {
            if (this.getFetchOptions().hasRegistrator()) {
                return this.registrator;
            } else {
                 throw new NotFetchedException("Registrator has not been fetched.");
            }
        };
        prototype.setRegistrator = function(registrator) {
            this.registrator = registrator;
        };
        prototype.getModifier = function() {
            if (this.getFetchOptions().hasModifier()) {
                return this.modifier;
            } else {
                 throw new NotFetchedException("Modifier has not been fetched.");
            }
        };
        prototype.setModifier = function(modifier) {
            this.modifier = modifier;
        };
        prototype.getAttachments = function() {
            if (this.getFetchOptions().hasAttachments()) {
                return this.attachments;
            } else {
                 throw new NotFetchedException("Attachments has not been fetched.");
            }
        };
        prototype.setAttachments = function(attachments) {
            this.attachments = attachments;
        };
        prototype.toString = function() {
            return "Sample " + this.permId;
        };
    }, {fetchOptions: "SampleFetchOptions", permId: "SamplePermId", identifier: "SampleIdentifier", registrationDate: "Date", modificationDate: "Date", type: "SampleType", space: "Space", experiment: "Experiment", properties: {name: "Map", arguments: [null, null]}, parents: {name: "List", arguments: ["Sample"]}, children: {name: "List", arguments: ["Sample"]}, container: "Sample", contained: {name: "List", arguments: ["Sample"]}, tags: {name: "Set", arguments: ["Tag"]}, registrator: "Person", modifier: "Person", attachments: {name: "List", arguments: ["Attachment"]}});
    return Sample;
})