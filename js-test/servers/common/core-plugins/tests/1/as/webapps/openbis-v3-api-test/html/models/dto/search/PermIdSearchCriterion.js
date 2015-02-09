/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/StringFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriterion, SearchFieldType) {
	var PermIdSearchCriterion = function() {
		StringFieldSearchCriterion.call(this, "perm id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PermIdSearchCriterion, StringFieldSearchCriterion, [ StringFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.PermIdSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PermIdSearchCriterion;
})