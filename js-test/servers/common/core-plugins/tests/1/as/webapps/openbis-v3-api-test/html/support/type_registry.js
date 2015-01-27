define(["sys/exceptions"], function(exceptions) {
	var store = {};
	return {
		register: function(name, type) {
			console.log("Registering " + name);
			store[name] = type;
		},
		
		get: function(name) {
			if (!store.hasOwnProperty(name)) {
				throw new exceptions.IllegalArgumentException("Type [" + name + "] was not registered yet.");
			}
			return store[name];
		}
	};
});