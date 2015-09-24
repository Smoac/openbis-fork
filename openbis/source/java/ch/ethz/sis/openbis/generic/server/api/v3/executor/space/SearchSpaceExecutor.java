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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.space;

import java.util.List;

import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.server.api.v3.executor.common.AbstractSearchObjectManuallyExecutor;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.IdSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.PermIdSearchCriteria;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpaceExecutor extends AbstractSearchObjectManuallyExecutor<SpaceSearchCriteria, SpacePE> implements ISearchSpaceExecutor
{

    @Override
    protected List<SpacePE> listAll()
    {
        return daoFactory.getSpaceDAO().listAllEntities();
    }

    @Override
    protected Matcher getMatcher(ISearchCriteria criteria)
    {
        if (criteria instanceof IdSearchCriteria<?>)
        {
            return new IdMatcher();
        } else if (criteria instanceof PermIdSearchCriteria || criteria instanceof CodeSearchCriteria)
        {
            return new CodeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criteria: " + criteria.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher
    {

        @Override
        protected boolean isMatching(IOperationContext context, SpacePE object, ISearchCriteria criteria)
        {
            Object id = ((IdSearchCriteria<?>) criteria).getId();

            if (id == null)
            {
                return true;
            } else if (id instanceof SpacePermId)
            {
                return object.getCode().equals(((SpacePermId) id).getPermId());
            } else
            {
                throw new IllegalArgumentException("Unknown id: " + id.getClass());
            }
        }

    }

    private class CodeMatcher extends StringFieldMatcher
    {

        @Override
        protected String getFieldValue(SpacePE object)
        {
            return object.getCode();
        }

    }

}
