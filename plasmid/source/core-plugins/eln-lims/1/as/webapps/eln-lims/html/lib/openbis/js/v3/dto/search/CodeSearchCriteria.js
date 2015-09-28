/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/StringFieldSearchCriteria", "dto/search/SearchFieldType" ], function(stjs, StringFieldSearchCriteria, SearchFieldType) {
	var CodeSearchCriteria = function() {
		StringFieldSearchCriteria.call(this, "code", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(CodeSearchCriteria, StringFieldSearchCriteria, [ StringFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.CodeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return CodeSearchCriteria;
})