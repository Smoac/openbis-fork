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

import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.Container;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.AdapterField;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.menu.Item;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu.ActionMenuKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.PagingToolBarAdapter;

/**
 * Useful static methods for testing.
 * 
 * @author Franz-Josef Elmer
 */
public final class GWTTestUtil
{
    private GWTTestUtil()
    {
        // Can not be instantiated.
    }

    /**
     * Clicks on the {@link ActionMenu} specified by {@link ActionMenuKind}.
     */
    public static void selectTopActionMenu(final ActionMenuKind action)
    {
        final String id = action.getMenuId();
        final Widget item = tryToFindByID(id);
        assertWidgetFound("Menu element", id, item);
        ((MenuItem) item).fireEvent(Events.Select);
    }

    /**
     * Clicks on the {@link Button} with specified id.
     * 
     * @throws AssertionError if not found, isn't a button or is not enabled.
     */
    public static void clickButtonWithID(final String id)
    {
        final Widget widget = tryToFindByID(id);
        assertWidgetFound("Button", id, widget);
        assertTrue("Widget '" + id + "' isn't a Button: " + widget.getClass(),
                widget instanceof Button);
        final Button button = (Button) widget;
        assertTrue("Button '" + id + "' is not enabled.", button.isEnabled());
        button.fireEvent(Events.Select);
    }

    /**
     * Sets the value of the {@link TextField} with specified id.
     * 
     * @param valueOrNull If <code>null</code> the text field value will not be changed.
     * @throws AssertionError if no widget found for <code>id</code> or isn't a text field.
     */
    public static void setTextFieldValue(final String id, final String valueOrNull)
    {
        if (valueOrNull != null)
        {
            getTextFieldWithID(id).setValue(valueOrNull);
        }
    }

    /**
     * Sets the value of the {@link TextField} with specified id.
     * 
     * @param valueOrNull If <code>null</code> the text field value will not be changed.
     * @throws AssertionError if no widget found for <code>id</code> or isn't a text field.
     */
    public final static void setTextAreaValue(final String id, final String valueOrNull)
    {
        if (valueOrNull != null)
        {
            getTextAreaWithId(id).setValue(valueOrNull);
        }
    }

    /**
     * Gets the {@link TextField} with specified id.
     * 
     * @throws AssertionError if not found or isn't a text field.
     */
    @SuppressWarnings("unchecked")
    public static <T> TextField<T> getTextFieldWithID(final String id)
    {
        final Widget widget = tryToFindByID(id);
        assertWidgetFound("Text field", id, widget);
        Assert.assertTrue("Widget '" + id + "' isn't a TextField: " + widget.getClass(),
                widget instanceof TextField);
        return (TextField<T>) widget;
    }

    /**
     * Gets the {@link TextArea} with specified id.
     * 
     * @throws AssertionError if not found or isn't a text field.
     */
    public final static TextArea getTextAreaWithId(final String id)
    {
        final Widget widget = tryToFindByID(id);
        assertWidgetFound("Text field", id, widget);
        Assert.assertTrue("Widget '" + id + "' isn't a TextArea: " + widget.getClass(),
                widget instanceof TextArea);
        return (TextArea) widget;
    }

    /**
     * Gets the {@link ListBox} with specified id.
     * 
     * @throws AssertionError if not found or isn't a list box.
     */
    public final static ListBox getListBoxWithID(final String id)
    {
        final Widget widget = tryToFindByID(id);
        assertWidgetFound("List box", id, widget);
        Assert.assertTrue("Widget '" + id + "' isn't a ListBox: " + widget.getClass(),
                widget instanceof ListBox);
        return (ListBox) widget;
    }

    /**
     * Gets the {@link ComboBox} with specified id.
     * 
     * @throws AssertionError if not found or isn't a list box.
     */
    @SuppressWarnings("unchecked")
    public final static ComboBox<ModelData> getComboBoxWithID(final String id)
    {
        final Widget widget = tryToFindByID(id);
        assertWidgetFound("Combo box", id, widget);
        Assert.assertTrue("Widget '" + id + "' isn't a ComboBox: " + widget.getClass(),
                widget instanceof ComboBox);
        return (ComboBox<ModelData>) widget;
    }

