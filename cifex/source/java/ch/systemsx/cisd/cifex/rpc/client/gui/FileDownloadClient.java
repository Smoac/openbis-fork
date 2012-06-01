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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXDownloader;
import ch.systemsx.cisd.cifex.rpc.client.PersistenceStore;
import ch.systemsx.cisd.cifex.rpc.client.gui.PassphraseDialog.PassphraseAndFileDeletion;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * The GUI for the WebStart file download application. This class assembles the GUI and creates the
 * necessary contextual objects (like the ICIFEXComponent) for interacting with the server. Although
 * FileDownloadClientModel handles most of the logic, the contextual information needs to be stored
 * here so clean-up can be performed, e.g., if the user closes the window while a download is in
 * progress.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FileDownloadClient extends AbstractSwingGUI
{
    private static final int BUTTON_WIDTH = 510;

    private static final int LABEL_WIDTH = 60;

    private static final int BUTTON_HEIGHT = 30;

    static
    {
        // Disable any logging output.
        System.setProperty("org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    private static final String TITLE = "CIFEX Downloader";

    /**
     * Read the arguments from the command line or WebStart and instantiates the GUI appropriately.
     */
    public static void main(String[] args)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        setLookAndFeelToNative();

        try
        {
            CIFEXCommunicationState commState = new CIFEXCommunicationState(args);

            FileDownloadClient newMe = new FileDownloadClient(commState, SYSTEM_TIME_PROVIDER);
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

    private final ICIFEXDownloader downloader;

    private final FileDownloadClientModel tableModel;

    private JButton decryptButton;

    private JButton directoryButton;

    private JCheckBox willDecrypt;

    private String passphrase = "";

    private boolean deleteEncryptedFileAfterSuccessfulDecryption =
            PersistenceStore.isDeleteEncryptedFiles();

    FileDownloadClient(final CIFEXCommunicationState commState, final ITimeProvider timeProvider)
    {
        // save and create local state
        super(commState);
        downloader = cifex.createDownloader(this.sessionId);

        tableModel = new FileDownloadClientModel(this, getWindowFrame(), timeProvider);

        createGUI();
        addProgressListener();
    }

    public ICIFEXComponent getCifex()
    {
        return cifex;
    }

    public String getSessionId()
    {
        return sessionId;
    }

    public ICIFEXDownloader getDownloader()
    {
        return downloader;
    }

    public interface IDecryptionChecker
    {
        /** Returns <code>true</code> if downloaded files should be decrypted. */
        boolean willDecrypt();
    }

    public IDecryptionChecker getDecryptionChecker()
    {
        return new IDecryptionChecker()
            {
                @Override
                public boolean willDecrypt()
                {
                    return willDecrypt.isSelected();
                }
            };
    }

    private void createGUI()
    {
        JLabel spacer;
        final JFrame window = getWindowFrame();

        // Put everything into a panel which goes into the center of the frame
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 5));
        panel.add(createFileListComponent(), BorderLayout.CENTER);
        panel.add(createDirectoryAndPassphrasePanel(), BorderLayout.SOUTH);
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
        spacer.setPreferredSize(new Dimension(1000, 15));
        window.add(spacer, BorderLayout.SOUTH);
        window.pack();
        window.setLocationByPlatform(true);
        window.setVisible(true);
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

                @Override
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
        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.FILE_DETAILS_COLUMN);
        column.setPreferredWidth(320);
        column.setCellRenderer(new FileDetailsTableCellRenderer());

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.SENDER_COLUMN);
        column.setPreferredWidth(150);

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.COMMENT_COLUMN);
        column.setPreferredWidth(200);

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.SENT_DATE_COLUMN);
        column.setPreferredWidth(100);
        column.setCellRenderer(new DateTimeTableCellRenderer());

        column =
                fileTable.getColumnModel()
                        .getColumn(FileDownloadClientModel.EXPIRATION_DATE_COLUMN);
        column.setPreferredWidth(100);
        column.setCellRenderer(new DateTimeTableCellRenderer());

        column =
                fileTable.getColumnModel()
                        .getColumn(FileDownloadClientModel.DOWNLOAD_STATUS_COLUMN);
        column.setPreferredWidth(150);
        column.setCellRenderer(new DownloadStatusTableCellRenderer(tableModel));
        column.setCellEditor(new DownloadStatusTableCellEditor(tableModel));
        JScrollPane scrollPane = new JScrollPane(fileTable);
        return scrollPane;
    }

    private JComponent createDirectoryAndPassphrasePanel()
    {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        willDecrypt = new JCheckBox("Will Decrypt");
        willDecrypt.setEnabled(false);
        decryptButton = new JButton("Decrypt\u2026");
        decryptButton.setToolTipText("Decrypt files after downloading");
        decryptButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final PassphraseAndFileDeletion newPassphraseAndFileDeletionOrNull =
                            PassphraseDialog.tryGetPassphraseForDecrypt(getWindowFrame(),
                                    passphrase, deleteEncryptedFileAfterSuccessfulDecryption,
                                    "Decrypt Files", "Enter Passphrase");
                    if (newPassphraseAndFileDeletionOrNull != null)
                    {
                        passphrase = newPassphraseAndFileDeletionOrNull.getPassphrase();
                        deleteEncryptedFileAfterSuccessfulDecryption =
                                newPassphraseAndFileDeletionOrNull.tryGetDeleteEncrypted();
                        tableModel.setPassphraseAndEncryptedFileDeletion(passphrase,
                                deleteEncryptedFileAfterSuccessfulDecryption);
                        willDecrypt.setSelected(passphrase.length() > 0);
                    }
                }
            });

        // Download directory panel
        JLabel label = new JLabel("Save To", SwingConstants.TRAILING);
        label.setPreferredSize(new Dimension(LABEL_WIDTH, BUTTON_HEIGHT));
        directoryButton = new JButton("");
        directoryButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        directoryButton
                .setToolTipText("Click button to select a directory in which to save the downloaded files.");
        directoryButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    final File newDirOrNull =
                            FileChooserUtils.tryChooseFile(getWindowFrame(), tableModel
                                    .getDownloadDirectory(), true);
                    if (newDirOrNull != null)
                    {
                        tableModel.setDownloadDirectory(newDirOrNull);
                        updateDirectoryLabel();
                    }
                }
            });
        // set the text of the label to the current directory
        updateDirectoryLabel();

        panel.add(willDecrypt);
        panel.add(decryptButton);
        panel.add(Box.createHorizontalGlue());
        panel.add(label);
        panel.add(directoryButton);

        return panel;
    }

    private void updateDirectoryLabel()
    {
        try
        {
            directoryButton.setText(tableModel.getDownloadDirectory().getCanonicalPath());
        } catch (IOException ex)
        {
            directoryButton.setText(".");
        }
    }

    private void addProgressListener()
    {
        downloader.addProgressListener(createErrorLogListener());
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

        if (downloader.isInProgress() == false)
        {
            return true;
        }
        int answer =
                JOptionPane.showConfirmDialog(getWindowFrame(),
                        "Do you really want to stop downloading?");
        if (answer == JOptionPane.YES_OPTION)
        {
            downloader.cancel();
            return true;
        }
        return false;
    }

    @Override
    protected String getTitle()
    {
        return TITLE;
    }

    @Override
    protected File getWorkingDirectory()
    {
        return tableModel.getDownloadDirectory();
    }

    @Override
    protected boolean isDeleteEncryptedFile()
    {
        return deleteEncryptedFileAfterSuccessfulDecryption;
    }
}
