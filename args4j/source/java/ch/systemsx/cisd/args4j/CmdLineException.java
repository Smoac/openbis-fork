package ch.systemsx.cisd.args4j;

/**
 * Signals an error in the user input.
 * 
 * @author Kohsuke Kawaguchi
 */
public class CmdLineException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public CmdLineException(String message)
    {
        super(message);
    }

    public CmdLineException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public CmdLineException(Throwable cause)
    {
        super(cause);
    }
}
