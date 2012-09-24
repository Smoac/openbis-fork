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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WidgetContext;
import ch.systemsx.cisd.openbis.uitest.type.PropertyType;

/**
 * @author anttil
 */
public class Form implements Widget
{
    private WidgetContext context;

    public AtomicWidget getWidget(PropertyType type)
    {
        List<WebElement> elements = context.findAll(".//form/div/label");

        for (WebElement element : elements)
        {
            if (element.getText().toLowerCase().startsWith(type.getLabel().toLowerCase()))
            {

                AtomicWidget w;
                try
                {
                    w = type.getDataType().representedAs().newInstance();
                } catch (InstantiationException ex)
                {
                    throw new RuntimeException(ex);
                } catch (IllegalAccessException ex)
                {
                    throw new RuntimeException(ex);
                }

                WebElement e = element.findElement(By.xpath("../div/div"));
                if (w.getTagName() != null && !e.getTagName().equals(w.getTagName()))
                {
                    e = e.findElement(By.xpath(".//" + w.getTagName()));
                }

                w.setContext(new WidgetContext(e));
                return w;
            }
        }
        throw new IllegalArgumentException("Could not find " + type.getLabel());
    }

    public List<String> getLabels()
    {
        List<String> labels = new ArrayList<String>();
        List<WebElement> elements = context.findAll(".//form/div/label");
        for (WebElement element : elements)
        {
            labels.add(element.getText());
        }
        return labels;
    }

    @Override
    public void setContext(WidgetContext context)
    {
        this.context = context;
    }
}
