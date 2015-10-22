define([ "stjs", 	"dto/search/AbstractFieldSearchCriteria", 
         			"dto/search/NumberEqualToValue", 
         			"dto/search/NumberLessThanValue", 
         			"dto/search/NumberLessOrEqualThanValue", 
         			"dto/search/NumberGreaterThanValue", 
         			"dto/search/NumberGreaterOrEqualThanValue" ], function(stjs, 	AbstractFieldSearchCriteria,
         																			NumberEqualToValue,
         																			NumberLessThanValue,
         																			NumberLessOrEqualThanValue,
         																			NumberGreaterThanValue,
         																			NumberGreaterOrEqualThanValue) {
	var NumberFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(NumberFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		prototype.equalTo = function(number) {
			this.setFieldValue(new NumberEqualToValue(number));
		};
		prototype.thatIsLessThan = function(number) {
			this.setFieldValue(new NumberLessThanValue(number));
		};
		prototype.thatIsLessOrEqualThan = function(number) {
			this.setFieldValue(new NumberLessOrEqualThanValue(number));
		};
		prototype.thatIsGreaterThan = function(number) {
			this.setFieldValue(new NumberGreaterThanValue(number));
		};
		prototype.thatIsGreaterOrEqualThan = function(number) {
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