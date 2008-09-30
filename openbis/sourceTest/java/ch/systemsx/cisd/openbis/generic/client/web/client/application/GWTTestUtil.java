/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.List;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Useful static methods for testing.
 *
 * @author Franz-Josef Elmer
 */
public class GWTTestUtil
{
    private GWTTestUtil()
    {
    }
    
    /**
     * Gets the {@link Button} with specified id.
     * 
     * @throws AssertionError if not found or isn't a text field.
     */
    public static Button getButtonWithID(String id)
    {
        Widget widget = tryToFindByID(id);
        Assert.assertNotNull("Button '" + id + "' not found.", widget);
        Assert.assertTrue("Widget '" + id + "' isn't a Button: " + widget.getClass(),
                widget instanceof Button);
        return (Button) widget;
    }
    
    /**
     * Gets the {@link TextField} with specified id.
     * 
     * @throws AssertionError if not found or isn't a text field.
     */
    @SuppressWarnings("unchecked")
    public static <T> TextField<T> getTextFieldWithID(String id)
    {
        Widget widget = tryToFindByID(id);
        Assert.assertNotNull("Text field '" + id + "' not found.", widget);
        Assert.assertTrue("Widget '" + id + "' isn't a TextField: " + widget.getClass(),
                widget instanceof TextField);
        return (TextField<T>) widget;
    }
    
    /**
     * Tries to find the Widget of specified type with specified id.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Widget> T tryToFindByID(Class<T> widgetClass, String id)
    {
        return (T) tryToFindByID(id);
    }
    
    /**
     * Tries to find the widget with specified id.
     * 
     * @return <code>null</code> if not found.
     */
    public static Widget tryToFindByID(String id)
    {
        return tryToFindByID(RootPanel.get(), id);
    }

    @SuppressWarnings("unchecked")
    private static Widget tryToFindByID(Widget widget, String id)
    {
        Widget result = null;
        if (id.equals(widget.getElement().getId()))
        {
            result = widget;
        } else if ((widget instanceof Component) && id.equals(((Component) widget).getId()))
        {
            result = widget;
        } else if (widget instanceof ComplexPanel)
        {
            ComplexPanel panel = (ComplexPanel) widget;
            for (int i = 0, n = panel.getWidgetCount(); i < n && result == null; i++)
            {
                result = tryToFindByID(panel.getWidget(i), id);
            }
        } else if (widget instanceof Container)
        {
            Container<Component> container = (Container<Component>) widget;
            List<Component> items = container.getItems();
            for (int i = 0, n = items.size(); i < n && result == null; i++)
            {
                result = tryToFindByID(items.get(i), id);
            }
            if (result == null && widget instanceof ContentPanel)
            {
                ContentPanel contentPanel = (ContentPanel) widget;
                List<Button> buttons = contentPanel.getButtonBar().getItems();
                for (int i = 0, n = buttons.size(); i < n && result == null; i++)
                {
                    result = tryToFindByID(buttons.get(i), id);
                }
            }
        }
        return result;
    }


}
