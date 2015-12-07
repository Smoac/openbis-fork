/**
 * @author pkupczyk
 */
define([ "stjs", "dto/common/search/ITimeZone" ], function(stjs, ITimeZone) {
	var ServerTimeZone = function() {
	};
	stjs.extend(ServerTimeZone, null, [ ITimeZone ], function(constructor, prototype) {
		prototype['@type'] = 'dto.common.search.ServerTimeZone';
	}, {});
	return ServerTimeZone;
})