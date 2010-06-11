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

import static ch.systemsx.cisd.common.utilities.SystemTimeProvider.SYSTEM_TIME_PROVIDER;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.Spring;
import javax.swing.SpringLayout;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import ch.systemsx.cisd.cifex.rpc.client.FileWithOverrideName;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class FileUploadClient extends AbstractSwingGUI
{
    private static final int LINE_HEIGHT = 30;

    private static final int INPUT_WIDTH = 130;

    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private static final String TITLE = "CIFEX Uploader";

    private static final String REMOVE_FROM_TABLE_MENU_ITEM = "Remove selected files from table";

    public static void main(String[] args)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        setLookAndFeelToMetal();

        try
        {
            CIFEXCommunicationState commState = new CIFEXCommunicationState(args);
            FileUploadClient newMe = new FileUploadClient(commState, SYSTEM_TIME_PROVIDER);
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

    private final ICIFEXUploader uploader;

    private final FileDialog fileDialog;

    private JButton uploadButton;

    private JButton cancelButton;

    private JButton addButton;

    private JPopupMenu popupMenu;

    private UploadTableModel tableModel;

    private JTextArea recipientsTextArea;

    private JTextArea commentTextArea;

    FileUploadClient(final CIFEXCommunicationState commState, final ITimeProvider timeProvider)
    {
        // save and create local state
        super(commState);

        this.uploader = cifex.createUploader(sessionId);

        tableModel = new UploadTableModel(uploader, timeProvider);
        createGUI();

        fileDialog = new FileDialog(getWindowFrame());
        initializeFileDialog();

        addProgressListener();
    }

    private void initializeFileDialog()
    {
        fileDialog.setDirectory(".");
        fileDialog.setModal(true);
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setTitle("Select file to upload");
    }

    private void addProgressListener()
    {
        uploader.addProgressListener(createErrorLogListener());
        uploader.addProgressListener(createFinishLogListenerWhoExitsProgram());
    }

    private IProgressListener createFinishLogListenerWhoExitsProgram()
    {
        return new IProgressListener()
            {
                public void finished(boolean successful)
                {
                    setEnableStateOfButtons(true);
                    if (successful)
                    {
                        JOptionPane.showMessageDialog(getWindowFrame(),
                                "Uploading finished. Please refresh CIFEX in your Web browser.");
                        System.exit(0);
                    } else
                    {
                        JOptionPane.showMessageDialog(getWindowFrame(),
                                "Operation did not complete successfully. "
                                        + "Check the status in the CIFEX Web GUI "
                                        + "(Uploaded Files > Edit Sharing)");
                    }
                }

                public void start(File file, long fileSize, Long fileIdOrNull)
                {
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                }

                public void exceptionOccured(Throwable throwable)
                {
                }

                public void warningOccured(String warningMessage)
                {
                }
            };
    }

    void show()
    {
        getWindowFrame().setVisible(true);
    }

    private void createGUI()
    {
        // To add borders, don't put the GUI panel directly in the window, instead embed it in a
        // panel.
        JFrame window = getWindowFrame();

        JLabel spacer;
        JPanel panel = createGUIPanel();
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
        spacer.setPreferredSize(new Dimension(600, 15));
        window.add(spacer, BorderLayout.SOUTH);

        // Add a small gap at the top of the frame, to the GUI doesn't look too constrained
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(600, 15));
        window.add(spacer, BorderLayout.NORTH);

        window.setBounds(200, 200, 770, 300);
        window.setVisible(true);

    }

    private JPanel createGUIPanel()
    {
        // The panel for the GUI
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 5));

        // The panel for the upload form
        JPanel fileListPanel = createFileListPanel();

        // The buttons the implement the form actions (upload, cancel)
        JPanel actionButtonsPanel = createActionButtonsPanel();

        panel.add(fileListPanel, BorderLayout.CENTER);
        panel.add(actionButtonsPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Create the panel with the information required for uploading files to CIFEX (file path,
     * recipients, and comments).
     */
    private JPanel createFileListPanel()
    {
        JPanel panel = new JPanel(new SpringLayout());

        JLabel label;
        // Recipients label and input
        label = new JLabel("Recipients", JLabel.TRAILING);
        recipientsTextArea = new JTextArea();
        recipientsTextArea.setPreferredSize(new Dimension(INPUT_WIDTH, LINE_HEIGHT));
        label.setLabelFor(recipientsTextArea);
        panel.add(label);
        panel.add(new JScrollPane(recipientsTextArea));

        // Comment label and input
        label = new JLabel("Comment", JLabel.TRAILING);
        commentTextArea = new JTextArea();
        commentTextArea.setPreferredSize(new Dimension(INPUT_WIDTH, LINE_HEIGHT));
        label.setLabelFor(commentTextArea);
        panel.add(label);
        panel.add(new JScrollPane(commentTextArea));

        // Files label and table
        label = new JLabel("Files", JLabel.TRAILING);
        final JTable table = new JTable(tableModel)
            {
                private static final long serialVersionUID = 1L;

                @Override
                public String getToolTipText(MouseEvent evt)
                {
                    int index = rowAtPoint(evt.getPoint());
                    File file = tableModel.getFileItem(index).getFile();
                    String size = FileUtilities.byteCountToDisplaySize(file.length());
                    return file.getAbsolutePath() + " (" + size + ")";
                }
            };
        table.setColumnModel(createTableColumnModel());
        popupMenu = createPopupMenu(table);

        JScrollPane filesPane = new JScrollPane(table);
        filesPane.setMinimumSize(new Dimension(INPUT_WIDTH, LINE_HEIGHT * 2));
        filesPane.setPreferredSize(new Dimension(INPUT_WIDTH, LINE_HEIGHT * 6));
        label.setLabelFor(filesPane);
        panel.add(label);
        panel.add(filesPane);

        SpringUtilities.makeCompactGrid(panel, 3, 2, 5, 5, 5, 5);

        JPanel fileListPanel = new JPanel();
        fileListPanel.setLayout(new BorderLayout());
        JPanel buttonsPanel = createFileListButtonsPanel();
        fileListPanel.add(panel, BorderLayout.CENTER);
        fileListPanel.add(buttonsPanel, BorderLayout.PAGE_END);

        return fileListPanel;
    }

    private JPanel createFileListButtonsPanel()
    {
        JPanel buttonsPanel = new JPanel();
        BoxLayout layout = new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS);
        buttonsPanel.setLayout(layout);

        addButton = new JButton("Add File");
        addButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooseAndAddFile();
                }
            });
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(addButton);

        return buttonsPanel;
    }

    private JPanel createActionButtonsPanel()
    {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        uploadButton = createUploadButton(tableModel);
        cancelButton = new JButton("Stop");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    cancel();
                }
            });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(uploadButton);

        return buttonPanel;
    }

    private JButton createUploadButton(final UploadTableModel fileListModel)
    {
        final JButton button = new JButton("Upload");
        button.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    setEnableStateOfButtons(false);
                    new Thread(new Runnable()
                        {
                            public void run()
                            {
                                List<FileWithOverrideName> files =
                                        wrapFiles(fileListModel.getFiles());
                                String recipients = recipientsTextArea.getText();
                                String comment = commentTextArea.getText();
                                uploader.upload(files, recipients, comment);
                            }
                        }).start();
                }
            });
        button.setEnabled(false);
        fileListModel.addTableModelListener(new TableModelListener()
            {
                public void tableChanged(TableModelEvent e)
                {
                    if (e.getType() != TableModelEvent.UPDATE)
                    {
                        button.setEnabled(fileListModel.getFiles().size() > 0);
                    }
                }
            });
        return button;
    }

    private List<FileWithOverrideName> wrapFiles(List<File> files)
    {
        final List<FileWithOverrideName> filesWithOverrideName =
                new ArrayList<FileWithOverrideName>(files.size());
        for (File file : files)
        {
            filesWithOverrideName.add(new FileWithOverrideName(file, null));
        }
        return filesWithOverrideName;
    }

    private JPopupMenu createPopupMenu(final JTable table)
    {
        final JPopupMenu menu = new JPopupMenu();
        final JMenuItem menuItem = new JMenuItem(REMOVE_FROM_TABLE_MENU_ITEM);
        menuItem.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    final JMenuItem item = (JMenuItem) e.getSource();
                    if (REMOVE_FROM_TABLE_MENU_ITEM.equals(item.getText()))
                    {
                        final int oldRowCount = tableModel.getRowCount();
                        tableModel.removeRows(table.getSelectedRows());
                        tableModel.fireTableRowsDeleted(0, oldRowCount);
                    }
                }
            });
        menu.add(menuItem);
        table.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    showPopup(e);
                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e)
                {
                    if (table.getSelectedRows().length > 0 && menu.isEnabled()
                            && e.isPopupTrigger())
                    {
                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            });
        return menu;
    }

    private DefaultTableColumnModel createTableColumnModel()
    {
        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        TableColumn column = new TableColumn(0, 150);
        column.setHeaderValue("File");
        columnModel.addColumn(column);
        column = new TableColumn(1, 50);
        column.setHeaderValue("Status");
        column.setCellRenderer(new UploadTableCellRenderer());
        columnModel.addColumn(column);
        return columnModel;
    }

    private void chooseAndAddFile()
    {
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName != null)
        {
            File file = new File(new File(fileDialog.getDirectory()), fileName);
            try
            {
                file = file.getCanonicalFile();
            } catch (IOException ex)
            {
                JOptionPane.showMessageDialog(getWindowFrame(), "Problem canonicalizing file:\n"
                        + file.getAbsolutePath());

                return;
            }
            if (file.exists() == false)
            {
                JOptionPane.showMessageDialog(getWindowFrame(), "File does not exists:\n"
                        + file.getAbsolutePath());
                return;
            }
            if (file.isDirectory())
            {
                JOptionPane.showMessageDialog(getWindowFrame(), "Can't upload whole directories.");
                return;
            }
            if (tableModel.alreadyAdded(file))
            {
                JOptionPane.showMessageDialog(getWindowFrame(), "File already added:\n"
                        + file.getAbsolutePath());
                return;
            }
            tableModel.addFile(file);
        }
    }

    private void setEnableStateOfButtons(boolean enable)
    {
        if (addButton != null)
        {
            addButton.setEnabled(enable);
        }
        if (uploadButton != null)
        {
            uploadButton.setEnabled(enable);
        }
        if (cancelButton != null)
        {
            cancelButton.setEnabled(enable == false);
        }
        if (popupMenu != null)
        {
            popupMenu.setEnabled(enable);
        }
    }

    @Override
    protected boolean cancel()
    {
        if (uploader.isInProgress() == false)
        {
            return true;
        }
        int answer =
                JOptionPane.showConfirmDialog(getWindowFrame(),
                        "Do you really want to cancel uploading?");
        if (answer == JOptionPane.YES_OPTION)
        {
            uploader.cancel();
            return true;
        }
        return false;
    }

    @Override
    protected final String getTitle()
    {
        return TITLE;
    }
}

