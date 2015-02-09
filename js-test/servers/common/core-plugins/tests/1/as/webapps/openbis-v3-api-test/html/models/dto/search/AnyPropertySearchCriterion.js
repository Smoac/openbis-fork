/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/StringFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriterion, SearchFieldType) {
	var AnyPropertySearchCriterion = function() {
		StringFieldSearchCriterion.call(this, "any", SearchFieldType.ANY_PROPERTY);
	};
	stjs.extend(AnyPropertySearchCriterion, StringFieldSearchCriterion, [ StringFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AnyPropertySearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyPropertySearchCriterion;
})