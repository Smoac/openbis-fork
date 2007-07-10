package ch.systemsx.cisd.common.parser;

/**
 * A <code>ILineFilter</code> implementation that extends {@link ILineFilter#EXCLUDE_EMPTY_AND_COMMENT_LINE} by
 * excluding the header line (if <code>&gt; 1</code>) as well.
 * 
 * @author Christian Ribeaud
 */
public final class HeaderLineFilter implements ILineFilter
{

    /**
     * The line number of the header line.
     * <p>
     * If we set it bigger than <code>-1</code>, we assume that the header contains mapping information and should be
     * skipped by the parser.
     * </p>
     */
    private final int headerLineNumber;

    public HeaderLineFilter(int headerLineNumber)
    {
        this.headerLineNumber = headerLineNumber;
    }

    /**
     * Constructor for a line filter without a header line.
     */
    public HeaderLineFilter()
    {
        this(-1);
    }

    //
    // LineFilter
    //

    public final boolean acceptLine(String line, int lineNumber)
    {
        if (ExcludeEmptyAndCommentLineFilter.INSTANCE.acceptLine(line, lineNumber) == false
                || lineNumber == headerLineNumber)
        {
            return false;
        }
        return true;
    }

}