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
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.IQuotaInformationUpdater;
import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.IHistoryController.Page;
import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.AbstractFileGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The controller for the share page.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class ShareTabController extends AbstractMainPageTabController
{

    private final FileUploadWidget fileUploadWidget;

    public ShareTabController(ViewContext context,
            final List<GridWidget<AbstractFileGridModel>> fileGridWidgets)
    {
        super(context, fileGridWidgets);

        fileUploadWidget = new FileUploadWidget(context);
    }

    @Override
    protected Widget getWidget()
    {
        final LayoutContainer contentPanel = createOutermostWidgetContainer();
        final HTML explanationWidget = new HTML(createExplanationText());
        final IQuotaInformationUpdater quotaInformationUpdater =
                new QuotaInformationUpdater(explanationWidget);
        quotaInformationUpdater.triggerUpdate();
        contentPanel.add(createUploadPart(explanationWidget));
        FileListingTabHelper.createListOwnedFilesGrid(context, contentPanel, fileGridWidgets,
                quotaInformationUpdater);
        return contentPanel;
    }

    @Override
    protected void onOutermostContainerWindowResize(int aWidth, int aHeight)
    {
        fileUploadWidget.onOutermostContainerWindowResize(aWidth, aHeight);
        for (GridWidget<AbstractFileGridModel> gridWidget : fileGridWidgets)
        {
            gridWidget.getGrid().getView().layout();
            gridWidget.getWidget().layout(true);
        }
    }

    private String createExplanationText()
    {
        final IMessageResources messageResources = context.getMessageResources();
        final Model model = context.getModel();
        final UserInfoDTO user = model.getUser();
        final boolean isPermanent = user.isPermanent();
        final StringBuilder notesText = new StringBuilder();
        notesText.append(messageResources.getSharedFilesHelpUpload(FileListingTabHelper
                .getMaxFileSize(user.getMaxFileSizePerQuotaGroupInMB()), FileListingTabHelper
                .getCurrentFileSizeInMB(user.getCurrentFileSize()), FileListingTabHelper
                .getMaxFileCount(user.getMaxFileCountPerQuotaGroup()), user.getCurrentFileCount()));
        if (isPermanent)
        {
            notesText.append(messageResources.getUploadFilesHelpPermanentUser());
        } else
        {
            notesText.append(messageResources.getUploadFilesHelpTemporaryUser());
        }
        notesText.append(messageResources.getUploadFilesHelpSecurity());
        return notesText.toString();
    }

    private LayoutContainer createUploadPart(HTML explanationWidget)
    {
        final LayoutContainer verticalPanel = createContainer();
        addTitlePart(verticalPanel, context.getMessageResources().getUploadFilesPartTitle());
        verticalPanel.add(explanationWidget);
        verticalPanel.add(fileUploadWidget, new FlowData(5));
        return verticalPanel;
    }

    /**
     * A helper class for dealing with quota information.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private final class QuotaInformationUpdater implements IQuotaInformationUpdater
    {
        final HTML explanationWidget;

        QuotaInformationUpdater(HTML explanationWidget)
        {
            this.explanationWidget = explanationWidget;
        }

        public void triggerUpdate()
        {
            context.getCifexService().refreshQuotaInformationOfCurrentUser(
                    new QuotaInfoRefreshAsyncCallBack(explanationWidget));
        }
    }

    private final class QuotaInfoRefreshAsyncCallBack extends AbstractAsyncCallback<UserInfoDTO>
    {

        final HTML explanationWidget;

        QuotaInfoRefreshAsyncCallBack(HTML explanationWidget)
        {
            super(context);
            this.explanationWidget = explanationWidget;
        }

        public final void onSuccess(final UserInfoDTO result)
        {
            if (result != null)
            {
                context.getModel().setUser(result);
                explanationWidget.setHTML(createExplanationText());
            }
        }
    }

    @Override
    protected Page getPageIdentifier()
    {
        return Page.SHARE_PAGE;
    }

}
