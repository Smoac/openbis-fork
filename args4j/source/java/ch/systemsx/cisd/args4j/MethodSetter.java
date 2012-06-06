package ch.systemsx.cisd.args4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ch.systemsx.cisd.args4j.spi.Setter;

/**
 * {@link Setter} that sets to a {@link Method}.
 * 
 * @author Kohsuke Kawaguchi
 */
final class MethodSetter<T> implements Setter<T>
{
    private final Object bean;

    private final Method m;

    public MethodSetter(Object bean, Method m)
    {
        this.bean = bean;
        this.m = m;
        if (m.getParameterTypes().length != 1)
            throw new IllegalAnnotationError(Messages.ILLEGAL_METHOD_SIGNATURE.format(m));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getType()
    {
        return (Class<T>) m.getParameterTypes()[0];
    }

    @Override
    public void addValue(T value) throws CmdLineException
    {
        try
        {
            try
            {
                m.invoke(bean, value);
            } catch (IllegalAccessException _)
            {
                // try again
                m.setAccessible(true);
                try
                {
                    m.invoke(bean, value);
                } catch (IllegalAccessException e)
                {
                    throw new IllegalAccessError(e.getMessage());
                }
            }
        } catch (InvocationTargetException e)
        {
            Throwable t = e.getTargetException();
            if (t instanceof RuntimeException)
                throw (RuntimeException) t;
            if (t instanceof Error)
                throw (Error) t;
            if (t instanceof CmdLineException)
                throw (CmdLineException) t;

            // otherwise wrap
            if (t != null)
                throw new CmdLineException(t);
            else
                throw new CmdLineException(e);
        }
    }
}
