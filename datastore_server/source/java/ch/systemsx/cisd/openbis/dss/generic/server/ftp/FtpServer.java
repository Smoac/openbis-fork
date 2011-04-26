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

import java.util.Properties;

import org.apache.ftpserver.ConnectionConfigFactory;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.ssl.SslConfigurationFactory;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;

/**
 * Controls the lifecycle of an FTP server built into DSS.
 * 
 * @author Kaloyan Enimanev
 */
public class FtpServer implements FileSystemFactory
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FtpServer.class);

    private final IETLLIMSService openBisService;

    private final UserManager userManager;

    private final FtpServerConfig config;

    private final IFtpPathResolverRegistry pathResolverRegistry;

    private org.apache.ftpserver.FtpServer server;

    public FtpServer(IETLLIMSService openBisService, UserManager userManager, Properties configProps)
            throws Exception
    {
        this.openBisService = openBisService;
        this.userManager = userManager;
        this.config = new FtpServerConfig(configProps);
        this.pathResolverRegistry = new FtpPathResolverRegistry(config);

        if (config.isStartServer())
        {
            start();
        }
    }

    private void start() throws Exception
    {
        FtpServerFactory serverFactory = new FtpServerFactory();

        ListenerFactory factory = new ListenerFactory();
        factory.setPort(config.getPort());
        if (config.isUseSSL())
        {
            SslConfigurationFactory sslConfigFactory = new SslConfigurationFactory();
            sslConfigFactory.setKeystoreFile(config.getKeyStore());
            sslConfigFactory.setKeystorePassword(config.getKeyStorePassword());
            sslConfigFactory.setKeyPassword(config.getKeyPassword());
            factory.setSslConfiguration(sslConfigFactory.createSslConfiguration());
        }
        serverFactory.addListener("default", factory.createListener());

        ConnectionConfigFactory connectionConfigFactory = new ConnectionConfigFactory();
        connectionConfigFactory.setMaxThreads(config.getMaxThreads());
        serverFactory.setConnectionConfig(connectionConfigFactory.createConnectionConfig());

        serverFactory.setFileSystem(this);
        serverFactory.setUserManager(userManager);

        server = serverFactory.createServer();

        String startingMessage =
                String.format("Starting FTP server on port %d ...", config.getPort());
        operationLog.info(startingMessage);
        server.start();

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
    }

    public FileSystemView createFileSystemView(User user) throws FtpException
    {
        if (user instanceof FtpUser)
        {
            String sessionToken = ((FtpUser) user).getSessionToken();
            return new DSSFileSystemView(sessionToken, openBisService, pathResolverRegistry);
        } else
        {
            throw new FtpException("Unsupported user type.");
        }
    }

}
