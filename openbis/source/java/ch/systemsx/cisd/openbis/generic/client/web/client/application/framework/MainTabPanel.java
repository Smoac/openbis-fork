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

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.ConfirmationDialog;

/**
 * Main panel - where the tabs will open.
 * 
 * @author Izabela Adamczyk
 */
public class MainTabPanel extends TabPanel
{
    private static final String PREFIX = GenericConstants.ID_PREFIX + "main-tab-panel_";

    public static final String TAB_SUFFIX = "_tab";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public static final String ID = PREFIX.substring(0, PREFIX.length() - 1);

    private Map<String/* tab id */, MainTabItem> openTabs = new HashMap<String, MainTabItem>();

    MainTabPanel(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setTabScroll(true);
        add(createWelcomePanel());
        setId(ID);
    }

    private final MainTabItem createWelcomePanel()
    {
        final LayoutContainer layoutContainer = new LayoutContainer(new CenterLayout());
        String layoutContainerId = PREFIX + "welcome";
        layoutContainer.setId(layoutContainerId);
        layoutContainer.addText(createWelcomeText());
        final MainTabItem intro =
                new MainTabItem(DefaultTabItem.createUnaware("&nbsp;", layoutContainer, false),
                        layoutContainerId);
        intro.setClosable(false);
        return intro;
    }

    private final String createWelcomeText()
    {
        final Element div = DOM.createDiv();
        div.setClassName("intro-tab");
        div.setInnerText(viewContext.getMessage(Dict.WELCOME));
        return div.getString();
    }

    private final MainTabItem tryFindTab(final ITabItemFactory tabItemFactory)
    {
        return openTabs.get(tabItemFactory.getId());
    }

    /**
     * Set the currently selected tab to the given <i>tabItem</i>.
     * <p>
     * If the tab could not be found (meaning that it has not been created yet), then a new tab will
     * be generated out of given {@link ITabItem}.
     * </p>
     */
    public final void openTab(final ITabItemFactory tabItemFactory)
    {
        final MainTabItem tab = tryFindTab(tabItemFactory);
        if (tab != null)
        {
            setSelection(tab);
        } else
        {
            String tabId = tabItemFactory.getId();
            // Note that if not set, is then automatically generated. So this is why we test for
            // 'ID_PREFIX'. We want the user to set an unique id.
            assert tabId.startsWith(GenericConstants.ID_PREFIX) : "Unspecified component id.";
            final MainTabItem newTab = new MainTabItem(tabItemFactory.create(), tabId);
            add(newTab);
            openTabs.put(tabId, newTab);
            setSelection(newTab);
        }
    }

    //
    // Helper classes
    //

    private final class MainTabItem extends TabItem
    {
        public MainTabItem(final ITabItem tabItem, final String idPrefix)
        {
            setId(idPrefix + TAB_SUFFIX);
            setClosable(true);
            setLayout(new FitLayout());
            tabItem.getTabTitleUpdater().bind(this);
            addStyleName("pad-text");
            add(tabItem.getComponent());
            if (tabItem.isCloseConfirmationNeeded())
            {
                addListener(Events.BeforeClose, createBeforeCloseListener(this, idPrefix));
            }
            addListener(Events.Close, createCloseTabListener(tabItem, idPrefix));
            addListener(Events.Select, createActivateTabListener(tabItem));
        }

        private Listener<TabPanelEvent> createCloseTabListener(final ITabItem tabItem,
                final String id)
        {
            return new Listener<TabPanelEvent>()
                {
                    public final void handleEvent(final TabPanelEvent be)
                    {
                        if (be.type == Events.Close)
                        {
                            tabItem.onClose();
                            openTabs.remove(id);
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createActivateTabListener(final ITabItem tabItem)
        {
            return new Listener<TabPanelEvent>()
                {
                    public final void handleEvent(final TabPanelEvent be)
                    {
                        if (be.type == Events.Select)
                        {
                            tabItem.onActivate();
                        }
                    }
                };
        }

        private Listener<TabPanelEvent> createBeforeCloseListener(final MainTabItem mainTabItem,
                final String id)
        {
            return new Listener<TabPanelEvent>()
                {
                    public void handleEvent(final TabPanelEvent be)
                    {
                        be.doit = false;
                        new ConfirmationDialog(viewContext.getMessage(Dict.CONFIRM_TITLE),
                                viewContext.getMessage(Dict.CONFIRM_CLOSE_MSG))
                            {
                                @Override
                                protected void onYes()
                                {
                                    mainTabItem.close();
                                    openTabs.remove(id);
                                }
                            }.show();
                    }
                };
        }
    }
}