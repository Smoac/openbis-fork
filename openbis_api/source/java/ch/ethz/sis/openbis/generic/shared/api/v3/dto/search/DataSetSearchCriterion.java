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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("DataSetSearchCriterion")
public class DataSetSearchCriterion extends AbstractEntitySearchCriterion<IDataSetId>
{

    private static final long serialVersionUID = 1L;

    private DataSetSearchRelation relation;

    public DataSetSearchCriterion()
    {
        this(DataSetSearchRelation.DATASET);
    }

    DataSetSearchCriterion(DataSetSearchRelation relation)
    {
        this.relation = relation;
    }

    public DataSetSearchCriterion withParents()
    {
        return with(new DataSetParentsSearchCriterion());
    }

    public DataSetSearchCriterion withChildren()
    {
        return with(new DataSetChildrenSearchCriterion());
    }

    public DataSetSearchCriterion withContainer()
    {
        return with(new DataSetContainerSearchCriterion());
    }

    public DataSetSearchCriterion withOrOperator()
    {
        return (DataSetSearchCriterion) withOperator(SearchOperator.OR);
    }

    public DataSetSearchCriterion withAndOperator()
    {
        return (DataSetSearchCriterion) withOperator(SearchOperator.AND);
    }

    public DataSetSearchRelation getRelation()
    {
        return relation;
    }

    @Override
    protected SearchCriterionToStringBuilder createBuilder()
    {
        SearchCriterionToStringBuilder builder = super.createBuilder();
        builder.setName(relation.name());
        return builder;
    }

}
