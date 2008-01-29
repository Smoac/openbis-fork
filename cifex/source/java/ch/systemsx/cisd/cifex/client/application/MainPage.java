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

import java.util.HashMap;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.model.IDataGridModel;
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
    MainPage(final ViewContext context)
    {
        super(context);
    }

    private final HTML createExplanationPanel()
    {
        return new HTML(context.getMessageResources().getUploadFilesHelp());
    }

    //
    // AbstractMainPage
    //

    protected final ContentPanel createMainPanel()
    {
        final ContentPanel contentPanel = new ContentPanel("Main-Page");
        final VerticalPanel verticalPanel = new VerticalPanel();
        contentPanel.setWidth("100%");
        verticalPanel.setSpacing(5);
        contentPanel.add(verticalPanel);
        final HashMap urlParams = context.getModel().getUrlParams();
        String fileId = null;
        if (urlParams.isEmpty() == false)
        {
            fileId = (String) urlParams.get(Constants.FILE_ID_PARAMETER);
        }
        final User user = context.getModel().getUser();
        if (fileId == null && user.isPermanent())
        {
            verticalPanel.add(createPartTitle(context.getMessageResources().getUploadFilesPartTitle()));
            verticalPanel.add(createExplanationPanel());
            verticalPanel.add(new FileUploadWidget(context));
        }
        verticalPanel.add(createPartTitle(context.getMessageResources().getDownloadFilesPartTitle()));
        context.getCifexService().listDownloadFiles(new FileAsyncCallback(context, verticalPanel, fileId));
        return contentPanel;
    }

    //
    // Helper classes
    //

    private final class FileAsyncCallback extends AbstractAsyncCallback
    {

        private final VerticalPanel verticalPanel;

        /**
         * The file we are interested in.
         * <p>
         * Could be <code>null</code>.
         * </p>
         */
        private final String fileId;

        FileAsyncCallback(final ViewContext context, final VerticalPanel verticalPanel, final String fileId)
        {
            super(context);
            this.verticalPanel = verticalPanel;
            this.fileId = fileId;
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
            final IMessageResources messageResources = context.getMessageResources();
            if (files.length > 0)
            {
                final IDataGridModel gridModel = new FileGridModel(messageResources);
                final Grid fileGrid = new ModelBasedGrid(messageResources, getFiles(files), gridModel, null);
                fileGrid.addGridCellListener(new FileGridCellListener());
                widget = fileGrid;
            } else
            {
                final HTML html = new HTML();
                html.setText(messageResources.getDownloadFilesEmpty());
                widget = html;
            }
            verticalPanel.add(widget);
        }

    }

}
