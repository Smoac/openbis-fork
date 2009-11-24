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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import org.springframework.remoting.RemoteAccessException;

import ch.systemsx.cisd.cifex.rpc.client.ICIFEXComponent;
import ch.systemsx.cisd.cifex.rpc.client.ICIFEXDownloader;
import ch.systemsx.cisd.cifex.rpc.client.RPCServiceFactory;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
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
public class FileDownloadClient
{
    private static final String TITLE = "CIFEX Downloader";

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
            FileDownloadClient newMe = createFileDownloadClient(args);
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

    /**
     * Create a new instance of the FileDownloadClient based on the arguments, if possible.
     */
    static FileDownloadClient createFileDownloadClient(String[] args)
            throws ch.systemsx.cisd.cifex.shared.basic.UserFailureException,
            EnvironmentFailureException
    {
        if (args.length < 2)
            throw new ConfigurationFailureException(
                    "The CIFEX File Download Client was improperly configured -- the arguments it requires were not supplied. Please talk to the CIFEX administrator.");

        final String serviceURL = args[0];
        final String sessionId;
        final ICIFEXComponent cifex = RPCServiceFactory.createCIFEXComponent(serviceURL, true);

        switch (args.length)
        {
            case 2:
                sessionId = args[1];
                break;
            default:
                String userName = args[1];
                String passwd = args[2];
                sessionId = cifex.login(userName, passwd);
        }

        return new FileDownloadClient(cifex, sessionId, SYSTEM_TIME_PROVIDER);
    }

    // GUI Implementation State
    private final ICIFEXComponent cifex;

    private final String sessionId;

    private final ICIFEXDownloader downloader;

    private Thread shutdownHook;

    private final JFrame frame;

    private final FileDownloadClientModel tableModel;

    private static final long KEEP_ALIVE_PERIOD_MILLIS = 60 * 1000; // Every second

    private JButton directoryButton;

