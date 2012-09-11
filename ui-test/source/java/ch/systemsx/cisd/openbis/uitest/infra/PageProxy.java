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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import ch.systemsx.cisd.openbis.uitest.page.Page;

/**
 * @author anttil
 */
public class PageProxy
{
    private ScreenShotter shotter;

    public <T extends SeleniumTest> PageProxy(ScreenShotter shotter)
    {
        this.shotter = shotter;
    }

    @SuppressWarnings("unchecked")
    public <T extends Page> T get(Class<T> clazz)
    {

        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);

        MethodHandler handler = new MethodHandler()
            {
                @Override
                public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
                        throws Throwable
                {
                    try
                    {
                        return proceed.invoke(self, args);
                    } catch (InvocationTargetException e)
                    {
                        if (e.getTargetException() instanceof StaleElementReferenceException)
                        {
                            PageFactory.initElements(new ScreenShotDecorator(shotter), self);
                            return proceed.invoke(self, args);
                        } else
                        {
                            throw e.getTargetException();
                        }
                    }
                }
            };

        T t;
        try
        {
            t = (T) factory.create(new Class<?>[0], new Object[0], handler);
        } catch (IllegalArgumentException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (NoSuchMethodException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (InstantiationException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (IllegalAccessException ex1)
        {
            throw new RuntimeException(ex1);
        } catch (InvocationTargetException ex1)
        {
            throw new RuntimeException(ex1);
        }

        PageFactory.initElements(new ScreenShotDecorator(shotter), t);
        t.setPageProxy(this);
        t.setScreenShotter(shotter);

        Class<T> pageClass = clazz;
        while (pageClass != null)
        {
            for (Field field : pageClass.getDeclaredFields())
            {
                if ((field.getAnnotation(FindBy.class) != null)
                        && (field.getAnnotation(NotAlwaysPresent.class) == null))
                {
                    WebElement element = null;
                    try
                    {
                        field.setAccessible(true);
                        Object potentialWebElement = field.get(t);
                        if (potentialWebElement instanceof Collection)
                        {
                            continue;
                        }
                        element = (WebElement) potentialWebElement;
                    } catch (IllegalAccessException ex)
                    {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }

                    // Force wait for the element.
                    // This makes sure that page object is returned only when all the
                    // expected elements are present.
                    element.getTagName();
                }
            }

            pageClass = (Class<T>) pageClass.getSuperclass();
        }

        return t;
    }
}
