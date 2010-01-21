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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.TimeoutExceptionUnchecked;
import ch.systemsx.cisd.base.namedthread.NamedCallable;
import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities.ILogSettings;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * A class that can provide a dynamic {@link Proxy} for an interface that delegates the method
 * invocations to an implementation of this interface, monitoring for calls to
 * {@link Thread#interrupt()} and for timeouts and allowing the invocation to be retried if it
 * failed. (Note that by default no timeout is set and no retrying of failed operations is
 * performed.)
 * <p>
 * On calls to {@link Thread#interrupt()} the proxy will throw a
 * {@link InterruptedExceptionUnchecked}, on timeouts a {@link TimeoutExceptionUnchecked}.
 * <p>
 * Retrying failed invocations is enabled by calling {@link #timing(TimingParameters)} with a retry
 * parameter greater than 0. You need to carefully consider whether the methods in the interface are
 * suitable for retrying or not. Note that there is no retrying on thread interruption, but only on
 * timeout. You can configure a 'white-list' of exception classes that are suitable for retrying the
 * operation. By default this white-list is empty, thus method invocations failing with an exception
 * will not be retried.
 * <p>
 * The proxy can be configured by chaining. If all options have been set, the actual proxy can be
 * obtained by calling {@link #get()}. In order to e.g. set the timeout to 10s, the following call
 * chain can be used:
 * 
 * <pre>
 * If proxy = MonitoringProxy.create(If.class, someInstance).timeoutMillis(10000L).get();
 * </pre>
 * 
 * Instead of throwing an exception, the proxy can also be configured to provide special error
 * values on error conditions. This configuration is done by {@link #errorValueOnInterrupt()} for
 * thread interrupts and {@link #errorValueOnTimeout()} for timeouts.
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
 * <p>
 * <i>Note:</i> A MonitoringProxy object can only be used safely from more than one thread if
 * <ol>
 * <li>The proxied object is thread-safe</li>
 * <li>No observer / sensor pattern is used to detect activity (can produce "false negatives" on
 * hanging method calls)</li>
 * </ol>
 * 
 * @author Bernd Rinn
 */
public class MonitoringProxy<T>
{
    private final static int NUMBER_OF_CORE_THREADS = 10;

    private final static ExecutorService executor =
            new NamingThreadPoolExecutor("Monitoring Proxy").corePoolSize(NUMBER_OF_CORE_THREADS)
                    .daemonize();

    private final DelegatingInvocationHandler<T> delegate;

    private final Map<Class<?>, Object> errorTypeValueMap;

    private final Map<Method, Object> errorMethodValueMap;

    private final Set<Class<? extends Exception>> exceptionClassesSuitableForRetrying;

    private final MonitoringInvocationHandler handler;

    private final T proxy;

    private TimingParameters timingParameters;

    private boolean errorValueOnTimeout;

    private boolean errorValueOnInterrupt;

    private String nameOrNull;

    private ISimpleLogger loggerOrNull;

    private IMonitoringProxyLogger invocationLoggerOrNull;

    private IActivitySensor sensorOrNull;

    private Set<Thread> currentThreads = Collections.synchronizedSet(new HashSet<Thread>());

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
                try
                {
                    return method.invoke(objectToProxyFor, args);
                } catch (IllegalAccessException ex)
                {
                    method.setAccessible(true);
                    return method.invoke(objectToProxyFor, args);
                }
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
            final ExecutionResult<Object> result = retryingExecuteInThread(myProxy, method, args);
            if (result.getStatus() == ExecutionStatus.TIMED_OUT)
            {
                if (errorValueOnTimeout == false)
                {
                    throw new TimeoutExceptionUnchecked(describe(method) + " timed out (timeout="
                            + timingParameters.getTimeoutMillis() + "ms).");
                }
                return getErrorValue(method);
            }
            if (result.getStatus() == ExecutionStatus.INTERRUPTED && errorValueOnInterrupt)
            {
                return getErrorValue(method);
            }
            return ConcurrencyUtilities.tryDealWithResult(result);
        }

        private ExecutionResult<Object> retryingExecuteInThread(final Object myProxy,
                final Method method, final Object[] args)
        {
            int counter = 0;
            ExecutionResult<Object> result = null;
            boolean willRetry;
            do
            {
                result = executeInThread(myProxy, method, args);
                if (result.getStatus() == ExecutionStatus.COMPLETE
                        || result.getStatus() == ExecutionStatus.INTERRUPTED
                        || exceptionStatusUnsuitableForRetry(result))
                {
                    willRetry = false;
                } else
                {
                    willRetry = (counter++ < timingParameters.getMaxRetriesOnFailure());
                }
                if (invocationLoggerOrNull != null)
                {
                    invocationLoggerOrNull.log(method, result, willRetry);
                }
                if (willRetry && timingParameters.getIntervalToWaitAfterFailureMillis() > 0)
                {
                    try
                    {
                        Thread.sleep(timingParameters.getIntervalToWaitAfterFailureMillis());
                    } catch (InterruptedException ex)
                    {
                        result = ExecutionResult.createInterrupted();
                        if (invocationLoggerOrNull != null)
                        {
                            invocationLoggerOrNull.log(method, result, false);
                        }
                        return result;
                    }
                }
            } while (willRetry);
            return result;
        }

        private boolean exceptionStatusUnsuitableForRetry(ExecutionResult<Object> result)
        {
            return (result.getStatus() == ExecutionStatus.EXCEPTION)
                    && exceptionClassesSuitableForRetrying.contains(result.tryGetException()
                            .getClass()) == false;
        }

        private ExecutionResult<Object> executeInThread(final Object myProxy, final Method method,
                final Object[] args)
        {
            final String callingThreadName = Thread.currentThread().getName();
            currentThreads.add(Thread.currentThread());
            try
            {
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
                final ILogSettings logSettingsOrNull =
                        (loggerOrNull == null) ? null : new ILogSettings()
                            {
                                public LogLevel getLogLevelForError()
                                {
                                    return LogLevel.ERROR;
                                }

                                public ISimpleLogger getLogger()
                                {
                                    return loggerOrNull;
                                }

                                public String getOperationName()
                                {
                                    if (nameOrNull != null)
                                    {
                                        return describe(method) + "[" + nameOrNull + "]";
                                    } else
                                    {
                                        return describe(method);
                                    }
                                }
                            };
                return ConcurrencyUtilities.getResult(future, timingParameters.getTimeoutMillis(),
                        true, logSettingsOrNull, sensorOrNull);
            } finally
            {
                currentThreads.remove(Thread.currentThread());
            }
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
        this.exceptionClassesSuitableForRetrying = new HashSet<Class<? extends Exception>>();
        this.timingParameters = TimingParameters.getNoTimeoutNoRetriesParameters();
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
     * Sets the timing parameters to <var>newParameters</var>.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> timing(TimingParameters newParameters)
    {
        assert newParameters != null;
        this.timingParameters = newParameters;
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
     * Sets the logger to be used for error logging to <var>newLogger</var>.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> errorLog(ISimpleLogger newLogger)
    {
        this.loggerOrNull = newLogger;
        return this;
    }

    /**
     * Sets the logger to be used for all invocations of methods of this proxy.
     * 
     * @return This object (for chaining).
     */
    public MonitoringProxy<T> invocationLog(IMonitoringProxyLogger newLogger)
    {
        this.invocationLoggerOrNull = newLogger;
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
     * Add an {@link Exception} class that is suitable for retrying the operation.
     */
    public MonitoringProxy<T> exceptionClassSuitableForRetrying(Class<? extends Exception> clazz)
    {
        exceptionClassesSuitableForRetrying.add(clazz);
        return this;
    }

    /**
     * Add a list of {@link Exception} classes that are suitable for retrying the operation.
     */
    public MonitoringProxy<T> exceptionClassesSuitableForRetrying(
            Collection<Class<? extends Exception>> classes)
    {
        exceptionClassesSuitableForRetrying.addAll(classes);
        return this;
    }

    /**
     * Sets an sensor of fine-grained activity. Activity on this sensor can prevent a method
     * invocation from timing out.
     */
    public MonitoringProxy<T> sensor(IActivitySensor sensor)
    {
        this.sensorOrNull = sensor;
        return this;
    }

    /**
     * Returns the actual proxy.
     */
    public T get()
    {
        return proxy;
    }

    /**
     * Cancel all currently running operations.
     */
    public void cancelCurrentOperations()
    {
        synchronized (currentThreads)
        {
            for (Thread t : currentThreads)
            {
                t.interrupt();

            }
        }
    }

}
