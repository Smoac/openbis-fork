/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.generic.shared.IETLLIMSService;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;

/**
 * A <code>IDataSetService</code> implementation.
 * 
 * @author Franz-Josef Elmer
 */
final class DataSetService implements IETLLIMSService
{
    private IETLLIMSService service;

    DataSetService(final ConfigParameters configParameters)
    {
        String url = configParameters.getServerURL() + "/rmi-etl";
        service = HttpInvokerUtils.createServiceStub(IETLLIMSService.class, url, 5);
    }

    //
    // IETLLIMSService
    //

    public final ExternalDataPE tryGetDataSet(final String sessionToken, final String dataSetCode)
            throws UserFailureException
    {
        return service.tryGetDataSet(sessionToken, dataSetCode);
    }

    public final String authenticate(final String user, final String password)
            throws UserFailureException
    {
        return service.authenticate(user, password);
    }

    public final void closeSession(final String sessionToken) throws UserFailureException
    {
        service.closeSession(sessionToken);
    }

    public final int getVersion()
    {
        return service.getVersion();
    }

    public String createDataSetCode(String sessionToken) throws UserFailureException
    {
        return null;
    }

    public DatabaseInstancePE getHomeDatabaseInstance(String sessionToken)
    {
        return null;
    }

    public void registerDataSet(String sessionToken, SampleIdentifier sampleIdentifier,
            String procedureTypeCode, ExternalData externalData) throws UserFailureException
    {
    }

    public ExperimentPE tryToGetBaseExperiment(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        return null;
    }

    public SamplePropertyPE[] tryToGetPropertiesOfTopSampleRegisteredFor(String sessionToken,
            SampleIdentifier sampleIdentifier) throws UserFailureException
    {
        return null;
    }

    public DataStorePE getDataStore(String sessionToken, ExperimentIdentifier experimentIdentifier,
            String dataSetTypeCode) throws UserFailureException
    {
        return null;
    }

    public IAuthSession getSession(String sessionToken) throws UserFailureException
    {
        return null;
    }

}
