define([ "require", "stjs", "dto/search/AbstractFieldSearchCriteria",
         "dto/search/StringEqualToValue", "dto/search/StringStartsWithValue", "dto/search/StringEndsWithValue",
         "dto/search/StringContainsValue", "dto/search/AnyStringValue"], 
		function(require, stjs, AbstractFieldSearchCriteria) {
	var StringFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		var AnyStringValue = require("dto/search/AnyStringValue");
		this.setFieldValue(new AnyStringValue());
	};
	stjs.extend(StringFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.thatEquals = function(string) {
			var StringEqualToValue = require("dto/search/StringEqualToValue");
			this.setFieldValue(new StringEqualToValue(string));
		};
		prototype.thatStartsWith = function(prefix) {
			var StringStartsWithValue = require("dto/search/StringStartsWithValue");
			this.setFieldValue(new StringStartsWithValue(prefix));
		};
		prototype.thatEndsWith = function(suffix) {
			var StringEndsWithValue = require("dto/search/StringEndsWithValue");
			this.setFieldValue(new StringEndsWithValue(suffix));
		};
		prototype.thatContains = function(string) {
			var StringContainsValue = require("dto/search/StringContainsValue");
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