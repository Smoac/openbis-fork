define([ "support/stjs", "dto/search/AbstractValue" ], function(stjs, AbstractValue) {
	var AbstractStringValue = function(value) {
		AbstractValue.call(this, value);
	};
	stjs.extend(AbstractStringValue, AbstractValue, [ AbstractValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractStringValue';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractStringValue;
})