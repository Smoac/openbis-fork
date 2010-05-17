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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.server.api.v1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServicePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.util.DataTypeUtils;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.IRawDataServiceInternal;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.IRawDataService;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.DataStoreServerProcessingPluginInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.MsInjectionDataInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.api.v1.dto.PropertyKey;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.dto.MsInjectionSample;

/**
 * Implementation of {@link IRawDataService}.
 * 
 * @author Franz-Josef Elmer
 */
@Component(Constants.PHOSPHONETX_RAW_DATA_SERVICE)
public class RawDataService extends AbstractServer<IRawDataService> implements IRawDataService
{
    @Resource(name = Constants.PHOSPHONETX_RAW_DATA_SERVICE_INTERNAL)
    private IRawDataServiceInternal service;

    public RawDataService()
    {
    }

    public RawDataService(final ISessionManager<Session> sessionManager,
            final IDAOFactory daoFactory, IRawDataServiceInternal service)
    {
        super(sessionManager, daoFactory);
        this.service = service;
    }

    public String tryToAuthenticateAtRawDataServer(String userID, String userPassword)
    {
        SessionContextDTO session = tryToAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    public IRawDataService createLogger(IInvocationLoggerContext context)
    {
        return new RawDataServiceLogger(getSessionManager(), context);
    }

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
        info.setBiologicalSampleProperties(translate(bioSample.getProperties()));
        Map<String, Date> latestDataSetRegistrationDates = new HashMap<String, Date>();
        for (Entry<String, ExternalData> entry : sample.getLatestDataSets().entrySet())
        {
            latestDataSetRegistrationDates.put(entry.getKey(), entry.getValue().getRegistrationDate());
        }
        info.setLatestDataSetRegistrationDates(latestDataSetRegistrationDates);
        return info;
    }

    private Map<PropertyKey, Serializable> translate(List<IEntityProperty> properties)
    {
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

    public List<DataStoreServerProcessingPluginInfo> listDataStoreServerProcessingPluginInfos(
            String sessionToken)
    {
        checkSession(sessionToken);

        List<DataStoreServerProcessingPluginInfo> result = new ArrayList<DataStoreServerProcessingPluginInfo>();
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

    private SessionContextDTO login(String userID)
    {
        SessionContextDTO session = service.tryToAuthenticate(userID, "dummy-password");
        if (session == null)
        {
            throw new UserFailureException("Unknown user ID: " + userID);
        }
        return session;
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }

}
