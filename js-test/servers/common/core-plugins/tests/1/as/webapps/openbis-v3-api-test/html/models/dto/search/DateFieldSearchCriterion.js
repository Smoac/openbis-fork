define([ "support/stjs", "sys/exceptions", "sys/simpledateformat", "dto/search/AbstractFieldSearchCriterion", "dto/search/ServerTimeZone", "dto/search/DateObjectEqualToValue",
		"dto/search/DateEqualToValue", "dto/search/DateObjectLaterThanOrEqualToValue", "dto/search/DateLaterThanOrEqualToValue", "dto/search/DateObjectEarlierThanOrEqualToValue",
		"dto/search/DateEarlierThanOrEqualToValue", "dto/search/TimeZone", "dto/search/AbstractDateValue", "dto/search/ShortDateFormat", "dto/search/NormalDateFormat", "dto/search/LongDateFormat" ],
		function(stjs, exceptions, SimpleDateFormat, AbstractFieldSearchCriterion, ServerTimeZone, DateObjectEqualToValue, DateEqualToValue, DateObjectLaterThanOrEqualToValue,
				DateLaterThanOrEqualToValue, DateObjectEarlierThanOrEqualToValue, DateEarlierThanOrEqualToValue, TimeZone, AbstractDateValue, ShortDateFormat, NormalDateFormat, LongDateFormat) {
			var DateFieldSearchCriterion = function(fieldName, fieldType) {
				AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
				this.timeZone = new ServerTimeZone();
			};

			stjs.extend(DateFieldSearchCriterion, AbstractFieldSearchCriterion, [ AbstractFieldSearchCriterion ], function(constructor, prototype) {
				prototype['@type'] = 'dto.search.DateFieldSearchCriterion';
				constructor.serialVersionUID = 1;
				constructor.DATE_FORMATS = [ new ShortDateFormat(), new NormalDateFormat(), new LongDateFormat() ];
				prototype.thatEquals = function(date) {
					this.setFieldValue(new DateObjectEqualToValue(date));
				};
				prototype.thatEquals = function(date) {
					this.setFieldValue(new DateEqualToValue(date));
				};
				prototype.thatIsLaterThanOrEqualTo = function(date) {
					this.setFieldValue(new DateObjectLaterThanOrEqualToValue(date));
				};
				prototype.thatIsLaterThanOrEqualTo = function(date) {
					this.setFieldValue(new DateLaterThanOrEqualToValue(date));
				};
				prototype.thatIsEarlierThanOrEqualTo = function(date) {
					this.setFieldValue(new DateObjectEarlierThanOrEqualToValue(date));
				};
				prototype.thatIsEarlierThanOrEqualTo = function(date) {
					this.setFieldValue(new DateEarlierThanOrEqualToValue(date));
				};
				prototype.withServerTimeZone = function() {
					this.timeZone = new ServerTimeZone();
					return this;
				};
				prototype.withTimeZone = function(hourOffset) {
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
					DateFieldSearchCriterion.checkValueFormat(value);
					AbstractFieldSearchCriterion.prototype.setFieldValue.call(this, value);
				};
				constructor.checkValueFormat = function(value) {
					if (stjs.isInstanceOf(value.constructor, AbstractDateValue)) {
						for ( var dateFormat in DateFieldSearchCriterion.DATE_FORMATS.getItems()) {
							try {
								var simpleDateFormat = new SimpleDateFormat(dateFormat.getFormat());
								simpleDateFormat.setLenient(false);
								simpleDateFormat.parse((value).getValue());
								return;
							} catch (e) {
							}
						}
						throw new exceptions.IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: " + DateFieldSearchCriterion.DATE_FORMATS);
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

			return DateFieldSearchCriterion;
		})
