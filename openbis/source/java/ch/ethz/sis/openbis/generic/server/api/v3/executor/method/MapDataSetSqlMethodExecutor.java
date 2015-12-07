/*
 * Copyright 2015 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.as.api.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.IMapObjectByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.dataset.IMapDataSetTechIdByIdExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.dataset.IDataSetTranslator;

/**
 * @author pkupczyk
 */
@Component
public class MapDataSetSqlMethodExecutor extends AbstractMapMethodExecutor<IDataSetId, Long, DataSet, DataSetFetchOptions> implements
        IMapDataSetMethodExecutor
{

    @Autowired
    private IMapDataSetTechIdByIdExecutor mapExecutor;

    @Autowired
    private IDataSetTranslator translator;

    @Override
    protected IMapObjectByIdExecutor<IDataSetId, Long> getMapExecutor()
    {
        return mapExecutor;
    }

    @Override
    protected ITranslator<Long, DataSet, DataSetFetchOptions> getTranslator()
    {
        return translator;
    }

}
