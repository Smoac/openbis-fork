/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author pkupczyk
 */
@JsonObject("SearchCriterionToStringBuilder")
public class SearchCriterionToStringBuilder
{

    private String name;

    private SearchOperator operator;

    private Collection<ISearchCriterion> criteria;

    public SearchCriterionToStringBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    public SearchCriterionToStringBuilder setOperator(SearchOperator operator)
    {
        this.operator = operator;
        return this;
    }

    public SearchCriterionToStringBuilder setCriteria(Collection<ISearchCriterion> criteria)
    {
        this.criteria = criteria;
        return this;
    }

    public String toString(String anIndentation)
    {
        StringBuilder sb = new StringBuilder();
        String indentation = anIndentation;

        if (indentation.isEmpty())
        {
            sb.append(name.toUpperCase() + "\n");
        } else
        {
            sb.append(indentation + "with " + name.toLowerCase() + ":\n");
        }

        indentation += "    ";

        if (operator != null)
        {
            sb.append(indentation + "with operator '" + operator + "'\n");
        }

        for (ISearchCriterion criterion : criteria)
        {
            if (criterion instanceof AbstractCompositeSearchCriterion)
            {
                AbstractCompositeSearchCriterion compositeCriterion = (AbstractCompositeSearchCriterion) criterion;
                sb.append(compositeCriterion.toString(indentation));
            } else
            {
                sb.append(indentation + criterion.toString() + "\n");
            }
        }
        return sb.toString();
    }

}
