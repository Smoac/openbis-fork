/**
 * @author pkupczyk
 */
define([ "support/stjs", "sys/exceptions" ], function(stjs, exceptions) {
	var Deletion = function() {
		this.deletedObjects = [];
	};
	stjs.extend(Deletion, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.deletion.Deletion';
		constructor.serialVersionUID = 1;
		prototype.fetchOptions = null;
		prototype.id = null;
		prototype.reason = null;
		prototype.deletedObjects = null;
		prototype.getFetchOptions = function() {
			return this.fetchOptions;
		};
		prototype.setFetchOptions = function(fetchOptions) {
			this.fetchOptions = fetchOptions;
		};
		prototype.getId = function() {
			return this.id;
		};
		prototype.setId = function(id) {
			this.id = id;
		};
		prototype.getReason = function() {
			return this.reason;
		};
		prototype.setReason = function(reason) {
			this.reason = reason;
		};
		prototype.getDeletedObjects = function() {
			if (this.getFetchOptions().hasDeletedObjects()) {
				return this.deletedObjects;
			} else {
				throw new exceptions.NotFetchedException("Deleted objects have not been fetched.");
			}
		};
		prototype.setDeletedObjects = function(deletedObjects) {
			this.deletedObjects = deletedObjects;
		};
	}, {
		fetchOptions : "DeletionFetchOptions",
		id : "IDeletionId",
		deletedObjects : {
			name : "List",
			arguments : [ "DeletedObject" ]
		}
	});
	return Deletion;
})