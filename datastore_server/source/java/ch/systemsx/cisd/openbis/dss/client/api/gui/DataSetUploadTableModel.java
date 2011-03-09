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
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo;
import ch.systemsx.cisd.openbis.dss.client.api.gui.DataSetUploadClientModel.NewDataSetInfo.Status;

/**
 * The DataSetUploadClientModel manages the list of data sets to register, initiates uploads (which
 * run in a separate thread) and notifies the GUI of updates. It also coordinates the Metadata panel
 * to ensure the two are in sync.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetUploadTableModel extends AbstractTableModel
{

    private static final long serialVersionUID = 1L;

    // Constants for column order
    static final int DATA_SET_OWNER_COLUMN = 0;

    static final int DATA_SET_METADATA_COLUMN = 1;

    static final int DATA_SET_PATH_COLUMN = 2;

    static final int UPLOAD_STATUS_COLUMN = 3;

    private static ExecutorService executor = new NamingThreadPoolExecutor("Data Set Upload", 1, 1,
            0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>()).daemonize();

    private final JFrame mainWindow;

    private final DataSetUploadClientModel clientModel;

    private final DataSetMetadataPanel metadataPanel;

    private final ArrayList<DataSetUploadClientModel.NewDataSetInfo> newDataSetInfos =
            new ArrayList<DataSetUploadClientModel.NewDataSetInfo>();

    private JTable table;

    private int sortColumnIndex = 0;

    private boolean sortAscending = true;

    // Initialize to no row selected
    private int selectedRow = -1;

    /**
     * Internal class for triggering the sorting of columns.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class ColumnSortingListener extends MouseAdapter
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
            } else if (modelIndex == DATA_SET_METADATA_COLUMN)
            {
                // Don't offer sorting on metadata -- it doesn't make sense.
                return;
            } else if (sortColumnIndex == modelIndex)
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

            syncNewDataSetInfoListView();
        }
    }

    /**
     * Internal class for sorting new data set info objects.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private class NewDataSetInfoComparator implements Comparator<NewDataSetInfo>
    {
        public int compare(NewDataSetInfo info1, NewDataSetInfo info2)
        {
            int result = 0;
            switch (sortColumnIndex)
            {
                case DATA_SET_OWNER_COLUMN:
                    String identifier1 = info1.getNewDataSetBuilder().getDataSetOwnerIdentifier();
                    String identifier2 = info2.getNewDataSetBuilder().getDataSetOwnerIdentifier();
                    result = identifier1.compareTo(identifier2);
                    break;
                case DATA_SET_PATH_COLUMN:
                    File file1 = info1.getNewDataSetBuilder().getFile();
                    File file2 = info2.getNewDataSetBuilder().getFile();
                    String path1 = (null != file1) ? file1.getAbsolutePath() : "";
                    String path2 = (null != file2) ? file2.getAbsolutePath() : "";
                    result = path1.compareTo(path2);
                    break;
                case UPLOAD_STATUS_COLUMN:
                    result = info1.getStatus().compareTo(info2.getStatus());
                    break;
            }
            return sortAscending ? result : -result;
        }
    }

    DataSetUploadTableModel(DataSetUploadClient downloadClient,
            DataSetUploadClientModel clientModel, DataSetMetadataPanel metadataPanel,
            JFrame mainWindow)
    {
        this.mainWindow = mainWindow;
        this.clientModel = clientModel;
        this.metadataPanel = metadataPanel;
    }

    public void setTable(JTable table)
    {
        this.table = table;
        table.getTableHeader().addMouseListener(new ColumnSortingListener());
    }

    /**
     * Notification that the status of the newDataSetInfo has changed.
     * 
     * @param newDataSetInfo The object whose status has changed.
     * @param newStatus The new status.
     */
    void fireChanged(NewDataSetInfo newDataSetInfo, NewDataSetInfo.Status newStatus)
    {
        int index = newDataSetInfos.indexOf(newDataSetInfo);
        fireTableRowsUpdated(index, index);
    }

    void setSelectedIndices(ArrayList<Integer> selectedIndices)
    {
        if (selectedIndices.size() < 1)
        {
            selectNewDataSetInfo(null);
            return;
        }
        selectedRow = selectedIndices.get(0);
        NewDataSetInfo selectedDataSet = newDataSetInfos.get(selectedRow);
        selectNewDataSetInfo(selectedDataSet);
    }

    public int getColumnCount()
    {
        return 4;
    }

    public int getRowCount()
    {
        return newDataSetInfos.size();
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
            case DATA_SET_OWNER_COLUMN:
                name += " Owner";
                break;
            case DATA_SET_METADATA_COLUMN:
                name += " Metadata";
                break;
            case DATA_SET_PATH_COLUMN:
                name += " Path";
                break;
            case UPLOAD_STATUS_COLUMN:
                break;
        }

        return name;
    }

    public Object getValueAt(int rowIndex, int columnIndex)
    {
        NewDataSetInfo newDataSetInfo = newDataSetInfos.get(rowIndex);
        switch (columnIndex)
        {
            case DATA_SET_OWNER_COLUMN:
                return newDataSetInfo.getNewDataSetBuilder().getDataSetOwner();
            case DATA_SET_METADATA_COLUMN:
                return newDataSetInfo.getNewDataSetBuilder().getDataSetMetadata();
            case DATA_SET_PATH_COLUMN:
                return newDataSetInfo.getNewDataSetBuilder().getFile();
            case UPLOAD_STATUS_COLUMN:
                return newDataSetInfo;
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
        assert (col == UPLOAD_STATUS_COLUMN);

        NewDataSetInfo newDataSetInfo = (NewDataSetInfo) value;
        if (null == newDataSetInfo)
        {
            return;
        }

        // Only start uploading if the file hasn't been upload yet
        NewDataSetInfo.Status status = newDataSetInfo.getStatus();
        if ((status != NewDataSetInfo.Status.TO_UPLOAD)
                && (status != NewDataSetInfo.Status.COMPLETED_UPLOAD)
                && (status != NewDataSetInfo.Status.FAILED))
        {
            return;
        }

        // update the status of the info and start the upload
        queueUploadOfDataSet(newDataSetInfo);

        fireTableCellUpdated(row, col);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex)
    {
        // only the last column is editable
        if (columnIndex != UPLOAD_STATUS_COLUMN)
        {
            return false;
        }

        // and that, only when the file is not currently downloading or finished
        NewDataSetInfo newDataSetInfo = newDataSetInfos.get(rowIndex);
        if (null == newDataSetInfo)
        {
            return false;
        }
        NewDataSetInfo.Status status = newDataSetInfo.getStatus();
        return (status == NewDataSetInfo.Status.TO_UPLOAD)
                || (status == NewDataSetInfo.Status.FAILED)
                || (status == NewDataSetInfo.Status.COMPLETED_UPLOAD);
    }

    public JFrame getMainWindow()
    {
        return mainWindow;
    }

    public void selectedRowChanged()
    {
        if (selectedRow < 0)
        {
            return;
        }
        fireTableRowsUpdated(selectedRow, selectedRow);
    }

    public void addNewDataSet()
    {
        NewDataSetInfo newlyCreated = clientModel.addNewDataSetInfo();
        syncNewDataSetInfos();
        selectNewDataSetInfo(newlyCreated);
    }

    public void removeSelectedDataSet()
    {
        assert false : "Not yet implemented";
    }

    /**
     * Synchronize the internal list of data set infos with the model's list.
     */
    private void syncNewDataSetInfos()
    {
        newDataSetInfos.clear();
        newDataSetInfos.addAll(clientModel.getNewDataSetInfos());
        syncNewDataSetInfoListView();
    }

    private void syncNewDataSetInfoListView()
    {
        Collections.sort(newDataSetInfos, new NewDataSetInfoComparator());

        table.tableChanged(new TableModelEvent(DataSetUploadTableModel.this));
        table.repaint();
    }

    private void selectNewDataSetInfo(NewDataSetInfo newDataSetInfo)
    {
        metadataPanel.setNewDataSetInfo(newDataSetInfo);
    }

    /**
     * Start a data set upload in a separate thread. Callers need to ensure that queuing makes
     * sense.
     */
    private void queueUploadOfDataSet(NewDataSetInfo newDataSetInfo)
    {
        newDataSetInfo.setStatus(Status.QUEUED_FOR_UPLOAD);
        DataSetUploadOperation op = new DataSetUploadOperation(this, clientModel, newDataSetInfo);
        executor.submit(op);
    }

    public NewDataSetInfo getSelectedNewDataSet()
    {
        return newDataSetInfos.get(selectedRow);
    }
}
