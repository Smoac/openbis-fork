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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.remoting.httpinvoker.CommonsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;

import ch.systemsx.cisd.cifex.client.EnvironmentFailureException;
import ch.systemsx.cisd.cifex.client.UserFailureException;
import ch.systemsx.cisd.cifex.upload.IUploadService;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;
import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * Class which uploads file via an implementation of {@link IUploadService}. It handles the
 * protocol of the contract of <code>IUploadService</code>. Registered {@link IUploadListener}
 * instances will be informed what's going on during uploading.
 * 
 * @author Franz-Josef Elmer
 */
public class Uploader
{
    private static final String SPRING_BEAN_URL_PROTOCOL = "spring-bean://";

    private static final int SERVER_TIMEOUT_MIN = 5;

    private static final EnumSet<UploadState> RUNNING_STATES =
            EnumSet.of(UploadState.READY_FOR_NEXT_FILE, UploadState.UPLOADING);

    private static final class ServiceInvocationHandler implements InvocationHandler
    {
        private final IUploadService service;

        private ServiceInvocationHandler(IUploadService service)
        {
            this.service = service;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            synchronized (service)
            {
                try
                {
                    return method.invoke(service, args);
                } catch (InvocationTargetException ex)
                {
                    throw ex.getCause();
                }
            }
        }
    }

    private static final class RandomAccessFileProvider
    {
        private final File file;

        private RandomAccessFile randomAccessFile;

        RandomAccessFileProvider(File file)
        {
            this.file = file;
        }

        RandomAccessFile getRandomAccessFile()
        {
            if (randomAccessFile == null)
            {
                try
                {
                    randomAccessFile = new RandomAccessFile(file, "r");
                } catch (FileNotFoundException ex)
                {
                    throw new WrappedIOException(ex);
                }
            }
            return randomAccessFile;
        }

        void closeFile()
        {
            if (randomAccessFile != null)
            {
                try
                {
                    randomAccessFile.close();
                } catch (IOException ex)
                {
                    throw new WrappedIOException(ex);
                }
            }
        }
    }

    private static final int BLOCK_SIZE = 64 * 1024;

    private final Set<IUploadListener> listeners = new LinkedHashSet<IUploadListener>();

    private final IUploadService uploadService;

    private final String uploadSessionID;

    /**
     * Creates an instance for the specified service URL and session ID.
     */
    public Uploader(String serviceURL, String sessionID) throws UserFailureException,
            EnvironmentFailureException
    {
        this.uploadService = createServiceProxy(serviceURL);
        this.uploadSessionID = sessionID;
    }

    /**
     * Creates an instance for the specified service URL and session ID.
     */
    public Uploader(String serviceURL, String username, String passwd) throws UserFailureException,
            EnvironmentFailureException
    {
        this.uploadService = createServiceProxy(serviceURL);
        this.uploadSessionID = uploadService.login(username, passwd);
    }

    private IUploadService createServiceProxy(String serviceURL)
    {
        ClassLoader classLoader = getClass().getClassLoader();
        IUploadService service = createService(serviceURL);
        ServiceInvocationHandler invocationHandler = new ServiceInvocationHandler(service);
        return (IUploadService) Proxy.newProxyInstance(classLoader, new Class[]
            { IUploadService.class }, invocationHandler);
    }

