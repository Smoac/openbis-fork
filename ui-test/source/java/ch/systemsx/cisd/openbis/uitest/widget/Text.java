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

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * @author anttil
 */
public class Text extends Widget implements Fillable
{
    public void write(String text)
    {
        WebElement element = getInputElement();
        element.clear();
        element.sendKeys(text);
    }

    public void clear()
    {
        getInputElement().clear();
    }

    public void append(String text)
    {
        getInputElement().sendKeys(text);
    }

    private WebElement getInputElement()
    {
        if (context.getTagName().equals("input"))
        {
            return context;
        } else
        {
            return context.findElement(By.xpath("input"));
        }
    }

    @Override
    public void fillWith(String string)
    {
        write(string);
    }

}
