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
     * The line where the header is.
     * <p>
     * If we set it bigger than <code>-1</code>, we assume that the header contains mapping information and should be
     * skipped by the parser.
     * </p>
     */
    private int headerLine = -1;

    /** Sets <code>headerLine</code>. */
    public final void setHeaderLine(int headerLine)
    {
        this.headerLine = headerLine;
    }

    //
    // LineFilter
    //

    public boolean acceptLine(String line, int lineNumber)
    {
        if (lineNumber == headerLine)
        {
            return false;
        }
        String trimmed = line.trim();
        return trimmed.length() > 0 && trimmed.startsWith("#") == false;
    }

}