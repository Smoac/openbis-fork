/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.ipad.v2.server;

import java.util.Arrays;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.generic.shared.api.internal.v2.ISearchService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.api.ISimpleTableModelBuilderAdaptor;

/**
 * Abstract superclass for the handlers for the SEARCH request.
 * 
 * @author cramakri
 */
public class SearchRequestHandler extends AbstractRequestHandler
{

    /**
     * Abstract Handler for the SEARCH request.
     * 
     * @param parameters
     * @param builder
     * @param searchService
     * @param optionalHeaders
     */
    protected SearchRequestHandler(Map<String, Object> parameters,
            ISimpleTableModelBuilderAdaptor builder, ISearchService searchService)
    {
        super(parameters, builder, searchService, Arrays.asList("CATEGORY", "SUMMARY_HEADER",
                "SUMMARY", "CHILDREN"));
    }

    /**
     * A helper method to get the value of the search text parameter.
     * 
     * @return The search text parameter or an empty string if no search text was specified.
     */
    protected String getSearchTextParameter()
    {
        String searchText = (String) parameters.get("searchtext");
        if (null == searchText)
        {
            return "";
        }
        return searchText;
    }

    /**
     * A helper method to get a search criteria for the search text parameter.
     * 
     * @return A search criteria or null if the search text is empty.
     */
    protected SearchCriteria trySearchCriteria()
    {
        String searchText = getSearchTextParameter();
        if (searchText.trim().isEmpty())
        {
            return null;
        }
        SearchCriteria sc = new SearchCriteria();
        sc.addMatchClause(MatchClause.createAnyFieldMatch(searchText));
        return sc;
    }

}