    /**
     * Gets the {@link TabPanel} with specified id.
     * 
     * @throws AssertionError if not found or isn't a tab panel.
     */
    public final static TabPanel getTabPanelWithID(final String id)
    {
        final Widget widget = tryToFindByID(id);
        assertWidgetFound("Tab panel", id, widget);
        Assert.assertTrue("Widget '" + id + "' isn't a TabPanel: " + widget.getClass(),
                widget instanceof TabPanel);
        return (TabPanel) widget;
    }

    /**
     * Selects {@link TabItem} with <var>tabItemId</var>.
     */
    public final static void selectTabItemWithId(final String tabPanelId, final String tabItemId)
    {
        final TabPanel tabPanel = getTabPanelWithID(tabPanelId);
        final TabItem tabItem = tabPanel.findItem(tabItemId, false);
        Assert.assertTrue("No tab item with id '" + tabItemId + "' could be found.",
                tabItem != null);
        tabPanel.setSelection(tabItem);
    }

    /**
     * Tries to find the Widget of specified type with specified id.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Widget> T tryToFindByID(final Class<T> widgetClass, final String id)
    {
        return (T) tryToFindByID(id);
    }

    /**
     * Returns the ID of the specified widget.
     * 
     * @return <code>null</code> if there is no ID.
     */
    public static String tryToGetWidgetID(final Widget widgetOrNull)
    {
        if (widgetOrNull == null)
        {
            return null;
        }
        if (widgetOrNull instanceof Component)
        {
            return ((Component) widgetOrNull).getId();
        }
        final Element element = widgetOrNull.getElement();
        if (element == null)
        {
            return null;
        }
        return element.getId();
    }

    public static Widget getWidgetWithID(final String id)
    {
        final Widget widget = tryToFindByID(id);
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
        final WidgetPicker widgetPicker = new WidgetPicker(id);
        traverseRootPanel(widgetPicker);
        return widgetPicker.tryToGetPickedWidget();
    }

    /**
     * Traverses root panel tree with the specified widget handler. Traversal is stopped when
     * {@link IWidgetHandler#handle(Widget)} returns <code>true</code>.
     */
    public static void traverseRootPanel(final IWidgetHandler<Widget> handler)
    {
        new WidgetTreeTraverser(handler).handle(RootPanel.get());
    }

    private static void assertWidgetFound(final String widgetType, final String id,
            final Widget widgetOrNull)
    {
        if (widgetOrNull == null)
        {
            final List<String> ids = findWidgetWithIDsStartingWith(GenericConstants.ID_PREFIX);
            Assert.fail(widgetType + " '" + id + "' not found on page with following IDs: " + ids);
        }
    }

