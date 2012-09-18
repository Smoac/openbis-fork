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

/**
 * @author anttil
 */
public class Form extends Widget
{
    public Widget getWidget(String label)
    {
        List<WebElement> elements = context.findElements(By.xpath(".//form/div/label"));

        for (WebElement element : elements)
        {
            if (element.getText().toLowerCase().startsWith(label.toLowerCase()))
            {
                Widget w = new Widget()
                    {
                    };
                w.setContext(element.findElement(By.xpath("../div/div")));
                return w;
            }
        }
        throw new IllegalArgumentException("Could not find " + label);
    }

    public List<String> getLabels()
    {
        List<String> labels = new ArrayList<String>();
        List<WebElement> elements = context.findElements(By.xpath(".//form/div/label"));
        for (WebElement element : elements)
        {
            labels.add(element.getText());
        }
        return labels;
    }
}
