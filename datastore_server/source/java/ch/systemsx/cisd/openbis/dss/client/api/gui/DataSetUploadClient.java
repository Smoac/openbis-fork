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

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * The GUI for the WebStart data set upload application. This class assembles the GUI and creates
 * the necessary contextual objects (like the IDssComponent) for interacting with the server.
 * Although {@link DataSetUploadTableModel} handles most of the logic, the contextual information
 * needs to be stored here so clean-up can be performed, e.g., if the user closes the window while
 * an upload is in progress.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class DataSetUploadClient extends AbstractSwingGUI
{
    public static final int BUTTON_WIDTH = 120;

    public static final int LABEL_WIDTH = 110;

    public static final int BUTTON_HEIGHT = 30;

    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private static final String TITLE = "Data Set Uploader";

    /**
     * Read the arguments from the command line or WebStart and instantiates the GUI appropriately.
     */
    public static void main(String[] args)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        setLookAndFeelToMetal();

        try
        {
            DssCommunicationState commState = new DssCommunicationState(args);

            DataSetUploadClient newMe = new DataSetUploadClient(commState, SYSTEM_TIME_PROVIDER);
            newMe.show();
        } catch (RuntimeException ex)
        {
            final JFrame frame = new JFrame(TITLE);
            frame.setVisible(true);
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private final DataSetUploadClientModel clientModel;

    private final DataSetUploadTableModel tableModel;

    private final DataSetMetadataPanel metadataPanel;

    DataSetUploadClient(final DssCommunicationState commState, final ITimeProvider timeProvider)
    {
        // save and create local state
        super(commState);

        clientModel = new DataSetUploadClientModel(commState, timeProvider);
        metadataPanel = new DataSetMetadataPanel(clientModel, getWindowFrame());
        tableModel =
                new DataSetUploadTableModel(this, clientModel, metadataPanel, getWindowFrame());
        metadataPanel.setTableModel(tableModel);

        createGui();
        addProgressListener();
    }

    private void createGui()
    {
        JLabel spacer;
        final JFrame window = getWindowFrame();

        // Put everything into a panel which goes into the center of the frame
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 0;
        panel.add(createGlobalActionsPanel(), c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0.5;
        c.weighty = 0.5;
        panel.add(createFileListComponent(), c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0.5;
        c.weighty = 0.5;
        panel.add(metadataPanel, c);

        window.add(panel, BorderLayout.CENTER);

        // Add small gaps to the left and right of the frame, to give a bit of space
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(5, 5));
        window.add(spacer, BorderLayout.WEST);
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(5, 5));
        window.add(spacer, BorderLayout.EAST);

        // Add a small gap at the bottom of the frame, to the GUI doesn't look too constrained
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(15, 15));
        window.add(spacer, BorderLayout.SOUTH);
        window.pack();
        window.setLocationByPlatform(true);
        window.setVisible(true);
    }

    private Component createGlobalActionsPanel()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(50, BUTTON_HEIGHT));
        addButton.setToolTipText("Add a new data set");
        addButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    tableModel.addNewDataSet();
                }
            });

        JButton removeButton = new JButton("-");
        removeButton.setPreferredSize(new Dimension(50, BUTTON_HEIGHT));
        removeButton.setToolTipText("Remove the selected data set");
        removeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    tableModel.removeSelectedDataSet();
                }
            });

        JButton uploadAllButton = new JButton("Upload All");
        uploadAllButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        uploadAllButton.setToolTipText("Attach the data set to a sample.");
        uploadAllButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                }
            });

        panel.add(addButton);
        panel.add(removeButton);
        panel.add(Box.createHorizontalGlue());
        panel.add(uploadAllButton);

        return panel;
    }

    private JComponent createFileListComponent()
    {
        final JTable fileTable = new JTable(tableModel)
            {
                private static final long serialVersionUID = 1L;

                @Override
                public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
                {
                    final Component c = super.prepareRenderer(renderer, row, column);
                    final JComponent jc = (JComponent) c;
                    jc.setBorder(new MatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));

                    return c;
                }

            };
        fileTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
            {

                public void valueChanged(ListSelectionEvent e)
                {
                    // Notify the table model
                    ListSelectionModel selectionModel = (ListSelectionModel) e.getSource();
                    ArrayList<Integer> selectedIndices = new ArrayList<Integer>();
                    if (!selectionModel.isSelectionEmpty())
                    {
                        int minIndex = selectionModel.getMinSelectionIndex();
                        int maxIndex = selectionModel.getMaxSelectionIndex();
                        for (int i = minIndex; i <= maxIndex; i++)
                        {
                            if (selectionModel.isSelectedIndex(i))
                            {
                                selectedIndices.add(i);
                            }
                        }
                    }

                    tableModel.setSelectedIndices(selectedIndices);

                }
            });
        tableModel.setTable(fileTable);

        // We have two lines of text to display
        fileTable.setRowHeight(fileTable.getRowHeight() * 2);

        // Configure the table columns
        TableColumn column;
        column =
                fileTable.getColumnModel().getColumn(DataSetUploadTableModel.DATA_SET_OWNER_COLUMN);
        column.setPreferredWidth(320);

        column =
                fileTable.getColumnModel().getColumn(
                        DataSetUploadTableModel.DATA_SET_METADATA_COLUMN);
        column.setPreferredWidth(150);

        column = fileTable.getColumnModel().getColumn(DataSetUploadTableModel.DATA_SET_PATH_COLUMN);
        column.setPreferredWidth(200);

        column = fileTable.getColumnModel().getColumn(DataSetUploadTableModel.UPLOAD_STATUS_COLUMN);
        column.setPreferredWidth(150);
        column.setCellRenderer(new UploadStatusTableCellRenderer(tableModel));
        column.setCellEditor(new UploadStatusTableCellEditor(tableModel));
        JScrollPane scrollPane = new JScrollPane(fileTable);
        return scrollPane;
    }

    private void addProgressListener()
    {
    }

    /**
     * Display the GUI
     */
    private void show()
    {
        getWindowFrame().setVisible(true);
    }

    /**
     * @return true if it safe to quit or if the user says it is ok
     */
    @Override
    protected final boolean cancel()
    {

        int answer =
                JOptionPane.showConfirmDialog(getWindowFrame(),
                        "Do you really want to stop uploading?");
        if (answer == JOptionPane.YES_OPTION)
        {
            return true;
        }
        return false;
    }

    @Override
    protected String getTitle()
    {
        return TITLE;
    }
}
