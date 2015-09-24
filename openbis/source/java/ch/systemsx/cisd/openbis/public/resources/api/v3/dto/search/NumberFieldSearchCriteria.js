define([ "stjs", "dto/search/AbstractFieldSearchCriteria", "dto/search/NumberEqualToValue" ], function(stjs, AbstractFieldSearchCriteria, NumberEqualToValue) {
	var NumberFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
	};
	stjs.extend(NumberFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberFieldSearchCriteria';
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
	return NumberFieldSearchCriteria;
})