/**
 * @author pkupczyk
 */
define([ "stjs" ], function(stjs) {
	var DataSetArchiveOptions = function() {
	};
	stjs.extend(DataSetArchiveOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.dataset.archive.DataSetArchiveOptions';
		constructor.serialVersionUID = 1;
		prototype.removeFromDataStore = true;
		prototype.isRemoveFromDataStore = function() {
			return this.removeFromDataStore;
		};
		prototype.setRemoveFromDataStore = function(removeFromDataStore) {
			this.removeFromDataStore = removeFromDataStore;
		};
	}, {});
	return DataSetArchiveOptions;
})