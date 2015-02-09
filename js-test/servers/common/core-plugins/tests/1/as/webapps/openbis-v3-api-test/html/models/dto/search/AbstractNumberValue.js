/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractValue" ], function(stjs, AbstractValue) {
	var AbstractNumberValue = function(number) {
		AbstractValue.call(this, number);
	};
	stjs.extend(AbstractNumberValue, AbstractValue, [ AbstractValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractNumberValue';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractNumberValue;
})