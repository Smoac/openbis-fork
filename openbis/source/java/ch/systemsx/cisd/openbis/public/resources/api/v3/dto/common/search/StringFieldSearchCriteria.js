define([ "require", "stjs", "dto/common/search/AbstractFieldSearchCriteria", "dto/common/search/StringEqualToValue", "dto/common/search/StringStartsWithValue",
		"dto/common/search/StringEndsWithValue", "dto/common/search/StringContainsValue", "dto/common/search/AnyStringValue" ], function(require, stjs, AbstractFieldSearchCriteria) {
	var StringFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		var AnyStringValue = require("dto/common/search/AnyStringValue");
		this.setFieldValue(new AnyStringValue());
	};
	stjs.extend(StringFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.StringFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.thatEquals = function(string) {
			var StringEqualToValue = require("dto/common/search/StringEqualToValue");
			this.setFieldValue(new StringEqualToValue(string));
		};
		prototype.thatStartsWith = function(prefix) {
			var StringStartsWithValue = require("dto/common/search/StringStartsWithValue");
			this.setFieldValue(new StringStartsWithValue(prefix));
		};
		prototype.thatEndsWith = function(suffix) {
			var StringEndsWithValue = require("dto/common/search/StringEndsWithValue");
			this.setFieldValue(new StringEndsWithValue(suffix));
		};
		prototype.thatContains = function(string) {
			var StringContainsValue = require("dto/common/search/StringContainsValue");
			this.setFieldValue(new StringContainsValue(string));
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return StringFieldSearchCriteria;
})