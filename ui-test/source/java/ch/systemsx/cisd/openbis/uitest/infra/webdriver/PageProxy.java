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

package ch.systemsx.cisd.openbis.uitest.infra.webdriver;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.screenshot.ScreenShotProxy;
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.suite.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.widget.AtomicWidget;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

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
    public <T> T get(final Class<T> clazz)
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
                            initLocateFields(clazz, (T) self);
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

        initLocateFields(clazz, t);

        return t;
    }

    @SuppressWarnings("unchecked")
    private <T> void initLocateFields(Class<T> clazz, T t)
    {
        Class<T> pageClass = clazz;
        while (pageClass != null)
        {
            for (Field field : pageClass.getDeclaredFields())
            {
                Locate locate = field.getAnnotation(Locate.class);
                if (locate != null)
                {
                    try
                    {
                        field.setAccessible(true);
                        Widget widget = (Widget) field.getType().newInstance();

                        String tagName = null;
                        if (widget instanceof AtomicWidget)
                        {
                            tagName = ((AtomicWidget) widget).getTagName();
                        }

                        WebElement element;
                        if (field.getAnnotation(Lazy.class) != null)
                        {
                            element =
                                    (WebElement) WebElementProxy.newInstance(locate.value(),
                                            tagName);
                        } else
                        {
                            element = SeleniumTest.driver.findElement(By.id(locate.value()));
                            if (tagName != null && !element.getTagName().equals(tagName))
                            {
                                element = element.findElement(By.xpath(".//" + tagName));
                            }
                        }

                        widget.setContext(new WidgetContext((WebElement) ScreenShotProxy
                                .newInstance(element, shotter)));
                        field.set(t, widget);
                    } catch (IllegalArgumentException ex)
                    {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                        throw ex;
                    } catch (IllegalAccessException ex)
                    {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    } catch (InstantiationException ex)
                    {
                        // TODO Auto-generated catch block
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            }

            pageClass = (Class<T>) pageClass.getSuperclass();
        }
    }
}
