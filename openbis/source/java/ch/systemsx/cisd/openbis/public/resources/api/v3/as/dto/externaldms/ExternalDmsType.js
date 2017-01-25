/**
 * @author anttil
 */
 
define([ "stjs", "as/dto/common/Enum" ], function(stjs, Enum) {
	var ExternalDmsType = function() {
		Enum.call(this, [ "OPENBIS", "GIT", "UNDEFINED" ]);
	};
	stjs.extend(ExternalDmsType, Enum, [ Enum ], function(constructor, prototype) {
	}, {});
	return new ExternalDmsType();
})