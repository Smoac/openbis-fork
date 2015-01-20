/*
 * Copyright 2002-2005 the original author or authors.
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

package com.marathon.util.spring;

import java.io.Serializable;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

import org.springframework.remoting.support.RemoteInvocation;

/**
 * Provides the ability for subclasses to decorate a source <code>RemoteInvocation</code>. By
 * default, all methods simply invoke their counterpart on the delegate
 * <code>RemoteInvocation</code>.
 * 
 * @author Andy DePue
 * @since 1.2.3
 * @see RemoteInvocation
 */
public abstract class RemoteInvocationDecorator extends RemoteInvocation
{
    private static final long serialVersionUID = 1L;
    
    private RemoteInvocation delegate;

    public RemoteInvocationDecorator()
    {
        super();
    }

    public RemoteInvocationDecorator(final RemoteInvocation source)
    {
        super();
        this.delegate = source;
    }

    //
    // METHODS FROM CLASS RemoteInvocation
    //

    @Override
    public void setMethodName(String methodName)
    {
        getDelegate().setMethodName(methodName);
    }

    @Override
    public String getMethodName()
    {
        return getDelegate().getMethodName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setParameterTypes(Class[] parameterTypes)
    {
        getDelegate().setParameterTypes(parameterTypes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class[] getParameterTypes()
    {
        return getDelegate().getParameterTypes();
    }

    @Override
    public void setArguments(Object[] arguments)
    {
        getDelegate().setArguments(arguments);
    }

    @Override
    public Object[] getArguments()
    {
        return getDelegate().getArguments();
    }

    @Override
    public void addAttribute(String key, Serializable value)
    {
        getDelegate().addAttribute(key, value);
    }

    @Override
    public Serializable getAttribute(String key)
    {
        return getDelegate().getAttribute(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setAttributes(Map attributes)
    {
        getDelegate().setAttributes(attributes);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getAttributes()
    {
        return getDelegate().getAttributes();
    }

    @Override
    public Object invoke(Object targetObject) throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException
    {
        return getDelegate().invoke(targetObject);
    }

    //
    // SIMPLE PROPERTY ACCESSORS
    //

    public RemoteInvocation getDelegate()
    {
        return this.delegate;
    }

    public void setDelegate(final RemoteInvocation delegate)
    {
        this.delegate = delegate;
    }

    @Override
    public String toString()
    {
        return "RemoteInvocationDecorator{" + "delegate=" + this.delegate + "}";
    }
}
