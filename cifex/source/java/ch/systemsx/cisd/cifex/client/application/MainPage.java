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

import java.util.Map;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.FileUploadWidget;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.dto.File;
import ch.systemsx.cisd.cifex.client.dto.User;

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

    private final String getMaxRequestUploadSizeText(int maxRequestUploadSizeInMB)
    {
        if (maxRequestUploadSizeInMB < 0)
        {
            return "-";
        } else
        {
            return maxRequestUploadSizeInMB + " MB";
        }
    }

    private final HTML createExplanationPanel()
    {
        final boolean isPermanent = context.getModel().getUser().isPermanent();
        final String maxRequestUploadSizeTest =
                getMaxRequestUploadSizeText(context.getModel().getConfiguration().getMaxUploadRequestSizeInMB());
        return new HTML(isPermanent ? context.getMessageResources().getUploadFilesHelpPermanentUser(
                maxRequestUploadSizeTest) : context.getMessageResources().getUploadFilesHelpTemporaryUser(
                maxRequestUploadSizeTest));
    }

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel contentPanel = new ContentPanel("Main-Page");
        createUserPanel(context.getModel().getUser().isAdmin());
        createListCreatedUserPanel();

        final Map urlParams = context.getModel().getUrlParams();
        String fileId = null;
        if (urlParams.isEmpty() == false)
        {
            fileId = (String) urlParams.get(Constants.FILE_ID_PARAMETER);
        }
        final User user = context.getModel().getUser();
        if (fileId == null)
        {
            final VerticalPanel verticalPanel = createVerticalPanelPart();
            verticalPanel.add(createPartTitle(context.getMessageResources().getUploadFilesPartTitle()));
            verticalPanel.add(createExplanationPanel());
            verticalPanel.add(new FileUploadWidget(context));
            contentPanel.add(verticalPanel);
            if (user.isPermanent() && user.isAdmin() == false)
            {
                contentPanel.add(createUserPanel);
            }
            contentPanel.add(listCreatedUserPanel);
        }
        createListFilesGrid(contentPanel, fileId, UPLOAD);
        createListFilesGrid(contentPanel, fileId, DOWNLOAD);
        return contentPanel;
    }

    private void createListCreatedUserPanel()
    {
        listCreatedUserPanel = createVerticalPanelPart();
        context.getCifexService().listUsersRegisteredBy(context.getModel().getUser(), new CreatedUserAsyncCallback());
    }

    private void createListFilesGrid(final ContentPanel contentPanel, String fileId, boolean showDownload)
    {
        final FileAsyncCallback fileAsyncCallback = new FileAsyncCallback(context, contentPanel, fileId, showDownload);
        if (showDownload)
        {
            context.getCifexService().listDownloadFiles(fileAsyncCallback);
        } else
        {
            context.getCifexService().listUploadedFiles(fileAsyncCallback);
        }
    }

    //
    // Helper classes
    //
    private final class FileAsyncCallback extends AbstractAsyncCallback
    {

        private final ContentPanel contentPanel;

        /**
         * The file we are interested in.
         * <p>
         * Could be <code>null</code>.
         * </p>
         */
        private final String fileId;

        private Widget titleWidget;

        private boolean showDownloaded;

        FileAsyncCallback(final ViewContext context, final ContentPanel contentPanel, final String fileId,
                boolean showDownload)
        {
            super(context);
            if (showDownload)
            {
                titleWidget = createPartTitle(context.getMessageResources().getDownloadFilesPartTitle());
            } else
            {
                titleWidget = createPartTitle(context.getMessageResources().getUploadedFilesPartTitle());
            }

            this.contentPanel = contentPanel;
            this.fileId = fileId;
            this.showDownloaded = showDownload;
        }

        private final File[] getFiles(final File[] files)
        {
            if (fileId == null)
            {
                return files;
            }
            for (int i = 0; i < files.length; i++)
            {
                final File file = files[i];
                if (String.valueOf(file.getID()).equals(fileId))
                {
                    return new File[]
                        { file };
                }
            }
            return files;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            final File[] files = (File[]) result;
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
                final Grid fileGrid = new ModelBasedGrid(messageResources, getFiles(files), gridModel, "100px");
                fileGrid.addGridCellListener(new FileDownloadGridCellListener());
                if (showDownloaded == false)
                {
                    fileGrid.addGridCellListener(new FileActionGridCellListener(context));
                }
                widget = fileGrid;
            } else
            {
                final HTML html = new HTML();
                html.setText(showDownloaded ? messageResources.getDownloadFilesEmpty() : messageResources
                        .getUploadedFilesEmpty());
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

        private Widget createUserTable(final User[] users)
        {
            final IDataGridModel gridModel = new UserGridModel(context.getMessageResources());
            final Grid userGrid = new ModelBasedGrid(context.getMessageResources(), users, gridModel, "100px");
            // Delete user function
            userGrid.addGridCellListener(new UserActionGridCellListener(context));
            return userGrid;
        }

        //
        // AbstractAsyncCallback
        //

        public final void onSuccess(final Object result)
        {
            if (((User[]) result).length > 0)
            {
                listCreatedUserPanel.add(createPartTitle(context.getMessageResources().getOwnUserTitle()));
                listCreatedUserPanel.add(createUserTable((User[]) result));
            }
        }
    }

}
