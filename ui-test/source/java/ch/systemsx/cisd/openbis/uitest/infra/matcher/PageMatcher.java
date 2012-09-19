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

package ch.systemsx.cisd.openbis.uitest.infra.matcher;

import java.lang.reflect.Field;
import java.util.Collection;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.Lazy;
import ch.systemsx.cisd.openbis.uitest.infra.webdriver.PageProxy;
import ch.systemsx.cisd.openbis.uitest.page.common.Page;

/**
 * @author anttil
 */
public class PageMatcher extends TypeSafeMatcher<WebDriver>
{

    private Class<? extends Page> pageClass;

    private PageProxy pageProxy;

    public PageMatcher(Class<? extends Page> pageClass, PageProxy pageProxy)
    {
        this.pageClass = pageClass;
        this.pageProxy = pageProxy;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Browser on page described by " + this.pageClass.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean matchesSafely(WebDriver ignore)
    {
        Object o = pageProxy.get(pageClass);
        while (pageClass != null)
        {
            for (Field field : pageClass.getDeclaredFields())
            {
                if ((field.getAnnotation(FindBy.class) != null)
                        && (field.getAnnotation(Lazy.class) == null))
                {
                    WebElement element;
                    try
                    {
                        field.setAccessible(true);
                        Object potentialWebElement = field.get(o);
                        if (potentialWebElement instanceof Collection)
                        {
                            continue;
                        }
                        element = (WebElement) potentialWebElement;
                    } catch (IllegalArgumentException ex)
                    {
                        ex.printStackTrace();
                        return false;
                    } catch (IllegalAccessException ex)
                    {
                        ex.printStackTrace();
                        return false;
                    }
                    if (!element.isDisplayed())
                    {
                        return false;
                    }
                }
            }

            pageClass = (Class<? extends Page>) pageClass.getSuperclass();
        }
        return true;
    }
}