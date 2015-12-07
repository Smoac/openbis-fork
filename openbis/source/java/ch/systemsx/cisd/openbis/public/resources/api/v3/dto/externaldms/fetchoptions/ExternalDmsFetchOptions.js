/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/externaldms/fetchoptions/ExternalDmsSortOptions" ], function(require, stjs, FetchOptions) {
	var ExternalDmsFetchOptions = function() {
	};
	stjs.extend(ExternalDmsFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.externaldms.fetchoptions.ExternalDmsFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExternalDmsSortOptions = require("dto/externaldms/fetchoptions/ExternalDmsSortOptions");
				this.sort = new ExternalDmsSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "ExternalDmsSortOptions"
	});
	return ExternalDmsFetchOptions;
})