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

import java.util.List;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;

/**
 * @author Chandrasekhar Ramakrishnan
 */
class InboxTabController extends AbstractMainPageTabController
{

    public InboxTabController(ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets)
    {
        super(context, fileGridWidgets);
    }

    @Override
    protected Widget getWidget()
    {
        final LayoutContainer contentPanel = createOutermostWidgetContainer();
        FileListingTabHelper.createListDownloadFilesGrid(contentPanel, context, fileGridWidgets,
                null);
        return contentPanel;
    }

    @Override
    protected Page getPageIdentifier()
    {
        return Page.INBOX_PAGE;
    }
}
