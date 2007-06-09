package ch.systemsx.cisd.common.parser;

/**
 * A default <code>LineFilter</code> implementation that filters out comment and empty lines (lines starting with
 * '#').
 * 
 * @author Christian Ribeaud
 */
public final class DefaultLineFilter implements ILineFilter
{

    /**
     * The line number of the header line.
     * <p>
     * If we set it bigger than <code>-1</code>, we assume that the header contains mapping information and should be
     * skipped by the parser.
     * </p>
     */
    final private int headerLineNumber;
    
    public DefaultLineFilter(int headerLineNumber)
    {
        this.headerLineNumber = headerLineNumber;
    }
    
    /**
     * Constructor for a line filter without a header line.
     */
    public DefaultLineFilter()
    {
        this(-1);
    }

    //
    // LineFilter
    //

    public boolean acceptLine(String line, int lineNumber)
    {
        if (lineNumber == headerLineNumber)
        {
            return false;
        }
        final String trimmed = line.trim();
        return trimmed.length() > 0 && trimmed.startsWith("#") == false;
    }

}