package ch.systemsx.cisd.args4j.spi;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.Option;

/**
 * {@link Enum} {@link OptionHandler}.
 * 
 * @author Kohsuke Kawaguchi
 */
public class EnumOptionHandler<T extends Enum<T>> extends OptionHandler
{
    private final Setter<? super T> setter;

    private final Class<T> enumType;

    public EnumOptionHandler(Option option, Setter<? super T> setter, Class<T> enumType)
    {
        super(option);
        this.setter = setter;
        this.enumType = enumType;
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
        T enumValue = getEnumOption(value);
        if (enumValue == null)
            throw new CmdLineException(Messages.ILLEGAL_OPERAND.format(getName(), value));
        set(enumValue);
    }

    private T getEnumOption(String s)
    {
        for (T o : enumType.getEnumConstants())
            if (o.name().equalsIgnoreCase(s))
            {
                return o;
            }
        return null;
    }

    /**
     * Can be overridden by sub-classes when the <var>value</var> needs to be manipulated.
     */
    protected void set(T value) throws CmdLineException
    {
        setter.addValue(value);
    }

    @Override
    public String getDefaultMetaVariable()
    {
        String n = enumType.getName();
        int idx = n.lastIndexOf('.');
        if (idx >= 0)
            n = n.substring(idx + 1);
        return n.toUpperCase();
    }

}
