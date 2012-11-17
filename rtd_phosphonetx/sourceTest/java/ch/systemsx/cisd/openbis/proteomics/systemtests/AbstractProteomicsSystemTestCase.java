/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.proteomics.systemtests;

import ch.systemsx.cisd.openbis.datastoreserver.systemtests.SystemTestCase;
import ch.systemsx.cisd.openbis.generic.server.ICommonServerForInternalUse;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.proteomics.server.api.v1.Constants;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IPhosphoNetXServer;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.IProteomicsDataService;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractProteomicsSystemTestCase extends SystemTestCase
{

    /**
     *
     *
     */
    public AbstractProteomicsSystemTestCase()
    {
        super();
    }

    @Override
    protected String getApplicationContextLocation()
    {
        return "classpath:proteomics-applicationContext.xml";
    }

    protected String registerPerson(String userID)
    {
        ICommonServerForInternalUse commonServer = getCommonServer();
        String systemSessionToken = commonServer .tryToAuthenticateAsSystem().getSessionToken();
        commonServer.registerPerson(systemSessionToken, userID);
        return userID;
    }

    protected void assignInstanceRole(String userID, RoleCode roleCode)
    {
        ICommonServerForInternalUse commonServer = getCommonServer();
        String systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
        commonServer.registerInstanceRole(systemSessionToken, roleCode,
                Grantee.createPerson(userID));
    }

    protected void assignSpaceRole(String userID, RoleCode roleCode, SpaceIdentifier spaceIdentifier)
    {
        ICommonServerForInternalUse commonServer = getCommonServer();
        String systemSessionToken = commonServer.tryToAuthenticateAsSystem().getSessionToken();
        commonServer.registerSpaceRole(systemSessionToken, roleCode, spaceIdentifier,
                Grantee.createPerson(userID));
    }

    protected String authenticateAs(String user)
    {
        return getCommonServer().tryAuthenticate(user, "password").getSessionToken();
    }

    protected ICommonServerForInternalUse getCommonServer()
    {
        return getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
    }

    protected IProteomicsDataServiceInternal getDataServiceInternal()
    {
        return getBean(Constants.PROTEOMICS_DATA_SERVICE_INTERNAL);
    }

    protected IProteomicsDataService getDataService()
    {
        return getBean(Constants.PROTEOMICS_DATA_SERVICE);
    }

    protected IPhosphoNetXServer getServer()
    {
        return getBean(ResourceNames.PROTEOMICS_PLUGIN_SERVER);
    }

    @SuppressWarnings("unchecked")
    private <T> T getBean(String beanId)
    {
        return (T) applicationContext.getBean(beanId);
    }

}