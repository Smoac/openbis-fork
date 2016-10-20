/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue", "as/dto/attachment/update/AttachmentListUpdateValue" ], function(stjs, FieldUpdateValue, AttachmentListUpdateValue) {
	var ProjectUpdate = function() {
		this.spaceId = new FieldUpdateValue();
		this.description = new FieldUpdateValue();
		this.attachments = new AttachmentListUpdateValue();
	};
	stjs.extend(ProjectUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.project.update.ProjectUpdate';
		constructor.serialVersionUID = 1;
		prototype.projectId = null;
		prototype.spaceId = null;
		prototype.description = null;
		prototype.attachments = null;

		prototype.getObjectId = function() {
			return this.getProjectId();
		};
		prototype.getProjectId = function() {
			return this.projectId;
		};
		prototype.setProjectId = function(projectId) {
			this.projectId = projectId;
		};
		prototype.getSpaceId = function() {
			return this.spaceId;
		};
		prototype.setSpaceId = function(spaceId) {
			this.spaceId.setValue(spaceId);
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.getAttachments = function() {
			return this.attachments;
		};
		prototype.setAttachmentsActions = function(actions) {
			this.attachments.setActions(actions);
		};
	}, {
		projectId : "IProjectId",
		spaceId : {
			name : "FieldUpdateValue",
			arguments : [ "ISpaceId" ]
		},
		description : {
			name : "FieldUpdateValue",
			arguments : [ null ]
		},
		attachments : "AttachmentListUpdateValue"
	});
	return ProjectUpdate;
})