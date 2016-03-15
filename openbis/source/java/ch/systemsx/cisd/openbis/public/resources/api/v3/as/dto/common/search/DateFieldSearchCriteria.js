define([ "require", "stjs", "util/Exceptions", "as/dto/common/search/AbstractFieldSearchCriteria", "as/dto/common/search/ServerTimeZone", "as/dto/common/search/ShortDateFormat",
		"as/dto/common/search/NormalDateFormat", "as/dto/common/search/LongDateFormat", "as/dto/common/search/DateEqualToValue", "as/dto/common/search/DateObjectEqualToValue",
		"as/dto/common/search/DateLaterThanOrEqualToValue", "as/dto/common/search/DateObjectLaterThanOrEqualToValue", "as/dto/common/search/DateEarlierThanOrEqualToValue",
		"as/dto/common/search/DateObjectEarlierThanOrEqualToValue", "as/dto/common/search/TimeZone", "as/dto/common/search/AbstractDateValue", "util/DateFormat" ], function(require, stjs, exceptions,
		AbstractFieldSearchCriteria, ServerTimeZone) {
	var DateFieldSearchCriteria = function(fieldName, fieldType) {
		AbstractFieldSearchCriteria.call(this, fieldName, fieldType);
		this.timeZone = new ServerTimeZone();
	};

	stjs.extend(DateFieldSearchCriteria, AbstractFieldSearchCriteria, [ AbstractFieldSearchCriteria ], function(constructor, prototype) {
		prototype['@type'] = 'as.dto.common.search.DateFieldSearchCriteria';
		constructor.serialVersionUID = 1;
		var ShortDateFormat = require("as/dto/common/search/ShortDateFormat");
		var NormalDateFormat = require("as/dto/common/search/NormalDateFormat");
		var LongDateFormat = require("as/dto/common/search/LongDateFormat");
		constructor.DATE_FORMATS = [ new ShortDateFormat(), new NormalDateFormat(), new LongDateFormat() ];
		var value = function(DateValueClass, DateObjectValueClass, date) {
			if (date instanceof Date) {
				return new DateObjectValueClass(date);
			}
			return new DateValueClass(date);
		}
		prototype.thatEquals = function(date) {
			var DateEqualToValue = require("as/dto/common/search/DateEqualToValue");
			var DateObjectEqualToValue = require("as/dto/common/search/DateObjectEqualToValue");
			this.setFieldValue(value(DateEqualToValue, DateObjectEqualToValue, date));
		};
		prototype.thatIsLaterThanOrEqualTo = function(date) {
			var DateLaterThanOrEqualToValue = require("as/dto/common/search/DateLaterThanOrEqualToValue");
			var DateObjectLaterThanOrEqualToValue = require("as/dto/common/search/DateObjectLaterThanOrEqualToValue");
			this.setFieldValue(value(DateLaterThanOrEqualToValue, DateObjectLaterThanOrEqualToValue, date));
		};
		prototype.thatIsEarlierThanOrEqualTo = function(date) {
			var DateEarlierThanOrEqualToValue = require("as/dto/common/search/DateEarlierThanOrEqualToValue");
			var DateObjectEarlierThanOrEqualToValue = require("as/dto/common/search/DateObjectEarlierThanOrEqualToValue");
			this.setFieldValue(value(DateEarlierThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue, date));
		};
		prototype.withServerTimeZone = function() {
			this.timeZone = new ServerTimeZone();
			return this;
		};
		prototype.withTimeZone = function(hourOffset) {
			var TimeZone = require("as/dto/common/search/TimeZone");
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
			var AbstractDateValue = require("as/dto/common/search/AbstractDateValue");
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
