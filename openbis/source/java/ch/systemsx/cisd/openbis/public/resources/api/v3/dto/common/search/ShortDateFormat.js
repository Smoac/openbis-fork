/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/IDateFormat" ], function(stjs, IDateFormat) {
	var ShortDateFormat = function() {
	};
	stjs.extend(ShortDateFormat, null, [ IDateFormat ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.ShortDateFormat';
		prototype.getFormat = function() {
			return "YYYY-MM-DD";
		};
		prototype.toString = function() {
			return this.getFormat();
		};
	}, {});
	return ShortDateFormat;
})