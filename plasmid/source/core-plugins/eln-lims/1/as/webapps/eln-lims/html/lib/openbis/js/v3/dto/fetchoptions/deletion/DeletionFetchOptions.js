/**
 * @author pkupczyk
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/deletion/DeletedObjectFetchOptions", "dto/fetchoptions/deletion/DeletionSortOptions" ], function(require, stjs,
		FetchOptions) {
	var DeletionFetchOptions = function() {
	};
	stjs.extend(DeletionFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.deletion.DeletionFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.deletedObjects = null;
		prototype.sort = null;
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
		prototype.sortBy = function() {
			if (this.sort == null) {
				var DeletionSortOptions = require("dto/fetchoptions/deletion/DeletionSortOptions");
				this.sort = new DeletionSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		deletedObjects : "DeletedObjectFetchOptions",
		sort : "DeletionSortOptions"
	});
	return DeletionFetchOptions;
})