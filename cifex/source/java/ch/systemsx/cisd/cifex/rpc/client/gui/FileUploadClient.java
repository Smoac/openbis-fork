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
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import ch.systemsx.cisd.cifex.rpc.client.Uploader;
import ch.systemsx.cisd.cifex.shared.basic.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class FileUploadClient
{
    private static final String TITLE = "CIFEX Uploader";

    private static final String REMOVE_FROM_TABLE_MENU_ITEM = "Remove selected files from table";

    public static void main(String[] args)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException, EnvironmentFailureException
    {
        try
        {
            final int maxUloadSizeInMB;
            final String serviceURL = args[0];
            if (args.length == 3)
            {
                final String sessionId = args[1];
                maxUloadSizeInMB = Integer.parseInt(args[2]);
                new FileUploadClient(serviceURL, sessionId, maxUloadSizeInMB, SYSTEM_TIME_PROVIDER)
                        .show();
            } else if (args.length == 4)
            {
                final String userName = args[1];
                final String passwd = args[2];
                maxUloadSizeInMB = Integer.parseInt(args[3]);
                new FileUploadClient(serviceURL, userName, passwd, maxUloadSizeInMB,
                        SYSTEM_TIME_PROVIDER).show();
            } else
            {
                throw new UserFailureException("Wrong number of arguments.");
            }
        } catch (RuntimeException ex)
        {
            final JFrame frame = new JFrame(TITLE);
            frame.setVisible(true);
            JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        }
    }

    private final Uploader uploader;

    private final FileDialog fileDialog;

    private JFrame frame;

    private JButton uploadButton;

    private JButton cancelButton;
    
    private JButton addButton;

    private JPopupMenu popupMenu;

    FileUploadClient(String serviceURL, String sessionId, int maxUploadSizeInMB,
            ITimeProvider timeProvider) throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        this(new Uploader(serviceURL, sessionId, true), maxUploadSizeInMB, timeProvider);
    }

    FileUploadClient(String serviceURL, String userName, String passwd, int maxUploadSizeInMB,
            ITimeProvider timeProvider) throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        this(new Uploader(serviceURL, userName, passwd), maxUploadSizeInMB, timeProvider);
    }

    FileUploadClient(Uploader uploader, int maxUploadSizeInMB, ITimeProvider timeProvider)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException, EnvironmentFailureException
    {
        this.uploader = uploader;
        frame = new JFrame(TITLE);
        frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    logout();
                }
            });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        UploadTableModel model = new UploadTableModel(uploader, maxUploadSizeInMB, timeProvider);
        frame.add(createGUI(model), BorderLayout.CENTER);
        frame.setBounds(200, 200, 500, 400);
        frame.setVisible(true);
        fileDialog = new FileDialog(frame);
        fileDialog.setDirectory(".");
        fileDialog.setModal(true);
        fileDialog.setMode(FileDialog.LOAD);
        fileDialog.setTitle("Select file to upload");
        uploader.addProgressListener(new IUploadProgressListener()
            {
                public void start(File file, long fileSize)
                {
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                }

                public void finished(boolean successful)
                {
                    setEnableStateOfButtons(true);
                    if (successful)
                    {
                        JOptionPane.showMessageDialog(frame,
                                "Uploading finished. Please refresh CIFEX in your Web browser.");
                        System.exit(0);
                    } else
                    {
                        JOptionPane.showMessageDialog(frame, "Operation did not complete successfully. " +
                        		"Check the status in the CIFEX Web GUI (Uploaded Files > Edit Sharing)");
                    }
                }

                public void fileUploaded()
                {
                }

                public void exceptionOccured(Throwable throwable)
                {
                    String message;
                    if (throwable instanceof UserFailureException)
                    {
                        message = throwable.getMessage();
                    } else
                    {
                        message = "FATAL ERROR: " + throwable;
                    }
                    JOptionPane.showMessageDialog(frame, message, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                private String lastWarningMessage;
                
                public void warningOccured(String warningMessage)
                {
                    if (warningMessage.equals(lastWarningMessage) == false)
                    {
                        lastWarningMessage = warningMessage;
                        JOptionPane.showMessageDialog(frame, warningMessage, "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }

                public void reset()
                {
                }

            });
    }

    void show()
    {
        frame.setVisible(true);
    }

    private JPanel createGUI(UploadTableModel tableModel)
    {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel centerPanel = new JPanel();
        panel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(createFilePanel(tableModel));
        final JTextArea recipientsTextArea = createAndAddTextArea(centerPanel, "Recipients");
        final JTextArea commentTextArea = createAndAddTextArea(centerPanel, "Comment");
        JPanel buttonPanel = new JPanel(new BorderLayout());
        panel.add(buttonPanel, BorderLayout.SOUTH);
        JPanel centerButtonPanel = new JPanel();
        buttonPanel.add(centerButtonPanel, BorderLayout.CENTER);
        uploadButton = createUploadButton(tableModel, recipientsTextArea, commentTextArea);
        centerButtonPanel.add(uploadButton);
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    cancel();
                }
            });
        centerButtonPanel.add(cancelButton);
        JPanel closeButtonPanel = new JPanel();
        buttonPanel.add(closeButtonPanel, BorderLayout.EAST);
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    logout();
                }
            });
        closeButtonPanel.add(closeButton);

        return panel;
    }

    private JButton createUploadButton(final UploadTableModel fileListModel,
            final JTextArea recipientsTextArea, final JTextArea commentTextArea)
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
                                List<File> files = fileListModel.getFiles();
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

    private JTextArea createAndAddTextArea(JPanel centerPanel, String title)
    {
        JTextArea textArea = new JTextArea(5, 20);
        JPanel panel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEtchedBorder();
        panel.setBorder(BorderFactory.createTitledBorder(border, title));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        centerPanel.add(panel);
        return textArea;
    }

    private JPanel createFilePanel(final UploadTableModel tableModel)
    {
        JPanel filePanel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEtchedBorder();
        filePanel.setBorder(BorderFactory.createTitledBorder(border, "Files to upload"));
        final JTable table = new JTable(tableModel)
            {
                private static final long serialVersionUID = 1L;

                public String getToolTipText(MouseEvent evt)
                {
                    int index = rowAtPoint(evt.getPoint());
                    File file = tableModel.getFileItem(index).getFile();
                    String size = FileUtilities.byteCountToDisplaySize(file.length());
                    return file.getAbsolutePath() + " (" + size + ")";
                }
            };
        table.setColumnModel(createTableColumnModel());
        popupMenu = createPopupMenu(table, tableModel);
        filePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        addButton = new JButton("Add File");
        addButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    chooseAndAddFile(tableModel);
                }
            });
        filePanel.add(addButton, BorderLayout.SOUTH);
        return filePanel;
    }

    private JPopupMenu createPopupMenu(final JTable table, final UploadTableModel tableModel)
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
                public void mousePressed(MouseEvent e)
                {
                    showPopup(e);
                }

                public void mouseReleased(MouseEvent e)
                {
                    showPopup(e);
                }

                private void showPopup(MouseEvent e)
                {
                    if (table.getSelectedRows().length > 0 && menu.isEnabled() && e.isPopupTrigger())
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

    private void chooseAndAddFile(final UploadTableModel tableModel)
    {
        fileDialog.setVisible(true);
        String fileName = fileDialog.getFile();
        if (fileName != null)
        {
            File file = new File(new File(fileDialog.getDirectory()), fileName);
            if (file.exists() == false)
            {
                JOptionPane.showMessageDialog(frame, "File does not exists:\n"
                        + file.getAbsolutePath());
                return;
            }
            if (file.isDirectory())
            {
                JOptionPane.showMessageDialog(frame, "Can't upload whole directories.");
                return;
            }
            if (tableModel.alreadyAdded(file))
            {
                JOptionPane.showMessageDialog(frame, "File already added:\n"
                        + file.getAbsolutePath());
                return;
            }
            long freeUploadSpace = tableModel.calculateFreeUploadSpace();
            long length = file.length();
            if (length > freeUploadSpace)
            {
                JOptionPane.showMessageDialog(frame, "File size of "
                        + FileUtilities.byteCountToDisplaySize(length)
                        + " exceeds the limit for uploads. File size has to be less than "
                        + FileUtilities.byteCountToDisplaySize(freeUploadSpace) + ".");
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

    private void logout()
    {
        if (cancel())
        {
            uploader.logout();
            System.exit(0);
        }
    }

    private boolean cancel()
    {
        if (uploader.isUploading() == false)
        {
            return true;
        }
        int answer =
                JOptionPane.showConfirmDialog(frame, "Do you really want to cancel uploading?");
        if (answer == JOptionPane.YES_OPTION)
        {
            uploader.cancel();
            return true;
        }
        return false;
    }

}
