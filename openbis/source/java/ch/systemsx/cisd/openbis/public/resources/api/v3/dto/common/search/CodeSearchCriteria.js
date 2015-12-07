/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/StringFieldSearchCriteria", "dto/common/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var CodeSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "code", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(CodeSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.CodeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return CodeSearchCriteria;
})