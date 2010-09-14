/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.AttributeMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.PropertyMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleAttributeSearchFieldKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SearchCriteriaConnection;

/**
 * Converts {@link SearchCriteria} objects to {@link DetailedSearchCriteria} objects.
 * <p>
 * Clients of this class need to provide a translator from {@link MatchClauseAttribute} to
 * {@link IAttributeSearchFieldKind} appropriate to the entity they are searching for.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class SearchCriteriaToDetailedSearchCriteriaTranslator
{
    private final SearchCriteria searchCriteria;

    private final IMatchClauseAttributeTranslator attributeTranslator;

    private final DetailedSearchCriteria newDetailedSearchCriteria;

    /**
     * Interface for a translator from {@link MatchClauseAttribute} to
     * {@link IAttributeSearchFieldKind}.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    public static interface IMatchClauseAttributeTranslator
    {
        IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute);
    }

    public static class SampleAttributeTranslator implements IMatchClauseAttributeTranslator
    {
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute)
        {
            IAttributeSearchFieldKind ans;
            switch (attribute)
            {
                case CODE:
                    ans = SampleAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = SampleAttributeSearchFieldKind.SAMPLE_TYPE;
                    break;
                default:
                    // Should never get here
                    ans = null;
                    break;
            }
            return ans;
        }

    }

    public SearchCriteriaToDetailedSearchCriteriaTranslator(SearchCriteria searchCriteria,
            IMatchClauseAttributeTranslator attributeTranslator)
    {
        this.searchCriteria = searchCriteria;
        this.attributeTranslator = attributeTranslator;
        newDetailedSearchCriteria = new DetailedSearchCriteria();
    }

    public DetailedSearchCriteria convertToDetailedSearchCriteria()
    {
        // The API is for expert users, we can trust them to provide their own wildcards
        newDetailedSearchCriteria.setUseWildcardSearchMode(true);
        newDetailedSearchCriteria.setConnection(convertSearchOperatorToConnection());
        newDetailedSearchCriteria.setCriteria(convertMatchClausesToDetailedSearchCriterionList());

        return newDetailedSearchCriteria;
    }

    private List<DetailedSearchCriterion> convertMatchClausesToDetailedSearchCriterionList()
    {
        List<MatchClause> searchCriterionList = searchCriteria.getMatchClauses();
        ArrayList<DetailedSearchCriterion> detailedSearchCriterionList =
                new ArrayList<DetailedSearchCriterion>(searchCriterionList.size());
        for (MatchClause matchClause : searchCriterionList)
        {
            detailedSearchCriterionList
                    .add(convertMatchClauseToDetailedSearchCriterion(matchClause));
        }

        return detailedSearchCriterionList;
    }

    private DetailedSearchCriterion convertMatchClauseToDetailedSearchCriterion(
            MatchClause matchClause)
    {
        DetailedSearchCriterion detailedSearchCriterion =
                new DetailedSearchCriterion(extractDetailedSearchField(matchClause), matchClause
                        .getDesiredValue());
        return detailedSearchCriterion;
    }

    private DetailedSearchField extractDetailedSearchField(MatchClause matchClause)
    {
        DetailedSearchField searchField;

        switch (matchClause.getFieldType())
        {
            case PROPERTY:
                searchField =
                        DetailedSearchField.createPropertyField(((PropertyMatchClause) matchClause)
                                .getPropertyCode());
                break;
            case ATTRIBUTE:
                searchField =
                        DetailedSearchField
                                .createAttributeField(attributeTranslator
                                        .convertMatchClauseAttributeToAttributeSearchFieldKind(((AttributeMatchClause) matchClause)
                                                .getAttribute()));
                break;
            default:
                // Should never reach here
                searchField = null;
        }

        return searchField;
    }

    private SearchCriteriaConnection convertSearchOperatorToConnection()
    {
        SearchCriteriaConnection connection;
        switch (searchCriteria.getOperator())
        {
            case MATCH_ALL_CLAUSES:
                connection = SearchCriteriaConnection.MATCH_ALL;
                break;
            case MATCH_ANY_CLAUSES:
                connection = SearchCriteriaConnection.MATCH_ANY;
                break;
            default:
                // Should never get here
                connection = SearchCriteriaConnection.MATCH_ALL;
        }

        return connection;
    }

}
