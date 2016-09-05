package ch.systemsx.cisd.openbis.generic.shared.dto.hibernate;

public class SortableNumberBridgeUtils
{
    //
    // Util Methods
    //
    private static int LUCENE_INTEGER_PADDING = 19; // On the UI a integer field can't have more than 18 characters, a long can have 19 taking out the
                                                    // minus sign

    private static final String NaN = "NaN";

    /*
     * Returns True for Integers, Reals and NaN value
     */
    public static boolean isValidNumber(String number)
    {
        try
        {
            getNumberForLucene(Long.valueOf(number));
        } catch (Exception ex)
        {
            try
            {
                getNumberForLucene(Double.valueOf(number));
            } catch (Exception ex2)
            {
                return false;
            }
        }
        return true;
    }

    public static String getNumberForLucene(String number)
    {
        try
        {
            return getNumberForLucene(Long.valueOf(number));
        } catch (Exception ex)
        {
            try
            {
                return getNumberForLucene(Double.valueOf(number));
            } catch (Exception ex2)
            {
                return NaN; // Returns NaN for non numbers, this method can potentially be called by the indexer with non number values
            }
        }
    }

    public static String getNumberForLucene(Number number)
    {
        if (number.toString().equals(NaN))
        {
            return NaN;
        } else if (number instanceof Integer || number instanceof Long)
        {
            return getIntegerAsStringForLucene(number) + ".0";
        } else if (number instanceof Float || number instanceof Double)
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