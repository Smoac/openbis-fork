/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/DateFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, DateFieldSearchCriterion, SearchFieldType) {
	var DatePropertySearchCriterion = function(fieldName) {
		DateFieldSearchCriterion.call(this, fieldName, SearchFieldType.PROPERTY);
	};
	stjs.extend(DatePropertySearchCriterion, DateFieldSearchCriterion, [ DateFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DatePropertySearchCriterion';
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
	return DatePropertySearchCriterion;
})