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

import ch.systemsx.cisd.openbis.uitest.infra.webdriver.WidgetContext;

/**
 * @author anttil
 */
public class PagingToolBar implements Widget, Refreshable
{

    private WidgetContext context;

    public void filters()
    {
        Button b = context.find(".//button[text()='Filters']", Button.class);
        if (!b.isPressed())
        {
            b.click();
        }
    }

    public void settings()
    {
        Button b = context.find(".//button[text()='Settings']", Button.class);
        b.click();
    }

    @Override
    public void setContext(WidgetContext context)
    {
        this.context = context;
    }

    String displayText;

    @Override
    public void prepareWait()
    {
        displayText = context.find(".//div[contains(@class, 'my-paging-display')]").getText();
    }

    @Override
    public boolean hasRefreshed()
    {
        String currentText =
                context.find(".//div[contains(@class, 'my-paging-display')]").getText();
        System.out.println("comparing " + displayText + " with " + currentText);

        if (currentText.contains("Loading"))
        {
            displayText = currentText;
            return false;
        }

        boolean result = (this.displayText.equals(currentText) == false);
        if (result)
        {
            System.out.println("--- polling ends --");
        }
        return result;
    }

    public boolean isEnabled()
    {
        return !context.getAttribute("class").contains("x-item-disabled");
    }
}
