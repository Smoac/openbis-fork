define([ "support/stjs", "dto/search/AbstractFieldSearchCriterion", "dto/search/NumberEqualToValue" ], function(stjs, AbstractFieldSearchCriterion, NumberEqualToValue) {
	var NumberFieldSearchCriterion = function(fieldName, fieldType) {
		AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
	};
	stjs.extend(NumberFieldSearchCriterion, AbstractFieldSearchCriterion, [ AbstractFieldSearchCriterion ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberFieldSearchCriterion';
		constructor.serialVersionUID = 1;
		prototype.equalTo = function(number) {
			this.setFieldValue(new NumberEqualToValue(number));
		};
	}, {
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});
	return NumberFieldSearchCriterion;
})