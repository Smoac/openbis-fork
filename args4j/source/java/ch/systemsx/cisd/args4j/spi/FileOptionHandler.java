package ch.systemsx.cisd.args4j.spi;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.Option;

import java.io.File;

/**
 * {@link File} {@link OptionHandler}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class FileOptionHandler extends OptionHandler
{
    private final Setter<? super File> setter;

    public FileOptionHandler(Option option, Setter<? super File> setter)
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
        set(new File(value));
    }

    /**
     * Can be overridden by sub-classes when the <var>value</var> needs to be manipulated.
     */
    protected void set(File value) throws CmdLineException
    {
        setter.addValue(value);
    }

    @Override
    public String getDefaultMetaVariable()
    {
        return "FILE";
    }

}
