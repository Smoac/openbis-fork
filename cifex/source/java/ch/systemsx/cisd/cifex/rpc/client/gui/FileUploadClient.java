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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.cifex.rpc.client.FileItem;
import ch.systemsx.cisd.cifex.rpc.client.FileItemStatus;
import ch.systemsx.cisd.cifex.rpc.client.FileWithOverrideName;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXUploader;
import ch.systemsx.cisd.cifex.rpc.client.PersistenceStore;
import ch.systemsx.cisd.cifex.rpc.client.encryption.OpenPGPSymmetricKeyEncryption;
import ch.systemsx.cisd.cifex.rpc.client.gui.PassphraseDialog.PassphraseAndFileDeletion;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.security.PasswordGenerator;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * @author Franz-Josef Elmer
 */
public class FileUploadClient extends AbstractSwingGUI
{
    private static final int LINE_HEIGHT = 30;

    private static final int INPUT_WIDTH = 600;

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
        setLookAndFeelToNative();

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

    private final PasswordGenerator passphraseGenerator;

    private JButton uploadButton;

    private JButton cancelButton;

    private JButton addButton;

    private JButton encryptButton;

    private JPopupMenu popupMenu;

    private UploadTableModel tableModel;

    private JTextArea recipientsTextArea;

    private JTextArea commentTextArea;

    private String passphrase = "";

    private boolean deleteEncryptedFilesAfterSuccessfulUpload =
            PersistenceStore.isDeleteEncryptedFiles();

    final List<File> encryptedFilesToBeDeleted = new LinkedList<File>();

    private File workingDirectory;

    FileUploadClient(final CIFEXCommunicationState commState, final ITimeProvider timeProvider)
    {
        // save and create local state
        super(commState);

        this.passphraseGenerator = new PasswordGenerator();
        this.uploader = cifex.createUploader(sessionId);

        workingDirectory = PersistenceStore.getWorkingDirectory();

        tableModel = new UploadTableModel(uploader, timeProvider);
        createGUI();

        addProgressListener();
    }

    private void addProgressListener()
    {
        uploader.addProgressListener(createErrorLogListener());
        uploader.addProgressListener(createEncryptedFileDeletionListener());
        uploader.addProgressListener(createFinishLogListenerWhoExitsProgram());
    }

