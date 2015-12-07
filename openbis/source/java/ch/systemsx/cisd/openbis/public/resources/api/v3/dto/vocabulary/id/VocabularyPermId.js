/**
 * Vocabulary perm id.
 * 
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/ObjectPermId", "dto/vocabulary/id/IVocabularyId" ], function(stjs, ObjectPermId, IVocabularyId) {
	/**
	 * @param permId
	 *            Vocabulary perm id, e.g. "MY_VOCABULARY".
	 */
	var VocabularyPermId = function(permId) {
		ObjectPermId.call(this, permId);
	};
	stjs.extend(VocabularyPermId, ObjectPermId, [ ObjectPermId, IVocabularyId ], function(constructor, prototype) {
		prototype['@type'] = 'dto.vocabulary.id.VocabularyPermId';
		constructor.serialVersionUID = 1;
	}, {});
	return VocabularyPermId;
})