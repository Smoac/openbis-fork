/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/dataset/FileFormatTypeSortOptions" ], function(stjs, FetchOptions) {
	var FileFormatTypeFetchOptions = function() {
	};
	stjs.extend(FileFormatTypeFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.dataset.FileFormatTypeFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.sort = null;
		prototype.sortBy = function() {
			if (this.sort == null) {
				var FileFormatTypeSortOptions = require("dto/fetchoptions/dataset/FileFormatTypeSortOptions");
				this.sort = new FileFormatTypeSortOptions();
			}
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		sort : "FileFormatTypeSortOptions"
	});
	return FileFormatTypeFetchOptions;
})