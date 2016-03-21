/**
 * @author pkupczyk
 */
define([ "stjs", "as/dto/common/search/StringFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var VocabularyTermCodeSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any", SearchFieldType.ANY_FIELD);
	};
	stjs.extend(VocabularyTermCodeSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.vocabulary.search.VocabularyTermCodeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
//		fieldType : {
//			name : "Enum",
//			arguments : [ "SearchFieldType" ]
//		}
	});
	return VocabularyTermCodeSearchCriteria;
})