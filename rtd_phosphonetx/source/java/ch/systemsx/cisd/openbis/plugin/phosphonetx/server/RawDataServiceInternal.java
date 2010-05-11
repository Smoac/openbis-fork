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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ICommonBusinessObjectFactory;
import ch.systemsx.cisd.openbis.generic.server.business.bo.IExternalDataTable;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.validator.IValidator;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTypeTranslator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.server.business.Manager;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.authorization.validator.RawDataSampleValidator;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class RawDataServiceInternal extends AbstractServer<IRawDataServiceInternal> implements IRawDataServiceInternal
{
     @Private static final String SPACE_CODE = "MS_DATA";

    @Private static final String RAW_DATA_SAMPLE_TYPE = "MS_INJECTION";
    
    private static final IValidator<MsInjectionSample> RAW_DATA_SAMPLE_VALIDATOR = new RawDataSampleValidator();

    private ICommonBusinessObjectFactory businessObjectFactory;

    private ISessionManager<Session> sessionManagerFromConstructor;

    public RawDataServiceInternal()
    {
    }

    public RawDataServiceInternal(ISessionManager<Session> sessionManager, IDAOFactory daoFactory,
            ICommonBusinessObjectFactory businessObjectFactory)
    {
        super(sessionManager, daoFactory);
        sessionManagerFromConstructor = sessionManager;
        this.businessObjectFactory = businessObjectFactory;
    }

    public void replaceAutoWiredSesseionManagerByConstructorSessionManager()
    {
        sessionManager = sessionManagerFromConstructor;
    }

    public IRawDataServiceInternal createLogger(IInvocationLoggerContext context)
    {
        return new RawDataServiceInternalLogger(getSessionManager(), context);
    }
    
    public List<MsInjectionSample> listRawDataSamples(String sessionToken)
    {
        return loadAllRawDataSamples(getSession(sessionToken));
    }
    
    public void processRawData(String sessionToken, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        Session session = getSession(sessionToken);
        PersonPE person = session.tryGetPerson();

        List<MsInjectionSample> samples = loadAllRawDataSamples(session);
        Set<Long> sampleIDs = new HashSet<Long>();
        List<String> dataSetCodes = new ArrayList<String>();
        Map<String, String> parameterBindings = new HashMap<String, String>();
        for (MsInjectionSample sample : samples)
        {
            if (RAW_DATA_SAMPLE_VALIDATOR.isValid(person, sample))
            {
                sampleIDs.add(sample.getSample().getId());
                Map<String, ExternalData> latestDataSets = sample.getLatestDataSets();
                ExternalData latestDataSet = latestDataSets.get(dataSetType);
                if (latestDataSet != null)
                {
                    String code = latestDataSet.getCode();
                    dataSetCodes.add(code);
                    parameterBindings.put(code, sample.getSample().getCode());
                }
            }
        }

        String dataStoreServerCode = findDataStoreServer(dataSetProcessingKey);
        IExternalDataTable externalDataTable =
                businessObjectFactory.createExternalDataTable(session);
        externalDataTable.processDatasets(dataSetProcessingKey, dataStoreServerCode, dataSetCodes,
                parameterBindings);
    }

    private List<MsInjectionSample> loadAllRawDataSamples(Session session)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        ListSampleCriteria criteria = new ListSampleCriteria();
        SampleTypePE sampleTypePE =
            getDAOFactory().getSampleTypeDAO().tryFindSampleTypeByCode(RAW_DATA_SAMPLE_TYPE);
        criteria.setSampleType(SampleTypeTranslator.translate(sampleTypePE, null));
        criteria.setIncludeSpace(true);
        criteria.setSpaceCode(SPACE_CODE);
        ListOrSearchSampleCriteria criteria2 = new ListOrSearchSampleCriteria(criteria);
        criteria2.setEnrichDependentSamplesWithProperties(true);
        List<Sample> samples = sampleLister.list(criteria2);
        Manager manager = new Manager();
        for (Sample sample : samples)
        {
            manager.addSample(sample);
        }
        manager.gatherDataSets(businessObjectFactory.createDatasetLister(session, ""));
        return manager.getSamples();
    }
    
    private String findDataStoreServer(String dataSetProcessingKey)
    {
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            Set<DataStoreServicePE> services = dataStore.getServices();
            for (DataStoreServicePE dataStoreService : services)
            {
                if (DataStoreServiceKind.PROCESSING.equals(dataStoreService.getKind())
                        && dataSetProcessingKey.equals(dataStoreService.getKey()))
                {
                    return dataStore.getCode();
                }
            }
        }
        throw new EnvironmentFailureException("No data store processing service with key '"
                + dataSetProcessingKey + "' found.");
    }
}
