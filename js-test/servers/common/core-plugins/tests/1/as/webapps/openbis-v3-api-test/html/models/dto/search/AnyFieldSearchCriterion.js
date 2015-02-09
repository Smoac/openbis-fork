/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/StringFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriterion, SearchFieldType) {
	var AnyFieldSearchCriterion = function() {
		StringFieldSearchCriterion.call(this, "any", SearchFieldType.ANY_FIELD);
	};
	stjs.extend(AnyFieldSearchCriterion, StringFieldSearchCriterion, [ StringFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AnyFieldSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyFieldSearchCriterion;
})