    private IUploadService createService(String serviceURL)
    {
        if (serviceURL.startsWith(SPRING_BEAN_URL_PROTOCOL))
        {
            AbstractApplicationContext applicationContext =
                    new ClassPathXmlApplicationContext(new String[]
                        { "applicationContext.xml" }, true);
            LogInitializer.init();
            return ((IUploadService) applicationContext.getBean("file-upload-service"));
        }
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

    /**
     * Creates an instance for the specified service and session ID.
     */
    public Uploader(IUploadService uploadService, String uploadSessionID)
    {
        this.uploadService = uploadService;
        this.uploadSessionID = uploadSessionID;
    }

    /**
     * Adds a listener for upload events.
     */
    public void addUploadListener(IUploadListener uploadListener)
    {
        listeners.add(uploadListener);
    }

    /**
     * Returns <code>true</code> if this uploader is still working.
     */
    public boolean isUploading()
    {
        try
        {
            UploadStatus status = uploadService.getUploadStatus(uploadSessionID);
            return RUNNING_STATES.contains(status.getUploadState());
        } catch (RuntimeException ex)
        {
            fireExceptionEvent(ex);
            return false;
        } catch (EnvironmentFailureException ex)
        {
            fireExceptionEvent(ex);
            return false;
        }
    }

    /**
     * Cancels uploading.
     */
    public void cancel()
    {
        try
        {
            uploadService.cancel(uploadSessionID);
        } catch (RuntimeException ex)
        {
            fireExceptionEvent(ex);
        } catch (EnvironmentFailureException ex)
        {
            fireExceptionEvent(ex);
        }
    }

    /**
     * Closes uploading.
     */
    public void close()
    {
        try
        {
            uploadService.close(uploadSessionID);
        } catch (RuntimeException ex)
        {
            ex.printStackTrace();
        }
    }

    /**
     * Uploads the specified files for the specified recipients.
     * 
     * @param recipients Comma or space-separated list of e-mail addresses or user ID's in the form
     *            <code>id:<i>user ID</i></code>. Can be an empty string.
     * @param comment Optional comment added to the outgoing e-mails. Can be an empty string.
     */
    public void upload(List<File> files, String recipients, String comment)
    {
        String[] paths = new String[files.size()];
        try
        {
            for (int i = 0; i < files.size(); i++)
            {
                paths[i] = files.get(i).getCanonicalPath();
            }
        } catch (IOException ex)
        {
            fireExceptionEvent(ex);
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }

        try
        {
            byte[] bytes = new byte[BLOCK_SIZE];
            RandomAccessFileProvider fileProvider = null;
            long fileSize = 0;
            boolean running = true;
            while (running)
            {
                UploadStatus status = uploadService.getUploadStatus(uploadSessionID);
                switch (status.getUploadState())
                {
                    case INITIALIZED:
                        uploadService.defineUploadParameters(uploadSessionID, paths, recipients,
                                comment);
                        break;
                    case READY_FOR_NEXT_FILE:
                        if (fileProvider != null)
                        {
                            fileProvider.closeFile();
                            fireUploadedEvent();
                        }
                        File file = new File(status.getCurrentFile());
                        fileSize = file.length();
                        fileProvider = new RandomAccessFileProvider(file);
                        fireStartedEvent(file, fileSize);
                        uploadService.startUploading(uploadSessionID);
                        break;
                    case UPLOADING:
                        uploadNextBlock(fileProvider, status.getFilePointer(), bytes);
                        fireProgressEvent(status.getFilePointer(), fileSize);
                        break;
                    case FINISHED:
                        uploadService.finish(uploadSessionID, true);
                        uploadService.close(uploadSessionID);
                        fireFinishedEvent(true);
                        running = false;
                        break;
                    case ABORTED:
                        System.out.println(status);
                        uploadService.finish(uploadSessionID, false);
                        fireFinishedEvent(false);
                        running = false;
                        break;
                }
            }
        } catch (Throwable throwable)
        {
            fireExceptionEvent(throwable);
            try
            {
                uploadService.finish(uploadSessionID, false);
            } catch (Throwable throwable2)
            {
                fireExceptionEvent(throwable2);
                throwable = throwable2;
            }
            fireFinishedEvent(false);
            throw CheckedExceptionTunnel.wrapIfNecessary(throwable);
        } finally
        {
            fireResetEvent();
        }
    }

    private void uploadNextBlock(RandomAccessFileProvider fileProvider, long filePointer,
            byte[] bytes) throws IOException, EnvironmentFailureException
    {
        RandomAccessFile randomAccessFile = fileProvider.getRandomAccessFile();
        int blockSize = bytes.length;
        long fileSize = randomAccessFile.length();
        boolean lastBlock = filePointer + blockSize >= fileSize;
        if (lastBlock)
        {
            blockSize = (int) (fileSize - filePointer);
        }
        randomAccessFile.seek(filePointer);
        randomAccessFile.readFully(bytes, 0, blockSize);
        uploadService.uploadBlock(uploadSessionID, bytes, blockSize, lastBlock);
    }

    private void fireStartedEvent(File file, long fileSize)
    {
        for (IUploadListener listener : listeners)
        {
            listener.uploadingStarted(file, fileSize);
        }
    }

    private void fireProgressEvent(long numberOfBytes, long fileSize)
    {
        int percentage = (int) ((numberOfBytes * 100) / Math.max(1, fileSize));
        for (IUploadListener listener : listeners)
        {
            listener.uploadingProgress(percentage, numberOfBytes);
        }
    }

    private void fireFinishedEvent(boolean successful)
    {
        for (IUploadListener listener : listeners)
        {
            listener.uploadingFinished(successful);
        }
    }

    private void fireUploadedEvent()
    {
        for (IUploadListener listener : listeners)
        {
            listener.fileUploaded();
        }
    }

    private void fireExceptionEvent(Throwable throwable)
    {
        for (IUploadListener listener : listeners)
        {
            listener.exceptionOccured(throwable);
        }
    }

    private void fireResetEvent()
    {
        for (IUploadListener listener : listeners)
        {
            listener.reset();
        }
    }

    private void setUpKeyStore(String serviceURL)
    {
        if (serviceURL.startsWith("https"))
        {
            Certificate[] certificates = getServerCertificate(serviceURL);
            KeyStore keyStore;
            try
            {
                keyStore = KeyStore.getInstance("JKS");
                keyStore.load(null, null);
                for (int i = 0; i < certificates.length; i++)
                {
                    keyStore.setCertificateEntry("cifex" + i, certificates[i]);
                }
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
            FileOutputStream fileOutputStream = null;
            try
            {
                String homeDir = System.getProperty("user.home");
                File cifexDir = new File(homeDir, ".cifex");
                cifexDir.mkdirs();
                File keyStoreFile = new File(cifexDir, "keystore");
                fileOutputStream = new FileOutputStream(keyStoreFile);
                keyStore.store(fileOutputStream, "changeit".toCharArray());
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

    private Certificate[] getServerCertificate(String serviceURL)
    {
        workAroundABugInJava6();

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
            return socket.getSession().getPeerCertificates();
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

    // see comment submitted on 31-JAN-2008 for
    // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6514454
    private void workAroundABugInJava6()
    {
        try
        {
            SSLContext.getInstance("SSL").createSSLEngine();
        } catch (Exception ex)
        {
            System.out.println(ex);
        }
    }

}
