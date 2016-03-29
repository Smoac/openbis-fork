/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "as/dto/common/fetchoptions/FetchOptions", "as/dto/person/fetchoptions/PersonFetchOptions", "as/dto/tag/fetchoptions/TagSortOptions" ], function(require, stjs, FetchOptions) {
	var TagFetchOptions = function() {
	};
	stjs.extend(TagFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.tag.fetchoptions.TagFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.owner = null;
		prototype.sort = null;
		prototype.withOwner = function() {
			if (this.owner == null) {
				var PersonFetchOptions = require("as/dto/person/fetchoptions/PersonFetchOptions");
				this.owner = new PersonFetchOptions();
			}
			return this.owner;
		};
		prototype.withOwnerUsing = function(fetchOptions) {
			return this.owner = fetchOptions;
		};
		prototype.hasOwner = function() {
			return this.owner != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var TagSortOptions = require("as/dto/tag/fetchoptions/TagSortOptions");
				this.sort = new TagSortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		owner : "PersonFetchOptions",
		sort : "TagSortOptions"
	});
	return TagFetchOptions;
})