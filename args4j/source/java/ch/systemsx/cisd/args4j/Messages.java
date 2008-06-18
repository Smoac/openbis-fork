package ch.systemsx.cisd.args4j;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * The {@link ResourceBundle} handler.
 * 
 * @author Christian Ribeaud
 */
enum Messages
{
    MISSING_OPERAND, UNDEFINED_OPTION, NO_ARGUMENT_ALLOWED, ILLEGAL_METHOD_SIGNATURE,
    ILLEGAL_FIELD_SIGNATURE, REQUIRED_OPTION_MISSING;

    private static ResourceBundle resourceBundle;

    /**
     * For given {@link #name()}, returns the corresponding formatted (with given <var>args</var>)
     * value.
     */
    public final String format(final Object... args)
    {
        synchronized (Messages.class)
        {
            if (resourceBundle == null)
            {
                resourceBundle = ResourceBundle.getBundle(Messages.class.getName());
            }
            final String value = resourceBundle.getString(name());
            return MessageFormat.format(value, args);
        }
    }
}
