/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/StringFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriterion, SearchFieldType) {
	var CodeSearchCriterion = function() {
		StringFieldSearchCriterion.call(this, "code", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(CodeSearchCriterion, StringFieldSearchCriterion, [ StringFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.CodeSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return CodeSearchCriterion;
})