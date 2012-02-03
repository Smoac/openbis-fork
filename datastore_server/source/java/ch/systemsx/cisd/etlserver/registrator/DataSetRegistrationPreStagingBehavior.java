package ch.systemsx.cisd.etlserver.registrator;

/**
 * Configures the behaviour of the original input dataset in a dropbox registration process.
 * 
 * @author jakubs
 */
public enum DataSetRegistrationPreStagingBehavior
{
    /**
     * The default behavior without pre-staging. The registration uses original input file through
     * the hole process.
     */
    USE_ORIGINAL,
    /**
     * Use the pre-staging dir and delete original file on success.
     */
    USE_PRESTAGING;

    DataSetRegistrationPreStagingBehavior()
    {
    }

    /**
     * Parses the string in a format acceptable as a property parameter.
     */
    public static DataSetRegistrationPreStagingBehavior fromString(String text)
    {
        if (text.equals("use_original"))
        {
            return USE_ORIGINAL;
        }
        if (text.equals("default") || text.equals("use_prestaging"))
        {
            return USE_PRESTAGING;
        }
        return null;
    }
}
