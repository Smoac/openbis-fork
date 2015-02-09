define([ "support/stjs", "dto/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringEqualToValue = function(value) {
		AbstractStringValue.call(this, value);
	};
	stjs.extend(StringEqualToValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "equal to '" + this.getValue() + "'";
		};
	}, {});
	return StringEqualToValue;
})