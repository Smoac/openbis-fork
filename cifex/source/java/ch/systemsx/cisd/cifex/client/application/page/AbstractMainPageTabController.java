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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import java.util.List;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.CreateUserWidget;

/**
 * An abstract superclass for the tab controllers. These objects are responsible for creating their
 * views as well as managing interactions between the view and model.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractMainPageTabController
{
    public final static int LEFT_MARGIN = 20;

    protected final ViewContext context;

    protected final List<GridWidget<AbstractFileGridModel>> fileGridWidgets;

    public AbstractMainPageTabController(final ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets)
    {
        this.context = context;
        this.fileGridWidgets = fileGridWidgets;
    }

    /**
     * The method that subclasses need to implement
     */
    protected abstract Widget getWidget();

    /**
     * Used to create breadcrumbs, so the pageContext knows where in the interface the user is.
     */
    protected abstract Page getPageIdentifier();

    /**
     * Create a container for the widget. Subclasses should call this for their layout container,
     * since a listener is automatically added to the container which informs the page context about
     * tab changes.
     */
    protected ContentPanel createOutermostWidgetContainerMonitoringWindowResize()
    {
        ContentPanel container = new ContentPanel()
            {
                @Override
                protected void onWindowResize(int aWidth, int aHeight)
                {
                    super.onWindowResize(aWidth, aHeight);
                    onOutermostContainerWindowResize(aWidth, aHeight);
                    layout(true);
                }
            };
        container.setMonitorWindowResize(true);
        container.setHeaderVisible(false);
        container.setFrame(false);
        // LayoutContainer container = new LayoutContainer();
        container.addListener(Events.Show, new Listener<ComponentEvent>()
            {

                public void handleEvent(ComponentEvent be)
                {
                    context.getPageController().setCurrentPage(getPageIdentifier());
                }
            });
        container.setWidth("100%");
        return container;
    }

    protected void onOutermostContainerWindowResize(int aWidth, int aHeight)
    {
        // Subclasses may override
        // for (GridWidget<AbstractFileGridModel> gridWidget : fileGridWidgets)
        // {
        // gridWidget.getGrid().getView().layout();
        // gridWidget.getWidget().layout(true);
        // }
    }

    /**
     * Create a container for the widget with row layout. Subclasses should call this for their
     * layout container, since a listener is automatically added to the container which informs the
     * page context about tab changes.
     */
    protected ContentPanel createOutermostWidgetContainer()
    {
        ContentPanel container = new ContentPanel();
        container.setHeaderVisible(false);
        container.setFrame(false);

        RowLayout layout = new RowLayout();
        layout.setAdjustForScroll(true);
        container.setLayout(layout);
        container.addListener(Events.Show, new Listener<ComponentEvent>()
            {

                public void handleEvent(ComponentEvent be)
                {
                    context.getPageController().setCurrentPage(getPageIdentifier());
                }
            });
        container.setScrollMode(Scroll.AUTOY);
        return container;
    }

    public static final ContentPanel createContainer()
    {
        final ContentPanel container = new ContentPanel();
        container.setLayout(new RowLayout());
        container.setScrollMode(Scroll.AUTOX);
        container.setHeaderVisible(false);
        return container;
    }

    public static final void addWidgetRow(ContentPanel container, final Widget widget)
    {
        container.add(widget, new RowData(1, -1, new Margins(0, 0, 0, LEFT_MARGIN)));
    }

    public static final void addTitleRow(ContentPanel container, final String text)
    {
        final Html html = new Html(text);
        html.setStyleName("cifex-heading");
        container.add(html, new RowData(1, -1, new Margins(3, 0, 0, LEFT_MARGIN)));
    }

    public static final void addTitleRowWithoutLeftMargin(ContentPanel container, final String text)
    {
        final Html html = new Html(text);
        html.setStyleName("cifex-heading");
        container.add(html, new RowData(1, -1, new Margins(3, 0, 0, 0)));
    }

    protected static final ContentPanel createUserPanel(final boolean allowPermanentUsers,
            ViewContext context)
    {
        ContentPanel createUserPanel = createContainer();
        String panelLabel =
                allowPermanentUsers ? msg(CREATE_USER_LABEL) : msg(CREATE_TEMP_USER_LABEL);
        addTitleRowWithoutLeftMargin(createUserPanel, panelLabel);
        final CreateUserWidget createUserWidget =
                new CreateUserWidget(context, allowPermanentUsers);
        createUserPanel.add(createUserWidget, new RowData(1, -1, new Margins(5)));
        return createUserPanel;
    }
}
