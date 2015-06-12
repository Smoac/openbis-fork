/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/person/PersonFetchOptions" ], function(require, stjs) {
	var HistoryEntryFetchOptions = function() {
	};
	stjs.extend(HistoryEntryFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.history.HistoryEntryFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.author = null;
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
	}, {
		author : "PersonFetchOptions"
	});
	return HistoryEntryFetchOptions;
})