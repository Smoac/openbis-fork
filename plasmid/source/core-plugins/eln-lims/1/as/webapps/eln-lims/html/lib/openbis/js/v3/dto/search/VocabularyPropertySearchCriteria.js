/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/VocabularyFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, VocabularyFieldSearchCriteria) {
	var VocabularyPropertySearchCriteria = function(fieldName) {
		VocabularyFieldSearchCriteria.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(VocabularyPropertySearchCriteria, VocabularyFieldSearchCriteria, [ VocabularyFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.VocabularyPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return VocabularyPropertySearchCriteria;
})