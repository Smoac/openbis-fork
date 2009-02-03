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

package ch.systemsx.cisd.cifex.upload.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ch.systemsx.cisd.cifex.upload.IUploadService;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;


/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FileUploadClient implements IUploadListener
{
    private static final class FileItem 
    {
        private final File file;
        private long length;
        
        FileItem(File file)
        {
            this.file = file;
            length = file.length();
        }
        
        public final File getFile()
        {
            return file;
        }
        
        public final long getLength()
        {
            return length;
        }

        public final void setLength(long length)
        {
            this.length = length;
        }
        
        public String render()
        {
            return file.getAbsolutePath() + " (" + FileUtilities.byteCountToDisplaySize(length) + ")";
        }

        @Override
        public String toString()
        {
            return file.getName();
        }
    }
    
    private static final class FileListModel extends AbstractListModel
    {
        private static final long serialVersionUID = 1L;
        private List<FileItem> fileItems = new ArrayList<FileItem>();
        
        void addFile(File file)
        {
            int size = fileItems.size();
            fileItems.add(new FileItem(file));
            fireIntervalAdded(this, size, size);
        }
        
        List<File> getFileItems()
        {
            List<File> files = new ArrayList<File>();
            for (FileItem fileItem : fileItems)
            {
                files.add(fileItem.getFile());
            }
            return files;
        }
        
        public Object getElementAt(int index)
        {
            return fileItems.get(index);
        }

        public int getSize()
        {
            return fileItems.size();
        }
    }
    
    private static final String TITLE = "CIFEX Uploader";

    private static final int SERVER_TIMEOUT_MIN = 5;
    
    public static void main(String[] args)
    {
        new FileUploadClient(args[0], args[1]).show();
    }

    private final Uploader uploader;
    
    private final JFileChooser fileChooser = new JFileChooser();

    private JFrame frame;

    private JPanel progressPanel;

    private JProgressBar progressBar;
    
    FileUploadClient(String serviceURL, String uploadSessionID)
    {
        uploader = new Uploader(createServiceStub(serviceURL), uploadSessionID);
        uploader.addUploadListener(this);
        frame = new JFrame(TITLE);
        frame.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(WindowEvent e)
                {
                    close();
                }
            });
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.add(createGUI(), BorderLayout.CENTER);
        frame.setBounds(200, 200, 600, 200);
        frame.setVisible(true);
    }
    
    void show()
    {
        frame.setVisible(true);
    }
    
    private JPanel createGUI()
    {
        JPanel panel = new JPanel(new BorderLayout());
        progressPanel = new JPanel();
        panel.add(progressPanel, BorderLayout.NORTH);
        JPanel centerPanel = new JPanel();
        panel.add(centerPanel, BorderLayout.CENTER);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
        final FileListModel fileListModel = new FileListModel();
        centerPanel.add(createFilePanel(fileListModel));
        final JTextArea recipientsTextArea = createAndAddTextArea(centerPanel, "Recipients");
        final JTextArea commentTextArea = createAndAddTextArea(centerPanel, "Comment");
        JPanel buttonPanel = new JPanel(new BorderLayout());
        panel.add(buttonPanel, BorderLayout.SOUTH);
        JPanel centerButtonPanel = new JPanel();
        buttonPanel.add(centerButtonPanel, BorderLayout.CENTER);
        centerButtonPanel.add(createUploadButton(fileListModel, recipientsTextArea, commentTextArea));
        JButton cancelButton = new JButton("Cancel");
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
                    close();
                }
            });
        closeButtonPanel.add(closeButton);
        
        return panel;
    }

    private JButton createUploadButton(final FileListModel fileListModel,
            final JTextArea recipientsTextArea, final JTextArea commentTextArea)
    {
        final JButton uploadButton = new JButton("upload");
        uploadButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    new Thread(new Runnable()
                        {
                            public void run()
                            {
                                List<File> files = fileListModel.getFileItems();
                                String recipients = recipientsTextArea.getText();
                                String comment = commentTextArea.getText();
                                uploader.upload(files, recipients, comment);
                            }
                        }).start();
                }
            });
        uploadButton.setEnabled(false);
        fileListModel.addListDataListener(new ListDataListener()
            {
                public void intervalRemoved(ListDataEvent e)
                {
                }
        
                public void intervalAdded(ListDataEvent e)
                {
                    uploadButton.setEnabled(fileListModel.getFileItems().size() > 0);
                }
        
                public void contentsChanged(ListDataEvent e)
                {
                }
            });
        return uploadButton;
    }

    private JTextArea createAndAddTextArea(JPanel centerPanel, String title)
    {
        JTextArea textArea = new JTextArea();
        JPanel panel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEtchedBorder();
        panel.setBorder(BorderFactory.createTitledBorder(border, title));
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        centerPanel.add(panel);
        return textArea;
    }

    private JPanel createFilePanel(final FileListModel fileListModel)
    {
        JPanel filePanel = new JPanel(new BorderLayout());
        Border border = BorderFactory.createEtchedBorder();
        filePanel.setBorder(BorderFactory.createTitledBorder(border, "Files to upload"));
        JList fileList = new JList(fileListModel)
            {
                private static final long serialVersionUID = 1L;

                public String getToolTipText(MouseEvent evt)
                {
                    int index = locationToIndex(evt.getPoint());
                    Object item = getModel().getElementAt(index);
                    if (item instanceof FileItem)
                    {
                        return ((FileItem) item).render();
                    }
                    return item.toString();
                }
            };
        fileList.setFixedCellWidth(200);
        filePanel.add(fileList, BorderLayout.CENTER);
        JButton addButton = new JButton("Add File");
        addButton.addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    int answer = fileChooser.showOpenDialog(frame);
                    if (answer == JFileChooser.APPROVE_OPTION)
                    {
                        File file = fileChooser.getSelectedFile();
                        fileListModel.addFile(file);
                    }
                }
            });
        filePanel.add(addButton, BorderLayout.SOUTH);
        return filePanel;
    }
    
    private IUploadService createServiceStub(String serviceURL)
    {
        setUpKeyStore(serviceURL);
        final HttpInvokerProxyFactoryBean httpInvokerProxy = new HttpInvokerProxyFactoryBean();
        httpInvokerProxy.setServiceUrl(serviceURL);
        httpInvokerProxy.setServiceInterface(IUploadService.class);
        final CommonsHttpInvokerRequestExecutor httpInvokerRequestExecutor =
                new CommonsHttpInvokerRequestExecutor();
        httpInvokerRequestExecutor.setReadTimeout((int) DateUtils.MILLIS_PER_MINUTE
                * SERVER_TIMEOUT_MIN);
        httpInvokerProxy.setHttpInvokerRequestExecutor(httpInvokerRequestExecutor);
        httpInvokerProxy.afterPropertiesSet();
        return (IUploadService) httpInvokerProxy.getObject();
    }

    private void setUpKeyStore(String serviceURL)
    {
        if (serviceURL.startsWith("https"))
        {
            Certificate certificate = getServerCertificate(serviceURL);
            KeyStore keyStore;
            try
            {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(null, null);
                keyStore.setCertificateEntry("cifex", certificate);
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            FileOutputStream fileOutputStream = null;
            try
            {
                String tmpDir = System.getProperty("java.io.tmpdir");
                File keyStoreFile = new File(tmpDir, "CIFEX-keystore");
                fileOutputStream = new FileOutputStream(keyStoreFile);
                keyStore.store(fileOutputStream, "cifextest".toCharArray());
                fileOutputStream.close();
                System.setProperty("javax.net.ssl.trustStore", keyStoreFile.getAbsolutePath());
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            } finally
            {
                // IOUtils.closeQuietly() isn't used because it is not in the client classpath
                if (fileOutputStream != null)
                {
                    try
                    {
                        fileOutputStream.close();
                    } catch (IOException ex)
                    {
                        // ignored
                    }
                }
            }
        }
    }
    
    private Certificate getServerCertificate(String serviceURL)
    {
        SSLSocket socket = null;
        try
        {
            URL url = new URL(serviceURL);
            int port = url.getPort();
            String hostname = url.getHost();
            System.out.println("host:" + hostname + " port:" + port);
            SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
            socket = (SSLSocket) factory.createSocket(hostname, port);
            socket.startHandshake();
            Certificate[] serverCerts = socket.getSession().getPeerCertificates();
            if (serverCerts.length == 0)
            {
                throw new EnvironmentFailureException("At least one server certificate expected.");
            }
            return serverCerts[0];
        } catch (Exception e)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(e);
        } finally
        {
            // IOUtils.closeQuietly() isn't used because it is not in the client classpath
            if (socket != null)
            {
                try
                {
                    socket.close();
                } catch (IOException ex)
                {
                    // ignored
                }
            }
        }
        
    }
    
    public void uploadingStarted(File file, long fileSize)
    {
        String path = file.getAbsolutePath();
        frame.setTitle(TITLE + ": " + path);
        progressPanel.removeAll();
        String humanReadableFileSize = FileUtilities.byteCountToDisplaySize(fileSize);
        progressPanel.add(new JLabel(file.getName() + " (" + humanReadableFileSize + ")"));
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar);
        progressPanel.invalidate();
        progressPanel.getParent().validate();
    }
    
    private void close()
    {
        if (cancel())
        {
            System.exit(0);
        }
    }
    
    private boolean cancel()
    {
        if (uploader.isUploading() == false)
        {
            return true;
        }
        int answer = JOptionPane.showConfirmDialog(frame, "Do you really want to cancel uploading?");
        if (answer == JOptionPane.YES_OPTION)
        {
            uploader.cancel();
            return true;
        }
        return false;
    }

    public void uploadingProgress(int percentage, long numberOfBytes)
    {
        progressBar.setValue(percentage);
    }
    
    public void fileUploaded()
    {
        System.out.println("FileUploadClient.fileUploaded()");
    }

    public void uploadingFinished(boolean successful)
    {
        if (successful)
        {
            JOptionPane.showMessageDialog(frame, "Uploading finish. Please update CIFEX in your Web browser");
            System.exit(0);
        } else
        {
            JOptionPane.showMessageDialog(frame, "Uploading aborted.");
        }
        
    }

}
