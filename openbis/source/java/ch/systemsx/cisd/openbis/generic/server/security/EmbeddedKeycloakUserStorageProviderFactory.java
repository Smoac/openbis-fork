package ch.systemsx.cisd.openbis.generic.server.security;

import java.io.File;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.server.asapi.v3.IApplicationServerInternalApi;
import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.CommonServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.ComponentNames;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;

public class EmbeddedKeycloakUserStorageProviderFactory implements UserStorageProviderFactory<EmbeddedKeycloakUserStorageProvider>
{

    private static final Logger log = LogFactory.getLogger(LogCategory.OPERATION, EmbeddedKeycloakUserStorageProviderFactory.class);

    @Autowired
    private IApplicationServerInternalApi applicationServerApi;

    @Autowired
    private IAuthenticationService authenticationService;

    @Override public String getId()
    {
        return "openbis-user-storage";
    }

    @Override public void init(final Config.Scope config)
    {
        applicationServerApi = CommonServiceProvider.getApplicationServerApi();
        authenticationService =
                CommonServiceProvider.getApplicationContext().getBean(ComponentNames.AUTHENTICATION_SERVICE, IAuthenticationService.class);
    }

    @Override public EmbeddedKeycloakUserStorageProvider create(final KeycloakSession session, final ComponentModel model)
    {
        return new EmbeddedKeycloakUserStorageProvider(session, model, applicationServerApi, authenticationService);
    }

}
