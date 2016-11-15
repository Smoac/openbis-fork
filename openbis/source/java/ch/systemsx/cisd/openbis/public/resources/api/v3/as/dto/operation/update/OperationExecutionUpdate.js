/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/update/FieldUpdateValue" ], function(stjs, FieldUpdateValue) {
	var OperationExecutionUpdate = function() {
		this.description = new FieldUpdateValue();
		this.deleteSummary = false;
		this.deleteDetails = false;
	};
	stjs.extend(OperationExecutionUpdate, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.operation.update.OperationExecutionUpdate';
		constructor.serialVersionUID = 1;
		prototype.executionId = null;
		prototype.description = null;
		prototype.deleteSummary = null;
		prototype.deleteDetails = null;

		prototype.getObjectId = function() {
			return this.getExecutionId();
		};
		prototype.getExecutionId = function() {
			return this.executionId;
		};
		prototype.setExecutionId = function(executionId) {
			this.executionId = executionId;
		};
		prototype.getDescription = function() {
			return this.description;
		};
		prototype.setDescription = function(description) {
			this.description.setValue(description);
		};
		prototype.deleteSummary = function() {
			this.deleteSummary = true;
		};
		prototype.isDeleteSummary = function() {
			return this.deleteSummary;
		};
		prototype.deleteDetails = function() {
			this.deleteDetails = true;
		};
		prototype.isDeleteDetails = function() {
			return this.deleteDetails;
		};
	}, {
		executionId : "IOperationExecutionId",
		description : {
			name : "FieldUpdateValue",
			arguments : [ null ]
		}
	});
	return OperationExecutionUpdate;
})