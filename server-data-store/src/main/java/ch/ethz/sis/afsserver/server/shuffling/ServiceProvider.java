package ch.ethz.sis.afsserver.server.shuffling;

import java.util.List;
import java.util.UUID;

import ch.ethz.sis.afs.dto.Lock;
import ch.ethz.sis.afs.manager.TransactionManager;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameterUtil;
import ch.ethz.sis.afsserver.worker.ConnectionFactory;
import ch.ethz.sis.shared.startup.Configuration;

public class ServiceProvider
{
    private static volatile boolean initialized = false;

    private static Configuration configuration;

    private static IShareIdManager shareIdManager;

    private static IEncapsulatedOpenBISService openBISService;

    private static IConfigProvider configProvider;

    public static void configure(Configuration configuration)
    {
        ServiceProvider.configuration = configuration;
    }

    public static IShareIdManager getShareIdManager()
    {
        initialize();
        return shareIdManager;
    }

    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        initialize();
        return openBISService;
    }

    public static IConfigProvider getConfigProvider()
    {
        initialize();
        return configProvider;
    }

    private static void initialize()
    {
        // initialize lazily only to verify configuration properties if they are really needed

        if (!initialized)
        {
            synchronized (ServiceProvider.class)
            {
                if (!initialized)
                {
                    if (configuration == null)
                    {
                        throw new RuntimeException("Cannot initialize with null configuration");
                    }

                    IEncapsulatedOpenBISService encapsulatedOpenBISService =
                            new EncapsulatedOpenBISService(AtomicFileSystemServerParameterUtil.getOpenBISFacade(configuration));
                    IShareIdManager shareIdManager =
                            new ShareIdManager(encapsulatedOpenBISService, getLockManager(),
                                    AtomicFileSystemServerParameterUtil.getDataSetLockingTimeout(configuration));
                    ConfigProvider configProvider = new ConfigProvider(configuration);

                    ServiceProvider.openBISService = encapsulatedOpenBISService;
                    ServiceProvider.shareIdManager = shareIdManager;
                    ServiceProvider.configProvider = configProvider;
                    initialized = true;
                }
            }
        }
    }

    private static IShareIdLockManager getLockManager()
    {
        Object connectionFactoryObject;

        try
        {
            connectionFactoryObject = configuration.getSharableInstance(AtomicFileSystemServerParameter.connectionFactoryClass);
        } catch (Exception e)
        {
            throw new RuntimeException("Could not get instance of connection factory", e);
        }

        if (connectionFactoryObject instanceof ConnectionFactory)
        {
            ConnectionFactory connectionFactory = (ConnectionFactory) connectionFactoryObject;
            TransactionManager transactionManager = connectionFactory.getTransactionManager();
            return new IShareIdLockManager()
            {
                @Override public void lock(final List<Lock<UUID, String>> locks)
                {
                    transactionManager.lock(locks);
                }

                @Override public void unlock(final List<Lock<UUID, String>> locks)
                {
                    transactionManager.unlock(locks);
                }
            };
        } else
        {
            throw new RuntimeException("Unsupported connection factory class " + connectionFactoryObject.getClass()
                    + ". Cannot extract instance of transaction manager from it.");
        }
    }

}
