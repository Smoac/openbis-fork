/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/person/PersonFetchOptions", "dto/fetchoptions/tag/TagSortOptions" ], function(require, stjs, FetchOptions) {
	var TagFetchOptions = function() {
	};
	stjs.extend(TagFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.tag.TagFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.owner = null;
		prototype.sort = null;
		prototype.withOwner = function() {
			if (this.owner == null) {
				var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
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
				var TagSortOptions = require("dto/fetchoptions/tag/TagSortOptions");
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