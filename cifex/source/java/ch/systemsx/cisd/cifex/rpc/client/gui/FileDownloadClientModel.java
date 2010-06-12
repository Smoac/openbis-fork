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
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.table.AbstractTableModel;

import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXDownloader;
import ch.systemsx.cisd.cifex.rpc.client.TransmissionSpeedCalculator;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo.STATUS;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * The model and central class for the download client. The FileDownloadClientModel knows how to
 * manages the list of downloadable files, initiates downloads (which run in a separate thread) and
 * notifies the GUI of updates.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileDownloadClientModel extends AbstractTableModel
{

    private static final long serialVersionUID = 1L;

    private static ExecutorService executor =
            new NamingThreadPoolExecutor("File download", 1, 1, 0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>()).daemonize();

    private final JFrame mainWindow;

    private final ICIFEXComponent cifex;

    private final String sessionId;

    private final ICIFEXDownloader downloader;

    private final ITimeProvider timeProvider;

    private final ArrayList<FileDownloadInfo> fileDownloadInfos = new ArrayList<FileDownloadInfo>();

    private FileDownloadInfo currentlyDownloadingFile;

    private File downloadDirectory;

    private char[] passphrase = new char[0];

    // Constants for column order
    static final int FILE_DETAILS_COLUMN = 0;

    static final int SENDER_COLUMN = 1;

    static final int COMMENT_COLUMN = 2;

    static final int SENT_DATE_COLUMN = 3;

    static final int EXPIRATION_DATE_COLUMN = 4;

    static final int DOWNLOAD_STATUS_COLUMN = 5;

    /**
     * FileDownloadInfo is a mixture of FileInfoDTO, which encapsulates information about files
     * available for download from CIFEX, and downloading progress state.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class FileDownloadInfo
    {
        static enum STATUS
        {
            TO_DOWNLOAD, QUEUED, DOWNLOADING, COMPLETED, FAILED, STALLED
        }

        private final FileInfoDTO fileInfoDTO;

        private final TransmissionSpeedCalculator transmissionSpeedCalculator;

        private File file;

        private int percentageDownloaded;

        private long numberOfBytesDownloaded;

        private STATUS status;

        FileDownloadInfo(FileInfoDTO fileInfo, ITimeProvider timeProvider)
        {
            this.fileInfoDTO = fileInfo;
            transmissionSpeedCalculator = new TransmissionSpeedCalculator(timeProvider);
            percentageDownloaded = 0;
            numberOfBytesDownloaded = 0;
            setStatus(STATUS.TO_DOWNLOAD);
        }

        public FileInfoDTO getFileInfoDTO()
        {
            return fileInfoDTO;
        }

        public void setFile(File file)
        {
            this.file = file;
        }

        public File getFile()
        {
            return file;
        }

        public int getPercentageDownloaded()
        {
            return percentageDownloaded;
        }

        public long getNumberOfBytesDownloaded()
        {
            return numberOfBytesDownloaded;
        }

        public long getEstimatedTimeOfArrival()
        {
            float remainingBytes = (fileInfoDTO.getSize() - numberOfBytesDownloaded);
            float bytesPerMillisecond =
                    transmissionSpeedCalculator.getEstimatedBytesPerMillisecond();
            if (bytesPerMillisecond < 0.001)
            {
                return -1;
            }
            return (long) (remainingBytes / bytesPerMillisecond);
        }

        void updateProgress(int percent, long numberOfBytes)
        {
            setStatus(STATUS.DOWNLOADING);
            int transmittedSinceLastUpdate = (int) (numberOfBytes - numberOfBytesDownloaded);
            percentageDownloaded = percent;
            numberOfBytesDownloaded = numberOfBytes;
            transmissionSpeedCalculator
                    .noteTransmittedBytesSinceLastUpdate(transmittedSinceLastUpdate);
        }

        public void setStatus(STATUS status)
        {
            this.status = status;
        }

        public STATUS getStatus()
        {
            return status;
        }
    }

    FileDownloadClientModel(FileDownloadClient downloadClient, JFrame mainWindow,
            ITimeProvider timeProvider)
    {
        this.cifex = downloadClient.getCifex();
        this.sessionId = downloadClient.getSessionId();
        this.downloader = downloadClient.getDownloader();
        this.mainWindow = mainWindow;
        this.timeProvider = timeProvider;
        this.downloadDirectory = new File(System.getProperty("user.home"));

        addProgessListener();

        updateDownloadableFiles();
    }

    public File getDownloadDirectory()
    {
        return downloadDirectory;
    }

    public void setDownloadDirectory(File downloadDirectory)
    {
        this.downloadDirectory = downloadDirectory;
    }

    public void setPassphrase(char[] passphrase)
    {
        this.passphrase = passphrase;
    }

    public ICIFEXDownloader getDownloader()
    {
        return downloader;
    }

    private void addProgessListener()
    {
        downloader.addProgressListener(new IProgressListener()
            {
                public void start(File file, long fileSize, Long fileIdOrNull)
                {
                    currentlyDownloadingFile = tryToFindDownloadInfoForFile(fileIdOrNull);
                    if (currentlyDownloadingFile != null)
                    {
                        currentlyDownloadingFile.updateProgress(0, 0);
                        fireChanged();
                    }
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                    if (currentlyDownloadingFile != null)
                    {
                        currentlyDownloadingFile.updateProgress(percentage, numberOfBytes);
                        fireChanged();
                    }
                }

                public void finished(boolean successful)
                {
                    if (currentlyDownloadingFile != null)
                    {
                        if (successful)
                        {
                            currentlyDownloadingFile.updateProgress(100,
                                    currentlyDownloadingFile.fileInfoDTO.getSize());
                            currentlyDownloadingFile.setStatus(FileDownloadInfo.STATUS.COMPLETED);
                        } else
                        {
                            currentlyDownloadingFile.setStatus(FileDownloadInfo.STATUS.FAILED);
                        }
                        fireChanged();
                    }
                }

                public void exceptionOccured(Throwable throwable)
                {
                }

                public void warningOccured(String warningMessage)
                {
                    currentlyDownloadingFile.setStatus(FileDownloadInfo.STATUS.STALLED);
                }

                private FileDownloadInfo tryToFindDownloadInfoForFile(Long fileIdOrNull)
                {
                    if (null == fileIdOrNull)
                        return null;

                    for (FileDownloadInfo fileDownloadInfo : fileDownloadInfos)
                    {
                        if (fileIdOrNull.equals(fileDownloadInfo.fileInfoDTO.getID()))
                            return fileDownloadInfo;
                    }
                    return null;
                }

                private void fireChanged()
                {
                    if (currentlyDownloadingFile != null)
                    {
                        int index = fileDownloadInfos.indexOf(currentlyDownloadingFile);
                        fireTableRowsUpdated(index, index);
                    }
                }

            });
    }

    void setSelectedIndices(ArrayList<Integer> selectedIndices)
    {

    }

    void updateDownloadableFiles()
    {
        // get the list of downloadable files from CIFEX
        FileInfoDTO[] downloadableFileInfos = cifex.listDownloadFiles(sessionId);

        // create FileDownloadInfos for each of the files
        fileDownloadInfos.clear();
        for (FileInfoDTO fileInfo : downloadableFileInfos)
        {
            fileDownloadInfos.add(new FileDownloadInfo(fileInfo, timeProvider));
        }

        fireTableDataChanged();
    }

    public int getColumnCount()
    {
        return 6;
    }

    public int getRowCount()
    {
        return fileDownloadInfos.size();
    }

    @Override
    public String getColumnName(int column)
    {
        switch (column)
        {
            case FILE_DETAILS_COLUMN:
                return "File";
            case SENDER_COLUMN:
                return "From";
            case COMMENT_COLUMN:
                return "Comment";
            case SENT_DATE_COLUMN:
                return "Sent Date";
            case EXPIRATION_DATE_COLUMN:
                return "Download Before";
            case DOWNLOAD_STATUS_COLUMN:
                return "";
        }
        return null;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        FileDownloadInfo fileDownloadInfo = fileDownloadInfos.get(rowIndex);
        switch (columnIndex)
        {
            case FILE_DETAILS_COLUMN:
                return fileDownloadInfo.fileInfoDTO;
            case SENDER_COLUMN:
                return fileDownloadInfo.fileInfoDTO.getOwner();
            case COMMENT_COLUMN:
                return fileDownloadInfo.fileInfoDTO.getComment();
            case SENT_DATE_COLUMN:
                return fileDownloadInfo.fileInfoDTO.getRegistrationDate();
            case EXPIRATION_DATE_COLUMN:
                return fileDownloadInfo.fileInfoDTO.getExpirationDate();
            case DOWNLOAD_STATUS_COLUMN:
                return fileDownloadInfo;
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int c)
    {
        return getValueAt(0, c).getClass();
    }

    @Override
    public void setValueAt(Object value, int row, int col)
    {
        // This only makes sense for the button column
        assert (col == DOWNLOAD_STATUS_COLUMN);

        FileDownloadInfo fileDownloadInfo = (FileDownloadInfo) value;
        if (null == fileDownloadInfo)
        {
            return;
        }

        // Only start downloading if the file hasn't been downloaded yet
        FileDownloadInfo.STATUS status = fileDownloadInfo.getStatus();
        if ((status != FileDownloadInfo.STATUS.TO_DOWNLOAD)
                && (status != FileDownloadInfo.STATUS.FAILED))
        {
            return;
        }

        // update the status of the info and start the download
        queueDownloadOfFile(fileDownloadInfo);

        fireTableCellUpdated(row, col);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        // only the last column is editable
        if (columnIndex != DOWNLOAD_STATUS_COLUMN)
            return false;

        // and that, only when the file is not currently downloading or finished
        FileDownloadInfo fileDownloadInfo = fileDownloadInfos.get(rowIndex);
        if (null == fileDownloadInfo)
            return false;
        FileDownloadInfo.STATUS status = fileDownloadInfo.getStatus();
        return (status == FileDownloadInfo.STATUS.TO_DOWNLOAD)
                || (status == FileDownloadInfo.STATUS.FAILED);
    }

    /**
     * Start a file download in a separate thread.
     */
    private void queueDownloadOfFile(FileDownloadInfo fileDownloadInfo)
    {
        fileDownloadInfo.setStatus(STATUS.QUEUED);
        FileDownloadOperation op =
                new FileDownloadOperation(this, fileDownloadInfo, downloadDirectory, passphrase);
        executor.submit(op);
    }

    public JFrame getMainWindow()
    {
        return mainWindow;
    }

}
