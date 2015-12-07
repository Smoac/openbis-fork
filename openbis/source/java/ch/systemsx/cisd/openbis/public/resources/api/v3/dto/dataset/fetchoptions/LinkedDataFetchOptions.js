/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/externaldms/fetchoptions/ExternalDmsFetchOptions", "dto/dataset/fetchoptions/LinkedDataSortOptions" ], function(require, stjs,
		FetchOptions) {
	var LinkedDataFetchOptions = function() {
	};
	stjs.extend(LinkedDataFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.dataset.fetchoptions.LinkedDataFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.externalDms = null;
		prototype.sort = null;
		prototype.withExternalDms = function() {
			if (this.externalDms == null) {
				var ExternalDmsFetchOptions = require("dto/externaldms/fetchoptions/ExternalDmsFetchOptions");
				this.externalDms = new ExternalDmsFetchOptions();
			}
			return this.externalDms;
		};
		prototype.withExternalDmsUsing = function(fetchOptions) {
			return this.externalDms = fetchOptions;
		};
		prototype.hasExternalDms = function() {
			return this.externalDms != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var LinkedDataSortOptions = require("dto/dataset/fetchoptions/LinkedDataSortOptions");
				this.sort = new LinkedDataSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		externalDms : "ExternalDmsFetchOptions",
		sort : "LinkedDataSortOptions"
	});
	return LinkedDataFetchOptions;
})