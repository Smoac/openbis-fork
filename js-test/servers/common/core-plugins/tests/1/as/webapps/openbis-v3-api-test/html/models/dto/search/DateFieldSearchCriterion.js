define(["support/stjs", "sys/exceptions"], function (stjs, exceptions, AbstractFieldSearchCriterion) {
    var DateFieldSearchCriterion = function(fieldName, fieldType) {
        AbstractFieldSearchCriterion.call(this, fieldName, fieldType);
    };
    stjs.extend(DateFieldSearchCriterion, AbstractFieldSearchCriterion, [AbstractFieldSearchCriterion], function(constructor, prototype) {
        prototype['@type'] = 'DateFieldSearchCriterion';
        constructor.serialVersionUID = 1;
        constructor.DATE_FORMATS = new ArrayList();
        prototype.timeZone = new ServerTimeZone();
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
                for (var dateFormat in DateFieldSearchCriterion.DATE_FORMATS) {
                    try {
                        var simpleDateFormat = new SimpleDateFormat(dateFormat.getFormat());
                        simpleDateFormat.setLenient(false);
                        simpleDateFormat.parse((value).getValue());
                        return;
                    }catch (e) {}
                }
                 throw new exceptions.IllegalArgumentException("Date value: " + value + " does not match any of the supported formats: " + DateFieldSearchCriterion.DATE_FORMATS);
            }
        };
    }, {DATE_FORMATS: {name: "List", arguments: ["IDateFormat"]}, timeZone: "ITimeZone", fieldType: {name: "Enum", arguments: ["SearchFieldType"]}});
    return DateFieldSearchCriterion;
})(function() {
    DateFieldSearchCriterion.DATE_FORMATS.add(new ShortDateFormat());
    DateFieldSearchCriterion.DATE_FORMATS.add(new NormalDateFormat());
    DateFieldSearchCriterion.DATE_FORMATS.add(new LongDateFormat());
})();
