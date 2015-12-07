define([ "require", "stjs", "util/Exceptions", "dto/common/search/AbstractFieldSearchCriteria", "dto/common/search/ServerTimeZone", "dto/common/search/ShortDateFormat",
		"dto/common/search/NormalDateFormat", "dto/common/search/LongDateFormat", "dto/common/search/DateEqualToValue", "dto/common/search/DateObjectEqualToValue",
		"dto/common/search/DateLaterThanOrEqualToValue", "dto/common/search/DateObjectLaterThanOrEqualToValue", "dto/common/search/DateEarlierThanOrEqualToValue",
		"dto/common/search/DateObjectEarlierThanOrEqualToValue", "dto/common/search/TimeZone", "dto/common/search/AbstractDateValue", "util/DateFormat" ], function(require, stjs, exceptions,
		AbstractFieldSearchCriteria, ServerTimeZone) {
	var DateFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		this.timeZone = new ServerTimeZone();
	};

	stjs.extend(DateFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'dto.search.DateFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		var ShortDateFormat = require("dto/common/search/ShortDateFormat");
		var NormalDateFormat = require("dto/common/search/NormalDateFormat");
		var LongDateFormat = require("dto/common/search/LongDateFormat");
		constructor.DATE_FORMATS = [ new ShortDateFormat(), new NormalDateFormat(), new LongDateFormat() ];
		var value = function(DateValueClass, DateObjectValueClass, date) {
			if (date instanceof Date) {
				return new DateObjectValueClass(date);
			}
			return new DateValueClass(date);
		}
		prototype.thatEquals = function(date) {
			var DateEqualToValue = require("dto/common/search/DateEqualToValue");
			var DateObjectEqualToValue = require("dto/common/search/DateObjectEqualToValue");
			this.setFieldValue(value(DateEqualToValue, DateObjectEqualToValue, date));
		};
		prototype.thatIsLaterThanOrEqualTo = function(date) {
			var DateLaterThanOrEqualToValue = require("dto/common/search/DateLaterThanOrEqualToValue");
			var DateObjectLaterThanOrEqualToValue = require("dto/common/search/DateObjectLaterThanOrEqualToValue");
			this.setFieldValue(value(DateLaterThanOrEqualToValue, DateObjectLaterThanOrEqualToValue, date));
		};
		prototype.thatIsEarlierThanOrEqualTo = function(date) {
			var DateEarlierThanOrEqualToValue = require("dto/common/search/DateEarlierThanOrEqualToValue");
			var DateObjectEarlierThanOrEqualToValue = require("dto/common/search/DateObjectEarlierThanOrEqualToValue");
			this.setFieldValue(value(DateEarlierThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue, date));
		};
		prototype.withServerTimeZone = function() {
			this.timeZone = new ServerTimeZone();
			return this;
		};
		prototype.withTimeZone = function(hourOffset) {
			var TimeZone = require("dto/common/search/TimeZone");
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
			var AbstractDateValue = require("dto/common/search/AbstractDateValue");
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
