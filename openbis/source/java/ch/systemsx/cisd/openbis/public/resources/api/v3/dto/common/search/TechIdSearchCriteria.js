/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/NumberFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var TechIdSearchCriteria = function() {
		NumberFieldSearchCriteria.call(this, "id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(TechIdSearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.TechIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return TechIdSearchCriteria;
})