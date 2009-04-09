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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.FactoryBean;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A class that encapsulates the {@link IETLLIMSService} and handles (re-)authentication automatically
 * as needed.
 * <p>
 * This class is thread safe (otherwise one thread can change the session and cause the other thread
 * to use the invalid one).
 * </p>
 * 
 * @author Bernd Rinn
 */
public final class EncapsulatedOpenBISService implements IEncapsulatedOpenBISService, FactoryBean
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EncapsulatedOpenBISService.class);

    private final int port;
    
    private final String username;

    private final String password;

    private final IETLLIMSService service;

    private final SessionTokenManager sessionTokenManager;
    
    private String sessionToken; // NOTE: can be changed in parallel by different threads

    private Integer version;

    private DatabaseInstancePE homeDatabaseInstance;

    public EncapsulatedOpenBISService(SessionTokenManager sessionTokenManager, String serverURL,
            int port, String username, String password)
    {
        this(sessionTokenManager, HttpInvokerUtils.createServiceStub(IETLLIMSService.class,
                serverURL + "/rmi-etl", 5), port, username, password);
    }

    public EncapsulatedOpenBISService(SessionTokenManager sessionTokenManager,
            IETLLIMSService service, int port, String username, String password)
    {
        assert sessionTokenManager != null : "Unspecified session token manager.";
        assert service != null : "Given IETLLIMSService implementation can not be null.";
        assert username != null : "Given username can not be null.";
        assert password != null : "Given password can not be null.";

        this.sessionTokenManager = sessionTokenManager;
        this.service = service;
        this.port = port;
        this.username = username;
        this.password = password;
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
        sessionToken = service.authenticate(username, password);
        if (sessionToken == null)
        {
            final String msg =
                    "Authentication failure to openBIS server. Most probable cause: user or password are invalid.";
            throw new ConfigurationFailureException(msg);
        }
        service.registerDataStoreServer(sessionToken, port, sessionTokenManager.drawSessionToken());
    }

    private final void checkSessionToken()
    {
        if (sessionToken == null)
        {
            authenticate();
        }
    }

    private final ExperimentPE primGetBaseExperiment(final SampleIdentifier sampleIdentifier)
    {
        return service.tryToGetBaseExperiment(sessionToken, sampleIdentifier);
    }

    private final void primRegisterDataSet(final DataSetInformation dataSetInformation,
            final ExternalData data)
    {
        service.registerDataSet(sessionToken, dataSetInformation.getSampleIdentifier(),
                data);
    }

    private final SamplePropertyPE[] primGetPropertiesOfSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier)
    {
        return service.tryToGetPropertiesOfTopSampleRegisteredFor(sessionToken, sampleIdentifier);
    }

    private final String primCreateDataSetCode()
    {
        return service.createDataSetCode(sessionToken);
    }
    
    //
    // IEncapsulatedLimsService
    //

    synchronized public final ExperimentPE getBaseExperiment(
            final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Given sample identifier can not be null.";

        checkSessionToken();
        try
        {
            return primGetBaseExperiment(sampleIdentifier);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            return primGetBaseExperiment(sampleIdentifier);
        }
    }

    synchronized public final void registerDataSet(final DataSetInformation dataSetInformation,
            final ExternalData data)
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

    synchronized public final SamplePropertyPE[] getPropertiesOfTopSampleRegisteredFor(
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

    synchronized public final int getVersion()
    {
        checkSessionToken();
        if (version == null)
        {
            version = service.getVersion();
        }
        return version;
    }

    synchronized public final DatabaseInstancePE getHomeDatabaseInstance()
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

    synchronized public ExternalDataPE tryGetDataSet(String sToken, String dataSetCode) throws UserFailureException
    {
        checkSessionToken();
        return service.tryGetDataSet(sToken, dataSetCode);
    }

}