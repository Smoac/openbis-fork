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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import ch.systemsx.cisd.common.annotation.Mandatory;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

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
     * For given <code>Class</code> returns a set of field names that are annotated with {@link Mandatory}.
     */
    public final static Set<String> getMandatoryFields(Class<?> clazz)
    {
        Set<String> set = new HashSet<String>();
        List<Field> fields = ClassUtils.getMandatoryFieldsList(clazz);
        for (Field field : fields)
        {
            set.add(field.getName());
        }
        return set;
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     */
    private final static List<Field> getMandatoryFieldsList(Class<?> clazz)
    {
        return getMandatoryFields(clazz, null);
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     * 
     * @param fields if <code>null</code>, then a new <code>List</code> is created.
     */
    private final static List<Field> getMandatoryFields(Class<?> clazz, List<Field> fields)
    {
        List<Field> list = fields;
        if (list == null)
        {
            list = new ArrayList<Field>();
        }
        for (Field field : clazz.getDeclaredFields())
        {
            if (field.getAnnotation(Mandatory.class) != null)
            {
                list.add(field);
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null)
        {
            return getMandatoryFields(superclass, list);
        }
        return list;
    }

    /**
     * Returns the currently called <code>Method</code>.
     * <p>
     * Returns <code>null</code> if none could be found.
     * </p>
     */
    public final static Method getCurrentMethod()
    {
        return getMethodOnStack(2);
    }

    /**
     * Returns the <code>Method</code> on the stack of <var>level</var>.
     * <p>
     * <code>level=0</code> is this method itself, <code>level=1</code> is the method that called it and so forth.
     * This method internally uses {@link Class#getMethods()} to retrieve the <code>Method</code> (meaning that
     * <code>private</code> methods will not be found).
     * </p>
     * <p>
     * IMPORTANT NOTE: You should carefully use this method in a class having more than one method with the same name.
     * The internal idea used here (<code>new Throwable().getStackTrace()</code>) only returns a method name and
     * does not make any other consideration.
     * </p>
     * 
     * @see StackTraceElement#getMethodName()
     * @return <code>null</code> if none could be found.
     */
    public final static Method getMethodOnStack(int level)
    {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        if (elements.length <= level)
        {
            return null;
        }
        StackTraceElement element = elements[level];
        String methodName = element.getMethodName();
        try
        {
            Method[] methods = Class.forName(element.getClassName()).getMethods();
            for (Method method : methods)
            {
                if (method.getName().equals(methodName))
                {
                    return method;
                }
            }
            // SecurityException, ClassNotFoundException
        } catch (Exception ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return null;
    }

    /**
     * Creates a new instance of a class specified by its fully-qualified name.
     * 
     * @param superClazz Super class <code>className</code> has to be implemented or extended.
     * @param className Fully-qualified class name.
     * @param properties Optional constructor argument. If not <code>null</code> an constructor with one single
     *            <code>Properties</code> argument is expected. Otherwise the default constructor will used.
     * @return an instance of type <code>interfaze</code>.
     */
    public static <T> T create(Class<T> superClazz, String className, Properties properties)
    {
        assert superClazz != null : "Missing super class";
        assert className != null : "Missing class name";

        try
        {
            final Class<?> clazz = Class.forName(className);
            assert clazz.isInterface() == false : clazz + " can not be instanciated";
            assert superClazz.isAssignableFrom(clazz) : clazz + " does not implements/extends " + superClazz.getName();
            if (properties == null)
            {
                return createInstance(clazz);
            }
            final Constructor<?> constructor = clazz.getConstructor(new Class[]
                { Properties.class });
            return createInstance(constructor, properties);
        } catch (Exception ex)
        {
            throw new ConfigurationFailureException("Cannot instanitate class " + className, ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(final Class<?> clazz) throws InstantiationException, IllegalAccessException
    {
        return (T) clazz.newInstance();
    }

    @SuppressWarnings("unchecked")
    private static <T> T createInstance(final Constructor<?> constructor, Properties properties)
            throws InstantiationException, IllegalAccessException, InvocationTargetException
    {
        return (T) constructor.newInstance(new Object[]
            { properties });
    }

}
