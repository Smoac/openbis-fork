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
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.commons.lang.ArrayUtils;

import ch.systemsx.cisd.cifex.rpc.client.FileItem;
import ch.systemsx.cisd.cifex.rpc.client.FileItemStatus;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

final class UploadTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;

    private final ITimeProvider timeProvider;

    private List<FileItem> fileItems = new ArrayList<FileItem>();

    private FileItem currentFileToBeUploaded;

    UploadTableModel(ICIFEXUploader uploader, ITimeProvider timeProvider)
    {
        this.timeProvider = timeProvider;
        uploader.addProgressListener(new IProgressListener()
            {
                public void start(File file, String operationName, long fileSize, Long fileIdOrNull)
                {
                    currentFileToBeUploaded = tryToFind(file);
                    setNumberOfBytes(0);
                    fireChanged(null);
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                    if (percentage == 100)
                    {
                        if (currentFileToBeUploaded != null)
                        {
                            setNumberOfBytes(currentFileToBeUploaded.getLength());
                            currentFileToBeUploaded.setStatus(FileItemStatus.FINISHED);
                        }
                    } else
                    {
                        setNumberOfBytes(numberOfBytes);
                    }
                    fireChanged(null);
                }

                public void finished(boolean successful)
                {
                    if (currentFileToBeUploaded != null)
                    {
                        if (successful)
                        {
                            setNumberOfBytes(currentFileToBeUploaded.getLength());
                            fireChanged(FileItemStatus.FINISHED);
                        } else
                        {
                            fireChanged(FileItemStatus.ABORTED);
                        }
                    }
                }

                public void exceptionOccured(Throwable throwable)
                {
                    fireChanged(FileItemStatus.STALLED);
                }

                public void warningOccured(String warningMessage)
                {
                    fireChanged(FileItemStatus.STALLED);
                }

                private void setNumberOfBytes(long numberOfBytes)
                {
                    if (currentFileToBeUploaded != null)
                    {
                        currentFileToBeUploaded.setNumberOfBytesUploaded(numberOfBytes);
                    }
                }

            });
    }

    void addFile(File file)
    {
        int size = fileItems.size();
        fileItems.add(new FileItem(file, timeProvider));
        fireTableRowsInserted(size, size);
    }

    List<File> getFiles()
    {
        List<File> files = new ArrayList<File>();
        for (FileItem fileItem : fileItems)
        {
            files.add(fileItem.getFile());
        }
        return files;
    }

    FileItem getFileItem(int index)
    {
        return fileItems.get(index);
    }

    boolean alreadyAdded(File file)
    {
        for (FileItem fileItem : fileItems)
        {
            if (file.equals(fileItem.getFile()))
            {
                return true;
            }
        }
        return false;
    }

    public FileItem tryToFind(File file)
    {
        for (FileItem fileItem : fileItems)
        {
            if (fileItem.getUploadedFile().equals(file))
            {
                return fileItem;
            }
        }
        return null;
    }

    private void fireChanged(FileItemStatus statusOrNull)
    {
        if (currentFileToBeUploaded != null)
        {
            if (statusOrNull != null)
            {
                currentFileToBeUploaded.setStatus(statusOrNull);
            }
            int index = fileItems.indexOf(currentFileToBeUploaded);
            fireTableRowsUpdated(index, index);
        }
    }
    
    public FileItem fireChanged(File file, FileItemStatus statusOrNull)
    {
        currentFileToBeUploaded = tryToFind(file);
        fireChanged(statusOrNull);
        return currentFileToBeUploaded;
    }

    //
    // TableModel
    //
    
    public int getRowCount()
    {
        return fileItems.size();
    }

    public int getColumnCount()
    {
        return 2;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        FileItem fileItem = fileItems.get(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return fileItem.getFile().getName();
            case 1:
                return fileItem;
        }
        return null;
    }

    public void removeRows(int[] rows)
    {
        ArrayUtils.reverse(rows);
        for (int rowIndex : rows)
        {
            fileItems.remove(rowIndex);
        }
    }

}