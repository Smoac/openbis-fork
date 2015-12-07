/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/datastore/fetchoptions/DataStoreSortOptions" ], function(require, stjs, FetchOptions) {
	var DataStoreFetchOptions = function() {
	};
	stjs.extend(DataStoreFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.datastore.fetchoptions.DataStoreFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var DataStoreSortOptions = require("dto/datastore/fetchoptions/DataStoreSortOptions");
				this.sort = new DataStoreSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "DataStoreSortOptions"
	});
	return DataStoreFetchOptions;
})