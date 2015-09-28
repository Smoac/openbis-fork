/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/NumberFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var NumberPropertySearchCriteria = function(fieldName) {
		NumberFieldSearchCriteria.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(NumberPropertySearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberPropertySearchCriteria;
})