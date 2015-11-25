/**
 * @author pkupczyk
 */
define([ "stjs", "dto/search/AbstractNumberValue" ], function(stjs, AbstractNumberValue) {
	var NumberLessOrEqualThanValue = function(number) {
		AbstractNumberValue.call(this, number);
	};
	stjs.extend(NumberLessOrEqualThanValue, AbstractNumberValue, [ AbstractNumberValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.NumberLessThanOrEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "less or equal to '" + this.getValue() + "'";
		};
	}, {});
	return NumberLessOrEqualThanValue;
})