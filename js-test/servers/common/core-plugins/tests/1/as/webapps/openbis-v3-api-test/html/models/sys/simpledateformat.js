define("sys/exceptions", function(exceptions) {
	var SimpleDateFormat = function(format) {
		this.format = format;
	}

	SimpleDateFormat.prototype.setLenient = function(lenient) {
		throw new "TODO implement this method";
	};

	SimpleDateFormat.prototype.parse = function(str) {
		throw new "TODO implement this method";
	};

	return SimpleDateFormat;
})