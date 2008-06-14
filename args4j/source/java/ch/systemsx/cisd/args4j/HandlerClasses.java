package ch.systemsx.cisd.args4j;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import ch.systemsx.cisd.args4j.spi.BooleanOptionHandler;
import ch.systemsx.cisd.args4j.spi.DoubleOptionHandler;
import ch.systemsx.cisd.args4j.spi.FileOptionHandler;
import ch.systemsx.cisd.args4j.spi.IntOptionHandler;
import ch.systemsx.cisd.args4j.spi.LongOptionHandler;
import ch.systemsx.cisd.args4j.spi.OptionHandler;
import ch.systemsx.cisd.args4j.spi.RegexOptionHandler;
import ch.systemsx.cisd.args4j.spi.Setter;
import ch.systemsx.cisd.args4j.spi.StringOptionHandler;

/**
 * A class that holds all handler classes.
 * 
 * @author Bernd Rinn
 */
public class HandlerClasses
{

    /**
     * All {@link OptionHandler}s known to the {@link CmdLineParser}. Constructors of
     * {@link OptionHandler}-derived class keyed by their supported types.
     */
    static final Map<Class<?>, Constructor<? extends OptionHandler>> handlerClasses =
            Collections
                    .synchronizedMap(new HashMap<Class<?>, Constructor<? extends OptionHandler>>());

    static
    {
        registerHandler(Boolean.class, BooleanOptionHandler.class);
        registerHandler(boolean.class, BooleanOptionHandler.class);
        registerHandler(File.class, FileOptionHandler.class);
        registerHandler(Integer.class, IntOptionHandler.class);
        registerHandler(int.class, IntOptionHandler.class);
        registerHandler(long.class, LongOptionHandler.class);
        registerHandler(Double.class, DoubleOptionHandler.class);
        registerHandler(double.class, DoubleOptionHandler.class);
        registerHandler(String.class, StringOptionHandler.class);
        registerHandler(Pattern.class, RegexOptionHandler.class);
        // enum is a special case
        // registerHandler(Map.class,MapOptionHandler.class);
    }

    /**
     * Registers a user-defined {@link OptionHandler} class with args4j.
     * <p>
     * This method allows users to extend the behavior of args4j by writing their own
     * {@link OptionHandler} implementation.
     * 
     * @param valueType The specified handler is used when the field/method annotated by
     *            {@link Option} is of this type.
     * @param handlerClass This class must have a constructor that takes an {@link Option} and a
     *            {@link Setter}.
     */
    public static void registerHandler(Class<?> valueType,
            Class<? extends OptionHandler> handlerClass)
    {
        if (valueType == null || handlerClass == null)
        {
            throw new IllegalArgumentException();
        }
        if (OptionHandler.class.isAssignableFrom(handlerClass) == false)
        {
            throw new IllegalArgumentException("Not an OptionHandler class");
        }

        Constructor<? extends OptionHandler> c = HandlerClasses.getConstructor(handlerClass);
        handlerClasses.put(valueType, c);
    }

    static Constructor<? extends OptionHandler> getConstructor(
            Class<? extends OptionHandler> handlerClass)
    {
        try
        {
            return handlerClass.getConstructor(Option.class, Setter.class);
        } catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException(handlerClass
                    + " does not have the proper constructor");
        }
    }

}
