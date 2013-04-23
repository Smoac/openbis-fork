/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.knime.file;

import java.io.File;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.OpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class DataSetRegistrator
{
    public void register(String url, String userID, String password, NewDataSetDTO newDataset, File dataSetFile)
    {
        IOpenbisServiceFacade service =
                OpenbisServiceFacadeFactory.tryCreate(userID, password, url, 10000);
        service.putDataSet(newDataset, dataSetFile);
    }
}
