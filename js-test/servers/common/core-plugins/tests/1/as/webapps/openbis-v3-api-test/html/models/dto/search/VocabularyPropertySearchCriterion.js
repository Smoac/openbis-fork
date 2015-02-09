/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/VocabularyFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, VocabularyFieldSearchCriterion) {
	var VocabularyPropertySearchCriterion = function(fieldName) {
		VocabularyFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(VocabularyPropertySearchCriterion, VocabularyFieldSearchCriterion, [ VocabularyFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.VocabularyPropertySearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return VocabularyPropertySearchCriterion;
})