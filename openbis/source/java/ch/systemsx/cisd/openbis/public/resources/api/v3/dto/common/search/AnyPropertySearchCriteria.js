/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/StringFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AnyPropertySearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any", SearchFieldType.ANY_PROPERTY);
	};
	stjs.extend(AnyPropertySearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.AnyPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyPropertySearchCriteria;
})