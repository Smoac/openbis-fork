package ch.systemsx.cisd.args4j;

import java.io.OutputStream;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import ch.systemsx.cisd.args4j.spi.OptionHandler;
import ch.systemsx.cisd.args4j.spi.Setter;

/**
 * Command line argument owner.
 * <p>
 * For a typical usage, see <a
 * href="https://args4j.dev.java.net/source/browse/args4j/args4j/examples/SampleMain.java?view=markup">this
 * example</a>.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class CmdLineParser
{
    /**
     * Option bean instance.
     */
    private final Object bean;

    /**
     * Provides all discovered options and their {@link OptionHandler}s.
     */
    private final CmdLineOptions options = new CmdLineOptions();

    private final CmdLineOptionPrinter optionPrinter = new CmdLineOptionPrinter(options);

    /**
     * {@link Setter} that accepts the arguments.
     */
    private Setter<String> argumentSetter;

    /**
     * Creates a new command line owner that parses arguments/options and set them into the given
     * object.
     * 
     * @param bean instance of a class annotated by {@link Option} and {@link Argument}. this
     *            object will receive values.
     * @throws IllegalAnnotationError if the option bean class is using args4j annotations
     *             incorrectly.
     */
    public CmdLineParser(Object bean)
    {
        this.bean = bean;
        addOptionsForBean();
    }

    @SuppressWarnings("unchecked")
    private void addOptionsForBean()
    {
        // Recursively process all the methods/fields.
        for (Class c = bean.getClass(); c != null; c = c.getSuperclass())
        {
            for (Method m : c.getDeclaredMethods())
            {
                Option o = m.getAnnotation(Option.class);
                if (o != null)
                {
                    options.addOption(new MethodSetter(bean, m), o);
                }
                Argument a = m.getAnnotation(Argument.class);
                if (a != null)
                {
                    addArgument(new MethodSetter<String>(bean, m));
                }
            }

            for (Field f : c.getDeclaredFields())
            {
                Option o = f.getAnnotation(Option.class);
                if (o != null)
                {
                    options.addOption(createFieldSetter(f), o);
                }
                Argument a = f.getAnnotation(Argument.class);
                if (a != null)
                {
                    addArgument(createFieldSetter(f));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Setter createFieldSetter(Field f)
    {
        if (List.class.isAssignableFrom(f.getType()))
            return new MultiValueFieldSetter(bean, f);
        // else if(Map.class.isAssignableFrom(f.getType()))
        // return new MapFieldSetter(bean,f);
        else
            return new FieldSetter(bean, f);
    }

    private void addArgument(Setter<String> setter)
    {
        if (argumentSetter != null)
            throw new IllegalAnnotationError("@Argument is used more than once");
        argumentSetter = setter;
    }

    /**
     * Prints the help text for the application to {@link System#err}.
     * 
     * @param programCall how to call the programm (e.g. "java MyApp")
     * @param genericOptions the generic form of the options (e.g. "[options [...]] ")
     * @param genericArgs the generic form of of the arguments (e.g. "<file1> <file2>")
     * @param mode whether and how to print an example
     */
    public void printHelp(String programCall, String genericOptions, String genericArgs,
            ExampleMode mode)
    {
        optionPrinter.printHelp(programCall, genericOptions, genericArgs, mode);
    }

    /**
     * Formats a command line example into a string. See
     * {@link #printExample(ExampleMode, ResourceBundle)} for more details.
     * 
     * @param mode must not be null.
     * @return always non-null.
     */
    public String printExample(ExampleMode mode)
    {
        return optionPrinter.getExampleString(mode);
    }

    /**
     * Formats a command line example into a string.
     * <p>
     * This method produces a string like " -d &lt;dir> -v -b", which is useful for printing a
     * command line example, perhaps as a part of the usage screen.
     * 
     * @param mode One of the {@link ExampleMode} constants. Must not be null. This determines what
     *            option should be a part of the returned string.
     * @param rb If non-null, meta variables (&lt;dir> in the above example) is treated as a key to
     *            this resource bundle, and the associated value is printed. See
     *            {@link Option#metaVar()}. This is to support localization. Passing <tt>null</tt>
     *            would print {@link Option#metaVar()} directly.
     * @return always non-null. If there's no option, this method returns just the empty string "".
     *         Otherwise, this method returns a string that contains a space at the beginning (but
     *         not at the end.) This allows you to do something like:
     * 
     * <pre>
     * System.err.println(&quot;java -jar my.jar&quot; + parser.printExample(REQUIRED) + &quot; arg1 arg2&quot;);
     * </pre>
     */
    public String printExample(ExampleMode mode, ResourceBundle rb)
    {
        return optionPrinter.getExampleString(mode, rb);
    }

    /**
     * Prints the list of options and their usages to the screen.
     * <p>
     * This is a convenience method for calling {@code printUsage(new OutputStreamWriter(out),null)}
     * so that you can do {@code printUsage(System.err)}.
     */
    public void printUsage(OutputStream out)
    {
        optionPrinter.printUsage(out);
    }

    /**
     * Prints the list of options and their usages to the <var>out</var>.
     * 
     * @param out the writer to output the usage to.
     * @param rb if this is non-null, {@link Option#usage()} is treated as a key to obtain the
     *            actual message from this resource bundle.
     */
    public void printUsage(Writer out, ResourceBundle rb)
    {
        optionPrinter.printUsage(out, rb);
    }

    /**
     * Parses the command line arguments and set them to the option bean given in the constructor.
     * 
     * @throws CmdLineException if there's any error parsing arguments, or if
     *             {@link Option#required() required} option was not given.
     */
    public void parseArgument(final String... args) throws CmdLineException
    {
        CmdLineParameters cmdLine = new CmdLineParameters(args);

        Set<OptionHandler> present = new HashSet<OptionHandler>();
        boolean endOfOptionMarkerFound = false;

        CommandLineArgumentLoop: while (cmdLine.hasMore())
        {
            String arg = cmdLine.getOptionName();
            if (endOfOptionMarkerFound == false && isOption(arg))
            {
                // check for "End Of Option" marker
                endOfOptionMarkerFound = isEndOfOptionMarker(arg);
                if (endOfOptionMarkerFound)
                {
                    cmdLine.proceed(1);
                    continue;
                }
                // parse this as an option
                final int eqIdx = arg.indexOf('=');
                if (eqIdx == -1)
                { // normal option
                    OptionHandler handler = options.getHandlerForOption(arg);

                    if (handler != null)
                    {
                        // known option
                        int diff = handler.parseArguments(cmdLine);
                        cmdLine.proceed(diff + 1);
                        present.add(handler);
                        continue;
                    }
                    // let's see whether we have an option with value but without whitespace
                    // inbetween
                    if (isLongOption(arg) == false)
                    {
                        int len = arg.length();
                        while (--len > 0)
                        {
                            final String optionOnTrial = arg.substring(0, len);
                            handler = options.getHandlerForOption(optionOnTrial);

                            if (handler != null)
                            {
                                // known option
                                handler.set(arg.substring(len));
                                cmdLine.proceed(1);
                                present.add(handler);
                                continue CommandLineArgumentLoop;
                            }
                        }
                    }
                } else
                { // key=value pair
                    OptionHandler handler = options.getHandlerForOption(arg.substring(0, eqIdx));

                    if (handler != null)
                    {
                        // known option
                        // int diff = handler.parseArguments(cmdLine);
                        handler.set(arg.substring(eqIdx + 1));
                        cmdLine.proceed(1);
                        present.add(handler);
                        continue;
                    }
                }

                throw new CmdLineException(Messages.UNDEFINED_OPTION.format(arg));
            } else
            {
                // parse this as arguments
                if (argumentSetter == null)
                    throw new CmdLineException(Messages.NO_ARGUMENT_ALLOWED.format(arg));
                argumentSetter.addValue(arg);
                cmdLine.proceed(1);
            }
        }

        options.checkRequiredOptionsPresent(present);
    }

    /**
     * Returns <code>true</code> if the given token is an option (as opposed to an argument.)
     */
    private boolean isOption(String arg)
    {
        return arg.startsWith("-");
    }

    /**
     * Returns <code>true</code> if the given token is a long-form option ("--something") (as
     * opposed to an argument.)
     */
    private boolean isLongOption(String arg)
    {
        return arg.startsWith("--");
    }

    /**
     * returns <code>true</code> if the given token is the "End Of Option" marker ("--").
     */
    private boolean isEndOfOptionMarker(String arg)
    {
        return "--".equals(arg);
    }

    public void setUsageWidth(int usageWidth)
    {
        optionPrinter.setTerminalWidth(usageWidth);
    }
}
