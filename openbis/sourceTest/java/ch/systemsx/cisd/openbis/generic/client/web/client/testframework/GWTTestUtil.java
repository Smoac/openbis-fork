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

package ch.systemsx.cisd.openbis.generic.client.web.client.testframework;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;

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
     * Clicks on the {@link Button} with specified id.
     * 
     * @throws AssertionError if not found or isn't a button.
     */
    public static void clickButtonWithID(String id)
    {
        Widget widget = tryToFindByID(id);
        assertWidgetFound("Button", id, widget);
        Assert.assertTrue("Widget '" + id + "' isn't a Button: " + widget.getClass(),
                widget instanceof Button);
        ((Button) widget).fireEvent(Events.Select);
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
        assertWidgetFound("Text field", id, widget);
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
     * Returns the ID of the specified widget.
     * 
     * @return <code>null</code> if there is no ID.
     */
    public static String tryToGetWidgetID(Widget widgetOrNull)
    {
        if (widgetOrNull == null)
        {
            return null;
        }
        if (widgetOrNull instanceof Component)
        {
            return ((Component) widgetOrNull).getId();
        }
        Element element = widgetOrNull.getElement();
        if (element == null)
        {
            return null;
        }
        return element.getId();
    }
    
    public static Widget getWidgetWithID(String id)
    {
        Widget widget = tryToFindByID(id);
        assertWidgetFound("Widget", id, widget);
        return widget;
    }

    /**
     * Tries to find the widget with specified id.
     * 
     * @return <code>null</code> if not found.
     */
    public static Widget tryToFindByID(final String id)
    {
        WidgetPicker widgetPicker = new WidgetPicker(id);
        traverseRootPanel(widgetPicker);
        return widgetPicker.tryToGetPickedWidget();
    }

    /**
     * Traverses root panel tree with the specified widget handler. Traversal is stopped
     * when {@link IWidgetHandler#handle(Widget)} returns <code>true</code>.
     */
    public static void traverseRootPanel(IWidgetHandler<Widget> handler)
    {
        new WidgetTreeTraverser(handler).handle(RootPanel.get());
    }

    private static void assertWidgetFound(String widgetType, String id, Widget widgetOrNull)
    {
        if (widgetOrNull == null)
        {
            List<String> ids = findWidgetWithIDsStartingWith(GenericConstants.ID_PREFIX);
            Assert.fail(widgetType + " '" + id + "' not found on page with following IDs: " + ids);
        }
    }
    
    private static List<String> findWidgetWithIDsStartingWith(final String idPrefix)
    {
        final List<String> ids = new ArrayList<String>();
        traverseRootPanel(new IWidgetHandler<Widget>()
            {
                public boolean handle(Widget widgetOrNull)
                {
                    String widgetID = tryToGetWidgetID(widgetOrNull);
                    if (widgetID != null && widgetID.startsWith(idPrefix))
                    {
                        ids.add(widgetID);
                    }
                    return false;
                }
            });
        return ids;
    }
    
    private static final class WidgetTreeTraverser implements IWidgetHandler<Widget>
    {
        private final IWidgetHandler<Widget> handler;

        WidgetTreeTraverser(IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }
        
        @SuppressWarnings("unchecked")
        public boolean handle(Widget widgetOrNull)
        {
            if (widgetOrNull instanceof ComplexPanel)
            {
                return new ComplexPanelHandler(this).handle((ComplexPanel) widgetOrNull);
            } else if (widgetOrNull instanceof Container)
            {
                return new ContainerHandler(this).handle((Container<Component>) widgetOrNull);
            } else if (widgetOrNull instanceof AdapterToolItem) 
            {
                return handler.handle(((AdapterToolItem) widgetOrNull).getWidget());
            } else 
            {
                return handler.handle(widgetOrNull);
            }
        }
        
    }
    
    private static final class ComplexPanelHandler implements IWidgetHandler<ComplexPanel>
    {
        private final IWidgetHandler<Widget> handler;

        ComplexPanelHandler(IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        public boolean handle(ComplexPanel panel)
        {
            for (int i = 0, n = panel.getWidgetCount(); i < n; i++)
            {
                if (handler.handle(panel.getWidget(i)))
                {
                    return true;
                }
            }
            return false;
        }
    }
    
    private static final class ContainerHandler implements IWidgetHandler<Container<Component>>
    {
        private final IWidgetHandler<Widget> handler;

        ContainerHandler(IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        public boolean handle(Container<Component> container)
        {
            List<Component> items = container.getItems();
            for (int i = 0, n = items.size(); i < n; i++)
            {
                if (handler.handle(items.get(i)))
                {
                    return true;
                }
            }
            if (container instanceof ContentPanel)
            {
                ContentPanel contentPanel = (ContentPanel) container;
                List<Button> buttons = contentPanel.getButtonBar().getItems();
                for (int i = 0, n = buttons.size(); i < n; i++)
                {
                    if (handler.handle(buttons.get(i)))
                    {
                        return true;
                    }
                }
                if (handler.handle(contentPanel.getBottomComponent()))
                {
                    return true;
                }
            }
            return false;
        }
        
    }
    
}
