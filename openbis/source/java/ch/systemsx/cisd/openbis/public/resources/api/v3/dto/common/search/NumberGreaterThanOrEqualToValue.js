/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/AbstractNumberValue" ], function(stjs, AbstractNumberValue) {
	var NumberGreaterOrEqualThanValue = function(number) {
		AbstractNumberValue.call(this, number);
	};
	stjs.extend(NumberGreaterOrEqualThanValue, AbstractNumberValue, [ AbstractNumberValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.NumberGreaterThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "greater or equal to '" + this.getValue() + "'";
		};
	}, {});
	return NumberGreaterOrEqualThanValue;
})