/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.utils;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomasz Pylak
 */
public class GuiUtils
{
    public static void replaceLastItem(LayoutContainer container, Widget newLastWidget)
    {
        int lastItemIx = container.getItemCount() - 1;
        if (lastItemIx >= 0)
        {
            container.remove(container.getWidget(lastItemIx));
            container.insert(newLastWidget, lastItemIx);
        }
        container.insert(newLastWidget, 0);
        container.layout();
    }

    public static Widget withLabel(Widget component, String label)
    {
        return withLabel(component, label, 0);
    }

    public static Widget withLabel(Widget component, String label, int margin)
    {
        LayoutContainer c = new LayoutContainer();
        c.setLayout(new TableLayout(2));
        TableData cellLayout = new TableData();
        cellLayout.setMargin(margin);
        Text labelWidget = new Text(label);
        labelWidget.setWidth(Math.max(label.length() * 9, 80));
        c.add(labelWidget, cellLayout);
        c.add(component);
        return c;
    }
}
