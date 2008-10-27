/*
 * Copyright 2007 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.systemsx.cisd.common.utilities;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;

/**
 * Operations on classes using reflection.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtils
{
    private ClassUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Gathers all classes and interfaces the specified object can be casted to.
     */
    public final static Collection<Class<?>> gatherAllCastableClassesAndInterfacesFor(
            final Object object)
    {
        assert object != null : "Unspecified object";

        final Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
        classes.add(object.getClass());
        classes.addAll(getAllSuperclasses(object));
        classes.addAll(getAllInterfaces(object));
        return classes;
    }

    @SuppressWarnings("unchecked")
    private final static List<Class<?>> getAllInterfaces(final Object object)
    {
        assert object != null : "Unspecified object";
        return org.apache.commons.lang.ClassUtils.getAllInterfaces(object.getClass());
    }

    @SuppressWarnings("unchecked")
    private final static List<Class<?>> getAllSuperclasses(final Object object)
    {
        assert object != null : "Unspecified object";
        return org.apache.commons.lang.ClassUtils.getAllSuperclasses(object.getClass());
    }

    /**
     * Creates a new instance of a class specified by its fully-qualified name.
     * 
     * @param superClazz Super class <code>className</code> has to be implemented or extended.
     * @param clazz Fully-qualified class.
     * @param argumentsOrNull Optional constructor arguments. If <code>(Object[]) null</code> then
     *            the empty constructor will be used. Note that <code>(Object) null</code> is not
     *            interpreted as <code>null</code> arguments but rather as
     *            <code>new Object[]{null}</code>.
     * @return an instance of type <code>interface</code>.
     */
    public final static <T, C> T create(final Class<T> superClazz, final Class<C> clazz,
            final Object... argumentsOrNull)
    {
        assert superClazz != null : "Missing super class";
        assert clazz != null : "Missing class name";

        try
        {
            assert clazz.isInterface() == false : "Interface '" + clazz.getName()
                    + "' can not be instanciated as it is an interface.";
            assert superClazz.isAssignableFrom(clazz) : "Class '" + clazz.getName()
                    + "' does not implements/extends '" + superClazz.getName() + "'.";
            if (argumentsOrNull == null)
            {
                return cast(clazz.newInstance());
            }
            final Class<?>[] classes = getClasses(argumentsOrNull);
            final Constructor<T> constructor = getConstructor(clazz, classes);
            if (constructor == null)
            {
                if (classes.length == 0)
                {
                    throw new IllegalArgumentException("No default constructor found for " + clazz);
                }
                throw new IllegalArgumentException("No constructor found for " + clazz
                        + " with arguments of the following types: " + Arrays.asList(classes));
            }
            return constructor.newInstance(argumentsOrNull);
        } catch (final InstantiationException ex)
        {
        } catch (final IllegalAccessException ex)
        {
        } catch (final InvocationTargetException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex.getCause());
        } catch (final NoSuchMethodException ex)
        {
        }
        throw new IllegalArgumentException(String.format(
                "Cannot instantiate class '%s' with given arguments '%s'.", clazz.getName(), Arrays
                        .asList(argumentsOrNull)));
    }

    /**
     * Creates a new instance of a class specified by its fully-qualified name.
     * 
     * @param superClazz Super class <code>className</code> has to be implemented or extended.
     * @param className Fully-qualified class name.
     * @param argumentsOrNull Optional constructor arguments. If <code>(Object[]) null</code> then
     *            the empty constructor will be used. Note that <code>(Object) null</code> is not
     *            interpreted as <code>null</code> arguments but rather as
     *            <code>new Object[]{null}</code>.
     * @return an instance of type <code>interface</code>.
     */
    public final static <T> T create(final Class<T> superClazz, final String className,
            final Object... argumentsOrNull)
    {
        assert superClazz != null : "Missing super class";
        assert className != null : "Missing class name";

        try
        {
            return create(superClazz, Class.forName(className), argumentsOrNull);
        } catch (final ClassNotFoundException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(final Object newInstance)
    {
        return (T) newInstance;
    }

    private final static Class<?>[] getClasses(final Object... initargs)
    {
        final Class<?>[] classes = new Class<?>[initargs.length];
        int i = 0;
        for (final Object initarg : initargs)
        {
            classes[i++] = initarg.getClass();
        }
        return classes;
    }

    private final static <T> Constructor<T> getConstructor(final Class<?> clazz,
            final Class<?>[] classes) throws NoSuchMethodException
    {
        final Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> returned = null;
        for (final Constructor<?> constructor : constructors)
        {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            final int len = parameterTypes.length;
            if (len != classes.length)
            {
                continue;
            }
            boolean match = true;
            for (int i = 0; i < len; i++)
            {
                final Class<?> parameterType = parameterTypes[i];
                final Class<?> c = classes[i];
                match &= parameterType.equals(c) || parameterType.isAssignableFrom(c);
            }
            if (match)
            {
                returned = constructor;
            }
        }
        return toGenericType(returned);
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> toGenericType(final Constructor<?> returned)
    {
        return (Constructor<T>) returned;
    }

    /**
     * Creates a new instance of given <var>clazz</var> and get rid of any checked exception.
     * <p>
     * Wraps any checked exception in a {@link CheckedExceptionTunnel}.
     * </p>
     */
    public final static <T> T createInstance(final Class<T> clazz)
    {
        try
        {
            return clazz.newInstance();
        } catch (final InstantiationException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } catch (final IllegalAccessException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    /**
     * Invokes given <var>method</var> and get rid of any checked exception.
     * <p>
     * Wraps any checked exception in a {@link CheckedExceptionTunnel}.
     * </p>
     */
    public final static Object invokeMethod(final Method method, final Object obj,
            final Object... args)
    {
        try
        {
            return method.invoke(obj, args);
        } catch (final IllegalAccessException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } catch (final InvocationTargetException ex)
        {
            // We are interested in the cause exception.
            throw CheckedExceptionTunnel.wrapIfNecessary(ex.getCause());
        }
    }

    /**
     * Sets declared field named <var>fieldName</var> of given <var>object</var> to given new
     * value <var>newValue</var>.
     * <p>
     * This is useful when you want to set a <code>private</code> field on which you do not have
     * access. Note that this method should only be used in very special cases. You should consider
     * it as a hack.
     * </p>
     * 
     * @return a <code>true</code> if <code>fieldName</code> has been modified.
     */
    public final static boolean setFieldValue(final Object object, final String fieldName,
            final Object newValue)
    {
        assert object != null : "Unspecified object.";
        final Class<?> clazz = object.getClass();
        try
        {
            final Field field = tryGetDeclaredField(clazz, fieldName);
            if (field != null)
            {
                field.set(object, newValue);
                return true;
            }
        } catch (final SecurityException ex)
        {
        } catch (final IllegalArgumentException ex)
        {
        } catch (final IllegalAccessException ex)
        {
        }
        return false;
    }

    /**
     * Gets declared field named <var>fieldName</var> in given class or superclass of it.
     * <p>
     * Before returning it, it call {@link Field#setAccessible(boolean)} with <code>true</code>.
     * </p>
     * 
     * @return <code>null</code> if given <var>fieldName</var> could not be found.
     */
    public final static Field tryGetDeclaredField(final Class<?> c, final String fieldName)
    {
        assert c != null : "Unspecified class.";
        assert StringUtils.isNotBlank(fieldName) : "Blank field name.";
        Field field = null;
        Class<?> clazz = c;
        do
        {
            try
            {
                field = clazz.getDeclaredField(fieldName);
            } catch (final SecurityException ex)
            {
            } catch (final NoSuchFieldException ex)
            {
            }
            clazz = clazz.getSuperclass();
        } while (field == null && clazz != null);
        if (field != null)
        {
            field.setAccessible(true);
        }
        return field;
    }

    /**
     * Describes given <var>method</var> in following format:
     * <code>&lt;class-name&gt;.&lt;method-name&gt;</code>, for instance
     * <code>Object.hashCode</code>.
     */
    public final static String describeMethod(final Method method)
    {
        assert method != null : "Unspecified method";
        return method.getDeclaringClass().getSimpleName() + "." + method.getName();
    }

    @SuppressWarnings("unchecked")
    public final static <T> T[] createArray(final Class<T> clazz, int len)
    {
        assert clazz != null : "Unspecified class.";
        assert len > -1 : "Negative array length.";
        return (T[]) Array.newInstance(clazz, len);
    }
}
