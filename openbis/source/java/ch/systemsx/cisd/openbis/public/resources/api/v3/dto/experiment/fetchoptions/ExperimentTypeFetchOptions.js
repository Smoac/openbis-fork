/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/common/fetchoptions/FetchOptions", "dto/experiment/fetchoptions/ExperimentTypeSortOptions" ], function(stjs, FetchOptions) {
	var ExperimentTypeFetchOptions = function() {
	};
	stjs.extend(ExperimentTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.experiment.fetchoptions.ExperimentTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var ExperimentTypeSortOptions = require("dto/experiment/fetchoptions/ExperimentTypeSortOptions");
				this.sort = new ExperimentTypeSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "ExperimentTypeSortOptions"
	});
	return ExperimentTypeFetchOptions;
})