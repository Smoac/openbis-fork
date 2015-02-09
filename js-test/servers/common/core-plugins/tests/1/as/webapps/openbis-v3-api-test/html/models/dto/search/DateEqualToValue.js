/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/AbstractDateValue" ], function(stjs, AbstractDateValue) {
	var DateEqualToValue = function(value) {
		AbstractDateValue.call(this, value);
	};
	stjs.extend(DateEqualToValue, AbstractDateValue, [ AbstractDateValue ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DateEqualToValue';
		constructor.serialVersionUID = 1;
		prototype.toString = function() {
			return "equal to '" + this.getValue() + "'";
		};
	}, {});
	return DateEqualToValue;
})