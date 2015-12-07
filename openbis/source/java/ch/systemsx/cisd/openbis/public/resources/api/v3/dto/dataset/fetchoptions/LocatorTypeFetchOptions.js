/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/common/fetchoptions/FetchOptions", "dto/dataset/fetchoptions/LocatorTypeSortOptions" ], function(stjs, FetchOptions) {
	var LocatorTypeFetchOptions = function() {
	};
	stjs.extend(LocatorTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.fetchoptions.LocatorTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var LocatorTypeSortOptions = require("dto/dataset/fetchoptions/LocatorTypeSortOptions");
				this.sort = new LocatorTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "LocatorTypeSortOptions"
	});
	return LocatorTypeFetchOptions;
})