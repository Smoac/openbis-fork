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

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;

/**
 * Main panel - where the pages will open.
 * 
 * @author Izabela Adamczyk
 */
public class MainPagePanel extends ContentPanel implements IMainPanel
{

    private static final String PREFIX = GenericConstants.ID_PREFIX + "main-page_";

    private IClosableItem content;

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public MainPagePanel(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
        setLayout(new FitLayout());
        setBodyBorder(false);
        setBorders(false);
        setHeaderVisible(false);
        add(createWelcomePanel());
    }

    private final Component createWelcomePanel()
    {
        return WelcomePanelHelper.createWelcomePanel(viewContext, PREFIX);
    }

    public final void open(final AbstractTabItemFactory tabItemFactory)
    {
        History.newItem(History.getToken()); // WORKAROUND for IE not seeing history changes
        GWTUtils.updatePageTitle(tabItemFactory.getTabTitle());
        reset();
        content = tabItemFactory.create();
        add(content.getComponent());
        layout();
    }

    public final void reset()
    {
        if (content != null)
        {
            content.onClose();
        }
        removeAll();
    }

    public Widget asWidget()
    {
        return this;
    }

}
