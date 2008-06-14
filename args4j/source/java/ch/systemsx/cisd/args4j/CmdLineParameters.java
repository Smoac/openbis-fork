package ch.systemsx.cisd.args4j;

import ch.systemsx.cisd.args4j.spi.Parameters;

/**
 * Essentially a pointer over a {@link String} array. Can move forward, can look ahead.
 * 
 * @author Kohsuke Kawaguchi
 */
class CmdLineParameters extends Parameters
{
    private final String[] args;

    private int pos;

    CmdLineParameters(String[] args)
    {
        this.args = args;
        pos = 0;
    }

    boolean hasMore()
    {
        return pos < args.length;
    }

    private String getCurrentToken()
    {
        return args[pos];
    }

    void proceed(int n)
    {
        pos += n;
    }

    @Override
    public String getOptionName()
    {
        return getCurrentToken();
    }

    @Override
    public String getParameter(int idx) throws CmdLineException
    {
        if (pos + idx + 1 >= args.length)
            throw new CmdLineException(Messages.MISSING_OPERAND.format(getOptionName()));
        return args[pos + idx + 1];
    }

    @Override
    public int getParameterCount()
    {
        return args.length - (pos + 1);
    }
}