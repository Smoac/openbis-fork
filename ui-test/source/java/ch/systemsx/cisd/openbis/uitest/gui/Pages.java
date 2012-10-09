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

package ch.systemsx.cisd.openbis.uitest.gui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.layout.Location;
import ch.systemsx.cisd.openbis.uitest.menu.TabBar;
import ch.systemsx.cisd.openbis.uitest.screenshot.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.webdriver.Context;
import ch.systemsx.cisd.openbis.uitest.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.webdriver.LazyLoader;
import ch.systemsx.cisd.openbis.uitest.webdriver.Locate;
import ch.systemsx.cisd.openbis.uitest.webdriver.WidgetContext;
import ch.systemsx.cisd.openbis.uitest.widget.AtomicWidget;
import ch.systemsx.cisd.openbis.uitest.widget.Widget;

/**
 * @author anttil
 */
public class Pages
{
    private ScreenShotter shotter;

    public <T extends SeleniumTest> Pages(ScreenShotter shotter)
    {
        this.shotter = shotter;
    }

    @SuppressWarnings("unchecked")
    public <T> T load(final Class<T> clazz)
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
                            System.out.println("STALE REFERENCE - RELOADING "
                                    + self.getClass().getSimpleName());
                            T t = load(clazz);
                            return thisMethod.invoke(t, args);
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

        return init(clazz, t);
    }

    private <T> T init(final Class<T> clazz, T t)
    {
        try
        {
            Class<?> pageClass = clazz;
            while (pageClass != null)
            {
                for (Field field : pageClass.getDeclaredFields())
                {
                    Locate locate = field.getAnnotation(Locate.class);
                    if (locate != null)
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
                                    (WebElement) LazyLoader.newInstance(locate.value(),
                                            tagName);
                        } else
                        {
                            element = SeleniumTest.driver.findElement(By.id(locate.value()));
                            if (tagName != null && !element.getTagName().equals(tagName))
                            {
                                element = element.findElement(By.xpath(".//" + tagName));
                            }
                        }

                        widget.setContext(new WidgetContext(element, shotter));
                        field.set(t, widget);
                    }

                    Context context = field.getAnnotation(Context.class);
                    if (context != null)
                    {
                        field.setAccessible(true);
                        try
                        {
                            field.set(t, new WidgetContext(SeleniumTest.driver.findElement(By
                                    .xpath("/html")),
                                    shotter));
                        } catch (IllegalArgumentException ex)
                        {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        } catch (IllegalAccessException ex)
                        {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                    }
                }

                pageClass = pageClass.getSuperclass();
            }

            return t;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> T tryLoad(Class<T> clazz)
    {
        return tryLoad(clazz, SeleniumTest.IMPLICIT_WAIT, TimeUnit.SECONDS);
    }

    public <T> T tryLoad(Class<T> clazz, long timeout, TimeUnit unit)
    {
        SeleniumTest.setImplicitWait(timeout, unit);
        try
        {
            return load(clazz);
        } catch (NoSuchElementException e)
        {
            e.printStackTrace();
            return null;
        } finally
        {
            SeleniumTest.setImplicitWaitToDefault();
        }

    }

    public <T> T goTo(Location<T> location)
    {
        if (load(TabBar.class).selectTab(location.getTabName()) == false)
        {
            location.moveTo(this);
        }
        return load(location.getPage());
    }

    public void setScreenShotter(ScreenShotter shotter)
    {
        this.shotter = shotter;
    }

    public void screenshot()
    {
        shotter.screenshot();
    }
}
