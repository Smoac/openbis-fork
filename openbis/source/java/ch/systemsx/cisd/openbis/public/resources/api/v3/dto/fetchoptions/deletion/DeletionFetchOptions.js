/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/fetchoptions/deletion/DeletedObjectFetchOptions" ], function(require, stjs) {
	var DeletionFetchOptions = function() {
	};
	stjs.extend(DeletionFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.deletion.DeletionFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.deletedObjects = null;
		prototype.fetchDeletedObjects = function() {
			if (this.deletedObjects == null) {
				var DeletedObjectFetchOptions = require("dto/fetchoptions/deletion/DeletedObjectFetchOptions");
				this.deletedObjects = new DeletedObjectFetchOptions();
			}
			return this.deletedObjects;
		};
		prototype.hasDeletedObjects = function() {
			return this.deletedObjects != null;
		};
	}, {
		deletedObjects : "DeletedObjectFetchOptions"
	});
	return DeletionFetchOptions;
})