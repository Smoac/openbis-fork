/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/StringFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var StringPropertySearchCriteria = function(fieldName) {
		StringFieldSearchCriteria.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(StringPropertySearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StringPropertySearchCriteria;
})