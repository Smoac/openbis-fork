define(['support/moment', 'sys/exceptions'], function(moment, exceptions) {
	var SimpleDateFormat = function(format) {
		this.format = format;
		this.lenient = true;
	}

	SimpleDateFormat.prototype.setLenient = function(lenient) {
		this.lenient = lenient;
	};

	SimpleDateFormat.prototype.parse = function(str) {
		var m =  moment(str, this.format, this.lenient === false);
		if (m.isValid()) {
			return m.toDate();
		}
		throw new exceptions.IllegalArgumentExcpetion("invalid date string: " + str);
	};

	return SimpleDateFormat;
})