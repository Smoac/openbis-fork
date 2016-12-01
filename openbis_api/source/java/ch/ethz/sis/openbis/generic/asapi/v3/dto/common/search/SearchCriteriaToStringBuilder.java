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

package ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author pkupczyk
 */
public class SearchCriteriaToStringBuilder implements Serializable
{

    private static final long serialVersionUID = 1L;

    private String name;

    private SearchOperator operator;

    private Collection<ISearchCriteria> criteria;

    public SearchCriteriaToStringBuilder setName(String name)
    {
        this.name = name;
        return this;
    }

    public SearchCriteriaToStringBuilder setOperator(SearchOperator operator)
    {
        this.operator = operator;
        return this;
    }

    public SearchCriteriaToStringBuilder setCriteria(Collection<ISearchCriteria> criteria)
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

        for (ISearchCriteria aCriteria : criteria)
        {
            if (aCriteria instanceof AbstractCompositeSearchCriteria)
            {
                AbstractCompositeSearchCriteria compositeCriteria = (AbstractCompositeSearchCriteria) aCriteria;
                sb.append(compositeCriteria.toString(indentation));
            } else
            {
                sb.append(indentation + aCriteria.toString() + "\n");
            }
        }
        return sb.toString();
    }

}
