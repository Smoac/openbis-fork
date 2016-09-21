/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.X509ExtendedKeyManager;

import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.DataConnectionConfigurationFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.AuthenticationFailedException;
import org.apache.ftpserver.ftplet.DefaultFtpReply;
import org.apache.ftpserver.ftplet.DefaultFtplet;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.FtpRequest;
import org.apache.ftpserver.ftplet.FtpSession;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.FtpletResult;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.log4j.Logger;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.KeyPairProvider;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.common.Session.AttributeKey;
import org.apache.sshd.common.keyprovider.AbstractKeyPairProvider;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.SshFile;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyParametersUtil;
import ch.systemsx.cisd.common.utilities.SystemTimeProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DssPropertyParametersUtil;
import ch.systemsx.cisd.openbis.generic.shared.IServiceForDataStoreServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;

/**
 * Controls the lifecycle of an FTP server built into DSS.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpServer implements FileSystemFactory, org.apache.sshd.server.FileSystemFactory
{
    private static final AttributeKey<User> USER_KEY = new Session.AttributeKey<User>();

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpServer.class);

    private final IServiceForDataStoreServer openBisService;

    private final UserManager userManager;

    private final FtpServerConfig config;

    private final IFtpPathResolverRegistry pathResolverRegistry;

    private org.apache.ftpserver.FtpServer server;

    private final IGeneralInformationService generalInfoService;

    private final IApplicationServerApi v3api;

    private SshServer sshServer;

    public FtpServer(IServiceForDataStoreServer openBisService, IGeneralInformationService generalInfoService, IApplicationServerApi v3api,
            UserManager userManager) throws Exception
    {
        this.openBisService = openBisService;
        this.generalInfoService = generalInfoService;
        this.v3api = v3api;
        this.userManager = userManager;
        ExtendedProperties serviceProperties = DssPropertyParametersUtil.loadServiceProperties();
        Properties ftpProperties = PropertyParametersUtil.extractSingleSectionProperties(
                serviceProperties, "ftp.server", true).getProperties();
        this.config = new FtpServerConfig(serviceProperties);
        FtpPathResolverConfig resolverConfig = new FtpPathResolverConfig(ftpProperties);
        this.pathResolverRegistry = resolverConfig.getResolverRegistry();

        if (config.isStartServer())
        {
            config.logStartupInfo();
            resolverConfig.logStartupInfo("SFTP/FTP");
            start();
        }
    }

    private void start() throws Exception
    {
        if (config.isSftpMode())
        {
            sshServer = createSftpServer();
            operationLog.info(String.format("Starting SFTP server on port %d ...",
                    config.getSftpPort()));
            sshServer.start();
            operationLog.info("SFTP server started.");
        }
        if (config.isFtpMode())
        {
            server = createFtpServer();
            operationLog.info(String.format("Starting FTP server on port %d ...",
                    config.getFtpPort()));
            server.start();
            operationLog.info("FTP server started.");
        }
    }

    private org.apache.ftpserver.FtpServer createFtpServer()
    {
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(config.getFtpPort());
        if (config.isUseSSL())
        {
            SslConfigurationFactory sslConfigFactory = new SslConfigurationFactory();
            sslConfigFactory.setKeystoreFile(config.getKeyStore());
            sslConfigFactory.setKeystorePassword(config.getKeyStorePassword());
            sslConfigFactory.setKeyPassword(config.getKeyPassword());
            factory.setSslConfiguration(sslConfigFactory.createSslConfiguration());
            factory.setImplicitSsl(config.isImplicitSSL());
            serverFactory.setFtplets(Collections.<String, Ftplet> singletonMap("",
                    new DefaultFtplet()
                        {
                            @Override
                            public FtpletResult beforeCommand(FtpSession session, FtpRequest request)
                                    throws FtpException, IOException
                            {
                                String cmd = request.getCommand().toUpperCase();
                                if ("USER".equals(cmd))
                                {
                                    if (session.isSecure() == false)
                                    {
                                        session.write(new DefaultFtpReply(500,
                                                "Control channel is not secure. "
                                                        + "Please, issue AUTH command first."));
                                        return FtpletResult.SKIP;
                                    }
                                }
                                return super.beforeCommand(session, request);
                            }
                        }));
        }

        DataConnectionConfigurationFactory dccFactory = new DataConnectionConfigurationFactory();
        dccFactory.setPassivePorts(config.getPassivePortsRange());
        if (config.isActiveModeEnabled())
        {
            dccFactory.setActiveEnabled(true);
            dccFactory.setActiveLocalPort(config.getActiveLocalPort());
        }

        factory.setDataConnectionConfiguration(dccFactory.createDataConnectionConfiguration());
        serverFactory.addListener("default", factory.createListener());

        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setMaxThreads(config.getMaxThreads());
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        serverFactory.setFileSystem(this);
        serverFactory.setUserManager(userManager);

        return serverFactory.createServer();
    }

    private SshServer createSftpServer()
    {
        SshServer s = SshServer.setUpDefaultServer();
        KeyPairProvider keyPairProvider = new KeystoreBasedKeyPairProvider(config, operationLog);
        s.setKeyPairProvider(keyPairProvider);
        s.setPort(config.getSftpPort());
        s.setSubsystemFactories(creatSubsystemFactories());
        s.setFileSystemFactory(this);
        s.setPasswordAuthenticator(new PasswordAuthenticator()
            {
                @Override
                public boolean authenticate(String username, String password, ServerSession session)
                {
                    try
                    {
                        UsernamePasswordAuthentication authentication =
                                new UsernamePasswordAuthentication(username, password);
                        User user = userManager.authenticate(authentication);
                        session.setAttribute(USER_KEY, user);
                        return true;
                    } catch (AuthenticationFailedException ex)
                    {
                        return false;
                    }
                }
            });
        return s;
    }

    private List<NamedFactory<Command>> creatSubsystemFactories()
    {
        return Arrays.<NamedFactory<Command>> asList(new SftpSubsystem.Factory());
    }

    /**
     * called by spring IoC container when the application shuts down.
     */
    public void stop()
    {
        if (server != null)
        {
            server.stop();
        }
        if (sshServer != null)
        {
            try
            {
                sshServer.stop();
            } catch (InterruptedException ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }
    }

    @Override
    public DSSFileSystemView createFileSystemView(User user) throws FtpException
    {
        if (user instanceof FtpUser)
        {
            String sessionToken = ((FtpUser) user).getSessionToken();
            return new DSSFileSystemView(sessionToken, openBisService, generalInfoService, v3api,
                    pathResolverRegistry);
        } else
        {
            throw new FtpException("Unsupported user type.");
        }
    }

    @Override
    public org.apache.sshd.server.FileSystemView createFileSystemView(Session session)
            throws IOException
    {
        User user = session.getAttribute(USER_KEY);
        try
        {
            final DSSFileSystemView view = createFileSystemView(user);
            return new org.apache.sshd.server.FileSystemView()
                {
                    private Cache cache = new Cache(SystemTimeProvider.SYSTEM_TIME_PROVIDER);

                    @Override
                    public SshFile getFile(SshFile baseDir, String file)
                    {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public SshFile getFile(String file)
                    {
                        return new FileView(view, file, cache);
                    }
                };
        } catch (FtpException ex)
        {
            throw new IOException(ex.getMessage());
        }
    }

    private static final class FileView implements SshFile
    {
        private final DSSFileSystemView fileView;

        private final String path;

        private final List<InputStream> inputStreams = new ArrayList<InputStream>();

        private FtpFile file;

        private final Cache cache;

        FileView(DSSFileSystemView fileView, String path, Cache cache)
        {
            this.fileView = fileView;
            this.path = path;
            this.cache = cache;
        }

        private FtpFile getFile()
        {
            if (file == null)
            {
                try
                {
                    file = fileView.getFile(path, cache);
                } catch (FtpException ex)
                {
                    throw CheckedExceptionTunnel.wrapIfNecessary(ex);
                }
            }
            return file;
        }

        @Override
        public String getAbsolutePath()
        {
            return getFile().getAbsolutePath();
        }

        @Override
        public String getName()
        {
            return FileUtilities.getFileNameFromRelativePath(path);
        }

        @Override
        public boolean isDirectory()
        {
            return getFile().isDirectory();
        }

        @Override
        public boolean isFile()
        {
            return getFile().isFile();
        }

        @Override
        public boolean doesExist()
        {
            return getFile().doesExist();
        }

        @Override
        public boolean isReadable()
        {
            return getFile().isReadable();
        }

        @Override
        public boolean isWritable()
        {
            return false;
        }

        @Override
        public boolean isExecutable()
        {
            return false;
        }

        @Override
        public boolean isRemovable()
        {
            return getFile().isRemovable();
        }

        @Override
        public SshFile getParentFile()
        {
            return null;
        }

        @Override
        public long getLastModified()
        {
            return getFile().getLastModified();
        }

        @Override
        public boolean setLastModified(long time)
        {
            return false;
        }

        @Override
        public long getSize()
        {
            return getFile().getSize();
        }

        @Override
        public boolean mkdir()
        {
            return false;
        }

        @Override
        public boolean delete()
        {
            return false;
        }

        @Override
        public boolean create() throws IOException
        {
            return false;
        }

        @Override
        public void truncate() throws IOException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean move(SshFile destination)
        {
            return false;
        }

        @Override
        public List<SshFile> listSshFiles()
        {
            List<FtpFile> files = getFile().listFiles();
            List<SshFile> result = new ArrayList<SshFile>();
            for (FtpFile child : files)
            {
                result.add(new FileView(fileView, child.getAbsolutePath(), cache));
            }
            return result;
        }

        @Override
        public OutputStream createOutputStream(long offset) throws IOException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream createInputStream(long offset) throws IOException
        {
            InputStream inputStream = getFile().createInputStream(offset);
            inputStreams.add(inputStream);
            return inputStream;
        }

        @Override
        public void handleClose() throws IOException
        {
            for (InputStream inputStream : inputStreams)
            {
                IOUtils.closeQuietly(inputStream);
            }
            inputStreams.clear();
        }

        @Override
        public String getOwner()
        {
            return "openBIS";
        }
    }

    private static final class KeystoreBasedKeyPairProvider extends AbstractKeyPairProvider
    {
        private final KeyPair[] keyPairs;

        private KeystoreBasedKeyPairProvider(FtpServerConfig config, Logger operationLog)
        {
            File keyStoreFile = config.getKeyStore();
            String keyStorePassword = config.getKeyStorePassword();
            String keyPassword = config.getKeyPassword();
            KeyStore keystore = loadKeystore(keyStoreFile, keyStorePassword);
            X509ExtendedKeyManager keyManager =
                    getKeyManager(keystore, keyStorePassword, keyPassword);
            List<KeyPair> list = new ArrayList<KeyPair>();
            try
            {
                Enumeration<String> aliases = keystore.aliases();
                while (aliases.hasMoreElements())
                {
                    String alias = aliases.nextElement();
                    if (keystore.isKeyEntry(alias))
                    {
                        Certificate certificate = keystore.getCertificate(alias);
                        PublicKey publicKey = certificate.getPublicKey();
                        PrivateKey privateKey = keyManager.getPrivateKey(alias);
                        list.add(new KeyPair(publicKey, privateKey));
                    }
                }
                keyPairs = list.toArray(new KeyPair[list.size()]);
                operationLog.info(keyPairs.length + " key pairs loaded from keystore "
                        + keyStoreFile);
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }
        }

        @Override
        protected KeyPair[] loadKeys()
        {
            return keyPairs;
        }

        private KeyStore loadKeystore(File keyStoreFile, String keyStorePassword)
        {
            InputStream stream = null;
            try
            {
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                stream = new FileInputStream(keyStoreFile);
                keystore.load(stream, keyStorePassword.toCharArray());
                return keystore;
            } catch (Exception e)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(e);
            } finally
            {
                IOUtils.closeQuietly(stream);
            }
        }

        private X509ExtendedKeyManager getKeyManager(KeyStore keystore, String keyStorePassword,
                String keyPassword)
        {
            try
            {
                String defaultAlgorithm = KeyManagerFactory.getDefaultAlgorithm();
                KeyManagerFactory factory = KeyManagerFactory.getInstance(defaultAlgorithm);
                char[] password =
                        (keyPassword == null ? keyStorePassword : keyPassword).toCharArray();
                factory.init(keystore, password);
                KeyManager[] keyManagers = factory.getKeyManagers();
                if (keyManagers.length != 1)
                {
                    throw new ConfigurationFailureException(
                            "Only one key manager expected instead of " + keyManagers.length + ".");
                }
                KeyManager keyManager = keyManagers[0];
                if (keyManager instanceof X509ExtendedKeyManager == false)
                {
                    throw new ConfigurationFailureException("Key manager is not of type "
                            + X509ExtendedKeyManager.class.getSimpleName() + ": "
                            + keyManager.getClass().getName());
                }
                return (X509ExtendedKeyManager) keyManager;
            } catch (Exception ex)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(ex);
            }

        }
    }
}
