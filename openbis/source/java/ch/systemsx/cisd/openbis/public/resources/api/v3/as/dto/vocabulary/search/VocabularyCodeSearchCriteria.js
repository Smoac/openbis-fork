/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var VocabularyCodeSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "vocabularyCode", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(VocabularyCodeSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.VocabularyCodeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
//		fieldType : {
//			name : "Enum",
//			arguments : [ "SearchFieldType" ]
//		}
	});
	return VocabularyCodeSearchCriteria;
})