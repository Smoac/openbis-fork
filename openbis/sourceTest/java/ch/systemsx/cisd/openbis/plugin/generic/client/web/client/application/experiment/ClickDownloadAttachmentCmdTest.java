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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.AttachmentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.AbstractDefaultTestCommand;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer.ExperimentInfoCallback;

/**
 * Simulates clicking on the specified attachment in the experiment info window.
 * 
 * @author Tomasz Pylak
 */
public class ClickDownloadAttachmentCmdTest extends AbstractDefaultTestCommand
{
    private static final int FILE_NAME_COLUMN_INDEX = 0;

    private final String fileName;

    private final String experimentIdentifier;

    public ClickDownloadAttachmentCmdTest(final String fileName, final String experimentIdentifier)
    {
        this.fileName = fileName;
        this.experimentIdentifier = experimentIdentifier;
        addCallbackClass(ExperimentInfoCallback.class);
        addCallbackClass(SampleBrowserGrid.ListEntitiesCallback.class);
    }

    @SuppressWarnings("unchecked")
    public void execute()
    {
        String attachmentGridId =
                AttachmentsSection.createAttachmentGridId(experimentIdentifier);
        final Widget widget = GWTTestUtil.getWidgetWithID(attachmentGridId);
        final Grid<AttachmentModel> table = (Grid<AttachmentModel>) widget;
        table.fireEvent(Events.CellClick, createGridEvent(table, fileName));
    }

    private static GridEvent createGridEvent(Grid<AttachmentModel> table, String fileName)
    {
        Integer rowIndex = tryGetRowIndex(table, fileName);
        if (rowIndex == null)
        {
            fail("Attachment '" + fileName + "' not found in store with "
                    + table.getStore().getCount() + " rows.");
        }

        final GridEvent gridEvent = new GridEvent(table);
        gridEvent.rowIndex = rowIndex;
        gridEvent.colIndex = FILE_NAME_COLUMN_INDEX;
        return gridEvent;
    }

    private static Integer tryGetRowIndex(Grid<AttachmentModel> table, String fileName)
    {
        final ListStore<AttachmentModel> store = table.getStore();

        for (int i = 0; i < store.getCount(); i++)
        {
            final AttachmentModel row = store.getAt(i);
            if (fileName.equals(row.get(ModelDataPropertyNames.FILE_NAME)))
            {
                return i;
            }
        }
        return null;
    }
}