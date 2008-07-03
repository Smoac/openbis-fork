/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.concurrent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ch.systemsx.cisd.common.exceptions.StopException;
import ch.systemsx.cisd.common.exceptions.TimeoutException;

/**
 * A class that can provide a dynamic {@link Proxy} for an interface that delegates the method
 * invocations to an implementation of this interface, monitoring for calls to
 * {@link Thread#interrupt()} and for timeouts. (Note that by default no timeout is set.)
 * <p>
 * On calls to {@link Thread#interrupt()} the proxy will throw a {@link StopException}, on timeouts
 * a {@link TimeoutException}.
 * <p>
 * The proxy can be configured by chaining. If all options have been set, the actual proxy can be
 * obtained by calling {@link #get()}. In order to e.g. set the timeout to 10s, the following call
 * chain can be used:
 * 
 * <pre>
 * If proxy = MonitoringProxy.create(If.class, someInstance).timeoutMillis(10000L).get();
 * </pre>
 * 
 * Instead of throwing a exception, the proxy can also be configured to provide special error values
 * on error conditions. This configuration is done by {@link #errorValueOnInterrupt()} for thread
 * interrupts and {@link #errorValueOnTimeout()} for timeouts.
 * <p>
 * The error return values can be set, either for the type of the return value of a method, or by
 * the method itself. If present, the specific method-value mapping will take precedence of the
 * generic return-type-value mapping. In order to set a value "ERROR" for String return types, use a
 * chaining call like:
 * 
 * <pre>
 * If proxy =
 *         MonitoringProxy.create(If.class, someInstance).errorValueOnInterrupt()
 *                 .errorTypeValueMapping(String.class, &quot;ERROR&quot;).get();
 * </pre>
 * 
 * @author Bernd Rinn
 */
public class MonitoringProxy<T>
{

    private final static ExecutorService executor =
            new NamingThreadPoolExecutor("Monitoring Proxy").daemonize();

    private final DelegatingInvocationHandler<T> delegate;

    private final Map<Class<?>, Object> errorTypeValueMap;

    private final Map<Method, Object> errorMethodValueMap;

    private final MonitoringInvocationHandler handler;

    private final T proxy;

    private long timeoutMillis;

    private boolean errorValueOnTimeout;

    private boolean errorValueOnInterrupt;

    private String nameOrNull;

    private static class DelegatingInvocationHandler<T> implements InvocationHandler
    {

        private final T objectToProxyFor;

        private DelegatingInvocationHandler(T objectToProxyFor)
        {
            this.objectToProxyFor = objectToProxyFor;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            try
            {
                return method.invoke(objectToProxyFor, args);
            } catch (InvocationTargetException ex)
            {
                throw ex.getTargetException();
            }
        }

    }

    private static String describe(Method method)
    {
        final StringBuilder builder = new StringBuilder(100);
        builder.append("Call to method '");
        builder.append(method.getDeclaringClass().getSimpleName());
        builder.append('.');
        builder.append(method.getName());
        builder.append('(');
        boolean addComma = false;
        for (Class<?> clazz : method.getParameterTypes())
        {
            if (addComma)
            {
                builder.append(',');
            }
            builder.append(clazz.getSimpleName());
            addComma = true;
        }
        builder.append(")'");
        return builder.toString();
    }

