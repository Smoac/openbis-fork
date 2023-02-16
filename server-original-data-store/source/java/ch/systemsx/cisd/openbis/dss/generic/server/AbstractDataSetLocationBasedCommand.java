/*
 * Copyright ETH 2011 - 2023 Zürich, Scientific IT Services
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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IDatasetLocation;

/**
 * Abstract superclass of implementations of {@link IDataSetCommand} which are based on a list of {@link IDatasetLocation} instances.
 * 
 * @author Piotr Buczek
 */
abstract class AbstractDataSetLocationBasedCommand implements IDataSetCommand
{
    private static final long serialVersionUID = 1L;

    protected final List<? extends IDatasetLocation> dataSets;

    AbstractDataSetLocationBasedCommand(List<? extends IDatasetLocation> dataSets)
    {
        this.dataSets = dataSets;
    }

    @Override
    public List<String> getDataSetCodes()
    {
        List<String> result = new ArrayList<String>();
        for (IDatasetLocation dataSet : dataSets)
        {
            result.add(dataSet.getDataSetCode());
        }
        return result;
    }

}
