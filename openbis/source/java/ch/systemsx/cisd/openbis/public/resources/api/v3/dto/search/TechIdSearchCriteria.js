/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/NumberFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, NumberFieldSearchCriteria, SearchFieldType) {
	var TechIdSearchCriteria = function() {
		NumberFieldSearchCriteria.call(this, "id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(TechIdSearchCriteria, NumberFieldSearchCriteria, [ NumberFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.TechIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return TechIdSearchCriteria;
})