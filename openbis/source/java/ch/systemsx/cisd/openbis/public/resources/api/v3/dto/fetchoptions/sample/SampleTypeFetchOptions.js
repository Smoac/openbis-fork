/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/sample/SampleTypeSortOptions" ], function(stjs, FetchOptions) {
	var SampleTypeFetchOptions = function() {
	};
	stjs.extend(SampleTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.sample.SampleTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var SampleTypeSortOptions = require("dto/fetchoptions/sample/SampleTypeSortOptions");
				this.sort = new SampleTypeSortOptions();
			}
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "SampleTypeSortOptions"
	});
	return SampleTypeFetchOptions;
})