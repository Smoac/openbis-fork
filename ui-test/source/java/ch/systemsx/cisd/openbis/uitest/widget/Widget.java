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

package ch.systemsx.cisd.openbis.uitest.widget;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author anttil
 */
public abstract class Widget
{
    protected WebElement context;

    public void setContext(WebElement context)
    {
        this.context = context;
    }

    public WebElement getContext()
    {
        return context;
    }

    protected Widget find(String xpath)
    {
        Widget w = new Widget()
            {
            };
        w.setContext(context.findElement(By.xpath(xpath)));
        return w;
    }

    protected List<WebElement> findAll(String xpath)
    {
        return context.findElements(By.xpath(xpath));
    }

    public <T extends Widget> T handleAs(Class<T> clazz)
    {
        T t;
        try
        {
            t = clazz.newInstance();
        } catch (InstantiationException ex)
        {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
        t.setContext(context);
        return t;
    }
}
