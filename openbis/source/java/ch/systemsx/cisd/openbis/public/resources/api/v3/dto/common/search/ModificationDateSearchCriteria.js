/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/DateFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, DateFieldSearchCriteria, SearchFieldType) {
	var ModificationDateSearchCriteria = function() {
		DateFieldSearchCriteria.call(this, "modification_date", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(ModificationDateSearchCriteria, DateFieldSearchCriteria, [ DateFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.ModificationDateSearchCriteria';
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
	return ModificationDateSearchCriteria;
})