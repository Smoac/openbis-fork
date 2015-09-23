define([ "stjs", "dto/search/AbstractDateObjectValue" ], function(stjs, AbstractDateObjectValue) {
	var DateObjectEarlierThanOrEqualToValue = function(value) {
		AbstractDateObjectValue.call(this, value);
	};
	stjs.extend(DateObjectEarlierThanOrEqualToValue, AbstractDateObjectValue, [ AbstractDateObjectValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DateObjectEarlierThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "earlier than or equal to '" + this.getFormattedValue() + "'";
		};
	}, {});
	return DateObjectEarlierThanOrEqualToValue;
})