/**
 *  @author pkupczyk
 */
define([], function () {
    var ExperimentCreation = function() {};
    stjs.extend(ExperimentCreation, null, [], function(constructor, prototype) {
        prototype['@type'] = 'ExperimentCreation';
        constructor.serialVersionUID = 1;
        prototype.typeId = null;
        prototype.projectId = null;
        prototype.code = null;
        prototype.tagIds = null;
        prototype.properties = new HashMap();
        prototype.attachments = null;
        prototype.setTypeId = function(typeId) {
            this.typeId = typeId;
        };
        prototype.setProjectId = function(projectId) {
            this.projectId = projectId;
        };
        prototype.setCode = function(code) {
            this.code = code;
        };
        prototype.getTypeId = function() {
            return this.typeId;
        };
        prototype.getProjectId = function() {
            return this.projectId;
        };
        prototype.getCode = function() {
            return this.code;
        };
        prototype.getTagIds = function() {
            return this.tagIds;
        };
        prototype.setTagIds = function(tagIds) {
            this.tagIds = tagIds;
        };
        prototype.setProperty = function(key, value) {
            this.properties.put(key, value);
        };
        prototype.getProperties = function() {
            return this.properties;
        };
        prototype.getAttachments = function() {
            return this.attachments;
        };
        prototype.setAttachments = function(attachments) {
            this.attachments = attachments;
        };
    }, {typeId: "IEntityTypeId", projectId: "IProjectId", tagIds: {name: "List", arguments: ["Object"]}, properties: {name: "Map", arguments: [null, null]}, attachments: {name: "List", arguments: ["AttachmentCreation"]}});
    return ExperimentCreation;
})