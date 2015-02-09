define([ "support/stjs", "dto/search/AbstractFieldSearchCriterion" ], function(stjs, AbstractFieldSearchCriterion) {
	var VocabularyFieldSearchCriterion = function(fieldName, fieldType) {
		AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
	};
	stjs.extend(VocabularyFieldSearchCriterion, AbstractFieldSearchCriterion, [ AbstractFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.VocabularyFieldSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return VocabularyFieldSearchCriterion;
})