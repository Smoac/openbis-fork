define([ "stjs", "dto/search/AbstractFieldSearchCriteria", "dto/search/AnyStringValue", "dto/search/StringEqualToValue", "dto/search/StringStartsWithValue", "dto/search/StringEndsWithValue",
		"dto/search/StringContainsValue" ], function(stjs, AbstractFieldSearchCriteria, AnyStringValue, StringEqualToValue, StringStartsWithValue, StringEndsWithValue, StringContainsValue) {
	var StringFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		this.setFieldValue(new AnyStringValue());
	};
	stjs.extend(StringFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.thatEquals = function(string) {
			this.setFieldValue(new StringEqualToValue(string));
		};
		prototype.thatStartsWith = function(prefix) {
			this.setFieldValue(new StringStartsWithValue(prefix));
		};
		prototype.thatEndsWith = function(suffix) {
			this.setFieldValue(new StringEndsWithValue(suffix));
		};
		prototype.thatContains = function(string) {
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