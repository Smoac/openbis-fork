/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/common/fetchoptions/FetchOptions", "dto/sample/fetchoptions/SampleTypeSortOptions" ], function(stjs, FetchOptions) {
	var SampleTypeFetchOptions = function() {
	};
	stjs.extend(SampleTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.sample.fetchoptions.SampleTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SampleTypeSortOptions = require("dto/sample/fetchoptions/SampleTypeSortOptions");
				this.sort = new SampleTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "SampleTypeSortOptions"
	});
	return SampleTypeFetchOptions;
})