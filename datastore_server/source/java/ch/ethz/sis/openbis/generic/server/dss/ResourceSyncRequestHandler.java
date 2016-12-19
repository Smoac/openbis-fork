/*
 * Copyright 2016 ETH Zuerich, SIS
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
package ch.ethz.sis.openbis.generic.server.dss;

import org.apache.commons.lang.time.DateUtils;

import ch.ethz.sis.openbis.generic.server.EntityRetriever;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.common.ssl.SslCertificateHelper;
import ch.systemsx.cisd.openbis.dss.generic.server.oaipmh.JythonBasedRequestHandler;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.jython.IRequestHandlerPluginScriptRunner;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.IMasterDataRegistrationTransaction;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.EncapsulatedCommonServer;
import ch.systemsx.cisd.openbis.generic.server.jython.api.v1.impl.MasterDataRegistrationService;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Ganime Betul Akin
 */
public class ResourceSyncRequestHandler extends JythonBasedRequestHandler
{
    private static final String V3_ENTITY_RETRIEVER_VARIABLE_NAME = "v3EntityRetriever";

    @Override
    protected void setVariables(IRequestHandlerPluginScriptRunner runner, SessionContextDTO session)
    {
        super.setVariables(runner, session);

        String openBisUrl = ServiceProvider.getConfigProvider().getOpenBisServerUrl();
        SslCertificateHelper.trustAnyCertificate(openBisUrl);
        ICommonServer commonService =
                HttpInvokerUtils.createServiceStub(ICommonServer.class, ((String.valueOf(properties.get("server-url")).trim()))
                        + "/rmi-common",
                        5 * DateUtils.MILLIS_PER_MINUTE);

        EncapsulatedCommonServer encapsulatedServer = EncapsulatedCommonServer.create(commonService, session.getSessionToken());
        MasterDataRegistrationService service = new MasterDataRegistrationService(encapsulatedServer);
        IMasterDataRegistrationTransaction masterDataRegistrationTransaction = service.transaction();

        runner.setVariable(V3_ENTITY_RETRIEVER_VARIABLE_NAME,
                EntityRetriever.createWithMasterDataRegistationTransaction(ServiceProvider.getV3ApplicationService(), session.getSessionToken(),
                        masterDataRegistrationTransaction));
    }
}
