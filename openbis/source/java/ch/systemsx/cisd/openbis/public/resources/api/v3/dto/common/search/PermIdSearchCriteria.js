/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/StringFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var PermIdSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "perm id", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(PermIdSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.PermIdSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return PermIdSearchCriteria;
})