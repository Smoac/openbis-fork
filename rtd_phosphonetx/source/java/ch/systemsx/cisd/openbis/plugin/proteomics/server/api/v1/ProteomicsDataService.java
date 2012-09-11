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

package ch.systemsx.cisd.openbis.plugin.proteomics.server.api.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.IProteomicsDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.IProteomicsDataService;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.MsInjectionDataInfo;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.PropertyKey;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.dto.MsInjectionSample;

/**
 * Implementation of {@link IProteomicsDataService}.
 * 
 * @author Franz-Josef Elmer
 */
@Component(Constants.PROTEOMICS_DATA_SERVICE)
public class ProteomicsDataService extends AbstractServer<IProteomicsDataService> implements
        IProteomicsDataService
{
    private static final String MS_SEARCH = "MS_SEARCH";

    @Resource(name = Constants.PROTEOMICS_DATA_SERVICE_INTERNAL)
    private IProteomicsDataServiceInternal service;

    public ProteomicsDataService()
    {
    }

    public ProteomicsDataService(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory, IPropertiesBatchManager propertiesBatchManager,
            IProteomicsDataServiceInternal service)
    {
        super(sessionManager, daoFactory, propertiesBatchManager);
        this.service = service;
    }

    @Override
    public String tryToAuthenticateAtRawDataServer(String userID, String userPassword)
    {
        SessionContextDTO session = tryToAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    @Override
    public IProteomicsDataService createLogger(IInvocationLoggerContext context)
    {
        return new ProteomicsDataServiceLogger(getSessionManager(), context);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<MsInjectionDataInfo> listRawDataSamples(String sessionToken, String userID)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            List<MsInjectionSample> list = service.listRawDataSamples(session.getSessionToken());
            ArrayList<MsInjectionDataInfo> result = new ArrayList<MsInjectionDataInfo>();
            for (MsInjectionSample sample : list)
            {
                result.add(translate(sample));
            }
            return result;

        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    private MsInjectionDataInfo translate(MsInjectionSample sample)
    {
        MsInjectionDataInfo info = new MsInjectionDataInfo();
        Sample msiSample = sample.getSample();
        info.setMsInjectionSampleID(msiSample.getId());
        info.setMsInjectionSampleCode(msiSample.getCode());
        info.setMsInjectionSampleRegistrationDate(msiSample.getRegistrationDate());
        info.setMsInjectionSampleProperties(translate(msiSample.getProperties()));
        Sample bioSample = msiSample.getGeneratedFrom();
        info.setBiologicalSampleID(bioSample.getId());
        info.setBiologicalSampleIdentifier(bioSample.getIdentifier());
        Experiment experiment = bioSample.getExperiment();
        if (experiment != null)
        {
            info.setBiologicalExperimentIdentifier(experiment.getIdentifier());
            info.setBiologicalExperiment(translate(experiment));
        }
        info.setBiologicalSampleProperties(translate(bioSample.getProperties()));
        List<ExternalData> dataSets = sample.getDataSets();
        Set<DataSet> transformedDataSets = new HashSet<DataSet>();
        for (ExternalData dataSet : dataSets)
        {
            DataSet transformedDataSet = transform(dataSet);
            transformedDataSets.add(transformedDataSet);
        }
        info.setDataSets(transformedDataSets);
        Map<String, Date> latestDataSetRegistrationDates = new HashMap<String, Date>();
        for (Entry<String, ExternalData> entry : sample.getLatestDataSets().entrySet())
        {
            latestDataSetRegistrationDates.put(entry.getKey(), entry.getValue()
                    .getRegistrationDate());
        }
        info.setLatestDataSetRegistrationDates(latestDataSetRegistrationDates);
        return info;
    }

    private DataSet transform(ExternalData dataSet)
    {
        DataSet transformedDataSet = new DataSet();
        transformedDataSet.setId(dataSet.getId());
        transformedDataSet.setCode(dataSet.getCode());
        transformedDataSet.setType(dataSet.getDataSetType().getCode());
        transformedDataSet.setRegistrationDate(dataSet.getRegistrationDate());
        transformedDataSet.setProperties(translate(dataSet.getProperties()));
        List<ExternalData> children = dataSet.getChildren();
        if (children != null && children.isEmpty() == false)
        {
            for (ExternalData child : children)
            {
                transformedDataSet.addChild(transform(child));
            }
        }
        return transformedDataSet;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos(
            String sessionToken)
    {
        checkSession(sessionToken);

        List<DataStoreServerProcessingPluginInfo> result =
                new ArrayList<DataStoreServerProcessingPluginInfo>();
        List<DataStorePE> dataStores = getDAOFactory().getDataStoreDAO().listDataStores();
        for (DataStorePE dataStore : dataStores)
        {
            Set<DataStoreServicePE> services = dataStore.getServices();
            for (DataStoreServicePE dataStoreService : services)
            {
                if (dataStoreService.getKind() == DataStoreServiceKind.PROCESSING)
                {
                    result.add(translate(dataStoreService));
                }
            }
        }
        return result;
    }

    private DataStoreServerProcessingPluginInfo translate(DataStoreServicePE dataStoreService)
    {
        String key = dataStoreService.getKey();
        String label = dataStoreService.getLabel();
        List<String> translatedCodes = new ArrayList<String>();
        Set<DataSetTypePE> datasetTypes = dataStoreService.getDatasetTypes();
        for (DataSetTypePE dataSetType : datasetTypes)
        {
            translatedCodes.add(dataSetType.getCode());
        }
        return new DataStoreServerProcessingPluginInfo(key, label, translatedCodes);
    }

    @Override
    @SuppressWarnings("deprecation")
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public void processingRawData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] rawDataSampleIDs, String dataSetType)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            service.processRawData(session.getSessionToken(), dataSetProcessingKey,
                    rawDataSampleIDs, dataSetType);
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public void processDataSets(String sessionToken, String userID, String dataSetProcessingKey,
            List<String> dataSetCodes)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            service.processDataSets(session.getSessionToken(), dataSetProcessingKey, dataSetCodes);
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment> listSearchExperiments(
            String sessionToken, String userID)
    {
        return listExperiments(sessionToken, userID, MS_SEARCH);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment> listExperiments(
            String sessionToken, String userID, String experimentTypeCode)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            List<Experiment> experiments =
                    service.listExperiments(session.getSessionToken(), experimentTypeCode);
            List<ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment> result =
                    new ArrayList<ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment>();
            for (Experiment experiment : experiments)
            {
                ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment e =
                        translate(experiment);
                result.add(e);
            }
            return result;
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    private ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment translate(
            Experiment experiment)
    {
        ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment e =
                new ch.systemsx.cisd.openbis.plugin.proteomics.shared.api.v1.dto.Experiment();
        e.setId(experiment.getId());
        e.setCode(experiment.getCode());
        e.setProjectCode(experiment.getProject().getCode());
        e.setSpaceCode(experiment.getProject().getSpace().getCode());
        e.setRegistrationDate(experiment.getRegistrationDate());
        e.setProperties(translate(experiment.getProperties()));
        return e;
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public List<DataSet> listDataSetsByExperiment(String sessionToken, String userID,
            long experimentID)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            List<DataSet> result = new ArrayList<DataSet>();
            List<ExternalData> dataSets =
                    service.listDataSetsByExperiment(session.getSessionToken(), new TechId(
                            experimentID));
            for (ExternalData dataSet : dataSets)
            {
                DataSet ds = new DataSet();
                ds.setId(dataSet.getId());
                ds.setCode(dataSet.getCode());
                ds.setType(dataSet.getDataSetType().getCode());
                ds.setProperties(translate(dataSet.getProperties()));
                result.add(ds);
            }
            return result;
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public void processSearchData(String sessionToken, String userID, String dataSetProcessingKey,
            long[] searchExperimentIDs)
    {
        processProteinResultDataSets(sessionToken, userID, dataSetProcessingKey, MS_SEARCH,
                searchExperimentIDs);
    }

    @Override
    @RolesAllowed(RoleWithHierarchy.INSTANCE_OBSERVER)
    public void processProteinResultDataSets(String sessionToken, String userID,
            String dataSetProcessingKey, String experimentTypeCode, long[] experimentIDs)
    {
        checkSession(sessionToken);
        SessionContextDTO session = login(userID);
        try
        {
            service.processProteinResultDataSets(session.getSessionToken(), dataSetProcessingKey,
                    experimentTypeCode, experimentIDs);
        } finally
        {
            service.logout(session.getSessionToken());
        }
    }

    private Map<PropertyKey, Serializable> translate(List<IEntityProperty> properties)
    {
        if (properties == null)
        {
            return null;
        }
        HashMap<PropertyKey, Serializable> map = new HashMap<PropertyKey, Serializable>();
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            PropertyKey key = new PropertyKey(propertyType.getCode(), propertyType.getLabel());
            DataTypeCode dataTypeCode = propertyType.getDataType().getCode();
            map.put(key, DataTypeUtils.convertValueTo(dataTypeCode, property.tryGetAsString()));
        }
        return map;
    }

    private SessionContextDTO login(String userID)
    {
        SessionContextDTO session = service.tryToAuthenticate(userID, "dummy-password");
        if (session == null)
        {
            throw new UserFailureException("Unknown user ID: " + userID);
        }
        return session;
    }

    @Override
    public int getMajorVersion()
    {
        return 1;
    }

    @Override
    public int getMinorVersion()
    {
        return 3;
    }

}
