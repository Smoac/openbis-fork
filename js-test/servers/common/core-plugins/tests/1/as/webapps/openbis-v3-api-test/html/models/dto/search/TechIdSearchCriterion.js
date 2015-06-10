/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/NumberFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriterion, SearchFieldType) {
	var TechIdSearchCriterion = function() {
		NumberFieldSearchCriterion.call(this, "id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(TechIdSearchCriterion, NumberFieldSearchCriterion, [ NumberFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.TechIdSearchCriterion';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return TechIdSearchCriterion;
})