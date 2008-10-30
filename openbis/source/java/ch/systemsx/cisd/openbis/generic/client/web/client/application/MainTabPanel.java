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

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

class MainTabPanel extends TabPanel
{

    public MainTabPanel()
    {
        setLayout(new FitLayout());
        setTabScroll(true);
        add(createIntro());
    }

    // TODO 2008-10-29, IA: Find out how to make tab panel calculate correctly the size of first
    // child
    private MainTabItem createIntro()
    {
        final ContentPanel cp = new ContentPanel();
        cp.setHeading("&nbsp;");
        cp.setLayout(new CenterLayout());
        cp.setHeaderVisible(false);
        cp.addText("<div class='intro-tab'>Welcome to OpenBIS</div>");
        final MainTabItem intro = new MainTabItem(cp);
        intro.setClosable(false);
        return intro;
    }

    private MainTabItem tryGetTab(ContentPanel c)
    {
        for (TabItem tab : getItems())
        {
            if (tab instanceof MainTabItem)
            {
                MainTabItem t = (MainTabItem) tab;
                if (t.getComponent().getId().equals(c.getId()))
                {
                    return t;
                }
            }
        }
        return null;
    }

    public void openTab(final ContentPanel lc)
    {
        final MainTabItem tab = tryGetTab(lc);
        if (tab != null)
        {
            setSelection(tab);
        } else
        {
            final MainTabItem newTab = new MainTabItem(lc);
            add(newTab);
            setSelection(newTab);
        }
    }

    class MainTabItem extends TabItem
    {
        private final ContentPanel component;

        public MainTabItem(final ContentPanel component)
        {
            this.component = component;
            setClosable(true);
            setLayout(new FitLayout());
            setText(component.getHeader() != null ? component.getHeader().getText() : component
                    .getId());
            addStyleName("pad-text");
            add(component);
        }

        LayoutContainer getComponent()
        {
            return component;
        }
    }
}