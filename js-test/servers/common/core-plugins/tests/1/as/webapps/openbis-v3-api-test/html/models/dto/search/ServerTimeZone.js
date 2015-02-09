/**
 * @author pkupczyk
 */
define([ "support/stjs", "dto/search/ITimeZone" ], function(stjs, ITimeZone) {
	var ServerTimeZone = function() {
	};
	stjs.extend(ServerTimeZone, null, [ ITimeZone ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.ServerTimeZone';
	}, {});
	return ServerTimeZone;
})