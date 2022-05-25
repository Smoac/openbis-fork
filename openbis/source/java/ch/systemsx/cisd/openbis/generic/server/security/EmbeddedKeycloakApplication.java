package ch.systemsx.cisd.openbis.generic.server.security;

import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.keycloak.Config;
import org.keycloak.exportimport.ExportImportManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.ApplianceBootstrap;
import org.keycloak.services.resources.KeycloakApplication;
import org.keycloak.services.util.JsonConfigProviderFactory;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

public class EmbeddedKeycloakApplication extends KeycloakApplication
{

    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION, EmbeddedKeycloakApplication.class);

    private static final EmbeddedKeycloakServerProperties keycloakServerProperties = new EmbeddedKeycloakServerProperties();

    protected void loadConfig()
    {
        JsonConfigProviderFactory factory = new RegularJsonConfigProviderFactory();
        Config.init(factory.create()
                .orElseThrow(() -> new NoSuchElementException("No value present")));
    }

    @Override
    protected ExportImportManager bootstrap()
    {
        final ExportImportManager exportImportManager = super.bootstrap();
        createMasterRealmAdminUser();
        return exportImportManager;
    }

    private void createMasterRealmAdminUser()
    {
        KeycloakSession session = getSessionFactory().create();
        ApplianceBootstrap applianceBootstrap = new ApplianceBootstrap(session);
        EmbeddedKeycloakServerProperties.AdminUser admin = keycloakServerProperties.getAdminUser();

        try
        {
            session.getTransactionManager().begin();
            applianceBootstrap.createMasterRealmUser(admin.getUsername(), admin.getPassword());
            session.getTransactionManager().commit();
        } catch (Exception ex)
        {
            log.warn("Couldn't create keycloak master admin user: {}", ex);
            session.getTransactionManager().rollback();
        }

        session.close();
    }

    static class RegularJsonConfigProviderFactory extends JsonConfigProviderFactory
    {

    }
}