/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/StringFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AnyFieldSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any", SearchFieldType.ANY_FIELD);
	};
	stjs.extend(AnyFieldSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.AnyFieldSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyFieldSearchCriteria;
})