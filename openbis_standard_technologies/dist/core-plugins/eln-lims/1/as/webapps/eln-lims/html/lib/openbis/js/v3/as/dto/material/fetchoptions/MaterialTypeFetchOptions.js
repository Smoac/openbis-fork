/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/material/fetchoptions/MaterialTypeSortOptions" ], function(stjs, FetchOptions) {
	var MaterialTypeFetchOptions = function() {
	};
	stjs.extend(MaterialTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.material.fetchoptions.MaterialTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var MaterialTypeSortOptions = require("as/dto/material/fetchoptions/MaterialTypeSortOptions");
				this.sort = new MaterialTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "MaterialTypeSortOptions"
	});
	return MaterialTypeFetchOptions;
})