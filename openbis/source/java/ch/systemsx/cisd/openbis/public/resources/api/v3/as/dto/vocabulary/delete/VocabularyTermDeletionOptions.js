define([ "stjs", "as/dto/deletion/AbstractObjectDeletionOptions" ], function(stjs, AbstractObjectDeletionOptions) {
	var VocabularyTermDeletionOptions = function() {
		AbstractObjectDeletionOptions.call(this);
	};
	stjs.extend(VocabularyTermDeletionOptions, AbstractObjectDeletionOptions, [ AbstractObjectDeletionOptions ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.delete.VocabularyTermDeletionOptions';
		constructor.serialVersionUID = 1;
		prototype.replacements = null;
		prototype.replace = function(termId, replacementId) {
			this.replacements[termId] = replacementId;
		};
		prototype.getReplacements = function() {
			return this.replacements;
		};
	}, {});
	return VocabularyTermDeletionOptions;
})
