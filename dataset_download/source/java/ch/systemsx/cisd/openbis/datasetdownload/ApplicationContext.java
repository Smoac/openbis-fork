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

import ch.systemsx.cisd.lims.base.IDataSetService;

/**
 *  Application context. It contains the object accessing the openBIS for retrieving the data set,
 *  configuration parameters, and the name of the application which will be a part of its URL. 
 *
 * @author Franz-Josef Elmer
 */
class ApplicationContext
{
    private final IDataSetService dataSetService;
    
    private final ConfigParameters configParameters;

    private final String applicationName;
    
    ApplicationContext(IDataSetService service, ConfigParameters configParameters,
            String applicationName)
    {
        this.dataSetService = service;
        this.configParameters = configParameters;
        this.applicationName = applicationName;
    }

    public final IDataSetService getDataSetService()
    {
        return dataSetService;
    }

    public final ConfigParameters getConfigParameters()
    {
        return configParameters;
    }

    public final String getApplicationName()
    {
        return applicationName;
    }
    
}
