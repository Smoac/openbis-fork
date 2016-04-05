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

package ch.ethz.sis.openbis.generic.server.asapi.v3.executor.method;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.Space;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.fetchoptions.SpaceFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.asapi.v3.executor.space.ISearchSpaceExecutor;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.ITranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.translator.entity.space.ISpaceTranslator;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpaceSqlMethodExecutor extends AbstractIdSearchMethodExecutor<Space, SpacePE, SpaceSearchCriteria, SpaceFetchOptions> implements
        ISearchSpaceMethodExecutor
{

    @Autowired
    private ISearchSpaceExecutor searchExecutor;

    @Autowired
    private ISpaceTranslator translator;

    @Override
    protected List<SpacePE> searchPEs(IOperationContext context, SpaceSearchCriteria criteria)
    {
        return searchExecutor.search(context, criteria);
    }

    @Override
    protected ITranslator<Long, Space, SpaceFetchOptions> getTranslator()
    {
        return translator;
    }

}
