package ch.systemsx.cisd.args4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import ch.systemsx.cisd.args4j.spi.EnumOptionHandler;
import ch.systemsx.cisd.args4j.spi.OptionHandler;
import ch.systemsx.cisd.args4j.spi.Setter;

/**
 * This class holds all command line options discovered for the program.
 * 
 * @author Kohsuke Kawaguchi
 * @author Bernd Rinn
 */
class CmdLineOptions
{

    /**
     * Returns all {@link OptionHandler}s registered for the program.
     */
    private final Map<String, OptionHandler> options = new HashMap<String, OptionHandler>();

    private final List<OptionHandler> handlers = new ArrayList<OptionHandler>();

    private boolean sorted;

    Collection<OptionHandler> getHandlers()
    {
        if (sorted == false)
        {
            Collections.sort(handlers, new Comparator<OptionHandler>()
                {
                    public int compare(OptionHandler h1, OptionHandler h2)
                    {
                        final String n1 = h1.getName();
                        final String n2 = h2.getName();
                        return n1.compareTo(n2);
                    }
                });
            sorted = true;
        }
        return handlers;
    }

    void addOption(Setter<?> setter, Option o)
    {
        if ("".equals(o.name()) && "".equals(o.longName()))
        {
            throw new IllegalAnnotationError(
                    "Option does have neither a short name nor a long name");
        }
        OptionHandler h = createOptionHandler(o, setter);
        String name = h.getName();
        String longName = h.getLongName();
        if (name.length() > 0 && options.put(name, h) != null)
        {
            throw new IllegalAnnotationError("Option name " + name + " is used more than once");
        }
        if (longName.length() > 0 && options.put(longName, h) != null)
        {
            throw new IllegalAnnotationError("Option name " + longName + " is used more than once");
        }
        handlers.add(h);
    }

    /**
     * Creates an {@link OptionHandler} that handles the given {@link Option} annotation and the
     * {@link Setter} instance.
     */
    private static <T> OptionHandler createOptionHandler(Option o, Setter<T> setter)
    {

        Constructor<? extends OptionHandler> handlerType;
        Class<? extends OptionHandler> h = o.handler();
        if (h == OptionHandler.class)
        {
            // infer the type

            // enum is the special case
            Class<T> t = setter.getType();
            if (Enum.class.isAssignableFrom(t))
            {
                return createEnumOptionHandler(o, setter, t);
            }

            handlerType = HandlerClasses.handlerClasses.get(t);
            if (handlerType == null)
            {
                throw new IllegalAnnotationError("No OptionHandler is registered to handle " + t);
            }
        } else
        {
            handlerType = HandlerClasses.getConstructor(h);
        }

        try
        {
            return handlerType.newInstance(o, setter);
        } catch (InstantiationException e)
        {
            throw new IllegalAnnotationError(e);
        } catch (IllegalAccessException e)
        {
            throw new IllegalAnnotationError(e);
        } catch (InvocationTargetException e)
        {
            throw new IllegalAnnotationError(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> OptionHandler createEnumOptionHandler(Option o, Setter<T> setter, Class<T> t)
    {
        return new EnumOptionHandler(o, setter, t);
    }

    void checkRequiredOptionsPresent(Set<OptionHandler> present) throws CmdLineException
    {
        // make sure that all mandatory options are present
        final StringBuilder builder = new StringBuilder();
        for (OptionHandler handler : getHandlers())
            if (handler.isRequired() && present.contains(handler) == false)
            {
                builder.append(handler.getName()).append(", ");
            }
        if (builder.length() > 0)
        {
            builder.setLength(builder.length() - 2); // remove trailing ", "
            throw new CmdLineException(Messages.REQUIRED_OPTION_MISSING.format() + builder);
        }
    }

    OptionHandler getHandlerForOption(String arg)
    {
        return options.get(arg);
    }

    int getMaxOptionPrintLength(ResourceBundle rb)
    {
        int maxLen = 0;
        for (OptionHandler handler : options.values())
        {
            String usage = handler.getUsage();
            if (usage.length() == 0)
                continue; // ignore

            final String metaVar = handler.getMetaVariable(rb);
            final int metaLen = (metaVar != null ? metaVar.length() + 1 : 0);
            final int nameLen = handler.getName().length();
            final int longNameLen = handler.getLongName().length();
            int len = nameLen + longNameLen + metaLen;
            if (nameLen > 0 && longNameLen > 0)
            {
                len += 3; // take into account "[,]"
            }
            maxLen = Math.max(maxLen, len);
        }
        return maxLen;
    }

}
