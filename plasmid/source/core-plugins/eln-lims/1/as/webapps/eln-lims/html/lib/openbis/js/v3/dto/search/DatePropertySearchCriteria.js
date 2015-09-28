/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/DateFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, DateFieldSearchCriteria, SearchFieldType) {
	var DatePropertySearchCriteria = function(fieldName) {
		DateFieldSearchCriteria.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(DatePropertySearchCriteria, DateFieldSearchCriteria, [ DateFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DatePropertySearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		DATE_FORMATS : {
			name : "List",
			arguments : [ "IDateFormat" ]
		},
		timeZone : "ITimeZone",
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return DatePropertySearchCriteria;
})