define(function() {
	var HashMap = function() {}
	HashMap.prototype.put = function(key, val) {
		this[key] = val;
	};
	
	return HashMap;
})