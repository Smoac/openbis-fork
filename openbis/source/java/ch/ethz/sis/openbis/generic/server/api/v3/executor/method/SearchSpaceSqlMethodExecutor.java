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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.ISearchObjectExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.space.ISearchSpaceExecutor;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.api.v3.translator.entity.space.sql.ISpaceSqlTranslator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.space.Space;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.space.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpaceSqlMethodExecutor extends AbstractSearchMethodExecutor<Space, Long, SpaceSearchCriteria, SpaceFetchOptions> implements
        ISearchSpaceMethodExecutor
{

    @Autowired
    private ISearchSpaceExecutor searchExecutor;

    @Autowired
    private ISpaceSqlTranslator translator;

    @Override
    protected ISearchObjectExecutor<SpaceSearchCriteria, Long> getSearchExecutor()
    {
        return new ISearchObjectExecutor<SpaceSearchCriteria, Long>()
            {
                @Override
                public List<Long> search(IOperationContext context, SpaceSearchCriteria criteria)
                {
                    List<SpacePE> spaces = searchExecutor.search(context, criteria);
                    List<Long> ids = new ArrayList<Long>();

                    for (SpacePE space : spaces)
                    {
                        ids.add(space.getId());
                    }

                    return ids;
                }
            };
    }

    @Override
    protected ITranslator<Long, Space, SpaceFetchOptions> getTranslator()
    {
        return translator;
    }

}
