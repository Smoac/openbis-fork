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

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.ISampleId;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("dto.search.SampleSearchCriteria")
public class SampleSearchCriteria extends AbstractEntitySearchCriteria<ISampleId>
{

    private static final long serialVersionUID = 1L;

    private SampleSearchRelation relation;

    public SampleSearchCriteria()
    {
        this(SampleSearchRelation.SAMPLE);
    }

    SampleSearchCriteria(SampleSearchRelation relation)
    {
        this.relation = relation;
    }

    public SpaceSearchCriteria withSpace()
    {
        return with(new SpaceSearchCriteria());
    }

    public ProjectSearchCriteria withProject()
    {
        return with(new ProjectSearchCriteria());
    }

    public SampleSearchCriteria withoutProject()
    {
        with(new NoProjectSearchCriteria());
        return this;
    }
    
    public ExperimentSearchCriteria withExperiment()
    {
        return with(new ExperimentSearchCriteria());
    }

    public SampleSearchCriteria withoutExperiment()
    {
        with(new NoExperimentSearchCriteria());
        return this;
    }

    public SampleParentsSearchCriteria withParents()
    {
        return with(new SampleParentsSearchCriteria());
    }

    public SampleChildrenSearchCriteria withChildren()
    {
        return with(new SampleChildrenSearchCriteria());
    }

    public SampleContainerSearchCriteria withContainer()
    {
        return with(new SampleContainerSearchCriteria());
    }

    public SampleSearchCriteria withoutContainer()
    {
        with(new NoSampleContainerSearchCriteria());
        return this;
    }

    public SampleSearchCriteria withOrOperator()
    {
        return (SampleSearchCriteria) withOperator(SearchOperator.OR);
    }

    public SampleSearchCriteria withAndOperator()
    {
        return (SampleSearchCriteria) withOperator(SearchOperator.AND);
    }

    public SampleSearchRelation getRelation()
    {
        return relation;
    }

    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName(relation.name());
        return builder;
    }

}
