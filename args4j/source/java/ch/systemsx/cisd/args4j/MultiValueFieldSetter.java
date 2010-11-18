package ch.systemsx.cisd.args4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.args4j.spi.Setter;

/**
 * {@link Setter} that sets multiple values to a collection {@link Field}.
 * 
 * @author Kohsuke Kawaguchi
 */
final class MultiValueFieldSetter implements Setter<Object>
{
    private final Object bean;

    private final Field f;

    public MultiValueFieldSetter(Object bean, Field f)
    {
        this.bean = bean;
        this.f = f;

        if (List.class.isAssignableFrom(f.getType()) == false)
        {
            throw new IllegalAnnotationError(Messages.ILLEGAL_FIELD_SIGNATURE.format(f.getType()));
        }
    }

    @SuppressWarnings("unchecked")
    public Class<Object> getType()
    {
        Type t = f.getGenericType();
        if (t instanceof ParameterizedType)
        {
            ParameterizedType pt = (ParameterizedType) t;
            t = pt.getActualTypeArguments()[0];
            if (t instanceof Class)
            {
                return (Class<Object>) t;
            }
        }
        return Object.class;
    }

    public void addValue(Object value)
    {
        try
        {
            doAddValue(bean, value);
        } catch (IllegalAccessException _)
        {
            // try again
            f.setAccessible(true);
            try
            {
                doAddValue(bean, value);
            } catch (IllegalAccessException e)
            {
                throw new IllegalAccessError(e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void doAddValue(Object beanToAddValueTo, Object value) throws IllegalAccessException
    {
        Object o = f.get(beanToAddValueTo);
        if (o == null)
        {
            o = new ArrayList<Object>();
            f.set(beanToAddValueTo, o);
        }
        if ((o instanceof List) == false)
        {
            throw new IllegalAnnotationError("type of " + f + " is not a List");
        }

        ((List) o).add(value);
    }
}
