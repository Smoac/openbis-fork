/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/DateFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, DateFieldSearchCriteria, SearchFieldType) {
	var RegistrationDateSearchCriteria = function() {
		DateFieldSearchCriteria.call(this, "registration_date", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(RegistrationDateSearchCriteria, DateFieldSearchCriteria, [ DateFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.RegistrationDateSearchCriteria';
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
	return RegistrationDateSearchCriteria;
})