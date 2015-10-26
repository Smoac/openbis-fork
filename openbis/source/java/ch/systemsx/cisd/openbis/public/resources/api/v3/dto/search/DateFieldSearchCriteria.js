define([ "stjs", "util/Exceptions", "dto/search/AbstractFieldSearchCriteria", "dto/search/ServerTimeZone"], 
		function(stjs, exceptions, AbstractFieldSearchCriteria, ServerTimeZone) {
	var DateFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		this.timeZone = new ServerTimeZone();
	};

	stjs.extend(DateFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DateFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		var ShortDateFormat = require("dto/search/ShortDateFormat");
		var NormalDateFormat = require("dto/search/NormalDateFormat");
		var LongDateFormat = require("dto/search/LongDateFormat");
		constructor.DATE_FORMATS = [ new ShortDateFormat(), new NormalDateFormat(), new LongDateFormat() ];
		var value = function(DateValueClass, DateObjectValueClass, date) {
			if (date instanceof Date) {
				return new DateObjectValueClass(date);
			}
			return new DateValueClass(date);
		}
		prototype.thatEquals = function(date) {
			var DateEqualToValue = require("dto/search/DateEqualToValue");
			var DateObjectEqualToValue = require("dto/search/DateObjectEqualToValue");
			this.setFieldValue(value(DateEqualToValue, DateObjectEqualToValue, date));
		};
		prototype.thatIsLaterThanOrEqualTo = function(date) {
			var DateLaterThanOrEqualToValue = require("dto/search/DateLaterThanOrEqualToValue", );
			var DateObjectLaterThanOrEqualToValue = require("dto/search/DateObjectLaterThanOrEqualToValue");
			this.setFieldValue(value(DateLaterThanOrEqualToValue, DateObjectLaterThanOrEqualToValue, date));
		};
		prototype.thatIsEarlierThanOrEqualTo = function(date) {
			var DateEarlierThanOrEqualToValue = require("dto/search/DateEarlierThanOrEqualToValue");
			var DateObjectEarlierThanOrEqualToValue = require("dto/search/DateObjectEarlierThanOrEqualToValue");
			this.setFieldValue(value(DateEarlierThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue, date));
		};
		prototype.withServerTimeZone = function() {
			this.timeZone = new ServerTimeZone();
			return this;
		};
		prototype.withTimeZone = function(hourOffset) {
			var TimeZone = require("dto/search/TimeZone");
			this.timeZone = new TimeZone(hourOffset);
			return this;
		};
		prototype.setTimeZone = function(timeZone) {
			this.timeZone = timeZone;
		};
		prototype.getTimeZone = function() {
			return this.timeZone;
		};
		prototype.setFieldValue = function(value) {
			DateFieldSearchCriteria.checkValueFormat(value);
			AbstractFieldSearchCriteria.prototype.setFieldValue.call(this, value);
		};
		constructor.checkValueFormat = function(value) {
			var AbstractDateValue = require("dto/search/AbstractDateValue");
			if (stjs.isInstanceOf(value.constructor, AbstractDateValue)) {
				var formats = DateFieldSearchCriteria.DATE_FORMATS;
				for ( var i in formats) {
					var dateFormat = formats[i];
					try {
						var DateFormat = require("util/DateFormat");
						var dateFormat = new DateFormat(dateFormat.getFormat());
						dateFormat.setLenient(false);
						dateFormat.parse(value.getValue());
						return;
					} catch (e) {
					}
				}
				throw new exceptions.IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: " + DateFieldSearchCriteria.DATE_FORMATS);
			}
		};
	}, {
		DATE_FORMATS : {
			name : "List",
			arguments : [ "IDateFormat" ]
		},
		timeZone : "ITimeZone",
		fieldType : {
			name : "Enum",
			arguments : [ "SearchFieldType" ]
		}
	});

	return DateFieldSearchCriteria;
})
