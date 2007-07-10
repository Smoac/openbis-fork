package ch.systemsx.cisd.common.parser;

/**
 * A default line filter that accepts any line.
 *
 * @author Christian Ribeaud
 */
public final class AlwaysAcceptLineFilter implements ILineFilter
{
    public static final ILineFilter INSTANCE = new AlwaysAcceptLineFilter();
    
    private AlwaysAcceptLineFilter() {}
    
    public final boolean acceptLine(String line, int lineNumber)
    {
        return true;
    }
}