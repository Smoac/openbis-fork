/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/dataset/LocatorTypeSortOptions" ], function(stjs, FetchOptions) {
	var LocatorTypeFetchOptions = function() {
	};
	stjs.extend(LocatorTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.LocatorTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var LocatorTypeSortOptions = require("dto/fetchoptions/dataset/LocatorTypeSortOptions");
				this.sort = new LocatorTypeSortOptions();
			}
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "LocatorTypeSortOptions"
	});
	return LocatorTypeFetchOptions;
})