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

import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * The main page for non-administrators (permanent and temporary users).
 * 
 * @author Franz-Josef Elmer
 */
final class MainPage extends AbstractMainPage
{
    private final static boolean DOWNLOAD = true;

    private final static boolean UPLOAD = false;

    MainPage(final ViewContext context)
    {
        super(context);
    }

    static private final String getMaxRequestUploadSizeText(final int maxRequestUploadSizeInMB)
    {
        if (maxRequestUploadSizeInMB < 0)
        {
            return Constants.TABLE_NULL_VALUE;
        } else
        {
            return maxRequestUploadSizeInMB + " MB";
        }
    }

    private static final Widget createExplanationPanel(ViewContext context)
    {
        IMessageResources messageResources = context.getMessageResources();
        Model model = context.getModel();
        final boolean isPermanent = model.getUser().isPermanent();
        Configuration configuration = model.getConfiguration();
        int maxUploadRequestSizeInMB = configuration.getMaxUploadRequestSizeInMB();
        Long usersMaxUploadSize = model.getUser().getMaxUploadRequestSizeInMB();
        if (usersMaxUploadSize != null)
        {
            maxUploadRequestSizeInMB = usersMaxUploadSize.intValue();
        }
        final String maxRequestUploadSize = getMaxRequestUploadSizeText(maxUploadRequestSizeInMB);
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

    @Override
    protected final LayoutContainer createMainPanel()
    {
        final LayoutContainer contentPanel = new LayoutContainer();
        Model model = context.getModel();
        final UserInfoDTO user = model.getUser();
        LayoutContainer createUserPanel = createUserPanel(user.isAdmin(), context);
        LayoutContainer listCreatedUserPanel = createContainer();
        createListCreatedUserPanel(listCreatedUserPanel, context);
        contentPanel.add(createUploadPart(context));
        createListFilesGrid(contentPanel, DOWNLOAD, context);
        if (user.isPermanent() && user.isAdmin() == false)
        {
            contentPanel.add(createUserPanel);
        }
        createListFilesGrid(contentPanel, UPLOAD, context);
        contentPanel.add(listCreatedUserPanel);
        return contentPanel;
    }

    static private LayoutContainer createUploadPart(ViewContext context)
    {
        IMessageResources messageResources = context.getMessageResources();
        final LayoutContainer verticalPanel = createContainer();
        addTitlePart(verticalPanel, context.getMessageResources().getUploadFilesPartTitle());
        verticalPanel.add(createExplanationPanel(context));
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

    static private void createListCreatedUserPanel(LayoutContainer listCreatedUserPanel,
            ViewContext context)
    {
        context.getCifexService().listUsersRegisteredBy(context.getModel().getUser().getUserCode(),
                new CreatedUserAsyncCallback(listCreatedUserPanel, context));
    }

    static private void createListFilesGrid(final LayoutContainer contentPanel,
            final boolean showDownload, ViewContext context)
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

    private static final class FileAsyncCallback extends AbstractAsyncCallback<List<FileInfoDTO>>
    {

        private final LayoutContainer contentPanel;

        private final boolean showDownloaded;

        private final ViewContext context;

        FileAsyncCallback(final ViewContext context, final LayoutContainer contentPanel,
                final boolean showDownload)
        {
            super(context);
            this.context = context;
            this.contentPanel = contentPanel;
            this.showDownloaded = showDownload;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final List<FileInfoDTO> result)
        {
            final List<FileInfoDTO> files = result;
            final Widget widget;
            IMessageResources messageResources = context.getMessageResources();
            if (files.size() > 0)
            {
                final Grid<?> fileGrid;
                if (showDownloaded)
                {
                    ListStore<DownloadFileGridModel> store = new ListStore<DownloadFileGridModel>();
                    store.add(DownloadFileGridModel.convert(messageResources, files));
                    fileGrid =
                            new Grid<DownloadFileGridModel>(store, new ColumnModel(
                                    DownloadFileGridModel.getColumnConfigs(messageResources)));
                } else
                {
                    ListStore<UploadedFileGridModel> store = new ListStore<UploadedFileGridModel>();
                    store.add(UploadedFileGridModel.convert(messageResources, files));
                    fileGrid =
                            new Grid<UploadedFileGridModel>(store, new ColumnModel(
                                    UploadedFileGridModel.getColumnConfigs(messageResources)));
                }
                fileGrid.addListener(Events.CellClick, new FileDownloadGridCellListener());
                fileGrid.addListener(Events.CellClick, new FileCommentGridCellListener(context));
                if (showDownloaded == false)
                {
                    fileGrid.addListener(Events.CellClick, new UploadedFileActionGridCellListener(
                            context));
                }
                fileGrid.setHeight(Constants.GRID_HEIGHT);
                widget = fileGrid;
            } else
            {
                final HTML html = new HTML();
                html.setText(showDownloaded ? messageResources.getDownloadFilesEmpty()
                        : messageResources.getUploadedFilesEmpty());
                widget = html;
            }
            final LayoutContainer verticalPanel = createContainer();
            if (showDownloaded)
            {
                addTitlePart(verticalPanel, context.getMessageResources()
                        .getDownloadFilesPartTitle());
            } else
            {
                addTitlePart(verticalPanel, context.getMessageResources()
                        .getUploadedFilesPartTitle());
            }
            verticalPanel.add(widget);
            contentPanel.add(verticalPanel);
            contentPanel.layout();
        }
    }

    private static final class CreatedUserAsyncCallback extends
            AbstractAsyncCallback<List<UserInfoDTO>>
    {

        private final LayoutContainer listCreatedUserPanel;

        private final ViewContext context;

        CreatedUserAsyncCallback(LayoutContainer listCreatedUserPanel, ViewContext context)
        {
            super(context);
            this.listCreatedUserPanel = listCreatedUserPanel;
            this.context = context;
        }

        private Widget createUserTable(final List<UserInfoDTO> users)
        {
            ListStore<UserGridModel> store = new ListStore<UserGridModel>();
            store.add(UserGridModel.convert(context, users));
            final Grid<UserGridModel> userGrid =
                    new Grid<UserGridModel>(store, new ColumnModel(UserGridModel
                            .getColumnConfigs(context.getMessageResources())));
            userGrid.setHeight(Constants.GRID_HEIGHT);
            // Delete user function
            userGrid.addListener(Events.CellClick,
                    new UserActionGridCellListener<AbstractFileGridModel>(context, null));
            return userGrid;
        }

        public final void onSuccess(final List<UserInfoDTO> result)
        {
            if (result.size() > 0)
            {
                addTitlePart(listCreatedUserPanel, context.getMessageResources().getOwnUserTitle());
                listCreatedUserPanel.add(createUserTable(result));
                listCreatedUserPanel.layout();
            }
        }
    }

}
