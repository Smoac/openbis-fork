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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;

/**
 * Main panel - where the tabs will open.
 * 
 * @author Izabela Adamczyk
 */
class MainTabPanel extends TabPanel
{
    private static final String PREFIX = "main-tab-panel_";

    private final GenericViewContext viewContext;

    MainTabPanel(final GenericViewContext viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setTabScroll(true);
        add(createWelcomePanel());
    }

    private final MainTabItem createWelcomePanel()
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
        layoutContainer.setId(GenericConstants.ID_PREFIX + PREFIX + "welcome");
        layoutContainer.addText(createWelcomeText());
        final MainTabItem intro = new MainTabItem(new DefaultTabItem("&nbsp;", layoutContainer));
        intro.setClosable(false);
        return intro;
    }

    private final String createWelcomeText()
    {
        final Element div = DOM.createDiv();
        div.setClassName("intro-tab");
        div.setInnerText(viewContext.getMessage("welcome"));
        return div.getString();
    }

    private final MainTabItem tryGetTab(final ITabItem tabItem)
    {
        for (final TabItem tab : getItems())
        {
            if (tab instanceof MainTabItem)
            {
                final MainTabItem mainTabItem = (MainTabItem) tab;
                if (mainTabItem.getTabItem().getId().equals(tabItem.getId()))
                {
                    return mainTabItem;
                }
            }
        }
        return null;
    }

    public final void openTab(final ITabItem tabItem)
    {
        final MainTabItem tab = tryGetTab(tabItem);
        if (tab != null)
        {
            setSelection(tab);
        } else
        {
            final MainTabItem newTab = new MainTabItem(tabItem);
            add(newTab);
            setSelection(newTab);
            tabItem.afterAddTabItem();
        }
    }

    //
    // Helper classes
    //

    private final static class MainTabItem extends TabItem
    {
        private final ITabItem tabItem;

        public MainTabItem(final ITabItem tabItem)
        {
            this.tabItem = tabItem;
            setClosable(true);
            setLayout(new FitLayout());
            setText(tabItem.getTitle());
            addStyleName("pad-text");
            add(tabItem.getComponent());
        }

        final ITabItem getTabItem()
        {
            return tabItem;
        }
    }
}