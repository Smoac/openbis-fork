
import ch.systemsx.cisd.args4j.Argument;
import ch.systemsx.cisd.args4j.CmdLineException;
import ch.systemsx.cisd.args4j.CmdLineParser;
import ch.systemsx.cisd.args4j.ExampleMode;
import ch.systemsx.cisd.args4j.Option;
import ch.systemsx.cisd.args4j.spi.BooleanOptionHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample program that shows how you can use args4j.
 * 
 * @author Kohsuke Kawaguchi (kk@kohsuke.org)
 */
public class SampleMain
{

    @Option(name = "r", longName = "--recursive", usage = "recursively run something")
    private boolean recursive;

    @Option(name = "o", usage = "output to this file", metaVar = "OUTPUT")
    private File out = new File(".");

    @Option(name = "s", longName = "str")
    // no usage
    private String str = "(default value)";

    @Option(name = "n", longName = "-number", required = true, usage = "repeat <n> times\nusage can have new lines in it and also it can be verrrrrrrrrrrrrrrrrry long")
    private int num = -1;

    @Option(name = "l", longName = "--long-number", usage = "repeat <n> times\nusage can have new lines in it and also it can be verrrrrrrrrrrrrrrrrry long")
    private long lnum = -1;

    // using 'handler=...' allows you to specify a custom OptionHandler
    // implementation class. This allows you to bind a standard Java type
    // with a non-standard option syntax
    @Option(longName = "custom", handler = BooleanOptionHandler.class, usage = "boolean value for checking the custom handler")
    private boolean data;

    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

    public static void main(String[] args) throws IOException
    {
        new SampleMain().doMain(args);
    }

    private CmdLineParser parser = new CmdLineParser(this);

    @Option(longName = "--help", usage = "show this help")
    private void help(boolean exit)
    {
        parser.printHelp("java SampleMain", "[options...]", "arguments...", ExampleMode.ALL);
        if (exit)
        {
            System.exit(0);
        }
    }

    public void doMain(String[] args) throws IOException
    {
        // if you have a wider console, you could increase the value;
        // here 80 is also the default
        parser.setUsageWidth(80);

        try
        {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

            // after parsing arguments, you should check
            // if enough arguments are given.
            if (arguments.isEmpty())
                throw new CmdLineException("No argument is given");

        } catch (CmdLineException e)
        {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage());
            help(false);

            return;
        }

        // this will redirect the output to the specified output
        if (out != null)
        {
            System.setOut(new PrintStream(out));
        }

        if (recursive)
            System.out.println("-r flag is set");

        if (data)
            System.out.println("-custom flag is set");

        System.out.println("-str was " + str);

        if (num >= 0)
            System.out.println("-n was " + num);

        if (lnum >= 0)
            System.out.println("-l was " + lnum);

        // access non-option arguments
        System.out.println("other arguments are:");
        for (String s : arguments)
            System.out.println(s);
        if (out != null)
        {
            System.out.close();
        }
    }
}
