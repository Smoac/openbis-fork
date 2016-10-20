/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/common/update/IdListUpdateValue", "as/dto/attachment/update/AttachmentListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue,
		AttachmentListUpdateValue) {
	var ExperimentUpdate = function() {
		this.properties = {};
		this.projectId = new FieldUpdateValue();
		this.tagIds = new IdListUpdateValue();
		this.attachments = new AttachmentListUpdateValue();
	};
	stjs.extend(ExperimentUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.experiment.update.ExperimentUpdate';
		constructor.serialVersionUID = 1;
		prototype.experimentId = null;

		prototype.properties = null;
		prototype.projectId = null;
		prototype.tagIds = null;
		prototype.attachments = null;

		prototype.getObjectId = function() {
			return this.getExperimentId();
		};
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId = experimentId;
		};
		prototype.getProperty = function(propertyName) {
			return this.properties[propertyName];
		};
		prototype.setProperty = function(propertyName, propertyValue) {
			this.properties[propertyName] = propertyValue;
		};
		prototype.getProperties = function() {
			return this.properties;
		};
		prototype.setProperties = function(properties) {
			this.properties = properties;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId.setValue(projectId);
		};
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.getTagIds = function() {
			return this.tagIds;
		};
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachmentsActions = function(actions) {
			this.attachments.setActions(actions);
		};
	}, {
		experimentId : "IExperimentId",
		properties : {
			name : "Map",
			arguments : [ null, null ]
		},
		projectId : {
			name : "FieldUpdateValue",
			arguments : [ "IProjectId" ]
		},
		tagIds : {
			name : "IdListUpdateValue",
			arguments : [ "ITagId" ]
		},
		attachments : "AttachmentListUpdateValue"
	});
	return ExperimentUpdate;
})