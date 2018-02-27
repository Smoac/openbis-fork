define([ "stjs", "as/dto/common/search/EnumFieldSearchCriteria", "as/dto/common/search/SearchFieldType" ], function(stjs, EnumFieldSearchCriteria, SearchFieldType) {
	var ScriptTypeSearchCriteria = function() {
		EnumFieldSearchCriteria.call(this, "script type", SearchFieldType.ATTRIBUTE);
	};
	stjs.extend(ScriptTypeSearchCriteria, EnumFieldSearchCriteria, [ EnumFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.plugin.search.ScriptTypeSearchCriteria';
		constructor.serialVersionUID = 1;
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return ScriptTypeSearchCriteria;
})
