define([ "require", "stjs", "dto/common/search/AbstractFieldSearchCriteria", "dto/common/search/NumberEqualToValue", "dto/common/search/NumberLessThanValue",
		"dto/common/search/NumberLessThanOrEqualToValue", "dto/common/search/NumberGreaterThanValue", "dto/common/search/NumberGreaterThanOrEqualToValue" ], function(require, stjs,
		AbstractFieldSearchCriteria) {
	var NumberFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(NumberFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.NumberFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.equalTo = function(number) {
			var NumberEqualToValue = require("dto/common/search/NumberEqualToValue");
			this.setFieldValue(new NumberEqualToValue(number));
		};
		prototype.thatIsLessThan = function(number) {
			var NumberLessThanValue = require("dto/common/search/NumberLessThanValue");
			this.setFieldValue(new NumberLessThanValue(number));
		};
		prototype.thatIsLessThanOrEqualTo = function(number) {
			var NumberLessThanOrEqualToValue = require("dto/common/search/NumberLessThanOrEqualToValue");
			this.setFieldValue(new NumberLessThanOrEqualToValue(number));
		};
		prototype.thatIsGreaterThan = function(number) {
			var NumberGreaterThanValue = require("dto/common/search/NumberGreaterThanValue");
			this.setFieldValue(new NumberGreaterThanValue(number));
		};
		prototype.thatIsGreaterThanOrEqualTo = function(number) {
			var NumberGreaterThanOrEqualToValue = require("dto/common/search/NumberGreaterThanOrEqualToValue");
			this.setFieldValue(new NumberGreaterThanOrEqualToValue(number));
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberFieldSearchCriteria;
})