/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/person/fetchoptions/PersonFetchOptions", "dto/history/fetchoptions/HistoryEntrySortOptions" ], function(require, stjs,
		FetchOptions) {
	var HistoryEntryFetchOptions = function() {
	};
	stjs.extend(HistoryEntryFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.history.fetchoptions.HistoryEntryFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.author = null;
		prototype.sort = null;
		prototype.withAuthor = function() {
			if (this.author == null) {
				var PersonFetchOptions = require("dto/person/fetchoptions/PersonFetchOptions");
				this.author = new PersonFetchOptions();
			}
			return this.author;
		};
		prototype.withAuthorUsing = function(fetchOptions) {
			return this.author = fetchOptions;
		};
		prototype.hasAuthor = function() {
			return this.author != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var HistoryEntrySortOptions = require("dto/history/fetchoptions/HistoryEntrySortOptions");
				this.sort = new HistoryEntrySortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		author : "PersonFetchOptions",
		sort : "HistoryEntrySortOptions"
	});
	return HistoryEntryFetchOptions;
})