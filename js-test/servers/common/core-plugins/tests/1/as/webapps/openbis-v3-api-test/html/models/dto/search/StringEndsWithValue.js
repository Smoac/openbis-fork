define([ "support/stjs", "dto/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringEndsWithValue = function(value) {
		AbstractStringValue.call(this, value);
	};
	stjs.extend(StringEndsWithValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringEndsWithValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "ends with '" + this.getValue() + "'";
		};
	}, {});
	return StringEndsWithValue;
})