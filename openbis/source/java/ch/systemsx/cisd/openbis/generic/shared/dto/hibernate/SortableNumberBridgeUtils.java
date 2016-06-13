package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

public class SortableNumberBridgeUtils
{
    //
    // Util Methods
    //
    private static int LUCENE_INTEGER_PADDING = 19; // On the UI a integer field can't have more than 18 characters, a long can have 19 taking out the
                                                    // minus sign

    public static boolean isValidNumber(String number)
    {
        return getNumberForLucene(number) != null;
    }

    public static String getNumberForLucene(String number)
    {
        try
        {
            return getNumberForLucene(Long.parseLong(number));
        } catch (Exception ex)
        {
            try
            {
                return getNumberForLucene(Double.parseDouble(number));
            } catch (Exception ex2)
            {
                return null;
            }
        }
    }

    public static String getNumberForLucene(Number number)
    {
        if (number instanceof Integer || number instanceof Long)
        {
            return getIntegerAsStringForLucene(number) + ".0";
        }
        if (number instanceof Float || number instanceof Double)
        {
            String rawReal = number.toString();
            int indexOfDot = rawReal.indexOf('.');
            return getIntegerAsStringForLucene(Long.parseLong(rawReal.substring(0, indexOfDot))) + rawReal.substring(indexOfDot, rawReal.length());
        } else
        {
            return number.toString();
        }
    }

    private static String getIntegerAsStringForLucene(Number number)
    {
        String rawInteger = number.toString();

        StringBuilder paddedInteger = new StringBuilder();

        if (rawInteger.startsWith("-"))
        {
            rawInteger = rawInteger.substring(1, rawInteger.length());
            paddedInteger.append('0');
        } else
        {
            paddedInteger.append('1');
        }

        if (rawInteger.length() > LUCENE_INTEGER_PADDING)
        {
            throw new IllegalArgumentException("Try to pad on a number too big");
        }

        for (int padIndex = rawInteger.length(); padIndex < LUCENE_INTEGER_PADDING; padIndex++)
        {
            paddedInteger.append('0');
        }

        paddedInteger.append(rawInteger);
        return paddedInteger.toString();
    }
}