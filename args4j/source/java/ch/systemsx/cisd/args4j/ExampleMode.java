package ch.systemsx.cisd.args4j;

/**
 * Used with {@link CmdLineParser#printExample(ExampleMode)}.
 * 
 * @author Kohsuke Kawaguchi
 */
public enum ExampleMode
{
    /**
     * Print all defined options in the example.
     * <p>
     * This would be useful only when you have small number of options.
     */
    ALL()
    {
        @Override
        public boolean print(Option o)
        {
            return true;
        }
    },

    /**
     * Doesn't print the example.
     */
    NONE()
    {
        @Override
        public boolean print(Option o)
        {
            return false;
        }
    },

    /**
     * Print all {@link Option#required() required} option.
     */
    REQUIRED()
    {
        @Override
        public boolean print(Option o)
        {
            return o.required();
        }
    };

    public abstract boolean print(Option o);
}
