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
public class TextArea implements AtomicWidget, Fillable
{
    private WidgetContext context;

    public void write(String text)
    {
        context.clear();
        context.sendKeys(text);
    }

    public void clear()
    {
        context.clear();
    }

    public void append(String text)
    {
        context.sendKeys(text);
    }

    @Override
    public void fillWith(String string)
    {
        write(string);
    }

    @Override
    public void setContext(WidgetContext context)
    {
        this.context = context;
    }

    @Override
    public String getTagName()
    {
        return "textarea";
    }
}
