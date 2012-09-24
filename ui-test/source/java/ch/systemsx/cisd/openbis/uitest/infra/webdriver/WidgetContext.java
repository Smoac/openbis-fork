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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import ch.systemsx.cisd.openbis.uitest.suite.SeleniumTest;
import ch.systemsx.cisd.openbis.uitest.widget.AtomicWidget;

/**
 * @author anttil
 */
public class WidgetContext
{

    public WebElement element;

    public WidgetContext(WebElement element)
    {
        this.element = element;
    }

    public void click()
    {
        element.click();
    }

    public String getAttribute(String key)
    {
        return element.getAttribute(key);
    }

    public String getTagName()
    {
        return element.getTagName();
    }

    public void sendKeys(String keys)
    {
        element.sendKeys(keys);
    }

    public void clear()
    {
        element.clear();
    }

    public WebElement find(String xpath)
    {
        return element.findElement(By.xpath(xpath));
    }

    public <T extends AtomicWidget> T find(String xpath, Class<T> widgetClass)
    {
        T t;
        try
        {
            t = widgetClass.newInstance();
        } catch (InstantiationException ex)
        {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }

        WebElement e = element.findElement(By.xpath(xpath));
        if (!e.getTagName().equals(t.getTagName()))
        {
            e = e.findElement(By.xpath(".//" + t.getTagName()));
        }
        t.setContext(new WidgetContext(e));
        return t;
    }

    public List<WebElement> findAll(String xpath)
    {
        return element.findElements(By.xpath(xpath));
    }

    public void mouseOver()
    {
        Actions builder = new Actions(SeleniumTest.driver);
        builder.moveToElement(element).build().perform();
    }

}
