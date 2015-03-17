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

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ch.ethz.sis.openbis.generic.server.api.v3.executor.IOperationContext;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.SpacePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.AnyStringValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.CodeSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.ISearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.IdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.PermIdSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SearchOperator;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.SpaceSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringContainsValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringEndsWithValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringEqualToValue;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringFieldSearchCriterion;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.search.StringStartsWithValue;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;

/**
 * @author pkupczyk
 */
@Component
public class SearchSpaceExecutor implements ISearchSpaceExecutor
{

    @Autowired
    protected IDAOFactory daoFactory;

    @Override
    public List<SpacePE> search(IOperationContext context, SpaceSearchCriterion criterion)
    {
        if (context == null)
        {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (criterion == null)
        {
            throw new IllegalArgumentException("Criterion cannot be null");
        }

        List<SpacePE> allSpaces = daoFactory.getSpaceDAO().listAllEntities();
        List<SpacePE> matchingSpaces = new ArrayList<SpacePE>();

        for (SpacePE space : allSpaces)
        {
            if (isMatching(space, criterion))
            {
                matchingSpaces.add(space);
            }
        }

        return matchingSpaces;
    }

    private boolean isMatching(SpacePE space, SpaceSearchCriterion criterion)
    {
        if (criterion.getCriteria() == null || criterion.getCriteria().isEmpty())
        {
            return true;
        } else
        {
            boolean matchingAll = true;
            boolean matchingAny = false;

            for (ISearchCriterion subCriterion : criterion.getCriteria())
            {
                boolean matching = isMatching(space, subCriterion);

                matchingAll = matchingAll && matching;
                matchingAny = matchingAny || matching;
            }

            if (SearchOperator.AND.equals(criterion.getOperator()))
            {
                return matchingAll;
            } else if (SearchOperator.OR.equals(criterion.getOperator()))
            {
                return matchingAny;
            } else
            {
                throw new IllegalArgumentException("Unknown search operator: " + criterion.getOperator());
            }
        }
    }

    private boolean isMatching(SpacePE space, ISearchCriterion criterion)
    {
        if (criterion == null)
        {
            return true;
        } else if (criterion instanceof IdSearchCriterion<?>)
        {
            return isMatchingId(space, (IdSearchCriterion<?>) criterion);
        } else if (criterion instanceof PermIdSearchCriterion || criterion instanceof CodeSearchCriterion)
        {
            return isMatchingCode(space, (StringFieldSearchCriterion) criterion);
        } else
        {
            throw new IllegalArgumentException("Unknown search criterion: " + criterion.getClass());
        }
    }

    private boolean isMatchingId(SpacePE space, IdSearchCriterion<?> criterion)
    {
        Object id = criterion.getId();

        if (id == null)
        {
            return true;
        } else if (id instanceof SpacePermId)
        {
            return space.getCode().equals(((SpacePermId) id).getPermId());
        } else
        {
            throw new IllegalArgumentException("Unknown search criterion: " + criterion.getClass());
        }
    }

    private boolean isMatchingCode(SpacePE space, StringFieldSearchCriterion criterion)
    {
        AbstractStringValue fieldValue = criterion.getFieldValue();

        if (fieldValue == null || fieldValue.getValue() == null || fieldValue instanceof AnyStringValue)
        {
            return true;
        }

        String code = space.getCode().toLowerCase();
        String value = fieldValue.getValue().toLowerCase();

        if (fieldValue instanceof StringEqualToValue)
        {
            return code.equals(value);
        } else if (fieldValue instanceof StringContainsValue)
        {
            return code.contains(value);
        } else if (fieldValue instanceof StringStartsWithValue)
        {
            return code.startsWith(value);
        } else if (fieldValue instanceof StringEndsWithValue)
        {
            return code.endsWith(value);
        } else
        {
            throw new IllegalArgumentException("Unknown search criterion: " + criterion.getClass());
        }
    }
}
