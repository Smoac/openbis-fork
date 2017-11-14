/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.FetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.common.search.ISearchObjectExecutor;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;

/**
 * @author Franz-Josef Elmer
 */
public abstract class AbstractIdSearchMethodExecutor<OBJECT, OBJECT_PE extends IIdHolder, CRITERIA extends AbstractSearchCriteria, FETCH_OPTIONS extends FetchOptions<OBJECT>>
        extends AbstractSearchMethodExecutor<OBJECT, Long, CRITERIA, FETCH_OPTIONS>
{

    @Override
    protected ISearchObjectExecutor<CRITERIA, Long> getSearchExecutor()
    {
        return new ISearchObjectExecutor<CRITERIA, Long>()
            {
                @Override
                public List<Long> search(IOperationContext context, CRITERIA criteria)
                {
                    List<OBJECT_PE> objectPEs = searchPEs(context, criteria);
                    List<Long> ids = new ArrayList<Long>();
                    for (OBJECT_PE objectPE : objectPEs)
                    {
                        ids.add(objectPE.getId());
                    }
                    return ids;
                }
            };
    }

    protected abstract List<OBJECT_PE> searchPEs(IOperationContext context, CRITERIA criteria);

}
