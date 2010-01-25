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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator.RawDataSampleValidator;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RawDataServiceInternal extends AbstractServer<IRawDataServiceInternal> implements IRawDataServiceInternal
{
    @Private static final String COPY_PROCESSING_KEY = "copy-data-sets";

    @Private static final String GROUP_CODE = "MS_DATA";

    @Private static final String RAW_DATA_SAMPLE_TYPE = "MS_INJECTION";
    
    private static final IValidator<Sample> RAW_DATA_SAMPLE_VALIDATOR = new RawDataSampleValidator();

    private ICommonBusinessObjectFactory businessObjectFactory;

    public RawDataServiceInternal()
    {
    }

    public RawDataServiceInternal(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory);
        this.businessObjectFactory = businessObjectFactory;
    }
    

    public IRawDataServiceInternal createLogger(boolean invocationSuccessful, long elapsedTime)
    {
        return new RawDataServiceInternalLogger(getSessionManager(), invocationSuccessful, elapsedTime);
    }
    
    public List<Sample> listRawDataSamples(String sessionToken)
    {
        return loadAllRawDataSamples(getSession(sessionToken));
    }
    
    public void copyRawData(String sessionToken, long[] rawDataSampleIDs)
    {
        Session session = getSession(sessionToken);
        PersonPE person = session.tryGetPerson();
        
        List<Sample> samples = loadAllRawDataSamples(session);
        Set<Long> sampleIDs = new HashSet<Long>();
        for (Sample sample : samples)
        {
            if (RAW_DATA_SAMPLE_VALIDATOR.isValid(person, sample))
            {
                sampleIDs.add(sample.getId());
            }
        }
        
        ISampleDAO sampleDAO = getDAOFactory().getSampleDAO();
        IExternalDataDAO externalDataDAO = getDAOFactory().getExternalDataDAO();
        List<String> dataSetCodes = new ArrayList<String>();
        for (long id : rawDataSampleIDs)
        {
            if (sampleIDs.contains(id) == false)
            {
                throw new UserFailureException("Invalid or unauthorized access on sample with ID: "
                        + id);
            }
            SamplePE sample = sampleDAO.getByTechId(new TechId(id));
            List<ExternalDataPE> dataSets = externalDataDAO.listExternalData(sample);
            for (ExternalDataPE dataSet : dataSets)
            {
                dataSetCodes.add(dataSet.getCode());
            }
        }
        String dataStoreServerCode = findDataStoreServer();
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.processDatasets(COPY_PROCESSING_KEY, dataStoreServerCode, dataSetCodes);
    }

    private List<Sample> loadAllRawDataSamples(Session session)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        ListSampleCriteria criteria = new ListSampleCriteria();
        SampleTypePE sampleTypePE =
            getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(RAW_DATA_SAMPLE_TYPE);
        criteria.setSampleType(SampleTypeTranslator.translate(sampleTypePE, null));
        criteria.setIncludeGroup(true);
        criteria.setGroupCode(GROUP_CODE);
        ListOrSearchSampleCriteria criteria2 = new ListOrSearchSampleCriteria(criteria);
        criteria2.setEnrichDependentSamplesWithProperties(true);
        return sampleLister.list(criteria2);
    }
    
    private String findDataStoreServer()
    {
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            Set<DataStoreServicePE> services = dataStore.getServices();
            for (DataStoreServicePE dataStoreService : services)
            {
                if (DataStoreServiceKind.PROCESSING.equals(dataStoreService.getKind())
                        && COPY_PROCESSING_KEY.equals(dataStoreService.getKey()))
                {
                    return dataStore.getCode();
                }
            }
        }
        throw new EnvironmentFailureException("No data store processing service with key '"
                + COPY_PROCESSING_KEY + "' found.");
    }
}
