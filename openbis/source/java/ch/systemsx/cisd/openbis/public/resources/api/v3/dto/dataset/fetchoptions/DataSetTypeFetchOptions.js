/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/common/fetchoptions/FetchOptions", "dto/dataset/fetchoptions/DataSetTypeSortOptions" ], function(stjs, FetchOptions) {
	var DataSetTypeFetchOptions = function() {
	};
	stjs.extend(DataSetTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.fetchoptions.DataSetTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var DataSetTypeSortOptions = require("dto/dataset/fetchoptions/DataSetTypeSortOptions");
				this.sort = new DataSetTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "DataSetTypeSortOptions"
	});
	return DataSetTypeFetchOptions;
})