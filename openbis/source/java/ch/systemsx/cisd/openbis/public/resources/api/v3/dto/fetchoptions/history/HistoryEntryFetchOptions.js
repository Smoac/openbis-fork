/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/person/PersonFetchOptions", "dto/fetchoptions/history/HistoryEntrySortOptions" ], function(require, stjs, FetchOptions) {
	var HistoryEntryFetchOptions = function() {
	};
	stjs.extend(HistoryEntryFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.history.HistoryEntryFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.author = null;
		prototype.sort = null;
		prototype.withAuthor = function() {
			if (this.author == null) {
				var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
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
				var HistoryEntrySortOptions = require("dto/fetchoptions/history/HistoryEntrySortOptions");
				this.sort = new HistoryEntrySortOptions();
			}
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