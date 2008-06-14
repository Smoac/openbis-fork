package ch.systemsx.cisd.args4j.spi;

import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.HandlerClasses;
import ch.systemsx.cisd.args4j.Option;

import java.util.ResourceBundle;

/**
 * Code that parses operands of an option into Java.
 * <p>
 * This class can be extended by application to support additional Java datatypes in option
 * operands.
 * <p>
 * Implementation of this class needs to be registered to args4j by using
 * {@link HandlerClasses#registerHandler(Class,Class)}
 * 
 * @author Kohsuke Kawaguchi
 */
public abstract class OptionHandler
{
    /**
     * The annotation.
     */
    private final Option option;

    /**
     * The (short) name.
     */
    private final String name;

    /**
     * The (long) name.
     */
    private final String longName;

    protected OptionHandler(Option option)
    {
        this.option = option;
        this.name = getCanonicalShortName(option);
        this.longName = getCanonicalLongName(option);
    }

    private static String getCanonicalShortName(Option option)
    {
        final String name = option.name();
        if (name.length() == 0)
        {
            return "";
        }
        final int nonDashOffset = findFirstNoneDash(name);
        return "-" + name.substring(nonDashOffset);
    }

    private static String getCanonicalLongName(Option option)
    {
        final String name = option.longName();
        if (name.length() == 0)
        {
            return "";
        }
        final int nonDashOffset = findFirstNoneDash(name);
        return "--" + name.substring(nonDashOffset);
    }

    private static int findFirstNoneDash(String s)
    {
        final int len = s.length();
        int idx = 0;
        while (idx < len && s.charAt(idx) == '-')
        {
            ++idx;
        }
        return idx;
    }

    /**
     * Called if the option that this owner recognizes is found.
     * 
     * @param params The rest of the arguments. This method can use this object to access the
     *            arguments of the option if necessary. The object is valid only during the method
     *            call.
     * @return The number of arguments consumed. For example, return 0 if this option doesn't take
     *         any parameter.
     */
    public abstract int parseArguments(Parameters params) throws CmdLineException;

    /**
     * Sets the <var>value</var> directly (after converting).
     */
    public abstract void set(String value) throws CmdLineException;

    /**
     * Gets the default meta variable name used to print the usage screen.
     * 
     * @return null to hide a meta variable.
     */
    public abstract String getDefaultMetaVariable();

    /**
     * The name of the option (including the leading "-").
     */
    public final String getName()
    {
        return name;
    }

    /**
     * The long name of the option (including the leading "--").
     */
    public final String getLongName()
    {
        return longName;
    }

    /**
     * Returns <code>true</code> if the option this handler represents is mandatory.
     */
    public final boolean isRequired()
    {
        return option.required();
    }

    /**
     * Returns the usage string for the option.
     */
    public final String getUsage()
    {
        return option.usage();
    }

    public final boolean printForExample(ExampleMode mode)
    {
        return mode.print(option) && (option.skipForExample() == false);
    }

    public final String getMetaVariable(ResourceBundle rb)
    {
        String token = option.metaVar();
        if (token.length() == 0)
            token = getDefaultMetaVariable();
        if (token == null)
            return null;

        if (rb != null)
        {
            String localized = rb.getString(token);
            if (localized != null)
                token = localized;
        }

        return token;
    }

}
