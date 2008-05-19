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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.grid.Grid;
import com.gwtext.client.widgets.grid.event.GridCellListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.model.AbstractUserGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.ui.FileShareUserGridRefresherCallback;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * GridCell action listener for link for sharing file with a user.
 * 
 * @author Izabela Adamczyk
 */
final class FileShareUserActionGridCellListener extends GridCellListenerAdapter
{

    private final ViewContext viewContext;

    private final FileShareUserDialog parentDialog;

    FileShareUserActionGridCellListener(final ViewContext viewContext,
            FileShareUserDialog parentDialog)
    {
        this.viewContext = viewContext;
        this.parentDialog = parentDialog;
    }

    private final static String getUserDescription(final Record record)
    {
        final String fullName = record.getAsString(UserGridModel.FULL_NAME);
        final String userCode = record.getAsString(UserGridModel.USER_CODE);
        if (StringUtils.isBlank(fullName))
        {
            return userCode;
        }
        return fullName;
    }

    //
    // GridCellListenerAdapter
    //

    public final void onCellClick(final Grid grid, final int rowIndex, final int colIndex,
            final EventObject e)
    {
        final IMessageResources messageResources = viewContext.getMessageResources();
        final ModelBasedGrid userGrid = (ModelBasedGrid) grid;
        if (grid.getColumnModel().getDataIndex(colIndex).equals(AbstractUserGridModel.ACTION))
        {
            final Record record = grid.getStore().getAt(rowIndex);
            final String userCode = record.getAsString(AbstractUserGridModel.USER_CODE);
            final String fileId = record.getAsString(AbstractUserGridModel.FILE_ID);
            final String fileName = record.getAsString(AbstractUserGridModel.FILE_NAME);
            final String userDescription = getUserDescription(record);
            final Element element = e.getTarget();
            if (element == null)
            {
                return;
            }
            final String targetId = DOM.getElementAttribute(e.getTarget(), "id");
            if (Constants.STOP_SHARING_ID.equals(targetId))
            {
                MessageBox.confirm(messageResources.getActionStopSharingLabel(), messageResources
                        .getStopSharingFileWithUserConfirmText(fileName, userDescription),
                        new MessageBox.ConfirmCallback()
                            {
                                public final void execute(final String btnID)
                                {
                                    if (btnID.equals("yes"))
                                    {
                                        viewContext.getCifexService().deleteSharingLink(
                                                fileId,
                                                userCode,
                                                new FileShareUserGridRefresherCallback(fileId,
                                                        viewContext, userGrid, parentDialog));
                                    }
                                }
                            });
            }
        }
    }
}