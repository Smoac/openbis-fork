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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.dsl.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.infra.screenshot.ScreenShotter;
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
        try
        {
            T t = clazz.newInstance();

            Class<T> pageClass = clazz;
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

                pageClass = (Class<T>) pageClass.getSuperclass();
            }

            return t;
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        }
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
