/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/FetchOptions", "dto/fetchoptions/vocabulary/VocabularyFetchOptions", "dto/fetchoptions/person/PersonFetchOptions",
		"dto/fetchoptions/vocabulary/VocabularyTermSortOptions" ], function(require, stjs, FetchOptions) {
	var VocabularyTermFetchOptions = function() {
	};
	stjs.extend(VocabularyTermFetchOptions, FetchOptions, [ FetchOptions ], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.vocabulary.VocabularyTermFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.vocabulary = null;
		prototype.registrator = null;
		prototype.sort = null;
		prototype.withVocabulary = function() {
			if (this.vocabulary == null) {
				var VocabularyFetchOptions = require("dto/fetchoptions/vocabulary/VocabularyFetchOptions");
				this.vocabulary = new VocabularyFetchOptions();
			}
			return this.vocabulary;
		};
		prototype.withVocabularyUsing = function(fetchOptions) {
			return this.vocabulary = fetchOptions;
		};
		prototype.hasVocabulary = function() {
			return this.vocabulary != null;
		};
		prototype.withRegistrator = function() {
			if (this.registrator == null) {
				var PersonFetchOptions = require("dto/fetchoptions/person/PersonFetchOptions");
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
				var VocabularyTermSortOptions = require("dto/fetchoptions/vocabulary/VocabularyTermSortOptions");
				this.sort = new VocabularyTermSortOptions();
			}
		};
		prototype.getSortBy = function() {
			return this.sort;
		};
	}, {
		vocabulary : "VocabularyFetchOptions",
		registrator : "PersonFetchOptions",
		sort : "VocabularyTermSortOptions"
	});
	return VocabularyTermFetchOptions;
})