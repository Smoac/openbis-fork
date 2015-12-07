/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/IDateFormat" ], function(stjs, IDateFormat) {
	var LongDateFormat = function() {
	};
	stjs.extend(LongDateFormat, null, [ IDateFormat ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.LongDateFormat';
		prototype.getFormat = function() {
			return "YYYY-MM-DD HH:mm:ss";
		};
		prototype.toString = function() {
			return this.getFormat();
		};
	}, {});
	return LongDateFormat;
})