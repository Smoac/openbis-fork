/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractCompositeSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchCriteriaToStringBuilder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchOperator;
import ch.systemsx.cisd.base.annotation.JsonObject;

/**
 * @author Jakub Straszewski
 */
@JsonObject("as.dto.global.search.GlobalSearchCriteria")
public class GlobalSearchCriteria extends AbstractCompositeSearchCriteria
{

    private static final long serialVersionUID = 1L;

    public GlobalSearchTextCriteria withText()
    {
        return with(new GlobalSearchTextCriteria());
    }

    public GlobalSearchObjectKindCriteria withObjectKind()
    {
        return with(new GlobalSearchObjectKindCriteria());
    }

    public GlobalSearchWildCardsCriteria withWildCards()
    {
        return with(new GlobalSearchWildCardsCriteria());
    }

    public GlobalSearchCriteria withOrOperator()
    {
        return (GlobalSearchCriteria) withOperator(SearchOperator.OR);
    }

    public GlobalSearchCriteria withAndOperator()
    {
        return (GlobalSearchCriteria) withOperator(SearchOperator.AND);
    }


    @Override
    protected SearchCriteriaToStringBuilder createBuilder()
    {
        SearchCriteriaToStringBuilder builder = super.createBuilder();
        builder.setName("GLOBAL_SEARCH");
        return builder;
    }
}