    private IProgressListener createEncryptedFileDeletionListener()
    {
        return new IProgressListener()
            {
                @Override
                public void start(File file, String operationName, long fileSize, Long fileIdOrNull)
                {
                }

                @Override
                public void reportProgress(int percentage, long numberOfBytes)
                {
                }

                @Override
                public void finished(boolean successful)
                {
                    // The check for doDeleteEncryptedFilesAfterSuccessfulUpload() shouldn't be
                    // necessary as that has happened already in encryptFilesIfRequested(), but just
                    // to be sure.
                    if (successful && doDeleteEncryptedFilesAfterSuccessfulUpload())
                    {
                        for (File file : encryptedFilesToBeDeleted)
                        {
                            if (file.delete() == false)
                            {
                                JOptionPane.showMessageDialog(getWindowFrame(),
                                        "Failed to delete file '" + file.getAbsolutePath() + "'.",
                                        "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        }
                    }
                }

                @Override
                public void exceptionOccured(Throwable throwable)
                {
                }

                @Override
                public void warningOccured(String warningMessage)
                {
                }
            };
    }

    private IProgressListener createFinishLogListenerWhoExitsProgram()
    {
        return new IProgressListener()
            {
                @Override
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
                                        + "(Uploaded Files > Edit Sharing)", "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }

                @Override
                public void start(File file, String operationName, long fileSize, Long fileIdOrNull)
                {
                }

                @Override
                public void reportProgress(int percentage, long numberOfBytes)
                {
                }

                @Override
                public void exceptionOccured(Throwable throwable)
                {
                }

                @Override
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

        window.pack();
        window.setLocationByPlatform(true);
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
        label = new JLabel("Recipients", SwingConstants.TRAILING);
        recipientsTextArea = new JTextArea();
        recipientsTextArea.setPreferredSize(new Dimension(INPUT_WIDTH, LINE_HEIGHT * 3));
        label.setLabelFor(recipientsTextArea);
        panel.add(label);
        panel.add(new JScrollPane(recipientsTextArea));

        // Comment label and input
        label = new JLabel("Comment", SwingConstants.TRAILING);
        commentTextArea = new JTextArea();
        commentTextArea.setPreferredSize(new Dimension(INPUT_WIDTH, LINE_HEIGHT * 3));
        label.setLabelFor(commentTextArea);
        panel.add(label);
        panel.add(new JScrollPane(commentTextArea));

        // Files label and table
        label = new JLabel("Files", SwingConstants.TRAILING);
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

        SpringLayoutUtilities.makeCompactGrid(panel, 3, 2, 5, 5, 5, 5);

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

        final JCheckBox willEncrypt = new JCheckBox("Will Encrypt");
        willEncrypt.setEnabled(false);
        encryptButton = new JButton("Encrypt\u2026");
        encryptButton.setToolTipText("Encrypt files before uploading");
        encryptButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final PassphraseAndFileDeletion newPassphraseAndFileDeletionOrNull =
                            PassphraseDialog.tryGetPassphraseForEncrypt(getWindowFrame(),
                                    passphrase, deleteEncryptedFilesAfterSuccessfulUpload,
                                    passphraseGenerator, "Encrypt Files", "Enter Passphrase");
                    if (newPassphraseAndFileDeletionOrNull != null)
                    {
                        passphrase = newPassphraseAndFileDeletionOrNull.getPassphrase();
                        deleteEncryptedFilesAfterSuccessfulUpload =
                                newPassphraseAndFileDeletionOrNull.tryGetDeleteEncrypted();
                        willEncrypt.setSelected(doEncrypt());
                    }
                }
            });

        addButton = new JButton("Add File\u2026");
        addButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    chooseAndAddFile();
                }
            });
        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(willEncrypt);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(encryptButton);
        buttonsPanel.add(Box.createHorizontalStrut(10));
        buttonsPanel.add(addButton);

        return buttonsPanel;
    }

    private JPanel createActionButtonsPanel()
    {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
        uploadButton = createUploadButton(tableModel);
        cancelButton = new JButton("Stop");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    cancel();
                }
            });

        buttonsPanel.add(Box.createHorizontalGlue());
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(Box.createHorizontalStrut(5));
        buttonsPanel.add(uploadButton);

        return buttonsPanel;
    }

    private JButton createUploadButton(final UploadTableModel fileListModel)
    {
        final JButton button = new JButton("Upload");
        button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    setEnableStateOfButtons(false);
                    new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                final List<FileWithOverrideName> files =
                                        wrapFiles(fileListModel.getFiles());
                                final String recipients = recipientsTextArea.getText();
                                final String comment = commentTextArea.getText();
                                final List<FileWithOverrideName> actualFiles =
                                        encryptFilesIfRequested(files);
                                if (actualFiles.isEmpty())
                                {
                                    setEnableStateOfButtons(true);
                                    return;
                                }
                                try
                                {
                                    uploader.upload(actualFiles, recipients, comment);
                                } catch (Throwable th)
                                {
                                    // Be silent - exception has already been reported by uploader
                                }
                            }
                        }).start();
                }
            });
        button.setEnabled(false);
        fileListModel.addTableModelListener(new TableModelListener()
            {
                @Override
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
                @Override
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
        File file = FileChooserUtils.tryChooseFile(getWindowFrame(), workingDirectory, false);
        if (file != null)
        {
            workingDirectory = file.getParentFile();
            try
            {
                // That's need to make the test reliable whether the file is already added.
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
        if (encryptButton != null)
        {
            encryptButton.setEnabled(enable);
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

    private List<FileWithOverrideName> encryptFilesIfRequested(
            final List<FileWithOverrideName> files)
    {
        if (doEncrypt())
        {
            final List<FileWithOverrideName> encryptedFiles =
                    new ArrayList<FileWithOverrideName>(files.size());
            for (FileWithOverrideName file : files)
            {
                try
                {
                    final FileItem fileItemOrNull =
                            tableModel.fireChanged(file.getOriginalFile(),
                                    FileItemStatus.ENCRYPTING);
                    if (file.getEncryptedFile().exists())
                    {
                        final int answer =
                                JOptionPane.showConfirmDialog(getWindowFrame(), "Output file '"
                                        + file.getEncryptedFile().getAbsolutePath()
                                        + "' already exists. " + "Overwrite?", "File exists",
                                        JOptionPane.YES_NO_OPTION);
                        if (answer == JOptionPane.YES_OPTION)
                        {
                            if (file.getEncryptedFile().delete() == false)
                            {
                                notifyUserOfThrowable(getWindowFrame(), file.getOriginalFile()
                                        .getPath(), "Encrypting", new IOException("File '"
                                        + file.getEncryptedFile() + "' could not be deleted."),
                                        null);
                                tableModel.fireChanged(file.getOriginalFile(),
                                        FileItemStatus.ABORTED);
                                continue;
                            }
                        } else
                        {
                            tableModel.fireChanged(file.getOriginalFile(), FileItemStatus.ABORTED);
                            continue;
                        }
                    }
                    final File encryptedFile =
                            OpenPGPSymmetricKeyEncryption.encrypt(file.getOriginalFile(), file
                                    .getEncryptedFile(), passphrase, false);
                    if (fileItemOrNull != null)
                    {
                        fileItemOrNull.setUploadedFile(encryptedFile);
                    }
                    encryptedFiles.add(new FileWithOverrideName(encryptedFile, file
                            .tryGetOverrideName()));
                    if (doDeleteEncryptedFilesAfterSuccessfulUpload())
                    {
                        encryptedFilesToBeDeleted.add(encryptedFile);
                    }
                } catch (Throwable th)
                {
                    notifyUserOfThrowable(getWindowFrame(), file.getOriginalFile().getPath(),
                            "Encrypting", th, null);
                    tableModel.fireChanged(file.getOriginalFile(), FileItemStatus.ABORTED);
                    break;
                }
            }
            return encryptedFiles;
        } else
        {
            return files;
        }
    }

    private boolean doDeleteEncryptedFilesAfterSuccessfulUpload()
    {
        return doEncrypt() && deleteEncryptedFilesAfterSuccessfulUpload;
    }

    private boolean doEncrypt()
    {
        return StringUtils.isNotEmpty(passphrase);
    }

    @Override
    protected File getWorkingDirectory()
    {
        return workingDirectory;
    }

    @Override
    protected boolean isDeleteEncryptedFile()
    {
        return deleteEncryptedFilesAfterSuccessfulUpload;
    }
}
