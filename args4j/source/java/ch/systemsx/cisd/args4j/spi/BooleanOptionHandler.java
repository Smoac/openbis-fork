package ch.systemsx.cisd.args4j.spi;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.Option;

/**
 * Boolean {@link OptionHandler}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class BooleanOptionHandler extends OptionHandler
{
    private final Setter<? super Boolean> setter;

    public BooleanOptionHandler(Option option, Setter<? super Boolean> setter)
    {
        super(option);
        this.setter = setter;
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException
    {
        set(true);
        return 0;
    }

    @Override
    public void set(String value) throws CmdLineException
    {
        try
        {
            set(Boolean.parseBoolean(value));
        } catch (NumberFormatException ex)
        {
            throw new CmdLineException(Messages.ILLEGAL_OPERAND.format(getName(), value));
        }
    }

    /**
     * Can be overridden by sub-classes when the <var>value</var> needs to be manipulated.
     */
    protected void set(boolean value) throws CmdLineException
    {
        setter.addValue(value);
    }

    @Override
    public String getDefaultMetaVariable()
    {
        return null;
    }

}
