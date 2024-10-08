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

    private static IEncapsulatedOpenBISService openBISService;

    private static ILockManager lockManager;

    private static IConfigProvider configProvider;

    public static void configure(Configuration configuration)
    {
        ServiceProvider.configuration = configuration;
    }

    public static IEncapsulatedOpenBISService getOpenBISService()
    {
        initialize();
        return openBISService;
    }

    public static ILockManager getLockManager()
    {
        initialize();
        return lockManager;
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

                    ServiceProvider.openBISService =
                            new EncapsulatedOpenBISService(AtomicFileSystemServerParameterUtil.getOpenBISFacade(configuration));
                    ServiceProvider.lockManager = createLockManager();
                    ServiceProvider.configProvider = new ConfigProvider(configuration);
                    initialized = true;
                }
            }
        }
    }

    private static ILockManager createLockManager()
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
            return new ILockManager()
            {
                @Override public boolean lock(final List<Lock<UUID, String>> locks)
                {
                    return transactionManager.lock(locks);
                }

                @Override public boolean unlock(final List<Lock<UUID, String>> locks)
                {
                    return transactionManager.unlock(locks);
                }
            };
        } else
        {
            throw new RuntimeException("Unsupported connection factory class " + connectionFactoryObject.getClass()
                    + ". Cannot extract instance of transaction manager from it.");
        }
    }

}
