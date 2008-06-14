package ch.systemsx.cisd.args4j.spi;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.Option;

/**
 * {@link Double} {@link OptionHandler}.
 * 
 * @author Leif Wickland
 */
public class DoubleOptionHandler extends OptionHandler
{
    private final Setter<? super Double> setter;

    public DoubleOptionHandler(Option option, Setter<? super Double> setter)
    {
        super(option);
        this.setter = setter;
    }

    @Override
    public int parseArguments(Parameters params) throws CmdLineException
    {
        set(params.getParameter(0));
        return 1;
    }

    @Override
    public void set(String value) throws CmdLineException
    {
        try
        {
            set(Double.parseDouble(value));
        } catch (NumberFormatException ex)
        {
            throw new CmdLineException(Messages.ILLEGAL_OPERAND.format(getName(), value));
        }
    }

    /**
     * Can be overridden by sub-classes when the <var>value</var> needs to be manipulated.
     */
    protected void set(double value) throws CmdLineException
    {
        setter.addValue(value);
    }

    @Override
    public String getDefaultMetaVariable()
    {
        return "N";
    }

}
