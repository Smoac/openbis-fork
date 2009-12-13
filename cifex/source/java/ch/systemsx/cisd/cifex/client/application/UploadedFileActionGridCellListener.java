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

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;

/**
 * FileActionGridCellListener for non-admin grids.
 * 
 * @author Izabela Adamczyk
 */
public class UploadedFileActionGridCellListener extends FileActionGridCellListener
{

    UploadedFileActionGridCellListener(ViewContext viewContext,
            GridWidget<AbstractFileGridModel> gridWidget,
            final IQuotaInformationUpdater quotaUpdaterOrNull)
    {
        super(false, viewContext, gridWidget, quotaUpdaterOrNull);
    }

    @Override
    protected AsyncCallback<Void> createUpdateFilesCallback(GridWidget<AbstractFileGridModel> grid,
            ViewContext context)
    {
        return new UpdateUploadedFileAsyncCallback(grid, context);
    }
}