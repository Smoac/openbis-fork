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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.IdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.PermIdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpaceExecutor extends AbstractSearchObjectManuallyExecutor<SpaceSearchCriterion, SpacePE> implements ISearchSpaceExecutor
{

    @Override
    protected List<SpacePE> listAll()
    {
        return daoFactory.getSpaceDAO().listAllEntities();
    }

    @Override
    protected Matcher getMatcher(ISearchCriterion criterion)
    {
        if (criterion instanceof IdSearchCriterion<?>)
        {
            return new IdMatcher();
        } else if (criterion instanceof PermIdSearchCriterion || criterion instanceof CodeSearchCriterion)
        {
            return new CodeMatcher();
        } else
        {
            throw new IllegalArgumentException("Unknown search criterion: " + criterion.getClass());
        }
    }

    private class IdMatcher extends SimpleFieldMatcher
    {

        @Override
        protected boolean isMatching(IOperationContext context, SpacePE object, ISearchCriterion criterion)
        {
            Object id = ((IdSearchCriterion<?>) criterion).getId();

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
