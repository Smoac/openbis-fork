define([ "stjs", "dto/search/AbstractDateObjectValue" ], function(stjs, AbstractDateObjectValue) {
	var DateObjectEqualToValue = function(value) {
		AbstractDateObjectValue.call(this, value);
	};
	stjs.extend(DateObjectEqualToValue, AbstractDateObjectValue, [ AbstractDateObjectValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DateObjectEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "equal to '" + this.getFormattedValue() + "'";
		};
	}, {});
	return DateObjectEqualToValue;
})