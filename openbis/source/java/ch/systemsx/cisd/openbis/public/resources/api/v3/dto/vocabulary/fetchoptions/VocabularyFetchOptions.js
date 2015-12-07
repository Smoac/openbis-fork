/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/common/fetchoptions/FetchOptions", "dto/person/fetchoptions/PersonFetchOptions", "dto/vocabulary/fetchoptions/VocabularySortOptions" ], function(require, stjs,
		FetchOptions) {
	var VocabularyFetchOptions = function() {
	};
	stjs.extend(VocabularyFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.vocabulary.fetchoptions.VocabularyFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("dto/person/fetchoptions/PersonFetchOptions");
				this.registrator = new PersonFetchOptions();
			}
			return this.registrator;
		};
		prototype.withRegistratorUsing = function(fetchOptions) {
			return this.registrator = fetchOptions;
		};
		prototype.hasRegistrator = function() {
			return this.registrator != null;
		};
		prototype.sortBy = function() {
			if (this.sort == null) {
				var VocabularySortOptions = require("dto/vocabulary/fetchoptions/VocabularySortOptions");
				this.sort = new VocabularySortOptions();
			}
			return this.sort;
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		registrator : "PersonFetchOptions",
		sort : "VocabularySortOptions"
	});
	return VocabularyFetchOptions;
})