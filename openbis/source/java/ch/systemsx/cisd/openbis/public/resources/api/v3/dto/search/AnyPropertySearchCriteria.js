/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/StringFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var AnyPropertySearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "any", SearchFieldType.ANY_PROPERTY);
	};
	stjs.extend(AnyPropertySearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AnyPropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return AnyPropertySearchCriteria;
})