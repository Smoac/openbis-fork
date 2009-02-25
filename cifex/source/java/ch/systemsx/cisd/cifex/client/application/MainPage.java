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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.dto.Configuration;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * The main page for non-administrators (permanent and temporary users).
 * 
 * @author Franz-Josef Elmer
 */
final class MainPage extends AbstractMainPage
{
    private final static boolean DOWNLOAD = true;

    private final static boolean UPLOAD = false;

    protected VerticalPanel listCreatedUserPanel;

    MainPage(final ViewContext context)
    {
        super(context);
    }

    private final String getMaxRequestUploadSizeText(final int maxRequestUploadSizeInMB)
    {
        if (maxRequestUploadSizeInMB < 0)
        {
            return Constants.TABLE_NULL_VALUE;
        } else
        {
            return maxRequestUploadSizeInMB + " MB";
        }
    }

    private final Widget createExplanationPanel()
    {
        Model model = context.getModel();
        final boolean isPermanent = model.getUser().isPermanent();
        Configuration configuration = model.getConfiguration();
        int maxUploadRequestSizeInMB = configuration.getMaxUploadRequestSizeInMB();
        final String maxRequestUploadSize =
                getMaxRequestUploadSizeText(maxUploadRequestSizeInMB);
        StringBuffer notesText = new StringBuffer();
        notesText.append(messageResources.getUploadFilesHelpUpload(maxRequestUploadSize));
        if (isPermanent)
        {
            notesText.append(messageResources.getUploadFilesHelpPermanentUser());
        } else
        {
            notesText.append(messageResources.getUploadFilesHelpTemporaryUser());
        }
        notesText.append(messageResources.getUploadFilesHelpSecurity());
        return new HTML(notesText.toString());
    }

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel contentPanel = new ContentPanel("Main-Page");
        Model model = context.getModel();
        final UserInfoDTO user = model.getUser();
        createUserPanel(user.isAdmin());
        createListCreatedUserPanel();
        contentPanel.add(createUploadPart());
        createListFilesGrid(contentPanel, DOWNLOAD);
        if (user.isPermanent() && user.isAdmin() == false)
        {
            contentPanel.add(createUserPanel);
        }
        createListFilesGrid(contentPanel, UPLOAD);
        contentPanel.add(listCreatedUserPanel);
        return contentPanel;
    }

    private VerticalPanel createUploadPart()
    {
        final VerticalPanel verticalPanel = createVerticalPanelPart();
        verticalPanel.add(createPartTitle(context.getMessageResources().getUploadFilesPartTitle()));
        verticalPanel.add(createExplanationPanel());
        verticalPanel.add(createPartTitle(messageResources.getUploadFilesPartTitleLess2GB()));
        verticalPanel.add(new FileUploadWidget(context));
        verticalPanel.add(createPartTitle(messageResources.getUploadFilesPartTitleGreater2GB()));
        String webStartLink = messageResources.getUploadFilesHelpJavaUploaderLink();
        String webStartTitle = messageResources.getUploadFilesHelpJavaUploaderTitle();
        String anchorWebstart =
            DOMUtils.createAnchor(webStartTitle, webStartLink, ServletPathConstants.FILE2GB_UPLOAD_SERVLET_NAME, null,
                    null, false);
        String cliLink = messageResources.getUploadFilesHelpCLILink();
        String cliTitle = messageResources.getUploadFilesHelpCLITitle();
        String anchorCLI =
            DOMUtils.createAnchor(cliTitle, cliLink, ServletPathConstants.COMMAND_LINE_CLIENT_DISTRIBUTION, null,
                    null, false);
        verticalPanel.add(new HTML(messageResources.getUploadFilesHelpJavaUpload(anchorWebstart, anchorCLI)));
        return verticalPanel;
    }

    private void createListCreatedUserPanel()
    {
        listCreatedUserPanel = createVerticalPanelPart();
        context.getCifexService().listUsersRegisteredBy(context.getModel().getUser().getUserCode(),
                new CreatedUserAsyncCallback());
    }

    private void createListFilesGrid(final ContentPanel contentPanel, final boolean showDownload)
    {
        final FileAsyncCallback callback =
                new FileAsyncCallback(context, contentPanel, showDownload);
        if (showDownload)
        {
            context.getCifexService().listDownloadFiles(callback);
        } else
        {
            context.getCifexService().listUploadedFiles(callback);
        }
    }

    //
    // Helper classes
    //

    private final class FileAsyncCallback extends AbstractAsyncCallback
    {

        private final ContentPanel contentPanel;

        private Widget titleWidget;

        private final boolean showDownloaded;

        FileAsyncCallback(final ViewContext context, final ContentPanel contentPanel,
                final boolean showDownload)
        {
            super(context);
            if (showDownload)
            {
                titleWidget =
                        createPartTitle(context.getMessageResources().getDownloadFilesPartTitle());
            } else
            {
                titleWidget =
                        createPartTitle(context.getMessageResources().getUploadedFilesPartTitle());
            }

            this.contentPanel = contentPanel;
            this.showDownloaded = showDownload;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            final FileInfoDTO[] files = (FileInfoDTO[]) result;
            final Widget widget;
            if (files.length > 0)
            {
                final IDataGridModel gridModel;
                if (showDownloaded)
                {
                    gridModel = new DownloadFileGridModel(messageResources);
                } else
                {
                    gridModel = new UploadedFileGridModel(messageResources);
                }
                final Grid fileGrid = new ModelBasedGrid(messageResources, files, gridModel);
                fileGrid.addGridCellListener(new FileDownloadGridCellListener());
                fileGrid.addGridCellListener(new FileCommentGridCellListener(context));
                if (showDownloaded == false)
                {
                    fileGrid.addGridCellListener(new FileActionGridCellListener(false, context));
                }
                widget = fileGrid;
            } else
            {
                final HTML html = new HTML();
                html.setText(showDownloaded ? messageResources.getDownloadFilesEmpty()
                        : messageResources.getUploadedFilesEmpty());
                widget = html;
            }
            final VerticalPanel verticalPanel = createVerticalPanelPart();
            verticalPanel.add(titleWidget);
            verticalPanel.add(widget);
            contentPanel.add(verticalPanel);
        }
    }

    private final class CreatedUserAsyncCallback extends AbstractAsyncCallback
    {

        CreatedUserAsyncCallback()
        {
            super(context);
        }

        private Widget createUserTable(final UserInfoDTO[] users)
        {
            final IDataGridModel gridModel = new UserGridModel(context);
            final Grid userGrid =
                    new ModelBasedGrid(context.getMessageResources(), users, gridModel);
            // Delete user function
            userGrid.addGridCellListener(new UserActionGridCellListener(context, null));
            return userGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            if (((UserInfoDTO[]) result).length > 0)
            {
                listCreatedUserPanel.add(createPartTitle(context.getMessageResources()
                        .getOwnUserTitle()));
                listCreatedUserPanel.add(createUserTable((UserInfoDTO[]) result));
            }
        }
    }

}
