/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.base;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

import org.hamcrest.Matcher;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

/**
 * @author anttil
 */
public class RefreshingMatcherProxy implements InvocationHandler
{

    @SuppressWarnings("unchecked")
    static <T extends Matcher<?>> T newInstance(T actualMatcher, BaseTest b)
    {

        Collection<Class<?>> interfaces = new HashSet<Class<?>>();
        interfaces.add(Matcher.class);
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                actualMatcher.getClass().getClassLoader(),
                interfaces.toArray(new Class<?>[0]),
                new RefreshingMatcherProxy(actualMatcher, b));
    }

    private Matcher<?> matcher;

    private BaseTest b;

    public RefreshingMatcherProxy(Matcher<?> matcher, BaseTest b)
    {
        this.matcher = matcher;
        this.b = b;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if (method.getName().equals("matches") && args.length == 1)
        {
            Object arg = args[0];
            if (arg instanceof ExternalData)
            {
                args[0] = b.refresh((ExternalData) arg);
            } else if (arg instanceof Sample)
            {
                args[0] = b.refresh((Sample) arg);
            } else if (arg instanceof Experiment)
            {
                args[0] = b.refresh((Experiment) arg);
            } else if (arg instanceof Project)
            {
                args[0] = b.refresh((Project) arg);
            } else
            {
                throw new IllegalArgumentException("Illegal matching: " + arg);
            }
        }

        return method.invoke(this.matcher, args);
    }
}
