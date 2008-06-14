package ch.systemsx.cisd.args4j.spi;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.Option;

/**
 * {@link Pattern} {@link OptionHandler}.
 * 
 * @author Bernd Rinn
 */
public class RegexOptionHandler extends OptionHandler
{
    private final Setter<? super Pattern> setter;

    public RegexOptionHandler(Option option, Setter<? super Pattern> setter)
    {
        super(option);
        this.setter = setter;
    }

    @Override
    public int parseArguments(ch.systemsx.cisd.args4j.spi.Parameters params) throws CmdLineException
    {
        set(params.getParameter(0));
        return 1;
    }

    @Override
    public void set(String value) throws CmdLineException
    {
        try
        {
            set(Pattern.compile(value));
        } catch (PatternSyntaxException ex)
        {
            throw new CmdLineException(Messages.ILLEGAL_OPERAND.format(getName(), value) + ": "
                    + ex.getMessage());
        }
    }

    /**
     * Can be overridden by sub-classes when the <var>value</var> needs to be manipulated.
     */
    protected void set(Pattern value) throws CmdLineException
    {
        setter.addValue(value);
    }

    @Override
    public String getDefaultMetaVariable()
    {
        return "REGEX";
    }

}