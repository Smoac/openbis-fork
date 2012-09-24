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
public class Dynamic implements Widget
{

    private WidgetContext context;

    public Widget define(Class<? extends Widget> clazz)
    {
        Widget widget;
        try
        {
            widget = clazz.newInstance();
        } catch (InstantiationException ex)
        {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }

        if (widget instanceof AtomicWidget)
        {
            AtomicWidget w = (AtomicWidget) widget;
            if (!w.getTagName().equals(context.getTagName()))
            {
                widget.setContext(new WidgetContext(context.find(".//" + w.getTagName())));
                return widget;
            }
        }
        widget.setContext(this.context);
        return widget;
    }

    @Override
    public void setContext(WidgetContext context)
    {
        this.context = context;
    }

}
