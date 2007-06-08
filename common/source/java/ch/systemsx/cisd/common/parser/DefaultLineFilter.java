package ch.systemsx.cisd.common.parser;

import org.apache.commons.lang.ArrayUtils;

/**
 * A default <code>LineFilter</code> implementation that filters out comment and empty lines (lines starting with
 * '#').
 * <p>
 * It is also possible here to define a set of lines that should be skipped.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class DefaultLineFilter implements ILineFilter
{

    /**
     * A set of lines that should be skipped.
     */
    private int[] skippedLines = ArrayUtils.EMPTY_INT_ARRAY;

    /** Add a line that should be skipped. */
    public final void addSkippedLine(int line)
    {
        ArrayUtils.add(skippedLines, line);
    }

    //
    // LineFilter
    //

    public final boolean acceptLine(String line, int lineNumber)
    {
        // Not found
        if (ArrayUtils.indexOf(skippedLines, lineNumber) > ArrayUtils.INDEX_NOT_FOUND)
        {
            return false;
        }
        String trimmed = line.trim();
        return trimmed.length() > 0 && trimmed.startsWith("#") == false;
    }

}