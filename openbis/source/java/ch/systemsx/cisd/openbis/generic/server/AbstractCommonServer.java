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

package ch.systemsx.cisd.openbis.generic.server;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.authentication.IAuthenticationService;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.authentication.Principal;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IDataSetTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

/**
 * @author Franz-Josef Elmer
 */
abstract class AbstractCommonServer<T extends IServer> extends AbstractServer<T>
{

    private final IAuthenticationService authenticationService;

    protected final ICommonBusinessObjectFactory businessObjectFactory;

    public AbstractCommonServer(IAuthenticationService authenticationService,
            ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.authenticationService = authenticationService;
        this.businessObjectFactory = businessObjectFactory;
    }

    protected List<PersonPE> registerPersons(final String sessionToken, final List<String> userIDs)
    {
        final Session session = getSession(sessionToken);
        final List<PersonPE> persons = getDAOFactory().getPersonDAO().listByCodes(userIDs);
        if (persons.size() > 0)
        {
            throw UserFailureException.fromTemplate("Following persons already exist: [%s]",
                    StringUtils.join(userIDs, ","));
        }
        List<String> unknownUsers = new ArrayList<String>();
        final DisplaySettings defaultDisplaySettings = getDefaultDisplaySettings(sessionToken);
        List<PersonPE> newPersons = new ArrayList<PersonPE>();
        for (String userID : userIDs)
        {
            try
            {
                final Principal principal = authenticationService.getPrincipal(userID);
                newPersons.add(createPerson(principal, session.tryGetPerson(),
                        defaultDisplaySettings));
            } catch (final IllegalArgumentException e)
            {
                unknownUsers.add(userID);
            }
        }
        if (unknownUsers.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Following persons unknown by the authentication service: [%s]",
                    StringUtils.join(userIDs, ","));
        } else
        {
            return newPersons;
        }

    }

    public int archiveDatasets(String sessionToken, List<String> datasetCodes,
            boolean deleteFromDataStore)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.archiveDatasets(deleteFromDataStore);
    }

    public int unarchiveDatasets(String sessionToken, List<String> datasetCodes)
    {
        Session session = getSession(sessionToken);
        IDataSetTable dataSetTable = businessObjectFactory.createDataSetTable(session);
        dataSetTable.loadByDataSetCodes(datasetCodes, false, true);
        return dataSetTable.unarchiveDatasets();
    }

    protected IDatasetLister createDatasetLister(Session session)
    {
        return businessObjectFactory.createDatasetLister(session);
    }

}
