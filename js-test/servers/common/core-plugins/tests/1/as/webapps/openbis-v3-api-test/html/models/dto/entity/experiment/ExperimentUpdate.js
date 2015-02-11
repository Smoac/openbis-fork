/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/entity/FieldUpdateValue", "dto/entity/IdListUpdateValue", "dto/entity/AttachmentListUpdateValue" ], function(stjs, FieldUpdateValue, IdListUpdateValue,
		AttachmentListUpdateValue) {
	var ExperimentUpdate = function() {
		this.properties = {};
		this.projectId = new FieldUpdateValue();
		this.tagIds = new IdListUpdateValue();
		this.attachments = new AttachmentListUpdateValue();
	};
	stjs.extend(ExperimentUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.entity.experiment.ExperimentUpdate';
		constructor.serialVersionUID = 1;
		prototype.experimentId = null;
		prototype.getExperimentId = function() {
			return this.experimentId;
		};
		prototype.setExperimentId = function(experimentId) {
			this.experimentId = experimentId;
		};
		prototype.setProperty = function(key, value) {
			this.properties[key] = value;
		};
		prototype.getProperties = function() {
			return this.properties;
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