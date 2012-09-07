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

package ch.systemsx.cisd.openbis.uitest.page;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import ch.systemsx.cisd.openbis.uitest.infra.PageProxy;
import ch.systemsx.cisd.openbis.uitest.infra.ScreenShotProxy;
import ch.systemsx.cisd.openbis.uitest.infra.ScreenShotter;
import ch.systemsx.cisd.openbis.uitest.infra.SeleniumTest;

public abstract class Page
{

    private PageProxy pageProxy;

    private ScreenShotter shotter;

    public void setPageProxy(PageProxy proxy)
    {
        this.pageProxy = proxy;
    }

    public void setScreenShotter(ScreenShotter shotter)
    {
        this.shotter = shotter;
    }

    public <T extends Page> T get(Class<T> clazz)
    {
        return this.pageProxy.get(clazz);
    }

    public WebElement findElementWithText(String text, By by)
    {
        WebElement element = null;
        for (WebElement e : SeleniumTest.driver.findElements(by))
        {
            if (e.getText().equals(text))
            {
                element = e;
                break;
            }
        }
        assertThat(element, is(notNullValue()));

        return (WebElement) ScreenShotProxy.newInstance(element, shotter);
    }

    public WebElement findElement(WebElement element, String xpath)
    {
        return (WebElement) ScreenShotProxy.newInstance(element.findElement(By.xpath(xpath)),
                shotter);
    }

    public Collection<WebElement> findElements(WebElement element, String xpath)
    {
        Collection<WebElement> elements = element.findElements(By.xpath(xpath));
        List<WebElement> wrapped = new ArrayList<WebElement>();
        for (WebElement e : elements)
        {
            wrapped.add((WebElement) ScreenShotProxy.newInstance(e, shotter));
        }
        return wrapped;
    }

    public void wait(final By by)
    {
        ExpectedCondition<?> condition = new ExpectedCondition<WebElement>()
            {
                @Override
                public WebElement apply(WebDriver d)
                {
                    return d.findElement(by);
                }
            };

        new WebDriverWait(SeleniumTest.driver, 10).until(condition);
    }

    protected void select(Collection<? extends WebElement> choices, String text)
    {
        Collection<String> found = new HashSet<String>();
        for (WebElement choice : choices)
        {
            if (choice.getText().equalsIgnoreCase(text))
            {
                choice.click();
                return;
            }
            found.add(choice.getText());
        }
        throw new IllegalArgumentException("Selection " + text + " not found");
    }

    protected void checkbox(WebElement box, boolean check)
    {
        if (box.getAttribute("checked") != null ^ check)
        {
            box.click();
        }
    }
}
