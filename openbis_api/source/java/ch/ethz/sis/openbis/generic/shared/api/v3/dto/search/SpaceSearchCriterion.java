/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.shared.api.v3.dto.search;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.space.ISpaceId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.search.SpaceSearchCriterion")
public class SpaceSearchCriterion extends AbstractObjectSearchCriterion<ISpaceId>
{

    private static final long serialVersionUID = 1L;

    public SpaceSearchCriterion()
    {
    }

    public CodeSearchCriterion withCode()
    {
        return with(new CodeSearchCriterion());
    }

    public PermIdSearchCriterion withPermId()
    {
        return with(new PermIdSearchCriterion());
    }

    public SpaceSearchCriterion withOrOperator()
    {
        return (SpaceSearchCriterion) withOperator(SearchOperator.OR);
    }

    public SpaceSearchCriterion withAndOperator()
    {
        return (SpaceSearchCriterion) withOperator(SearchOperator.AND);
    }

    @Override
    protected SearchCriterionToStringBuilder createBuilder()
    {
        SearchCriterionToStringBuilder builder = super.createBuilder();
        builder.setName("SPACE");
        return builder;
    }

}
