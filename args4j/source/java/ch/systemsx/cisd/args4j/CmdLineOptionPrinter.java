package ch.systemsx.cisd.args4j;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ResourceBundle;

import ch.systemsx.cisd.args4j.spi.OptionHandler;

/**
 * Prints out the usage and example information of the {@link CmdLineOptions}.
 * 
 * @author Kohsuke Kawaguchi
 * @author Bernd Rinn
 */
class CmdLineOptionPrinter
{

    private final CmdLineOptions options;

    /**
     * The length of a usage line. If the usage message is longer than this value, the parser wraps
     * the line. Defaults to 80.
     */
    private int usageWidth = 80;

    CmdLineOptionPrinter(CmdLineOptions options)
    {
        this.options = options;
    }

    /**
     * Sets the width of the terminal (default is 80).
     */
    void setTerminalWidth(int usageWidth)
    {
        this.usageWidth = usageWidth;
    }

    /**
     * Prints the help text for the application to {@link System#err}.
     * 
     * @param programCall how to call the programm (e.g. "java MyApp")
     * @param genericOptions the generic form of the options (e.g. "[options [...]] ")
     * @param genericArgs the generic form of of the arguments (e.g. "<file1> <file2>")
     * @param mode whether and how to print an example
     */
    void printHelp(String programCall, String genericOptions, String genericArgs, ExampleMode mode)
    {
        System.err.println(programCall + " " + genericOptions + " " + genericArgs);
        // print the list of available options
        printUsage(System.err);
        System.err.println();

        if (ExampleMode.NONE.equals(mode) == false)
        {
            // print option sample. This is useful some time
            System.err.println("  Example: " + programCall + getExampleString(mode) + " "
                    + genericArgs);
        }
    }

    /**
     * Formats a command line example into a string. See
     * {@link #getExampleString(ExampleMode, ResourceBundle)} for more details.
     * 
     * @param mode must not be null.
     * @return always non-null.
     */
    String getExampleString(ExampleMode mode)
    {
        return getExampleString(mode, null);
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
    String getExampleString(ExampleMode mode, ResourceBundle rb)
    {
        if (ExampleMode.NONE.equals(mode))
        {
            return "";
        }
        StringBuilder buf = new StringBuilder();

        for (OptionHandler handler : options.getHandlers())
        {
            if (handler.printForExample(mode) == false)
                continue;

            buf.append(' ');
            if (handler.getName().length() > 0)
            {
                buf.append(handler.getName());
            } else
            {
                buf.append(handler.getLongName());
            }

            String metaVar = handler.getMetaVariable(rb);
            if (metaVar != null)
            {
                buf.append(' ').append(metaVar);
            }
        }

        return buf.toString();
    }

    /**
     * Prints the list of options and their usages to the screen.
     * <p>
     * This is a convenience method for calling {@code printUsage(new OutputStreamWriter(out),null)}
     * so that you can do {@code printUsage(System.err)}.
     */
    public void printUsage(OutputStream out)
    {
        printUsage(new OutputStreamWriter(out), null);
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
        PrintWriter w = new PrintWriter(out);
        // determine the length of the option + metavar first
        int maxOptionLength = options.getMaxOptionPrintLength(rb);

        // then print
        for (OptionHandler handler : options.getHandlers())
        {
            print(w, rb, handler, maxOptionLength);
        }

        w.flush();
    }

    private void print(PrintWriter w, ResourceBundle rb, OptionHandler handler, int maxOptionLength)
    {
        String usage = handler.getUsage();
        if (handler.isRequired())
        {
            usage += " (required)";
        }
        if (usage.length() == 0)
            return; // ignore

        w.print(' ');
        String optionName = handler.getName();
        String longOptionName = handler.getLongName();
        int headLen = 0;
        if (optionName.length() > 0)
        {
            if (longOptionName.length() > 0)
            {
                headLen = optionName.length() + longOptionName.length() + 3; // take "[,]" into
                                                                                // account
                w.print('[');
                w.print(optionName);
                w.print(',');
                w.print(longOptionName);
                w.print(']');
            } else
            {
                headLen = optionName.length();
                w.print(optionName);
            }
        } else
        {
            headLen = longOptionName.length();
            w.print(longOptionName);
        }

        String metaVar = handler.getMetaVariable(rb);
        if (metaVar != null)
        {
            w.print(' ');
            w.print(metaVar);
            headLen += metaVar.length() + 1;
        }
        for (; headLen < maxOptionLength; headLen++)
            w.print(' ');
        w.print(" : ");

        if (rb != null)
            usage = rb.getString(usage);

        int descriptionWidth = usageWidth - maxOptionLength - 4; // 3 for " : " + 1 for left-most
                                                                    // SP
        while (usage != null && usage.length() > 0)
        {
            final String[] lines = split(usage, descriptionWidth);
            w.println(lines[0]);
            usage = lines[1];
            if (usage.length() > 0)
                indent(w, maxOptionLength + 4);
        }

    }

    private static String[] split(String s, int maxLength)
    {
        int length = s.indexOf('\n');
        int offset = length + 1;
        if (length >= 0 && length <= maxLength)
        {
            return new String[]
                { s.substring(0, length), s.substring(offset) };
        }
        if (s.length() <= maxLength)
        {
            return new String[]
                { s, "" };
        }

        // see whether we can find a space to break the line
        length = maxLength;
        offset = maxLength;
        while (s.charAt(length) != ' ' && length > 0)
        {
            --length;
        }
        if (s.charAt(length) == ' ')
        {
            offset = length + 1;
        } else
        { // no space found, have to break within word
            length = maxLength;
            offset = maxLength;
        }
        return new String[]
            { s.substring(0, length), s.substring(offset) };
    }

    private static void indent(PrintWriter w, int count)
    {
        for (int i = count; i > 0; i--)
            w.print(' ');
    }

}
