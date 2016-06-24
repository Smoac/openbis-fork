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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractEntitySearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.ExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.search.NoExperimentSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.NoSampleSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.search.SampleSearchCriteria;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("as.dto.dataset.search.DataSetSearchCriteria")
public class DataSetSearchCriteria extends AbstractEntitySearchCriteria<IDataSetId>
{

    private static final long serialVersionUID = 1L;

    private DataSetSearchRelation relation;

    public DataSetSearchCriteria()
    {
        this(DataSetSearchRelation.DATASET);
    }

    DataSetSearchCriteria(DataSetSearchRelation relation)
    {
        this.relation = relation;
    }

    public DataSetTypeSearchCriteria withType()
    {
        return with(new DataSetTypeSearchCriteria());
    }

    public PhysicalDataSearchCriteria withPhysicalData()
    {
        return with(new PhysicalDataSearchCriteria());
    }

    public LinkedDataSearchCriteria withLinkedData()
    {
        return with(new LinkedDataSearchCriteria());
    }

    public DataSetSearchCriteria withParents()
    {
        return with(new DataSetParentsSearchCriteria());
    }

    public DataSetSearchCriteria withChildren()
    {
        return with(new DataSetChildrenSearchCriteria());
    }

    public DataSetSearchCriteria withContainer()
    {
        return with(new DataSetContainerSearchCriteria());
    }

    public ExperimentSearchCriteria withExperiment()
    {
        return with(new ExperimentSearchCriteria());
    }

    public DataSetSearchCriteria withoutExperiment()
    {
        with(new NoExperimentSearchCriteria());
        return this;
    }

    public SampleSearchCriteria withSample()
    {
        return with(new SampleSearchCriteria());
    }

    public DataSetSearchCriteria withoutSample()
    {
        with(new NoSampleSearchCriteria());
        return this;
    }

    public DataSetSearchCriteria withOrOperator()
    {
        return (DataSetSearchCriteria) withOperator(SearchOperator.OR);
    }

    public DataSetSearchCriteria withAndOperator()
    {
        return (DataSetSearchCriteria) withOperator(SearchOperator.AND);
    }

    public DataSetSearchRelation getRelation()
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
