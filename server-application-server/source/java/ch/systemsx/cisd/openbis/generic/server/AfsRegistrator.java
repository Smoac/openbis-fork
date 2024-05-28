package ch.systemsx.cisd.openbis.generic.server;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.ExposablePropertyPlaceholderConfigurer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;

@Component
public class AfsRegistrator implements IAfsRegistrator, ApplicationListener<ApplicationEvent>
{

    private final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, AfsRegistrator.class);

    private static final String AFS_DATA_STORE_CODE = "AFS";

    private static final String AFS_URL_PROPERTY_NAME = ComponentNames.SERVER_PUBLIC_INFORMATION + "afs-server.url";

    @Autowired
    private IDAOFactory daoFactory;

    @Resource(name = ExposablePropertyPlaceholderConfigurer.PROPERTY_CONFIGURER_BEAN_NAME)
    private ExposablePropertyPlaceholderConfigurer configurer;

    @Autowired IAfsRegistrator self;

    @Override public void onApplicationEvent(final ApplicationEvent event)
    {
        Object source = event.getSource();
        if (source instanceof AbstractApplicationContext)
        {
            AbstractApplicationContext appContext = (AbstractApplicationContext) source;
            if ((event instanceof ContextStartedEvent) || (event instanceof ContextRefreshedEvent))
            {
                if (appContext.getParent() != null)
                {
                    // call the bean with transaction support
                    self.registerAfs();
                }
            }
        }
    }

    @Transactional
    public void registerAfs()
    {
        IDataStoreDAO dataStoreDAO = daoFactory.getDataStoreDAO();

        DataStorePE existingDataStore = dataStoreDAO.tryToFindDataStoreByCode(AFS_DATA_STORE_CODE);

        if (existingDataStore != null)
        {
            operationLog.info("AFS server has been already registered in the data stores table before. Nothing to do.");
            return;
        }

        String afsUrl = configurer.getResolvedProps().getProperty(AFS_URL_PROPERTY_NAME, "");

        DataStorePE dataStore = new DataStorePE();
        dataStore.setCode(AFS_DATA_STORE_CODE);
        dataStore.setDownloadUrl(afsUrl);
        dataStore.setRemoteUrl(afsUrl);
        dataStore.setDatabaseInstanceUUID("");
        dataStore.setSessionToken("");
        dataStore.setArchiverConfigured(false);

        operationLog.info("Registering AFS server in the data stores table.");

        dataStoreDAO.createOrUpdateDataStore(dataStore);
    }

}
