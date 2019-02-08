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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.IdentifierSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.NoProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.search.ProjectSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.NoSpaceSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.space.search.SpaceSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.sample.search.SampleSearchCriteria")
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

    public IdentifierSearchCriteria withIdentifier()
    {
        return with(new IdentifierSearchCriteria());
    }

    public SpaceSearchCriteria withSpace()
    {
        return with(new SpaceSearchCriteria());
    }

    public SampleSearchCriteria withoutSpace()
    {
        with(new NoSpaceSearchCriteria());
        return this;
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

    public SampleTypeSearchCriteria withType()
    {
        return with(new SampleTypeSearchCriteria());
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
