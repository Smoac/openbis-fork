/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/AbstractValue" ], function(stjs, AbstractValue) {
	var AbstractNumberValue = function(number) {
		AbstractValue.call(this, number);
	};
	stjs.extend(AbstractNumberValue, AbstractValue, [ AbstractValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.AbstractNumberValue';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractNumberValue;
})