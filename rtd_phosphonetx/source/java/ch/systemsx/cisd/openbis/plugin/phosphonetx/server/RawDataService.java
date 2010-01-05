/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.GroupValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Group;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataService;

/**
 * Imlementation of {@link IRawDataService}.
 *
 * @author Franz-Josef Elmer
 */
public class RawDataService extends AbstractServer<IRawDataService> implements IRawDataService
{
    @Private static final String GROUP_CODE = "MS_DATA";

    @Private static final String RAW_DATA_SAMPLE_TYPE = "MS_INJECTION";
    
    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    public RawDataService()
    {
    }

    public RawDataService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonServer commonServer)
    {
        super(sessionManager, daoFactory);
        this.commonServer = commonServer;
    }
    
    public IRawDataService createLogger(boolean invocationSuccessful, long elapsedTime)
    {
        return new RawDataServiceLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }

    public List<Sample> listRawDataSamples(String sessionToken, String userID)
    {
        checkSession(sessionToken);
        
        PersonPE person = getDAOFactory().getPersonDAO().tryFindPersonByUserId(userID);
        if (person == null)
        {
            throw new UserFailureException("Unknown user ID: " + userID);
        }
        ListSampleCriteria criteria = new ListSampleCriteria();
        SampleTypePE sampleTypePE =
                getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(RAW_DATA_SAMPLE_TYPE);
        criteria.setSampleType(SampleTypeTranslator.translate(sampleTypePE, null));
        criteria.setIncludeGroup(true);
        criteria.setGroupCode(GROUP_CODE);
        List<Sample> samples = commonServer.listSamples(sessionToken, criteria);
        List<Sample> filteredList = new ArrayList<Sample>();
        GroupValidator validator = new GroupValidator();
        for (Sample sample : samples)
        {
            Sample parent = sample.getGeneratedFrom();
            if (parent != null)
            {
                Group group = parent.getGroup();
                if (group == null || validator.doValidation(person, group))
                {
                    filteredList.add(sample);
                }
            }
        }
        return filteredList;
    }

}
