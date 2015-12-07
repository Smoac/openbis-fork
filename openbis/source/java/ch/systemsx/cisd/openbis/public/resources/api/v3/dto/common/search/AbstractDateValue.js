/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/AbstractValue", "dto/common/search/IDate" ], function(stjs, AbstractValue, IDate) {
	var AbstractDateValue = function(value) {
		AbstractValue.call(this, value);
	};
	stjs.extend(AbstractDateValue, AbstractValue, [ AbstractValue, IDate ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.AbstractDateValue';
		constructor.serialVersionUID = 1;
	}, {});
	return AbstractDateValue;
})