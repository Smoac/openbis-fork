package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search.detailed;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.testng.Assert;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CompareType;

/**
 * @author pkupczyk
 */
public class DateRangeCalculatorTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = "Couldn't parse date 'this should be a date'.*")
    public void testIncorrectDateFormat()
    {
        new DateRangeCalculator("this should be a date", "0", CompareType.EQUALS);
    }

    @Test
    public void testIncorrectTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 00:59", "this should be a time zone", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-14 00:59:00.000");
    }

    @Test
    public void testZeroTimeZoneOffsetWithZCharacter()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 00:59", "Z", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-14 00:59:00.000");
    }

    @Test
    public void testPositiveTimeZoneOffsetWithPlusSign()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 00:59", "+1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-13 23:59:00.000");
    }

    @Test
    public void testEqualToDateWithoutTimeWithZeroTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "0", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithoutTimeWithPositiveTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "+1", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithoutTimeWithNegativeTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "-1", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithoutTimeWithServerTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "server", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithTimeWithZeroTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 23:59", "0", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");

        calculator = new DateRangeCalculator("2014-11-14 00:01", "0", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithTimeWithPositiveTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 00:59", "1", CompareType.EQUALS);
        assertDates(calculator, "2014-11-13 00:00:00.000", "2014-11-14 00:00:00.000");

        calculator = new DateRangeCalculator("2014-11-14 01:01", "1", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithTimeWithNegativeTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 22:59", "-1", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");

        calculator = new DateRangeCalculator("2014-11-14 23:01", "-1", CompareType.EQUALS);
        assertDates(calculator, "2014-11-15 00:00:00.000", "2014-11-16 00:00:00.000");
    }

    @Test
    public void testEqualToDateWithTimeWithServerTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 00:59", "server", CompareType.EQUALS);
        assertDates(calculator, "2014-11-13 00:00:00.000", "2014-11-14 00:00:00.000");

        calculator = new DateRangeCalculator("2014-11-14 01:01", "server", CompareType.EQUALS);
        assertDates(calculator, "2014-11-14 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithoutTimeWithZeroTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "0", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithoutTimeWithPositiveTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithoutTimeWithNegativeTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "-1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithoutTimeWithServerTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "server", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-15 00:00:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithTimeWithZeroTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "0", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-14 14:37:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithTimeWithPositiveTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-14 13:37:00.000");

        calculator = new DateRangeCalculator("2014-11-14 00:59", "1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-13 23:59:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithTimeWithNegativeTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "-1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-14 15:37:00.000");

        calculator = new DateRangeCalculator("2014-11-14 23:59", "-1", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-15 00:59:00.000");
    }

    @Test
    public void testLessThanOrEqualToDateWithTimeWithServerTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "server", CompareType.LESS_THAN_OR_EQUAL);
        assertDates(calculator, "1970-01-01 00:00:00.000", "2014-11-14 13:37:00.000");
    }

    @Test
    public void testMoreThanOrEqualToDateWithoutTimeWithZeroTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "0", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 00:00:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithoutTimeWithPositiveTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "1", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 00:00:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithoutTimeWithNegativeTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "-1", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 00:00:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithoutTimeWithServerTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14", "server", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 00:00:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithTimeWithZeroTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "0", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 14:37:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithTimeWithPositiveTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "1", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 13:37:00.000", "292278994-08-17 07:12:55.807");

        calculator = new DateRangeCalculator("2014-11-14 00:59", "1", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-13 23:59:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithTimeWithNegativeTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "-1", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 15:37:00.000", "292278994-08-17 07:12:55.807");

        calculator = new DateRangeCalculator("2014-11-14 23:59", "-1", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-15 00:59:00.000", "292278994-08-17 07:12:55.807");
    }

    @Test
    public void testMoreThanOrEqualToDateWithTimeWithServerTimeZoneOffset()
    {
        DateRangeCalculator calculator = new DateRangeCalculator("2014-11-14 14:37", "server", CompareType.MORE_THAN_OR_EQUAL);
        assertDates(calculator, "2014-11-14 13:37:00.000", "292278994-08-17 07:12:55.807");
    }

    private void assertDates(DateRangeCalculator calculator, String expectedLowerDate, String expectedUpperDate)
    {
        assertDate(calculator.getLowerDate(), expectedLowerDate);
        assertDate(calculator.getUpperDate(), expectedUpperDate);
    }

    private void assertDate(Date actualDate, String expectedDate)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        Assert.assertEquals(format.format(actualDate), expectedDate);
    }

}
