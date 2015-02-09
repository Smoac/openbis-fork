/**
 * @author pkupczyk
 */
define([ "support/stjs" ], function(stjs) {
	var AbstractTimeZoneValue = function() {
	};
	stjs.extend(AbstractTimeZoneValue, null, [], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.AbstractTimeZoneValue';
	}, {});
	return AbstractTimeZoneValue;
})