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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.annotation.Mandatory;
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
     * For given <code>Class</code> returns a list of fields that are annotated with {@link Mandatory}.
     * 
     * @param fields if <code>null</code>, then a new <code>List</code> is created.
     */
    public final static List<Field> getMandatoryFields(Class clazz, List<Field> fields)
    {
        List<Field> list = fields;
        if (list == null)
        {
            list = new ArrayList<Field>();
        }
        for (Field field : clazz.getFields())
        {
            if (field.getAnnotation(Mandatory.class) != null)
            {
                list.add(field);
            }
        }
        Class superclass = clazz.getSuperclass();
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
    // TODO 2007.06.14 Christian Ribeaud: 'method.getName()' is not specific enough. You have to used kind of
    // or part of 'Method.toGenericString()'.
    public final static Method getCurrentMethod()
    {
        StackTraceElement[] elements = new Throwable().getStackTrace();
        // Index 0 is *this* method
        StackTraceElement element = elements[1];
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
     * The {@link InvocationHandler} for the {@link ClassUtils#createProxy(Object, Class, Class[])} method.
     * 
     * @author Bernd Rinn
     */
    private static final class ProxyingInvocationHandler implements InvocationHandler
    {
        private final Object proxiedObject;

        ProxyingInvocationHandler(Object proxiedObject)
        {
            this.proxiedObject = proxiedObject;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            final Method proxyThroughMethod =
                    proxiedObject.getClass().getMethod(method.getName(), method.getParameterTypes());
            return proxyThroughMethod.invoke(proxiedObject, args);
        }
    }

    private static boolean fitsInterfaces(Class classToCheck, List<Class> interfaces)
    {
        for (Class ifs : interfaces)
        {
            if (ifs.isInterface() == false)
            {
                System.err.println("'" + ifs.getCanonicalName() + "' is not an interface.");
                return false;
            }
            for (Method method : ifs.getMethods())
            {
                try
                {
                    classToCheck.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ex)
                {
                    System.err.println("'" + classToCheck.getCanonicalName() + "' does not have a method '" + method
                            + "'");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a {@link Proxy} for <var>objectToBeProxied</var>. This allows to retroactively fit a class into a couple
     * of interfaces.
     * 
     * @param objectToProxy The object to proxy through to.
     * @param mainInterfaceToProvideProxyFor The main interface to provide a proxy for and to cast to.
     * @param otherInterfacesToProvideProxyFor Other interfaces to provide a proxy for.
     */
    // Proxy guarantees that the type cast is OK.
    @SuppressWarnings("unchecked")
    public final static <T> T createProxy(Object objectToProxy, Class<T> mainInterfaceToProvideProxyFor,
            Class... otherInterfacesToProvideProxyFor)
    {
        final List<Class> interfaces = new ArrayList<Class>();
        interfaces.add(mainInterfaceToProvideProxyFor);
        interfaces.addAll(Arrays.asList(otherInterfacesToProvideProxyFor));
        assert fitsInterfaces(objectToProxy.getClass(), interfaces);
        return (T) Proxy.newProxyInstance(ClassUtils.class.getClassLoader(), interfaces.toArray(new Class[interfaces
                .size()]), new ProxyingInvocationHandler(objectToProxy));
    }

    private static boolean isNull(Object objectToCheck)
    {
        return (objectToCheck instanceof Number) && ((Number) objectToCheck).longValue() == 0;
    }

    /**
     * Checks the list of bean objects item by item for public getters which return <code>null</code> or 0.
     * 
     * @param beanListToCheck The list of beans to check. Can be <code>null</code>.
     * @return <var>beanListToCheck</var> (the parameter itself)
     * @see #checkGettersNotNull(Object)
     * @throws IllegalStateException If at least one of the public getters returns <code>null</code> or 0.
     */
    public final static <T> List<T> checkGettersNotNull(List<T> beanListToCheck)
    {
        if (beanListToCheck == null)
        {
            return beanListToCheck;
        }
        for (Object bean : beanListToCheck)
        {
            checkGettersNotNull(bean);
        }
        return beanListToCheck;
    }

    /**
     * Checks bean object for public getters which return <code>null</code> or 0.
     * 
     * @param beanToCheck The bean to check. Can be <code>null</code>. Must not be an array type.
     * @return <var>beanToCheck</var> (the parameter itself)
     * @throws IllegalArgumentException If the <var>beanToCheck</var> is an array type.
     * @throws IllegalStateException If at least one of the public getters returns <code>null</code> or 0.
     */
    public final static <T> T checkGettersNotNull(T beanToCheck)
    {
        if (beanToCheck == null)
        {
            return beanToCheck;
        }
        if (beanToCheck.getClass().isArray())
        {
            throw new IllegalArgumentException("Arrays are not supported.");
        }
        for (Method method : beanToCheck.getClass().getMethods())
        {
            if (method.getName().startsWith("get") && method.getParameterTypes().length == 0
                    && Modifier.isPublic(method.getModifiers()))
            {
                try
                {
                    final Object result = method.invoke(beanToCheck, new Object[0]);
                    if (result == null)
                    {
                        throw new IllegalStateException("Method '" + method.getName() + "' returns null.");
                    } else if (isNull(result))
                    {
                        throw new IllegalStateException("Method '" + method.getName() + "' returns 0.");
                    }
                } catch (InvocationTargetException ex)
                {
                    final Throwable cause = ex.getCause();
                    if (cause instanceof Error)
                    {
                        throw (Error) cause;
                    }
                    throw CheckedExceptionTunnel.wrapIfNecessary((Exception) cause);
                } catch (IllegalAccessException ex)
                {
                    // Can't happen since we checked for isAccessible()
                    throw new Error("Cannot call method '" + method.getName() + "'.");
                }
            }
        }
        return beanToCheck;
    }

}
