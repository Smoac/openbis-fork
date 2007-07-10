package ch.systemsx.cisd.common.parser;

/**
 * A default <code>LineFilter</code> implementation that excludes empty and comment lines.
 * <p>
 * A comment line starts with '#'.
 * </p>
 *
 * @author Christian Ribeaud
 */
public final class ExcludeEmptyAndCommentLineFilter implements ILineFilter
{
    public static final ILineFilter INSTANCE = new ExcludeEmptyAndCommentLineFilter();
    
    private ExcludeEmptyAndCommentLineFilter() {}
    
    public final boolean acceptLine(String line, int lineNumber)
    {
        final String trimmed = line.trim();
        return trimmed.length() > 0 && trimmed.startsWith("#") == false;
    }
}