    private static Map<Class<?>, Object> createDefaultErrorTypeValueMap()
    {
        final Map<Class<?>, Object> result = new HashMap<Class<?>, Object>();
        result.put(Void.TYPE, Void.TYPE.cast(null));
        result.put(Boolean.TYPE, false);
        result.put(Byte.TYPE, (byte) 0);
        result.put(Short.TYPE, (short) 0);
        result.put(Integer.TYPE, 0);
        result.put(Long.TYPE, 0L);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static <T> T cast(Class<T> interfaceClass, Object objectToProxyFor)
    {
        return (T) objectToProxyFor;
    }

    private class MonitoringInvocationHandler implements InvocationHandler
    {

        public Object invoke(final Object myProxy, final Method method, final Object[] args)
                throws Throwable
        {
            final String callingThreadName = Thread.currentThread().getName();
            final Future<Object> future = executor.submit(new NamedCallable<Object>()
                {
                    public Object call() throws Exception
                    {
                        try
                        {
                            return delegate.invoke(myProxy, method, args);
                        } catch (Throwable th)
                        {
                            if (th instanceof Error)
                            {
                                throw (Error) th;
                            } else
                            {
                                throw (Exception) th;
                            }
                        }
                    }

                    public String getCallableName()
                    {
                        if (nameOrNull != null)
                        {
                            return callingThreadName + "::" + nameOrNull;
                        } else
                        {
                            return callingThreadName + "::" + describe(method);
                        }
                    }
                });
            final ExecutionResult<Object> result =
                    ConcurrencyUtilities.getResult(future, timeoutMillis);
            if (result.getStatus() == ExecutionStatus.TIMED_OUT)
            {
                if (errorValueOnTimeout == false)
                {
                    throw new TimeoutException(describe(method) + " timed out (timeout="
                            + timeoutMillis + "ms).");
                }
                return getErrorValue(method);
            }
            if (result.getStatus() == ExecutionStatus.INTERRUPTED && errorValueOnInterrupt)
            {
                return getErrorValue(method);
            }
            return ConcurrencyUtilities.tryDealWithResult(result);
        }

        private Object getErrorValue(final Method method)
        {
            if (errorMethodValueMap.containsKey(method))
            {
                return errorMethodValueMap.get(method);
            }
            if (errorTypeValueMap.containsKey(method.getReturnType()))
            {
                return errorTypeValueMap.get(method.getReturnType());
            }
            return null;
        }
    }

    private MonitoringProxy(Class<T> interfaceClass, T objectToProxyFor)
    {
        assert interfaceClass.isInterface();

        this.errorTypeValueMap = createDefaultErrorTypeValueMap();
        this.errorMethodValueMap = new HashMap<Method, Object>();
        this.timeoutMillis = ConcurrencyUtilities.NO_TIMEOUT;
        this.delegate = new DelegatingInvocationHandler<T>(objectToProxyFor);
        this.handler = new MonitoringInvocationHandler();
        this.proxy = createProxy(interfaceClass, objectToProxyFor);
    }

    private T createProxy(Class<T> interfaceClass, T objectToProxyFor)
    {
        final Class<?>[] interfaceClasses = new Class<?>[]
            { interfaceClass };
        final Object proxyInstance =
                Proxy.newProxyInstance(interfaceClass.getClassLoader(), interfaceClasses, handler);
        return cast(interfaceClass, proxyInstance);

    }

    /**
     * Creates a {@link MonitoringProxy} of type <var>interfaceClass</var> for the
     * <var>objectToProxyFor</var>.
     * 
     * @param interfaceClass The type of the interface to proxy for. It is a programming error to
     *            provide a class that does not represent an interface.
     * @param objectToProxyFor The object to proxy for.
     * @return A monitoring proxy that can be configured as required and that finally provides the
     *         actual proxy with {@link #get()}.
     */
    public static <T> MonitoringProxy<T> create(Class<T> interfaceClass, T objectToProxyFor)
    {
        return new MonitoringProxy<T>(interfaceClass, objectToProxyFor);
    }

    /**
     * Sets the timeout to <var>newTimeoutMillis</var>.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> timeoutMillis(long newTimeoutMillis)
    {
        this.timeoutMillis = newTimeoutMillis;
        return this;
    }

    /**
     * Sets the mode where a special error value is provided on timeout.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> errorValueOnTimeout()
    {
        this.errorValueOnTimeout = true;
        return this;
    }

    /**
     * Sets the mode where a special error value is provided on thread interruption.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> errorValueOnInterrupt()
    {
        this.errorValueOnInterrupt = true;
        return this;
    }

    /**
     * Sets the name of this proxy (for setting the thread name) to <var>newName</var>.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> name(String newName)
    {
        this.nameOrNull = newName;
        return this;
    }

    /**
     * Sets an error return <var>value</var> for the type <var>clazz</var>.
     * <p>
     * <i>A value set by this method is only relevant if the proxy is configured to return error
     * values rather than to throw exceptions.</i>
     */
    public <V> MonitoringProxy<T> errorTypeValueMapping(Class<V> clazz, V value)
    {
        errorTypeValueMap.put(clazz, value);
        return this;
    }

    /**
     * Sets an error return <var>value</var> for the given <var>method</var>. This <var>value</var>
     * takes precedence over the error type mapping for methods with the same return type.
     * <p>
     * <i>A value set by this method is only relevant if the proxy is configured to return error
     * values rather than to throw exceptions.</i>
     */
    public MonitoringProxy<T> errorMethodValueMapping(Method method, Object value)
    {
        errorMethodValueMap.put(method, value);
        return this;
    }

    /**
     * Returns the actual proxy.
     */
    public T get()
    {
        return proxy;
    }

}
