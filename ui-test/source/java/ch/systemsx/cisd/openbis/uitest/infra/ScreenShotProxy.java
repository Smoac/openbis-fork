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

package ch.systemsx.cisd.openbis.uitest.infra;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;

public class ScreenShotProxy implements InvocationHandler
{

    private Object obj;

    private ScreenShotter shotter;

    public static Object newInstance(Object obj, ScreenShotter shotter)
    {
        Collection<Class<?>> interfaces = new HashSet<Class<?>>();

        Class<?> current = obj.getClass();
        while (current != null)
        {
            for (Class<?> c : current.getInterfaces())
            {
                interfaces.add(c);
            }
            current = current.getSuperclass();
        }

        return java.lang.reflect.Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                interfaces.toArray(new Class<?>[0]),
                new ScreenShotProxy(obj, shotter));
    }

    private ScreenShotProxy(Object obj, ScreenShotter shotter)
    {
        this.obj = obj;
        this.shotter = shotter;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        Object result;
        try
        {
            if (m.getName().equals("click") || m.getName().equals("sendKeys"))
            {
                shotter.screenshot();
            }
            result = m.invoke(this.obj, args);
        } catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        } catch (Exception e)
        {
            throw new RuntimeException("unexpected invocation exception: " +
                    e.getMessage());
        }
        return result;
    }
}
