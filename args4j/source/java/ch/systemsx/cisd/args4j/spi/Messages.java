package ch.systemsx.cisd.args4j.spi;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Kohsuke Kawaguchi
 */
enum Messages
{
    ILLEGAL_OPERAND;

    private static ResourceBundle rb;

    public String format(Object... args)
    {
        synchronized (Messages.class)
        {
            try
            {
                if (rb == null)
                {
                    rb = ResourceBundle.getBundle(Messages.class.getName());
                }
                return MessageFormat.format(rb.getString(name()), args);
            } catch (MissingResourceException ex)
            {
                // Fallback
                return name() + ": " + Arrays.asList(args).toString();
            }
        }
    }
}
