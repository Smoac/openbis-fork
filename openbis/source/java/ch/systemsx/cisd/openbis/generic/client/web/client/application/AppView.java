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

/**
 * Main application view.
 * 
 * @author Izabela Adamczyk
 */
public class AppView extends View
{

    private final GenericViewContext viewContext;

    private Viewport viewport;

    private ContentPanel west;

    private MainTabPanel center;

    private TopMenu north;

    private ComponentProvider componentProvider;

    private CategoriesBuilder categoriesBuilder;

    public AppView(final Controller controller, final GenericViewContext viewContext2)
    {
        super(controller);
        viewContext = viewContext2;
    }

    @Override
    protected void handleEvent(final AppEvent<?> event)
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

    ContentPanel getData(final AppEvent<?> event)
    {
        final Object data = event.getData(GenericConstants.ASSOCIATED_CONTENT_PANEL);
        if (data instanceof ContentPanel)
        {
            return (ContentPanel) data;
        } else
        {
            throw new IllegalArgumentException("Incorrect event data");
        }
    }

    private void activateTab(final ContentPanel c)
    {
        center.openTab(c);
    }

    @Override
    protected void initialize()
    {
        componentProvider = new ComponentProvider(viewContext);
        categoriesBuilder = new CategoriesBuilder(componentProvider);
    }

    private void initUI()
    {

        viewport = new Viewport();
        viewport.setLayout(new BorderLayout());

        createNorth();
        createWest();
        createCenter();

        RootPanel.get().clear();
        RootPanel.get().add(viewport);
    }

    private void createNorth()
    {
        north = new TopMenu(viewContext, componentProvider.getDummyComponent());

        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 30);
        data.setMargins(new Margins());

        viewport.add(north, data);
    }

    private void createWest()
    {
        west = new LeftMenu(categoriesBuilder.getCategories());

        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.WEST, 200, 150, 350);
        data.setMargins(new Margins(5, 0, 5, 5));

        viewport.add(west, data);
    }

    private void createCenter()
    {
        center = new MainTabPanel();

        final BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
        data.setMargins(new Margins(5, 5, 5, 5));

        viewport.add(center, data);
    }

}
