/**
 * Class automatically generated with
 * {@link ch.ethz.sis.openbis.generic.shared.api.v3.dto.generators.DtoGenerator}
 */
define([ "require", "stjs", "dto/fetchoptions/person/PersonFetchOptions" ], function(require, stjs) {
	var VocabularyFetchOptions = function() {
	};
	stjs.extend(VocabularyFetchOptions, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.fetchoptions.vocabulary.VocabularyFetchOptions';
		constructor.serialVersionUID = 1;
		prototype.registrator = null;
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
		registrator : "PersonFetchOptions"
	});
	return VocabularyFetchOptions;
})