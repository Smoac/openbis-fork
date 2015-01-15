/**
 *  @author pkupczyk
 */
define(["support/stjs"], function (stjs) {
    var SampleUpdate = function() {};
    stjs.extend(SampleUpdate, null, [], function(constructor, prototype) {
        prototype['@type'] = 'SampleUpdate';
        constructor.serialVersionUID = 1;
        prototype.sampleId = null;
        prototype.experimentId = new FieldUpdateValue();
        prototype.spaceId = new FieldUpdateValue();
        prototype.tagIds = new IdListUpdateValue();
        prototype.properties = new HashMap();
        prototype.containerId = new FieldUpdateValue();
        prototype.containedIds = new IdListUpdateValue();
        prototype.parentIds = new IdListUpdateValue();
        prototype.childIds = new IdListUpdateValue();
        prototype.attachments = new AttachmentListUpdateValue();
        prototype.getSampleId = function() {
            return this.sampleId;
        };
        prototype.setSampleId = function(sampleId) {
            this.sampleId = sampleId;
        };
        prototype.getExperimentId = function() {
            return this.experimentId;
        };
        prototype.setExperimentId = function(experimentId) {
            this.experimentId.setValue(experimentId);
        };
        prototype.getSpaceId = function() {
            return this.spaceId;
        };
        prototype.setSpaceId = function(spaceId) {
            this.spaceId.setValue(spaceId);
        };
        prototype.getContainerId = function() {
            return this.containerId;
        };
        prototype.setContainerId = function(containerId) {
            this.containerId.setValue(containerId);
        };
        prototype.setProperty = function(key, value) {
            this.properties.put(key, value);
        };
        prototype.getProperties = function() {
            return this.properties;
        };
        prototype.getTagIds = function() {
            return this.tagIds;
        };
        prototype.setTagActions = function(actions) {
            this.tagIds.setActions(actions);
        };
        prototype.getContainedIds = function() {
            return this.containedIds;
        };
        prototype.setContainedActions = function(actions) {
            this.containedIds.setActions(actions);
        };
        prototype.getParentIds = function() {
            return this.parentIds;
        };
        prototype.setParentActions = function(actions) {
            this.parentIds.setActions(actions);
        };
        prototype.getChildIds = function() {
            return this.childIds;
        };
        prototype.setChildActions = function(actions) {
            this.childIds.setActions(actions);
        };
        prototype.getAttachments = function() {
            return this.attachments;
        };
        prototype.setAttachmentsActions = function(actions) {
            this.attachments.setActions(actions);
        };
    }, {sampleId: "ISampleId", experimentId: {name: "FieldUpdateValue", arguments: ["IExperimentId"]}, spaceId: {name: "FieldUpdateValue", arguments: ["ISpaceId"]}, tagIds: {name: "IdListUpdateValue", arguments: ["ITagId"]}, properties: {name: "Map", arguments: [null, null]}, containerId: {name: "FieldUpdateValue", arguments: ["ISampleId"]}, containedIds: {name: "IdListUpdateValue", arguments: ["ISampleId"]}, parentIds: {name: "IdListUpdateValue", arguments: ["ISampleId"]}, childIds: {name: "IdListUpdateValue", arguments: ["ISampleId"]}, attachments: "AttachmentListUpdateValue"});
    return SampleUpdate;
})