/**
 * A 1.4 file that provides utility methods for creating form- or grid-style layouts with
 * SpringLayout. These utilities are used by several programs, such as SpringBox and
 * SpringCompactGrid.
 */
class SpringUtilities
{
    /**
     * A debugging utility that prints to stdout the component's minimum, preferred, and maximum
     * sizes.
     */
    public static void printSizes(Component c)
    {
        System.out.println("minimumSize = " + c.getMinimumSize());
        System.out.println("preferredSize = " + c.getPreferredSize());
        System.out.println("maximumSize = " + c.getMaximumSize());
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in a
     * grid. Each component is as big as the maximum preferred width and height of the components.
     * The parent is made just big enough to fit them all.
     * 
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeGrid(Container parent, int rows, int cols, int initialX, int initialY,
            int xPad, int yPad)
    {
        SpringLayout layout;
        try
        {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc)
        {
            System.err.println("The first argument to makeGrid must use SpringLayout.");
            return;
        }

        Spring xPadSpring = Spring.constant(xPad);
        Spring yPadSpring = Spring.constant(yPad);
        Spring initialXSpring = Spring.constant(initialX);
        Spring initialYSpring = Spring.constant(initialY);
        int max = rows * cols;

        // Calculate Springs that are the max of the width/height so that all
        // cells have the same size.
        Spring maxWidthSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
        Spring maxHeightSpring = layout.getConstraints(parent.getComponent(0)).getWidth();
        for (int i = 1; i < max; i++)
        {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

            maxWidthSpring = Spring.max(maxWidthSpring, cons.getWidth());
            maxHeightSpring = Spring.max(maxHeightSpring, cons.getHeight());
        }

        // Apply the new width/height Spring. This forces all the
        // components to have the same size.
        for (int i = 0; i < max; i++)
        {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));

            cons.setWidth(maxWidthSpring);
            cons.setHeight(maxHeightSpring);
        }

