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

package ch.systemsx.cisd.cifex.rpc.client.gui;

import java.io.File;

import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo;

/**
 * The download operation runs in its own thread. This class manages keeps track of the thread and
 * interfaces between the thread the download operation runs in and the GUI thread for updates to
 * the GUI.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileDownloadOperation implements Runnable
{
    private final FileDownloadClientModel tableModel;

    private final FileDownloadInfo fileDownloadInfo;

    private final File downloadDirectory;

    private final Thread thread;

    FileDownloadOperation(FileDownloadClientModel model, FileDownloadInfo info, File downloadDirectory)
    {
        super();
        this.tableModel = model;
        this.fileDownloadInfo = info;
        this.downloadDirectory = downloadDirectory;
        this.thread = new Thread(this);
    }

    public void run()
    {
        tableModel.getDownloader().download(
                Long.parseLong(fileDownloadInfo.getFileInfoDTO().getIDStr()), downloadDirectory,
                null);
        tableModel.finishedDownloadingFile(this);
    }

    public void start()
    {
        thread.start();
    }
}
