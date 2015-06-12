/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/vocabulary/VocabularyFetchOptions", "dto/fetchoptions/person/PersonFetchOptions" ], function(require, stjs) {
	var VocabularyTermFetchOptions = function() {
	};
	stjs.extend(VocabularyTermFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.vocabulary.VocabularyTermFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.vocabulary = null;
		prototype.registrator = null;
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
	}, {
		vocabulary : "VocabularyFetchOptions",
		registrator : "PersonFetchOptions"
	});
	return VocabularyTermFetchOptions;
})