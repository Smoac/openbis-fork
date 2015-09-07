/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/material/MaterialTypeSortOptions" ], function(stjs, FetchOptions) {
	var MaterialTypeFetchOptions = function() {
	};
	stjs.extend(MaterialTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.material.MaterialTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var MaterialTypeSortOptions = require("dto/fetchoptions/material/MaterialTypeSortOptions");
				this.sort = new MaterialTypeSortOptions();
			}
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "MaterialTypeSortOptions"
	});
	return MaterialTypeFetchOptions;
})