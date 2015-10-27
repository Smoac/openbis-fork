define([ "require", "stjs", "dto/search/AbstractFieldSearchCriteria",
         "dto/search/NumberEqualToValue", "dto/search/NumberLessThanValue", "dto/search/NumberLessThanOrEqualToValue",
         "dto/search/NumberGreaterThanValue", "dto/search/NumberGreaterThanOrEqualToValue"], 
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
		prototype.thatIsLessThanOrEqualTo = function(number) {
			var NumberLessThanOrEqualToValue = require("dto/search/NumberLessThanOrEqualToValue");
			this.setFieldValue(new NumberLessThanOrEqualToValue(number));
		};
		prototype.thatIsGreaterThan = function(number) {
			var NumberGreaterThanValue = require("dto/search/NumberGreaterThanValue");
			this.setFieldValue(new NumberGreaterThanValue(number));
		};
		prototype.thatIsGreaterThanOrEqualTo = function(number) {
			var NumberGreaterThanOrEqualToValue = require("dto/search/NumberGreaterThanOrEqualToValue");
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