define([ "stjs", "dto/search/AbstractFieldSearchCriteria" ], function(stjs, AbstractFieldSearchCriteria) {
	var VocabularyFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(VocabularyFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.VocabularyFieldSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return VocabularyFieldSearchCriteria;
})