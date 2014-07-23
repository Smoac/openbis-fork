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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("AbstractStringValue")
public abstract class AbstractCompositeSearchCriterion extends AbstractSearchCriterion
{

    private static final long serialVersionUID = 1L;

    protected Collection<AbstractSearchCriterion> criteria = new LinkedList<AbstractSearchCriterion>();

    public Collection<ISearchCriterion> getCriteria()
    {
        return Collections.<ISearchCriterion> unmodifiableCollection(criteria);
    }

    public void setCriteria(Collection<AbstractSearchCriterion> criteria)
    {
        this.criteria = criteria;
    }

    protected <T extends AbstractSearchCriterion> T with(T criterion)
    {
        criteria.add(criterion);
        return criterion;
    }

    @Override
    public final String toString()
    {
        return toString("");
    }

    protected final String toString(String indentation)
    {
        return createBuilder().toString(indentation);
    }

    protected SearchCriterionToStringBuilder createBuilder()
    {
        SearchCriterionToStringBuilder builder = new SearchCriterionToStringBuilder();
        builder.setCriteria(criteria);
        return builder;
    }

}
