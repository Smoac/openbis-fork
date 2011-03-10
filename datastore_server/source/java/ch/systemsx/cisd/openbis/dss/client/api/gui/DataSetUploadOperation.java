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

package ch.systemsx.cisd.openbis.dss.client.api.gui;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.FileExistsException;
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo.Status;

/**
 * DataSetUploadOperation represents a request to upload a data set to openBIS/dss. The upload
 * operation runs in its own thread, which is managed by this class, and notifies the GUI and client
 * model of changes in status.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
final class DataSetUploadOperation implements Runnable
{
    private final DataSetUploadTableModel tableModel;

    private final DataSetUploadClientModel clientModel;

    private final NewDataSetInfo newDataSetInfo;

    DataSetUploadOperation(DataSetUploadTableModel model, DataSetUploadClientModel clientModel,
            NewDataSetInfo newDataSetInfo)
    {
        this.tableModel = model;
        this.clientModel = clientModel;
        this.newDataSetInfo = newDataSetInfo;
    }

    public void run()
    {
        try
        {
            if (newDataSetInfo.getStatus() == NewDataSetInfo.Status.QUEUED_FOR_UPLOAD)
            {
                newDataSetInfo.setStatus(Status.UPLOADING);
                tableModel.fireChanged(newDataSetInfo, Status.UPLOADING);
                clientModel.getDssComponent().putDataSet(
                        newDataSetInfo.getNewDataSetBuilder().asNewDataSetDTO(),
                        newDataSetInfo.getNewDataSetBuilder().getFile());
            }
            newDataSetInfo.setStatus(Status.COMPLETED_UPLOAD);
            tableModel.fireChanged(newDataSetInfo, Status.COMPLETED_UPLOAD);
        } catch (Throwable th)
        {
            newDataSetInfo.setStatus(Status.FAILED);
            tableModel.fireChanged(newDataSetInfo, Status.FAILED);
            Throwable actualTh;
            if (th instanceof Error)
            {
                actualTh = th;
            } else
            {
                actualTh = CheckedExceptionTunnel.unwrapIfNecessary((Exception) th);
                if (actualTh instanceof EnvironmentFailureException)
                {
                    actualTh = ((EnvironmentFailureException) actualTh).getCause();
                }
            }
            if (actualTh instanceof FileExistsException == false)
            {
                DataSetUploadClient.notifyUserOfThrowable(tableModel.getMainWindow(),
                        newDataSetInfo.getNewDataSetBuilder().getFile().getAbsolutePath(),
                        "Uploading", actualTh, null);
            }
        }
    }
}
