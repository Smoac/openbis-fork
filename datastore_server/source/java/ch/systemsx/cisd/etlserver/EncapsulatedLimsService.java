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

package ch.systemsx.cisd.etlserver;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.InvalidSessionException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
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
final class EncapsulatedLimsService implements IEncapsulatedLimsService
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, EncapsulatedLimsService.class);

    private final String username;

    private final String password;

    private final IETLLIMSService limsService;

    private String sessionToken; // NOTE: can be changed in parallel by different threads

    private Integer version;

    private DatabaseInstancePE homeDatabaseInstance;

    EncapsulatedLimsService(final IETLLIMSService limsService, final String username,
            final String password)
    {
        assert limsService != null : "Given IETLLIMSService implementation can not be null.";
        assert username != null : "Given username can not be null.";
        assert password != null : "Given password can not be null.";

        this.limsService = limsService;
        this.username = username;
        this.password = password;
    }

    private final void authenticate()
    {
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("Authenticating to LIMS server as user '" + username + "'.");
        }
        sessionToken = limsService.authenticate(username, password);
        if (sessionToken == null)
        {
            final String msg =
                    "Authentication failure to LIMS server. Most probable cause: user or password are invalid.";
            throw new ConfigurationFailureException(msg);
        }
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
        return limsService.tryToGetBaseExperiment(sessionToken, sampleIdentifier);
    }

    private final void primRegisterDataSet(final DataSetInformation dataSetInformation,
            final String procedureTypeCode, final ExternalData data)
    {
        limsService.registerDataSet(sessionToken, dataSetInformation.getSampleIdentifier(),
                procedureTypeCode, data);
    }

    private final SamplePropertyPE[] primGetPropertiesOfSampleRegisteredFor(
            final SampleIdentifier sampleIdentifier)
    {
        return limsService.tryToGetPropertiesOfTopSampleRegisteredFor(sessionToken, sampleIdentifier);
    }

    private final String primCreateDataSetCode()
    {
        return limsService.createDataSetCode(sessionToken);
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
            final String procedureTypeCode, final ExternalData data)
    {
        assert dataSetInformation != null : "missing sample identifier";
        assert procedureTypeCode != null : "missing procedure type";
        assert data != null : "missing data";

        checkSessionToken();
        try
        {
            primRegisterDataSet(dataSetInformation, procedureTypeCode, data);
        } catch (final InvalidSessionException ex)
        {
            authenticate();
            primRegisterDataSet(dataSetInformation, procedureTypeCode, data);
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Registered in openBIS: data set " + dataSetInformation.describe()
                    + " PROCEDURE_TYPE('" + procedureTypeCode + "').");
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
            version = limsService.getVersion();
        }
        return version;
    }

    synchronized public final DatabaseInstancePE getHomeDatabaseInstance()
    {
        checkSessionToken();
        if (homeDatabaseInstance == null)
        {
            homeDatabaseInstance = limsService.getHomeDatabaseInstance(sessionToken);
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

}