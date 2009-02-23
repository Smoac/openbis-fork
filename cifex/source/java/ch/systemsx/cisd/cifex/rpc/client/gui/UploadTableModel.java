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

import ch.systemsx.cisd.cifex.rpc.client.FileItem;
import ch.systemsx.cisd.cifex.rpc.client.FileItemStatus;
import ch.systemsx.cisd.cifex.rpc.client.Uploader;
import ch.systemsx.cisd.common.utilities.ITimeProvider;


final class UploadTableModel extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;
    private static final long MB = 1024L * 1024L;

    private final int maxUploadSizeInMB;
    private final ITimeProvider timeProvider;
    
    private List<FileItem> fileItems = new ArrayList<FileItem>();
    private FileItem currentFileToBeUploaded;
    
    UploadTableModel(Uploader uploader, int maxUploadSizeInMB, ITimeProvider timeProvider)
    {
        this.maxUploadSizeInMB = maxUploadSizeInMB;
        this.timeProvider = timeProvider;
        uploader.addUploadListener(new IUploadListener()
            {
                public void uploadingStarted(File file, long fileSize)
                {
                    currentFileToBeUploaded = tryToFind(file);
                    setNumberOfBytes(0);
                    fireChanged();
                }
                
                public void uploadingProgress(int percentage, long numberOfBytes)
                {
                    setNumberOfBytes(numberOfBytes);
                    fireChanged();
                }
                
                public void uploadingFinished(boolean successful)
                {
                    if (currentFileToBeUploaded != null)
                    {
                        if (successful)
                        {
                            setNumberOfBytes(currentFileToBeUploaded.getLength());
                            currentFileToBeUploaded.setStatus(FileItemStatus.FINISHED);
                        } else
                        {
                            currentFileToBeUploaded.setStatus(FileItemStatus.ABORTED);
                        }
                        fireChanged();
                    }
                }
                
                public void reset()
                {
                    if (currentFileToBeUploaded != null)
                    {
                        currentFileToBeUploaded.setStatus(FileItemStatus.NOT_STARTED);
                        fireChanged();
                    }
                }

                public void fileUploaded()
                {
                    uploadingFinished(true);
                }
                
                public void exceptionOccured(Throwable throwable)
                {
                }

                private FileItem tryToFind(File file)
                {
                    for (FileItem fileItem : fileItems)
                    {
                        if (fileItem.getFile().equals(file))
                        {
                            return fileItem;
                        }
                    }
                    return null;
                }
        
                private void setNumberOfBytes(long numberOfBytes)
                {
                    if (currentFileToBeUploaded != null)
                    {
                        currentFileToBeUploaded.setNumberOfBytesUploaded(numberOfBytes);
                        currentFileToBeUploaded.setStatus(FileItemStatus.UPLOADING);
                    }
                }

                private void fireChanged()
                {
                    if (currentFileToBeUploaded != null)
                    {
                        int index = fileItems.indexOf(currentFileToBeUploaded);
                        fireTableRowsUpdated(index, index);
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
    
    long calculateFreeUploadSpace()
    {
        long result = maxUploadSizeInMB * MB;
        for (FileItem fileItem : fileItems)
        {
            result -= fileItem.getFile().length();
        }
        return result;
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

}