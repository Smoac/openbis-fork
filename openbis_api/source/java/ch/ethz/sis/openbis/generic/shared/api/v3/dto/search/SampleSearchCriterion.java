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

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("SampleSearchCriterion")
public class SampleSearchCriterion extends AbstractEntitySearchCriterion
{

    private static final long serialVersionUID = 1L;

    private SampleSearchRelation relation;

    public SampleSearchCriterion()
    {
        this(SampleSearchRelation.SAMPLE);
    }

    SampleSearchCriterion(SampleSearchRelation relation)
    {
        this.relation = relation;
    }

    public SpaceSearchCriterion withSpace()
    {
        return with(new SpaceSearchCriterion());
    }

    public ExperimentSearchCriterion withExperiment()
    {
        return with(new ExperimentSearchCriterion());
    }

    public SampleParentsSearchCriterion withParents()
    {
        return with(new SampleParentsSearchCriterion());
    }

    public SampleChildrenSearchCriterion withChildren()
    {
        return with(new SampleChildrenSearchCriterion());
    }

    public SampleContainerSearchCriterion withContainer()
    {
        return with(new SampleContainerSearchCriterion());
    }

    public SampleSearchCriterion withOrOperator()
    {
        return (SampleSearchCriterion) withOperator(SearchOperator.OR);
    }

    public SampleSearchCriterion withAndOperator()
    {
        return (SampleSearchCriterion) withOperator(SearchOperator.AND);
    }

    public SampleSearchRelation getRelation()
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
