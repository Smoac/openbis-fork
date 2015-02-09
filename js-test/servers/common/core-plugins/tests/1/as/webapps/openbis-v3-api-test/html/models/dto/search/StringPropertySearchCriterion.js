/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/StringFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriterion, SearchFieldType) {
	var StringPropertySearchCriterion = function(fieldName) {
		StringFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(StringPropertySearchCriterion, StringFieldSearchCriterion, [ StringFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringPropertySearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StringPropertySearchCriterion;
})