    private static List<String> findWidgetWithIDsStartingWith(final String idPrefix)
    {
        final List<String> ids = new ArrayList<String>();
        traverseRootPanel(new IWidgetHandler<Widget>()
            {
                public boolean handle(final Widget widgetOrNull)
                {
                    final String widgetID = tryToGetWidgetID(widgetOrNull);
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

        WidgetTreeTraverser(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        @SuppressWarnings("unchecked")
        public final boolean handle(final Widget widgetOrNull)
        {
            Widget widget = unwrapWidget(widgetOrNull);
            if (handler.handle(widget))
            {
                return true;
            }
            if (widget instanceof ComplexPanel)
            {
                return new ComplexPanelHandler(this).handle((ComplexPanel) widget);
            } else if (widget instanceof TopMenuItem)
            {
                return new TopMenuItemHandler(this).handle((TopMenuItem) widget);
            } else if (widget instanceof MenuItem)
            {
                return new MenuItemHandler(this).handle((MenuItem) widget);
            } else if (widget instanceof Menu)
            {
                return new MenuHandler(this).handle((Menu) widget);
            } else if (widget instanceof Tree)
            {
                return new TreeHandler(this).handle((Tree) widget);
            } else if (widget instanceof Container)
            {
                return new ContainerHandler(this).handle((Container<Component>) widget);
            } else if (widget instanceof MultiField)
            {
                return new MultiFieldHandler(this).handle((MultiField<Field<?>>) widget);
            } else if (widget instanceof PagingToolBarAdapter)
            {
                return new PagingToolBarHandler(this).handle((PagingToolBarAdapter) widget);

            } else
            {
                return false;
            }
        }

        private static Widget unwrapWidget(final Widget widgetOrNull)
        {
            if (widgetOrNull == null)
            {
                return null;
            }
            if (widgetOrNull instanceof AdapterToolItem)
            {
                return ((AdapterToolItem) widgetOrNull).getWidget();
            }
            if (widgetOrNull instanceof AdapterField)
            {
                return ((AdapterField) widgetOrNull).getWidget();
            }
            return widgetOrNull;
        }
    }

    private static final class PagingToolBarHandler implements IWidgetHandler<PagingToolBarAdapter>
    {
        private final IWidgetHandler<Widget> handler;

        PagingToolBarHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final PagingToolBarAdapter pagingToolBar)
        {
            for (final ToolItem item : pagingToolBar.getItems())
            {
                if (handler.handle(item))
                {
                    return true;
                }

            }
            return false;
        }
    }

    /** Handle for handling {@link TopMenuItem} widget. */
    private static final class TopMenuItemHandler implements IWidgetHandler<TopMenuItem>
    {
        private final IWidgetHandler<Widget> handler;

        TopMenuItemHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final TopMenuItem topMenuItem)
        {
            if (handler.handle(topMenuItem.getMenu()))
            {
                return true;
            }
            return false;
        }

    }

    /** Handle for handling {@link MenuItem} widget. */
    private static final class MenuItemHandler implements IWidgetHandler<MenuItem>
    {
        private final IWidgetHandler<Widget> handler;

        MenuItemHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final MenuItem menuItem)
        {
            if (handler.handle(menuItem.getSubMenu()))
            {
                return true;
            }
            return false;
        }

    }

    /** Handle for handling {@link Menu} widget. */
    private static final class MenuHandler implements IWidgetHandler<Menu>
    {
        private final IWidgetHandler<Widget> handler;

        MenuHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final Menu menu)
        {
            if (menu != null)
            {
                for (final Item i : menu.getItems())
                {
                    if (handler.handle(i))
                    {
                        return true;
                    }
                }
            }
            return false;
        }

    }

    private static final class TreeHandler implements IWidgetHandler<Tree>
    {
        private final IWidgetHandler<Widget> handler;

        TreeHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final Tree tree)
        {
            for (final TreeItem i : tree.getAllItems())
            {
                if (handler.handle(i))
                {
                    return true;
                }

            }
            return false;
        }
    }

    private static final class ComplexPanelHandler implements IWidgetHandler<ComplexPanel>
    {
        private final IWidgetHandler<Widget> handler;

        ComplexPanelHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final ComplexPanel panel)
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

        ContainerHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public final boolean handle(final Container<Component> container)
        {
            for (Component c : container.getItems())
            {
                if (handler.handle(c))
                {
                    return true;
                }
            }

            if (container instanceof ContentPanel)
            {
                final ContentPanel contentPanel = (ContentPanel) container;
                for (Button b : contentPanel.getButtonBar().getItems())
                {
                    if (handler.handle(b))
                    {
                        return true;
                    }
                }
                if (handler.handle(contentPanel.getBottomComponent()))
                {
                    return true;
                }
                if (handler.handle(contentPanel.getTopComponent()))
                {
                    return true;
                }
            }
            return false;
        }

    }

    private static final class MultiFieldHandler implements IWidgetHandler<MultiField<Field<?>>>
    {
        private final IWidgetHandler<Widget> handler;

        MultiFieldHandler(final IWidgetHandler<Widget> handler)
        {
            this.handler = handler;
        }

        //
        // IWidgetHandler
        //

        public boolean handle(final MultiField<Field<?>> widget)
        {
            for (Field<?> f : widget.getAll())
            {
                if (handler.handle(f))
                {
                    return true;
                }
            }
            return false;
        }

    }

}