        // Then adjust the x/y constraints of all the cells so that they
        // are aligned in a grid.
        SpringLayout.Constraints lastCons = null;
        SpringLayout.Constraints lastRowCons = null;
        for (int i = 0; i < max; i++)
        {
            SpringLayout.Constraints cons = layout.getConstraints(parent.getComponent(i));
            if (i % cols == 0)
            { // start of new row
                lastRowCons = lastCons;
                cons.setX(initialXSpring);
            } else
            { // x position depends on previous component
                cons.setX(Spring.sum(lastCons.getConstraint(SpringLayout.EAST), xPadSpring));
            }

            if (i / cols == 0)
            { // first row
                cons.setY(initialYSpring);
            } else
            { // y position depends on previous row
                cons.setY(Spring.sum(lastRowCons.getConstraint(SpringLayout.SOUTH), yPadSpring));
            }
            lastCons = cons;
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, Spring.sum(Spring.constant(yPad), lastCons
                .getConstraint(SpringLayout.SOUTH)));
        pCons.setConstraint(SpringLayout.EAST, Spring.sum(Spring.constant(xPad), lastCons
                .getConstraint(SpringLayout.EAST)));
    }

    /* Used by makeCompactGrid. */
    private static SpringLayout.Constraints getConstraintsForCell(int row, int col,
            Container parent, int cols)
    {
        SpringLayout layout = (SpringLayout) parent.getLayout();
        Component c = parent.getComponent(row * cols + col);
        return layout.getConstraints(c);
    }

    /**
     * Aligns the first <code>rows</code> * <code>cols</code> components of <code>parent</code> in a
     * grid. Each component in a column is as wide as the maximum preferred width of the components
     * in that column; height is similarly determined for each row. The parent is made just big
     * enough to fit them all.
     * 
     * @param rows number of rows
     * @param cols number of columns
     * @param initialX x location to start the grid at
     * @param initialY y location to start the grid at
     * @param xPad x padding between cells
     * @param yPad y padding between cells
     */
    public static void makeCompactGrid(Container parent, int rows, int cols, int initialX,
            int initialY, int xPad, int yPad)
    {
        SpringLayout layout;
        try
        {
            layout = (SpringLayout) parent.getLayout();
        } catch (ClassCastException exc)
        {
            System.err.println("The first argument to makeCompactGrid must use SpringLayout.");
            return;
        }

        // Align all cells in each column and make them the same width.
        Spring x = Spring.constant(initialX);
        for (int c = 0; c < cols; c++)
        {
            Spring width = Spring.constant(0);
            for (int r = 0; r < rows; r++)
            {
                width = Spring.max(width, getConstraintsForCell(r, c, parent, cols).getWidth());
            }
            for (int r = 0; r < rows; r++)
            {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setX(x);
                constraints.setWidth(width);
            }
            x = Spring.sum(x, Spring.sum(width, Spring.constant(xPad)));
        }

        // Align all cells in each row and make them the same height.
        Spring y = Spring.constant(initialY);
        for (int r = 0; r < rows; r++)
        {
            Spring height = Spring.constant(0);
            for (int c = 0; c < cols; c++)
            {
                height = Spring.max(height, getConstraintsForCell(r, c, parent, cols).getHeight());
            }
            for (int c = 0; c < cols; c++)
            {
                SpringLayout.Constraints constraints = getConstraintsForCell(r, c, parent, cols);
                constraints.setY(y);
                constraints.setHeight(height);
            }
            y = Spring.sum(y, Spring.sum(height, Spring.constant(yPad)));
        }

        // Set the parent's size.
        SpringLayout.Constraints pCons = layout.getConstraints(parent);
        pCons.setConstraint(SpringLayout.SOUTH, y);
        pCons.setConstraint(SpringLayout.EAST, x);
    }
}
