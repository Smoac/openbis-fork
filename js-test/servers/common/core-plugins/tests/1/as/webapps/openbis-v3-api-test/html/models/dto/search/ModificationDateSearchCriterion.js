/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/DateFieldSearchCriterion", "dto/search/SearchFieldType" ], function(stjs, DateFieldSearchCriterion, SearchFieldType) {
	var ModificationDateSearchCriterion = function() {
		DateFieldSearchCriterion.call(this, "modification_date", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(ModificationDateSearchCriterion, DateFieldSearchCriterion, [ DateFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.ModificationDateSearchCriterion';
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
	return ModificationDateSearchCriterion;
})