    FileDownloadClient(final ICIFEXComponent cifex, final String sessionId,
            final ITimeProvider timeProvider) throws EnvironmentFailureException,
            InvalidSessionException
    {
        // save and create local state
        this.cifex = cifex;
        this.sessionId = sessionId;
        this.downloader = this.cifex.createDownloader(this.sessionId);

        // create the window frame
        frame = new JFrame(TITLE);
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // add callbacks to close the app properly
        addShutdownHook();
        startSessionKeepAliveTimer(KEEP_ALIVE_PERIOD_MILLIS);
        addWindowCloseHook();

        tableModel = new FileDownloadClientModel(this, timeProvider);

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

    private static void setLookAndFeelToMetal()
    {
        // Set the look and feel to Metal, if possible
        try
        {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ex)
        {
            // just ignore -- no big deal
        }
    }

    private void createGUI()
    {
        JLabel spacer;

        // Put everything into a panel which goes into the center of the frame
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(0, 5));
        panel.add(createFileListComponent(), BorderLayout.CENTER);
        panel.add(createDirectoryPanel(), BorderLayout.SOUTH);
        frame.add(panel, BorderLayout.CENTER);

        // Add small gaps to the left and right of the frame, to give a bit of space
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(5, 5));
        frame.add(spacer, BorderLayout.WEST);
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(5, 5));
        frame.add(spacer, BorderLayout.EAST);

        // Add a small gap at the bottom of the frame, to the GUI doesn't look too constrained
        spacer = new JLabel("");
        spacer.setPreferredSize(new Dimension(600, 15));
        frame.add(spacer, BorderLayout.SOUTH);
        frame.setBounds(200, 200, 770, 300);
        frame.setVisible(true);
    }

    private JComponent createFileListComponent()
    {
        final JTable fileTable = new JTable(tableModel);
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

        // We have two lines of text to display
        fileTable.setRowHeight(fileTable.getRowHeight() * 2);

        // Configure the table columns
        TableColumn column;
        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.FILE_DETAILS_COLUMN);
        column.setPreferredWidth(150);
        column.setCellRenderer(new FileDetailsTableCellRenderer());

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.SENDER_COLUMN);
        column.setPreferredWidth(150);
        
        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.COMMENT_COLUMN);
        column.setPreferredWidth(150);

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.SENT_DATE_COLUMN);
        column.setPreferredWidth(100);
        column.setCellRenderer(new DateTimeTableCellRenderer());

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.EXPIRATION_DATE_COLUMN);
        column.setPreferredWidth(100);
        column.setCellRenderer(new DateTimeTableCellRenderer());

        column = fileTable.getColumnModel().getColumn(FileDownloadClientModel.DOWNLOAD_STATUS_COLUMN);
        column.setPreferredWidth(100);
        column.setCellRenderer(new DownloadStatusTableCellRenderer(tableModel));
        column.setCellEditor(new DownloadStatusTableCellEditor(tableModel));
        JScrollPane scrollPane = new JScrollPane(fileTable);
        return scrollPane;
    }

    private JComponent createDirectoryPanel()
    {
        final JPanel directoryPanel = new JPanel();
        directoryPanel.setLayout(new BorderLayout());
        final JLabel saveLabel = new JLabel("Save To:");
        saveLabel.setPreferredSize(new Dimension(60, 30));
        directoryPanel.add(saveLabel, BorderLayout.WEST);

        directoryButton = new JButton("");
        directoryButton.setPreferredSize(new Dimension(510, 30));
        directoryPanel.add(directoryButton, BorderLayout.CENTER);
        directoryButton
                .setToolTipText("Click button to select a directory in which to save the downloaded files.");
        directoryButton.addActionListener(new ActionListener()
            {

                public void actionPerformed(ActionEvent e)
                {
                    JFileChooser fileChooser = new JFileChooser(getDownloadDirectory());
                    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    int returnVal = fileChooser.showOpenDialog(frame);
                    if (returnVal == JFileChooser.APPROVE_OPTION)
                    {
                        setDownloadDirectory(fileChooser.getSelectedFile());
                        updateDirectoryLabel();
                    }
                }
            });
        // set the text of the label to the current directory
        updateDirectoryLabel();
        return directoryPanel;
    }

    private void updateDirectoryLabel()
    {
        try
        {
            directoryButton.setText(getDownloadDirectory().getCanonicalPath());
        } catch (IOException ex)
        {
            directoryButton.setText(".");
        }
    }

    /**
     * Log the user out automatically if the window is closed.
     */
    private void addWindowCloseHook()
    {
        frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    logout();
                }
            });
    }

    /**
     * Log the user out automatically if the app is shutdown.
     */
    private void addShutdownHook()
    {
        shutdownHook = new Thread()
            {
                @Override
                public void run()
                {
                    cifex.logout(sessionId);
                }
            };
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void addProgressListener()
    {
        downloader.addProgressListener(new IProgressListener()
            {
                public void start(File file, long fileSize)
                {
                }

                public void reportProgress(int percentage, long numberOfBytes)
                {
                }

                public void finished(boolean successful)
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

            });
    }

    /**
     * Periodically remind the server that we are still alive.
     */
    private void startSessionKeepAliveTimer(final long checkTimeIntervalMillis)
    {
        final Timer timer = new Timer("Session Keep Alive", true);
        timer.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    try
                    {
                        cifex.checkSession(sessionId);
                    } catch (RemoteAccessException ex)
                    {
                        System.err.println("Error connecting to the server");
                        ex.printStackTrace();
                    } catch (InvalidSessionException ex)
                    {
                        JOptionPane.showMessageDialog(frame,
                                "Your session has expired on the server. Please log in again",
                                "Error connecting to server", JOptionPane.ERROR_MESSAGE);
                        Runtime.getRuntime().removeShutdownHook(shutdownHook);
                        System.exit(1);
                    }
                }
            }, 0L, checkTimeIntervalMillis);
    }

    /**
     * Display the GUI
     */
    private void show()
    {
        frame.setVisible(true);
    }

    /**
     * Checks if it is safe to quit, if not, asks the user before doing so.
     */
    private void logout()
    {
        if (cancel())
        {
            cifex.logout(sessionId);
            System.exit(0);
        }
    }

    /**
     * @return true if it safe to quit or if the user says it is ok
     */
    private boolean cancel()
    {

        if (!downloader.isInProgress())
        {
            return true;
        }
        int answer =
                JOptionPane.showConfirmDialog(frame, "Do you really want to stop downloading?");
        if (answer == JOptionPane.YES_OPTION)
        {
            downloader.cancel();
            return true;
        }
        return false;
    }

    public void setDownloadDirectory(File downloadDirectory)
    {
        tableModel.setDownloadDirectory(downloadDirectory);
    }

    public File getDownloadDirectory()
    {
        return tableModel.getDownloadDirectory();
    }
}
