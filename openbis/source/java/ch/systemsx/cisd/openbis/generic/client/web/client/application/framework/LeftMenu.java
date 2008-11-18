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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.List;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TreeEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.tree.Tree;
import com.extjs.gxt.ui.client.widget.tree.TreeItem;
import com.google.gwt.user.client.Event;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.MenuCategory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.MenuElement;

/**
 * User menu allowing to get to wanted functionality of application (e.g. sample listing).
 * 
 * @author Izabela Adamczyk
 */
public class LeftMenu extends ContentPanel
{
    public static final String ID = GenericConstants.ID_PREFIX + "left-menu";

    public static final String TREE_SUFFIX = "_tree";

    private final List<MenuCategory> categories;

    LeftMenu(final List<MenuCategory> categories)
    {
        this.categories = categories;
        setId(ID);
        setBodyBorder(true);
        setLayoutOnChange(true);
        setCollapsible(true);
        setHeaderVisible(false);
        setLayout(new AccordionLayout());
        addCategories();
    }

    final void addCategories()
    {
        for (final MenuCategory menuCategory : categories)
        {
            final String categoryId = ID + "_" + menuCategory.getPartOfId();
            final SubMenu subMenu = new SubMenu(menuCategory.getName(), categoryId);
            for (final MenuElement me : menuCategory.getElements())
            {
                subMenu.addCommand(categoryId + "_" + me.getPartOfId(), me.getTitle(), me
                        .getTabItem());
            }
            add(subMenu);
        }
    }

    //
    // Helper classes
    //

    private final static class SubMenu extends ContentPanel
    {
        public static final String TAB_ITEM_KEY = "tabItemKey";

        private final Tree tree;

        SubMenu(final String title, final String id)
        {
            setHeading(title);
            setId(id);
            tree = new Tree();
            tree.setId(id + TREE_SUFFIX);
            tree.addListener(Event.ONCLICK, new Listener<TreeEvent>()
                {

                    //
                    // Listener
                    //

                    public final void handleEvent(final TreeEvent be)
                    {
                        if (tree.getSelectedItem().isLeaf())
                        {
                            final AppEvent<ITabItem> event =
                                    new AppEvent<ITabItem>(AppEvents.NAVI_EVENT);
                            event.data = tree.getSelectedItem().getData(TAB_ITEM_KEY);
                            Dispatcher.get().dispatch(event);
                        } else
                        {
                            tree.getSelectedItem().setExpanded(true);
                        }
                    }
                });
            tree.setAnimate(false);
            setBodyStyleName("pad-text");
            tree.getStyle().setNodeCloseIconStyle("");
            tree.getStyle().setNodeOpenIconStyle("");
            add(tree);
        }

        private final void addCommand(final String id, final String name, final ITabItem tabItem)
        {
            final TreeItem item = new TreeItem(name);
            item.setId(id);
            item.setData(TAB_ITEM_KEY, tabItem);
            tree.getRootItem().add(item);
        }

    }

}