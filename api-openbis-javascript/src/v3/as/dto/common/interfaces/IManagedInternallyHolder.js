define([ "stjs", "util/Exceptions" ], function(stjs, exceptions) {
	var IManagedInternallyHolder = function() {
	};
	stjs.extend(IManagedInternallyHolder, null, [], function(constructor, prototype) {
		prototype.isManagedInternally = function() {
			throw new exceptions.RuntimeException("Interface method.");
		};
	}, {});
	return IManagedInternallyHolder;
})