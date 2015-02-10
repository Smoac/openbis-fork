/**
 * @author pkupczyk
 */
define([ "support/stjs" ], function(stjs) {
	var SampleCreation = function() {
		this.properties = {};
	};
	stjs.extend(SampleCreation, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.sample.SampleCreation';
		constructor.serialVersionUID = 1;
		prototype.typeId = null;
		prototype.experimentId = null;
		prototype.spaceId = null;
		prototype.code = null;
		prototype.tagIds = null;
		prototype.containerId = null;
		prototype.containedIds = null;
		prototype.parentIds = null;
		prototype.childIds = null;
		prototype.attachments = null;
		prototype.creationId = null;
		prototype.getTypeId = function() {
			return this.typeId;
		};
		prototype.setTypeId = function(typeId) {
			this.typeId = typeId;
		};
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId = experimentId;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId = spaceId;
		};
		prototype.getCode = function() {
			return this.code;
		};
		prototype.setCode = function(code) {
			this.code = code;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.setTagIds = function(tagIds) {
			this.tagIds = tagIds;
		};
		prototype.getContainerId = function() {
			return this.containerId;
		};
		prototype.setContainerId = function(containerId) {
			this.containerId = containerId;
		};
		prototype.getContainedIds = function() {
			return this.containedIds;
		};
		prototype.setContainedIds = function(containedIds) {
			this.containedIds = containedIds;
		};
		prototype.getParentIds = function() {
			return this.parentIds;
		};
		prototype.setParentIds = function(parentIds) {
			this.parentIds = parentIds;
		};
		prototype.getChildIds = function() {
			return this.childIds;
		};
		prototype.setChildIds = function(childIds) {
			this.childIds = childIds;
		};
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachments = function(attachments) {
			this.attachments = attachments;
		};
		prototype.setProperty = function(key, value) {
			this.properties[key] = value;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.getCreationId = function() {
			return this.creationId;
		};
		prototype.setCreationId = function(creationId) {
			this.creationId = creationId;
		};
	}, {
		typeId : "IEntityTypeId",
		experimentId : "IExperimentId",
		spaceId : "ISpaceId",
		tagIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		containerId : "ISampleId",
		containedIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		parentIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		childIds : {
			name : "List",
			arguments : [ "Object" ]
		},
		attachments : {
			name : "List",
			arguments : [ "AttachmentCreation" ]
		},
		creationId : "CreationId"
	});
	return SampleCreation;
})