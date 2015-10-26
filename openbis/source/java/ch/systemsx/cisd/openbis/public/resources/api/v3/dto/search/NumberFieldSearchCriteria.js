define([ "require", "stjs", "dto/search/AbstractFieldSearchCriteria",
         "dto/search/NumberEqualToValue", "dto/search/NumberLessThanValue", "dto/search/NumberLessOrEqualThanValue",
         "dto/search/NumberGreaterThanValue", "dto/search/NumberGreaterOrEqualThanValue"], 
		function(require, stjs, AbstractFieldSearchCriteria) {
	var NumberFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(NumberFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.equalTo = function(number) {
			var NumberEqualToValue = require("dto/search/NumberEqualToValue");
			this.setFieldValue(new NumberEqualToValue(number));
		};
		prototype.thatIsLessThan = function(number) {
			var NumberLessThanValue = require("dto/search/NumberLessThanValue");
			this.setFieldValue(new NumberLessThanValue(number));
		};
		prototype.thatIsLessOrEqualThan = function(number) {
			var NumberLessOrEqualThanValue = require("dto/search/NumberLessOrEqualThanValue");
			this.setFieldValue(new NumberLessOrEqualThanValue(number));
		};
		prototype.thatIsGreaterThan = function(number) {
			var NumberGreaterThanValue = require("dto/search/NumberGreaterThanValue");
			this.setFieldValue(new NumberGreaterThanValue(number));
		};
		prototype.thatIsGreaterOrEqualThan = function(number) {
			var NumberGreaterOrEqualThanValue = require("dto/search/NumberGreaterOrEqualThanValue");
			this.setFieldValue(new NumberGreaterOrEqualThanValue(number));
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberFieldSearchCriteria;
})