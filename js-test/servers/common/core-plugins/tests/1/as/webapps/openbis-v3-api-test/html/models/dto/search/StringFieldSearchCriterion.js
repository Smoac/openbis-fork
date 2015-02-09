define([ "support/stjs", "dto/search/AbstractFieldSearchCriterion", "dto/search/AnyStringValue", "dto/search/StringEqualToValue", "dto/search/StringStartsWithValue", "dto/search/StringEndsWithValue",
		"dto/search/StringContainsValue" ], function(stjs, AbstractFieldSearchCriterion, AnyStringValue, StringEqualToValue, StringStartsWithValue, StringEndsWithValue, StringContainsValue) {
	var StringFieldSearchCriterion = function(fieldName, fieldType) {
		AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
		this.setFieldValue(new AnyStringValue());
	};
	stjs.extend(StringFieldSearchCriterion, AbstractFieldSearchCriterion, [ AbstractFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringFieldSearchCriterion';
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
	return StringFieldSearchCriterion;
})