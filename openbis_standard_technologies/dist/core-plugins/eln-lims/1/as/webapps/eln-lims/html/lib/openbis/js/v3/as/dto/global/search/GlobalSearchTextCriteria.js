define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractFieldSearchCriteria", "as/dto/common/search/SearchFieldType", "as/dto/common/search/StringContainsValue",
		"as/dto/common/search/StringContainsExactlyValue" ], function(require, stjs, exceptions, AbstractFieldSearchCriteria, SearchFieldType) {
	var GlobalSearchTextCriteria = function() {
		AbstractFieldSearchCriteria.call(this, "anything", SearchFieldType.ANY_FIELD);
	};

	stjs.extend(GlobalSearchTextCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.global.search.GlobalSearchTextCriteria';
		constructor.serialVersionUID = 1;

		prototype.thatContains = function(str) {
			var StringContainsValue = require("as/dto/common/search/StringContainsValue");
			this.setFieldValue(new StringContainsValue(str));
		};
		prototype.thatContainsExactly = function(str) {
			var StringContainsExactlyValue = require("as/dto/common/search/StringContainsExactlyValue");
			this.setFieldValue(new StringContainsExactlyValue(str));
		};
	}, {});

	return GlobalSearchTextCriteria;
})
