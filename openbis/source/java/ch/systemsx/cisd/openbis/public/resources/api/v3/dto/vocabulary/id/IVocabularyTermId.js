/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/id/IObjectId" ], function(stjs, IObjectId) {
	var IVocabularyTermId = function() {
	};
	stjs.extend(IVocabularyTermId, null, [ IObjectId ], null, {});
	return IVocabularyTermId;
})