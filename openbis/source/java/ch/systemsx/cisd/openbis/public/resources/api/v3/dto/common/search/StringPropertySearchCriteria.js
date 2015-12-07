/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/StringFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var StringPropertySearchCriteria = function(fieldName) {
		StringFieldSearchCriteria.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(StringPropertySearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.StringPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StringPropertySearchCriteria;
})