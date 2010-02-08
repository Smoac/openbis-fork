/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.PluginTaskProviders;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetTypeWithVocabularyTerms;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseInstance;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletedDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStoreServerInfo;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatastoreServiceDescriptions;
import ch.systemsx.cisd.openbis.generic.shared.dto.ListSamplesByPropertyCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A class that encapsulates the {@link IETLLIMSService} and handles (re-)authentication
 * automatically as needed.
 * <p>
 * This class is thread safe (otherwise one thread can change the session and cause the other thread
 * to use the invalid one).
 * </p>
 * 
 * @author Bernd Rinn
 */
// TODO 2009-07-03, Tomasz Pylak: remove all the setters which are used to configure this class,
// only methods which contact openbis server should be public. Create a separate class which
// describes this class configuration and move all appropriate setters there.
public final class EncapsulatedOpenBISService implements IEncapsulatedOpenBISService, FactoryBean
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EncapsulatedOpenBISService.class);

    private final IETLLIMSService service;

    private final SessionTokenManager sessionTokenManager;

    private int port;

    private boolean useSSL;

    private String username;

    private String password;

    private String downloadUrl;

    private String dataStoreCode;

    private String sessionToken; // NOTE: can be changed in parallel by different threads

    private Integer version;

    private DatabaseInstance homeDatabaseInstance;

    private DatastoreServiceDescriptions pluginTaskDescriptions;

    public EncapsulatedOpenBISService(SessionTokenManager sessionTokenManager, String serverURL,
            PluginTaskProviders pluginTaskParameters)
    {
        this(sessionTokenManager, HttpInvokerUtils.createServiceStub(IETLLIMSService.class,
                serverURL + "/rmi-etl", 5), pluginTaskParameters);
    }

    public EncapsulatedOpenBISService(SessionTokenManager sessionTokenManager,
            IETLLIMSService service, PluginTaskProviders pluginTaskParameters)
    {
        assert sessionTokenManager != null : "Unspecified session token manager.";
        assert service != null : "Given IETLLIMSService implementation can not be null.";
        assert pluginTaskParameters != null : "Unspecified plugin tasks";

        this.sessionTokenManager = sessionTokenManager;
        this.service = service;
        this.pluginTaskDescriptions = pluginTaskParameters.getPluginTaskDescriptions();
    }

    public final void setPort(int port)
    {
        this.port = port;
    }

    public final void setUseSSL(String useSSL)
    {
        // NOTE: 'use-ssl' property is optional. If it is not specified in DS properties file
        // String value passed by Spring is "${use-ssl}". Default value, which is 'true',
        // should be used in such case.
        boolean booleanValue = true;
        if (useSSL.equalsIgnoreCase("false"))
        {
            booleanValue = false;
        }
        this.useSSL = booleanValue;
    }

    public final void setUsername(String username)
    {
        this.username = username;
    }

    public final void setPassword(String password)
    {
        this.password = password;
    }

    public final void setDownloadUrl(String downloadUrl)
    {
        this.downloadUrl = downloadUrl;
    }

    public final void setDataStoreCode(String dataStoreCode)
    {
        this.dataStoreCode = dataStoreCode;
    }

    public Object getObject() throws Exception
    {
        return this;
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType()
    {
        return IEncapsulatedOpenBISService.class;
    }

    public boolean isSingleton()
    {
        return true;
    }

    private final void authenticate()
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Authenticating to openBIS server as user '" + username + "'.");
        }
        SessionContextDTO session = service.tryToAuthenticate(username, password);
        sessionToken = session == null ? null : session.getSessionToken();
        if (sessionToken == null)
        {
            final String msg =
                    "Authentication failure to openBIS server. Most probable cause: user or password are invalid.";
            throw new ConfigurationFailureException(msg);
        }
        DataStoreServerInfo dataStoreServerInfo = new DataStoreServerInfo();
        dataStoreServerInfo.setPort(port);
        dataStoreServerInfo.setUseSSL(useSSL);
        dataStoreServerInfo.setDataStoreCode(dataStoreCode);
        dataStoreServerInfo.setDownloadUrl(downloadUrl);
        dataStoreServerInfo.setSessionToken(sessionTokenManager.drawSessionToken());
        dataStoreServerInfo.setServicesDescriptions(pluginTaskDescriptions);
        service.registerDataStoreServer(sessionToken, dataStoreServerInfo);
    }

    private final void checkSessionToken()
    {
        if (sessionToken == null)
        {
            authenticate();
        }
    }

    private Experiment primTryToGetExperiment(ExperimentIdentifier experimentIdentifier)
    {
        return service.tryToGetExperiment(sessionToken, experimentIdentifier);
    }

    private List<Sample> primListSamples(ListSampleCriteria criteria)
    {
        return service.listSamples(sessionToken, criteria);
    }

    private final Sample primTryGetSampleWithExperiment(final SampleIdentifier sampleIdentifier)
    {
        return service.tryGetSampleWithExperiment(sessionToken, sampleIdentifier);
    }

    private long primRegisterSample(NewSample newSample)
    {
        return service.registerSample(sessionToken, newSample);
    }

    private final void primRegisterDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data)
    {
        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        if (sampleIdentifier == null)
        {
            ExperimentIdentifier experimentIdentifier =
                    dataSetInformation.getExperimentIdentifier();
            service.registerDataSet(sessionToken, experimentIdentifier, data);
        } else
        {
            service.registerDataSet(sessionToken, sampleIdentifier, data);
        }
    }

    private final IEntityProperty[] primGetPropertiesOfSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier)
    {
        return service.tryToGetPropertiesOfTopSampleRegisteredFor(sessionToken, sampleIdentifier);
    }

    private List<Sample> primListSamplesByCriteria(final ListSamplesByPropertyCriteria criteria)
            throws UserFailureException
    {
        return service.listSamplesByCriteria(sessionToken, criteria);
    }

    private final String primCreateDataSetCode()
    {
        return service.createDataSetCode(sessionToken);
    }

    //
    // IEncapsulatedOpenBISService
    //

    synchronized public Experiment tryToGetExperiment(ExperimentIdentifier experimentIdentifier)
    {
        assert experimentIdentifier != null : " Unspecified experiment identifier.";

        checkSessionToken();
        try
        {
            return primTryToGetExperiment(experimentIdentifier);
        } catch (InvalidSessionException ex)
        {
            authenticate();
            return primTryToGetExperiment(experimentIdentifier);
        }
    }

    synchronized public List<Sample> listSamples(ListSampleCriteria criteria)
    {
        assert criteria != null : "Unspecifed criteria.";

        checkSessionToken();
        try
        {
            return primListSamples(criteria);
        } catch (InvalidSessionException ex)
        {
            authenticate();
            return primListSamples(criteria);
        }
    }

    synchronized public final Sample tryGetSampleWithExperiment(
            final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";

        checkSessionToken();
        try
        {
            return primTryGetSampleWithExperiment(sampleIdentifier);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primTryGetSampleWithExperiment(sampleIdentifier);
        }
    }

    synchronized public SampleType getSampleType(String sampleTypeCode) throws UserFailureException
    {
        checkSessionToken();
        try
        {
            return service.getSampleType(sessionToken, sampleTypeCode);
        } catch (InvalidSessionException ex)
        {
            authenticate();
            return service.getSampleType(sessionToken, sampleTypeCode);
        }
    }

    synchronized public DataSetTypeWithVocabularyTerms getDataSetType(String dataSetTypeCode)
    {
        checkSessionToken();
        try
        {
            return service.getDataSetType(sessionToken, dataSetTypeCode);
        } catch (InvalidSessionException ex)
        {
            authenticate();
            return service.getDataSetType(sessionToken, dataSetTypeCode);
        }
    }

    synchronized public List<ExternalData> listDataSetsBySampleID(long sampleID,
            boolean showOnlyDirectlyConnected)
    {
        checkSessionToken();
        TechId id = new TechId(sampleID);
        try
        {
            return service.listDataSetsBySampleID(sessionToken, id, showOnlyDirectlyConnected);
        } catch (InvalidSessionException ex)
        {
            authenticate();
            return service.listDataSetsBySampleID(sessionToken, id, showOnlyDirectlyConnected);
        }
    }

    public long registerExperiment(NewExperiment experiment) throws UserFailureException
    {
        assert experiment != null : "Unspecified experiment.";
        
        checkSessionToken();
        try
        {
            return service.registerExperiment(sessionToken, experiment);
        } catch (InvalidSessionException ex)
        {
            authenticate();
            return service.registerExperiment(sessionToken, experiment);
        }
    }

    synchronized public long registerSample(NewSample newSample) throws UserFailureException
    {
        assert newSample != null : "Unspecified sample.";

        checkSessionToken();
        try
        {
            return primRegisterSample(newSample);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primRegisterSample(newSample);
        }
    }

    synchronized public final void registerDataSet(final DataSetInformation dataSetInformation,
            final NewExternalData data)
    {
        assert dataSetInformation != null : "missing sample identifier";
        assert data != null : "missing data";

        checkSessionToken();
        try
        {
            primRegisterDataSet(dataSetInformation, data);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            primRegisterDataSet(dataSetInformation, data);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Registered in openBIS: data set " + dataSetInformation.describe()
                    + ".");
        }
    }

    synchronized public final IEntityProperty[] getPropertiesOfTopSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";

        checkSessionToken();
        try
        {
            return primGetPropertiesOfSampleRegisteredFor(sampleIdentifier);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primGetPropertiesOfSampleRegisteredFor(sampleIdentifier);
        }
    }

    synchronized public final List<Sample> listSamplesByCriteria(
            final ListSamplesByPropertyCriteria criteria) throws UserFailureException
    {
        checkSessionToken();
        try
        {
            return primListSamplesByCriteria(criteria);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primListSamplesByCriteria(criteria);
        }
    }

    synchronized public final int getVersion()
    {
        checkSessionToken();
        if (version == null)
        {
            version = service.getVersion();
        }
        return version;
    }

    synchronized public final DatabaseInstance getHomeDatabaseInstance()
    {
        checkSessionToken();
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = service.getHomeDatabaseInstance(sessionToken);
        }
        return homeDatabaseInstance;
    }

    synchronized public final String createDataSetCode()
    {
        checkSessionToken();
        try
        {
            return primCreateDataSetCode();
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primCreateDataSetCode();
        }
    }

    synchronized public ExternalData tryGetDataSet(String sToken, String dataSetCode)
            throws UserFailureException
    {
        checkSessionToken();
        return service.tryGetDataSet(sToken, dataSetCode);
    }

    synchronized public void checkDataSetAccess(String sToken, String dataSetCode)
            throws UserFailureException
    {
        checkSessionToken();
        service.checkDataSetAccess(sToken, dataSetCode);
    }

    synchronized public List<SimpleDataSetInformationDTO> listDataSets()
            throws UserFailureException
    {
        checkSessionToken();
        try
        {
            return primListDataSets();
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primListDataSets();
        }
    }

    private List<SimpleDataSetInformationDTO> primListDataSets()
    {
        return service.listDataSets(sessionToken, dataStoreCode);
    }

    public List<DeletedDataSet> listDeletedDataSets(Long lastSeenDeletionEventIdOrNull)
    {
        checkSessionToken();
        try
        {
            return service.listDeletedDataSets(sessionToken, lastSeenDeletionEventIdOrNull);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return service.listDeletedDataSets(sessionToken, lastSeenDeletionEventIdOrNull);
        }
    }

}