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

package ch.systemsx.cisd.openbis.uitest.webdriver;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.dsl.SeleniumTest;

public class LazyLoader implements InvocationHandler
{

    private String id;

    private String tag;

    private WebElement element;

    public static Object newInstance(String id, String tag)
    {
        return java.lang.reflect.Proxy.newProxyInstance(
                WebElement.class.getClassLoader(),
                new Class<?>[]
                    { WebElement.class },
                new LazyLoader(id, tag));
    }

    private LazyLoader(String id, String tag)
    {
        this.id = id;
        this.tag = tag;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
    {
        try
        {
            if (element == null)
            {
                WebElement e = SeleniumTest.driver.findElement(By.id(id));
                if (tag != null && !tag.equals(e.getTagName()))
                {
                    e = e.findElement(By.xpath(".//" + tag));
                }

                element = e;
            }
            return m.invoke(element, args);
        } catch (InvocationTargetException e)
        {
            throw e.getTargetException();
        } catch (Exception e)
        {
            throw new RuntimeException("unexpected invocation exception: " +
                    e.getMessage());
        }
    }
}
