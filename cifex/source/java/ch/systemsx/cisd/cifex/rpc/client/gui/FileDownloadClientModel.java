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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import ch.systemsx.cisd.base.namedthread.NamingThreadPoolExecutor;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXDownloader;
import ch.systemsx.cisd.cifex.rpc.client.PersistenceStore;
import ch.systemsx.cisd.cifex.rpc.client.TransmissionSpeedCalculator;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClient.IDecryptionChecker;
import ch.systemsx.cisd.cifex.rpc.client.gui.FileDownloadClientModel.FileDownloadInfo.Status;
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

    // Constants for column order
    static final int FILE_DETAILS_COLUMN = 0;

    static final int SENDER_COLUMN = 1;

    static final int COMMENT_COLUMN = 2;

    static final int SENT_DATE_COLUMN = 3;

    static final int EXPIRATION_DATE_COLUMN = 4;

    static final int DOWNLOAD_STATUS_COLUMN = 5;

    private static ExecutorService executor =
            new NamingThreadPoolExecutor("File download", 1, 1, 0, TimeUnit.SECONDS,
                    new LinkedBlockingQueue<Runnable>()).daemonize();

    private final JFrame mainWindow;

    private JTable table;

    private final ICIFEXComponent cifex;

    private final String sessionId;

    private final ICIFEXDownloader downloader;

    private final IDecryptionChecker decryptionChecker;

    private final ITimeProvider timeProvider;

    private final ArrayList<FileDownloadInfo> fileDownloadInfos = new ArrayList<FileDownloadInfo>();

    private FileDownloadInfo currentlyDownloadingFile;

    private File downloadDirectory;

    private String passphrase = "";

    private boolean deleteEncryptedFileAfterSuccessfulDecryption;

    protected int sortColumnIndex = 0;

    protected boolean sortAscending = true;

    /**
     * FileDownloadInfo is a mixture of FileInfoDTO, which encapsulates information about files
     * available for download from CIFEX, and downloading progress state.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    static class FileDownloadInfo
    {
        static enum Status
        {
            TO_DOWNLOAD, QUEUED_FOR_DOWNLOAD, QUEUED_FOR_DECRYPTION, DOWNLOADING, DECRYPTING,
            COMPLETED_DOWNLOAD, COMPLETED_DOWNLOAD_AND_DECRYPTION, FAILED, STALLED
        }

        private final FileInfoDTO fileInfoDTO;

        private final TransmissionSpeedCalculator transmissionSpeedCalculator;

        private File file;

        private int percentageDownloaded;

        private long numberOfBytesDownloaded;

        private Status status;

        FileDownloadInfo(FileInfoDTO fileInfo, ITimeProvider timeProvider)
        {
            this.fileInfoDTO = fileInfo;
            transmissionSpeedCalculator = new TransmissionSpeedCalculator(timeProvider);
            percentageDownloaded = 0;
            numberOfBytesDownloaded = 0;
            setStatus(Status.TO_DOWNLOAD);
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
            setStatus(Status.DOWNLOADING);
            int transmittedSinceLastUpdate = (int) (numberOfBytes - numberOfBytesDownloaded);
            percentageDownloaded = percent;
            numberOfBytesDownloaded = numberOfBytes;
            transmissionSpeedCalculator
                    .noteTransmittedBytesSinceLastUpdate(transmittedSinceLastUpdate);
        }

        public void setStatus(Status status)
        {
            this.status = status;
        }

        public Status getStatus()
        {
            return status;
        }
    }

    class ColumnSortingListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            TableColumnModel colModel = table.getColumnModel();
            int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
            int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

            if (modelIndex < 0)
            {
                return;
            }
            if (sortColumnIndex == modelIndex)
            {
                sortAscending = (sortAscending == false);
            } else
            {
                sortColumnIndex = modelIndex;
                sortAscending = true;
            }

            for (int i = 0; i < getColumnCount(); i++)
            {
                TableColumn column = colModel.getColumn(i);
                column.setHeaderValue(getColumnName(column.getModelIndex()));
            }
            table.getTableHeader().repaint();

            Collections.sort(fileDownloadInfos, new FileDownloadInfoComparator());

            table.tableChanged(new TableModelEvent(FileDownloadClientModel.this));
            table.repaint();
        }
    }

    class FileDownloadInfoComparator implements Comparator<FileDownloadInfo>
    {
        public int compare(FileDownloadInfo info1, FileDownloadInfo info2)
        {
            int result = 0;
            switch (sortColumnIndex)
            {
                case FILE_DETAILS_COLUMN:
                    result =
                            info1.getFileInfoDTO().getName().compareTo(
                                    info2.getFileInfoDTO().getName());
                    break;
                case SENDER_COLUMN:
                    result =
                            info1.getFileInfoDTO().getOwner().toString().compareTo(
                                    info2.getFileInfoDTO().getOwner().toString());
                    break;
                case COMMENT_COLUMN:
                    result =
                            info1.getFileInfoDTO().getComment().compareTo(
                                    info2.getFileInfoDTO().getComment());
                    break;
                case SENT_DATE_COLUMN:
                    result =
                            info1.getFileInfoDTO().getRegistrationDate().compareTo(
                                    info2.getFileInfoDTO().getRegistrationDate());
                    break;
                case EXPIRATION_DATE_COLUMN:
                    result =
                            info1.getFileInfoDTO().getExpirationDate().compareTo(
                                    info2.getFileInfoDTO().getExpirationDate());
                    break;
                case DOWNLOAD_STATUS_COLUMN:
                    result = info1.getStatus().compareTo(info2.getStatus());
                    break;
            }
            return sortAscending ? result : -result;
        }
    }

    FileDownloadClientModel(FileDownloadClient downloadClient, JFrame mainWindow,
            ITimeProvider timeProvider)
    {
        this.cifex = downloadClient.getCifex();
        this.sessionId = downloadClient.getSessionId();
        this.downloader = downloadClient.getDownloader();
        this.decryptionChecker = downloadClient.getDecryptionChecker();
        this.mainWindow = mainWindow;
        this.timeProvider = timeProvider;
        this.downloadDirectory = PersistenceStore.getWorkingDirectory();

        addProgessListener();

        updateDownloadableFiles();
    }

    public void setTable(JTable table)
    {
        this.table = table;
        table.getTableHeader().addMouseListener(new ColumnSortingListener());
    }

    public File getDownloadDirectory()
    {
        return downloadDirectory;
    }

    public void setDownloadDirectory(File downloadDirectory)
    {
        this.downloadDirectory = downloadDirectory;
    }

    public void setPassphraseAndEncryptedFileDeletion(String passphrase,
            boolean deleteEncryptedFileAfterSuccessfulDecryption)
    {
        this.passphrase = passphrase;
        this.deleteEncryptedFileAfterSuccessfulDecryption =
                deleteEncryptedFileAfterSuccessfulDecryption;
        fireTableDataChanged();
    }

    public ICIFEXDownloader getDownloader()
    {
        return downloader;
    }

    private void addProgessListener()
    {
        downloader.addProgressListener(new IProgressListener()
            {
                public void start(File file, String operationname, long fileSize, Long fileIdOrNull)
                {
                    currentlyDownloadingFile = tryToFindDownloadInfoForFile(fileIdOrNull);
                    if (currentlyDownloadingFile != null)
                    {
                        currentlyDownloadingFile.updateProgress(0, 0);
                        fireChanged(null);
                    }
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                    if (currentlyDownloadingFile != null)
                    {
                        currentlyDownloadingFile.updateProgress(percentage, numberOfBytes);
                        fireChanged(null);
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
                            if (passphrase.length() > 0)
                            {
                                currentlyDownloadingFile
                                        .setStatus(FileDownloadInfo.Status.DECRYPTING);
                                fireChanged(FileDownloadInfo.Status.DECRYPTING);
                            } else
                            {
                                currentlyDownloadingFile
                                        .setStatus(FileDownloadInfo.Status.COMPLETED_DOWNLOAD);
                                fireChanged(FileDownloadInfo.Status.COMPLETED_DOWNLOAD);
                            }
                        } else
                        {
                            currentlyDownloadingFile.setStatus(FileDownloadInfo.Status.FAILED);
                            fireChanged(FileDownloadInfo.Status.FAILED);
                        }
                    }
                }

                public void exceptionOccured(Throwable throwable)
                {
                }

                public void warningOccured(String warningMessage)
                {
                    currentlyDownloadingFile.setStatus(FileDownloadInfo.Status.STALLED);
                }

            });
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

    void resetCurrentlyDownloadingFile()
    {
        currentlyDownloadingFile = null;
    }

    void fireChanged(Long fileIdOrNull, FileDownloadInfo.Status statusOrNull)
    {
        currentlyDownloadingFile = tryToFindDownloadInfoForFile(fileIdOrNull);
        fireChanged(statusOrNull);
    }

    void fireChanged(FileDownloadInfo.Status statusOrNull)
    {
        if (currentlyDownloadingFile != null)
        {
            if (statusOrNull != null)
            {
                currentlyDownloadingFile.setStatus(statusOrNull);
            }
            int index = fileDownloadInfos.indexOf(currentlyDownloadingFile);
            fireTableRowsUpdated(index, index);
        }
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
        Collections.sort(fileDownloadInfos, new FileDownloadInfoComparator());

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
    public String getColumnName(int columnIndex)
    {
        String name = "";
        if (columnIndex == sortColumnIndex)
        {
            name = sortAscending ? "\u25b2" : "\u25bc";
        }
        switch (columnIndex)
        {
            case FILE_DETAILS_COLUMN:
                name += " File";
                break;
            case SENDER_COLUMN:
                name += " From";
                break;
            case COMMENT_COLUMN:
                name += " Comment";
                break;
            case SENT_DATE_COLUMN:
                name += " Sent Date";
                break;
            case EXPIRATION_DATE_COLUMN:
                name += " Download Before";
                break;
            case DOWNLOAD_STATUS_COLUMN:
                break;
        }

        return name;
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
        final Object columnValue = getValueAt(0, c);
        if (columnValue == null)
        {
            return super.getColumnClass(c);
        } else
        {
            return columnValue.getClass();
        }
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
        FileDownloadInfo.Status status = fileDownloadInfo.getStatus();
        if ((status != FileDownloadInfo.Status.TO_DOWNLOAD)
                && (status != FileDownloadInfo.Status.FAILED)
                && (status != FileDownloadInfo.Status.COMPLETED_DOWNLOAD))
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
        {
            return false;
        }

        // and that, only when the file is not currently downloading or finished
        FileDownloadInfo fileDownloadInfo = fileDownloadInfos.get(rowIndex);
        if (null == fileDownloadInfo)
        {
            return false;
        }
        FileDownloadInfo.Status status = fileDownloadInfo.getStatus();
        return (status == FileDownloadInfo.Status.TO_DOWNLOAD)
                || (status == FileDownloadInfo.Status.FAILED)
                || (status == FileDownloadInfo.Status.COMPLETED_DOWNLOAD);
    }

    /**
     * Start a file download in a separate thread.
     */
    private void queueDownloadOfFile(FileDownloadInfo fileDownloadInfo)
    {
        if (fileDownloadInfo.getStatus() == Status.COMPLETED_DOWNLOAD)
        {
            fileDownloadInfo.setStatus(Status.QUEUED_FOR_DECRYPTION);
        } else
        {
            fileDownloadInfo.setStatus(Status.QUEUED_FOR_DOWNLOAD);
        }
        FileDownloadOperation op =
                new FileDownloadOperation(this, fileDownloadInfo, downloadDirectory, passphrase,
                        deleteEncryptedFileAfterSuccessfulDecryption);
        executor.submit(op);
    }

    public JFrame getMainWindow()
    {
        return mainWindow;
    }

    public IDecryptionChecker getDecryptionChecker()
    {
        return decryptionChecker;
    }
}
