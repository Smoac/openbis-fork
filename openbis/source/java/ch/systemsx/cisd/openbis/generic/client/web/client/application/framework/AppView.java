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

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.RootPanel;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.TopMenu;

/**
 * Main application view.
 * 
 * @author Izabela Adamczyk
 */
final class AppView extends View
{
    private final GenericViewContext viewContext;

    private Viewport viewport;

    private ContentPanel west;

    private MainTabPanel center;

    private TopMenu north;

    private ComponentProvider componentProvider;

    private CategoriesBuilder categoriesBuilder;

    AppView(final Controller controller, final GenericViewContext viewContext)
    {
        super(controller);
        this.viewContext = viewContext;
    }

    private final ITabItem getData(final AppEvent<?> event)
    {
        final Object data = event.getData(GenericConstants.ASSOCIATED_CONTENT_PANEL);
        if (data instanceof ContentPanel)
        {
            return new ContentPanelAdapter((ContentPanel) data);
        }
        return (ITabItem) data;
    }

    private final void activateTab(final ITabItem tabItem)
    {
        center.openTab(tabItem);
    }

    private final void initUI()
    {
        viewport = new Viewport();
        viewport.setLayout(new BorderLayout());
        createNorth();
        createWest();
        createCenter();
        RootPanel.get().clear();
        RootPanel.get().add(viewport);
    }

    private final void createNorth()
    {
        north = new TopMenu(viewContext, componentProvider.getDummyComponent());
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 30);
        data.setMargins(new Margins());
        viewport.add(north, data);
    }

    private final void createWest()
    {
        west = new LeftMenu(categoriesBuilder.getCategories());
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 200, 150, 350);
        data.setMargins(new Margins(5, 0, 5, 5));
        viewport.add(west, data);
    }

    private final void createCenter()
    {
        center = new MainTabPanel(viewContext);
        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(5, 5, 5, 5));
        viewport.add(center, data);
    }

    //
    // View
    //

    @Override
    protected final void initialize()
    {
        componentProvider = new ComponentProvider(viewContext);
        categoriesBuilder = new CategoriesBuilder(componentProvider);
    }

    @Override
    protected final void handleEvent(final AppEvent<?> event)
    {
        switch (event.type)
        {
            case AppEvents.INIT:
                initUI();
                break;

            case AppEvents.NAVI_EVENT:
                activateTab(getData(event));
                break;
        }
    }
}
