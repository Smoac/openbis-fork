/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.page;

import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.ui.CreateUserWidget;

/**
 * An abstract superclass for the tab controllers. These objects are responsible for creating their
 * views as well as managing interactions between the view and model.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
abstract class AbstractCIFEXTabController
{
    protected final ViewContext context;

    public AbstractCIFEXTabController(final ViewContext context)
    {
        this.context = context;
    }

    /**
     * The method that subclasses need to implement
     */
    protected abstract Widget getWidget();

    protected static final LayoutContainer createContainer()
    {
        final LayoutContainer container = new LayoutContainer();
        container.setWidth("100%");
        return container;
    }

    protected static final void addTitlePart(LayoutContainer container, final String text)
    {
        final Html html = new Html(text);
        html.setStyleName("cifex-heading");
        container.add(html, new FlowData(new Margins(3, 0, 0, 0)));
    }

    protected static final LayoutContainer createUserPanel(final boolean allowPermanentUsers,
            ViewContext context)
    {
        LayoutContainer createUserPanel = createContainer();
        if (allowPermanentUsers)
        {
            addTitlePart(createUserPanel, context.getMessageResources().getAdminCreateUserLabel());
        } else
        {
            addTitlePart(createUserPanel, context.getMessageResources().getCreateUserLabel());
        }
        final CreateUserWidget createUserWidget =
                new CreateUserWidget(context, allowPermanentUsers);
        createUserPanel.add(createUserWidget);
        return createUserPanel;
    }
}
