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

package ch.systemsx.cisd.cifex.client.application;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.user.client.ui.HTML;

import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The page where files may be uploaded, and uploaded files can be maintained.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
final class SharePage extends AbstractFileListPage
{
    SharePage(final ViewContext context)
    {
        super(context);
    }

    @Override
    protected final LayoutContainer createMainPanel()
    {
        final LayoutContainer contentPanel = new LayoutContainer();
        final HTML explanationWidget = new HTML(createExplanationText());
        final IQuotaInformationUpdater quotaInformationUpdater =
                new QuotaInformationUpdater(explanationWidget);
        quotaInformationUpdater.triggerUpdate();
        contentPanel.add(createUploadPart(explanationWidget));
        createListFilesGrid(contentPanel, UPLOAD, context, quotaInformationUpdater);
        return contentPanel;
    }

    private String createExplanationText()
    {
        final IMessageResources messageResources = context.getMessageResources();
        final Model model = context.getModel();
        final UserInfoDTO user = model.getUser();
        final boolean isPermanent = user.isPermanent();
        final StringBuilder notesText = new StringBuilder();
        notesText.append(messageResources.getUploadFilesHelpUpload(getMaxFileSize(user
                .getMaxFileSizePerQuotaGroupInMB()), getCurrentFileSizeInMB(user
                .getCurrentFileSize()), getMaxFileCount(user.getMaxFileCountPerQuotaGroup()), user
                .getCurrentFileCount()));
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
        IMessageResources messageResources = context.getMessageResources();
        final LayoutContainer verticalPanel = createContainer();
        addTitlePart(verticalPanel, context.getMessageResources().getUploadFilesPartTitle());
        verticalPanel.add(explanationWidget);
        addTitlePart(verticalPanel, messageResources.getUploadFilesPartTitleLess2GB());
        verticalPanel.add(new FileUploadWidget(context));
        addTitlePart(verticalPanel, messageResources.getUploadFilesPartTitleGreater2GB());
        String webStartLink = messageResources.getUploadFilesHelpJavaUploaderLink();
        String webStartTitle = messageResources.getUploadFilesHelpJavaUploaderTitle();
        String anchorWebstart =
                DOMUtils.createAnchor(webStartTitle, webStartLink,
                        ServletPathConstants.FILE2GB_UPLOAD_SERVLET_NAME, null, null, false);
        String cliLink = messageResources.getUploadFilesHelpCLILink();
        String cliTitle = messageResources.getUploadFilesHelpCLITitle();
        String anchorCLI =
                DOMUtils.createAnchor(cliTitle, cliLink,
                        ServletPathConstants.COMMAND_LINE_CLIENT_DISTRIBUTION, null, null, false);
        verticalPanel.add(new HTML(messageResources.getUploadFilesHelpJavaUpload(anchorWebstart,
                anchorCLI)));
        return verticalPanel;
    }

    //
    // Helper classes
    //

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
}
