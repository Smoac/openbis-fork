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

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.annotation.Mandatory;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Operations on classes using reflection.
 * 
 * @author Christian Ribeaud
 */
public final class ClassUtils
{
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, ClassUtils.class);

    private ClassUtils()
    {
        // Can not be instantiated.
    }

    /**
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     */
    public final static List<Field> getMandatoryFields(Class<?> clazz)
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
     * </p>
     * IMPORTANT NOTE: You should carefully use this method in a class having more than one method with the same name.
     * The internal idea used here (<code>new Throwable().getStackTrace()</code>) only returns a method name and
     * does not make any other consideration.
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
     * Searches for <code>resource</code> in different places. The search order is as follows:
     * <ol>
     * <li>Search for <code>resource</code> using the thread context class loader under Java2. If that fails, search
     * for <code>resource</code> using the class loader that loaded this class (<code>Loader</code>).</li>
     * <li>Try one last time with <code>ClassLoader.getSystemResourceAsStream(resource)</code>, that is is using the
     * system class loader in JDK 1.2 and virtual machine's built-in class loader in JDK 1.1.</li>
     * </ol>
     */
    public final static InputStream getResourceAsStream(String resource)
    {
        ClassLoader classLoader = null;
        try
        {
            // Let's try the Thread Context Class Loader
            classLoader = getTCL();
            if (classLoader != null)
            {
                if (machineLog.isDebugEnabled())
                {
                    machineLog.debug("Trying to find [" + resource + "] using '" + classLoader + "' class loader.");
                }
                InputStream is = classLoader.getResourceAsStream(resource);
                if (is != null)
                {
                    return is;
                }
            }
        } catch (Throwable t)
        {
            machineLog.warn("Caught Exception while in Loader.getResourceAsStream. This may be innocuous.", t);
        }

        try
        {
            // We could not find resource. Let us now try with the
            // classloader that loaded this class.
            classLoader = ClassUtils.class.getClassLoader();
            if (classLoader != null)
            {
                if (machineLog.isDebugEnabled())
                {
                    machineLog.debug("Trying to find [" + resource + "] using '" + classLoader + "' class loader.");
                }
                InputStream is = classLoader.getResourceAsStream(resource);
                if (is != null)
                {
                    return is;
                }
            }
        } catch (Throwable t)
        {
            machineLog.warn("Caught Exception while in Loader.getResourceAsStream. This may be innocuous.", t);
        }

        // Last ditch attempt: get the resource from the class path. It
        // may be the case that clazz was loaded by the Extentsion class
        // loader which the parent of the system class loader. Hence the
        // code below.
        if (machineLog.isDebugEnabled())
        {
            machineLog.debug("Trying to find [" + resource + "] using ClassLoader.getSystemResourceAsStream().");
        }
        return ClassLoader.getSystemResourceAsStream(resource);
    }

    /**
     * Gets the thread context class loader.
     */
    private final static ClassLoader getTCL() throws IllegalAccessException, InvocationTargetException
    {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>()
            {
                //
                // PrivilegedAction
                //

                public final ClassLoader run()
                {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
    }
}
