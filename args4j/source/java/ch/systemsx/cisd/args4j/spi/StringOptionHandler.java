package ch.systemsx.cisd.args4j.spi;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.Option;

/**
 * String {@link OptionHandler}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class StringOptionHandler extends OptionHandler
{
    private final Setter<? super String> setter;

    public StringOptionHandler(Option option, Setter<? super String> setter)
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
        setter.addValue(value);
    }

    @Override
    public String getDefaultMetaVariable()
    {
        return "VAL";
    }

}
