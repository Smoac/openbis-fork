define([ "support/stjs", "dto/search/AbstractStringValue" ], function(stjs, AbstractStringValue) {
	var StringContainsValue = function(value) {
		AbstractStringValue.call(this, value);
	};
	stjs.extend(StringContainsValue, AbstractStringValue, [ AbstractStringValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.StringContainsValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "contains '" + this.getValue() + "'";
		};
	}, {});
	return StringContainsValue;
})