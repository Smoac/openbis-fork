/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/NumberFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriterion, SearchFieldType) {
	var NumberPropertySearchCriterion = function(fieldName) {
		NumberFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(NumberPropertySearchCriterion, NumberFieldSearchCriterion, [ NumberFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberPropertySearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberPropertySearchCriterion;
})