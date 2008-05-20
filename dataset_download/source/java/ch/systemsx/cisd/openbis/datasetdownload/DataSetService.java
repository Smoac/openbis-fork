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

package ch.systemsx.cisd.openbis.datasetdownload;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.lims.base.IDataSetService;
import ch.systemsx.cisd.lims.base.ILIMSServiceFactory;
import ch.systemsx.cisd.lims.base.ServiceRegistry;
import ch.systemsx.cisd.lims.base.dto.ExternalData;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DataSetService implements IDataSetService
{
    private IDataSetService service;

    DataSetService(ConfigParameters configParameters)
    {
        ILIMSServiceFactory factory = ServiceRegistry.getLIMSServiceFactory();
        service = factory.createDataSetService(configParameters.getServerURL());
    }
    
    public ExternalData getDataSet(String sessionToken, String dataSetCode)
            throws UserFailureException
    {
        return service.getDataSet(sessionToken, dataSetCode);
    }

    public String authenticate(String user, String password) throws UserFailureException
    {
        return service.authenticate(user, password);
    }

    public Void closeSession(String sessionToken) throws UserFailureException
    {
        return service.closeSession(sessionToken);
    }

    public int getVersion()
    {
        return service.getVersion();
    }

}
