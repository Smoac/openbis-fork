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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.AttributeMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.PropertyMatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchableEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AssociatedEntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchCriterion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchField;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DetailedSearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentAttributeSearchFieldKind;
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
    private final static String INVALID_SEARCH_ATTRIBUTE_TEMPLATE =
            "%s is not a valid search attribute for %s";

    static Map<SearchableEntityKind, IMatchClauseAttributeTranslator> translators =
            new HashMap<SearchableEntityKind, IMatchClauseAttributeTranslator>();

    static
    {
        translators.put(SearchableEntityKind.EXPERIMENT, new ExperimentAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE_PARENT, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE_CHILD, new SampleAttributeTranslator());
        translators.put(SearchableEntityKind.SAMPLE_CONTAINER, new SampleAttributeTranslator());
    }

    private static IMatchClauseAttributeTranslator translatorFor(SearchableEntityKind entityKind)
    {
        return translators.get(entityKind);
    }

    private static void throwInvalidSearchAttributeException(MatchClauseAttribute attribute,
            SearchableEntityKind entityKind) throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException(String.format(INVALID_SEARCH_ATTRIBUTE_TEMPLATE,
                attribute, entityKind));
    }

    private static AssociatedEntityKind convertToAssociatedEntityKind(
            SearchableEntityKind entityKind)
    {
        switch (entityKind)
        {
            case SAMPLE:
                return AssociatedEntityKind.SAMPLE;
            case EXPERIMENT:
                return AssociatedEntityKind.EXPERIMENT;
            case SAMPLE_CONTAINER:
                return AssociatedEntityKind.SAMPLE_CONTAINER;
            case SAMPLE_PARENT:
                return AssociatedEntityKind.SAMPLE_PARENT;
            case SAMPLE_CHILD:
                return AssociatedEntityKind.SAMPLE_CHILD;
        }
        return null; // can't happen
    }

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
            final IAttributeSearchFieldKind ans;
            switch (attribute)
            {
                case CODE:
                    ans = SampleAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = SampleAttributeSearchFieldKind.SAMPLE_TYPE;
                    break;
                case SPACE:
                    ans = SampleAttributeSearchFieldKind.SPACE;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.SAMPLE);
                    ans = null; // for Eclipse
            }
            return ans;
        }

    }

    public static class ExperimentAttributeTranslator implements IMatchClauseAttributeTranslator
    {
        public IAttributeSearchFieldKind convertMatchClauseAttributeToAttributeSearchFieldKind(
                MatchClauseAttribute attribute)
        {
            final IAttributeSearchFieldKind ans;
            switch (attribute)
            {
                case CODE:
                    ans = ExperimentAttributeSearchFieldKind.CODE;
                    break;
                case TYPE:
                    ans = ExperimentAttributeSearchFieldKind.EXPERIMENT_TYPE;
                    break;
                case SPACE:
                    ans = ExperimentAttributeSearchFieldKind.PROJECT_SPACE;
                    break;
                case PROJECT:
                    ans = ExperimentAttributeSearchFieldKind.PROJECT;
                    break;
                default:
                    throwInvalidSearchAttributeException(attribute, SearchableEntityKind.SAMPLE);
                    ans = null; // for Eclipse
            }
            return ans;
        }

    }

    public static DetailedSearchCriteria convert(SearchableEntityKind entityKind,
            SearchCriteria criteria)
    {
        DetailedSearchCriteria mainCriteria =
                new SearchCriteriaToDetailedSearchCriteriaTranslator(criteria, entityKind)
                        .convertToDetailedSearchCriteria();
        for (SearchSubCriteria subCriteria : criteria.getSubCriterias())
        {
            DetailedSearchSubCriteria detailedSearchSubCriteria =
                    SearchCriteriaToDetailedSearchCriteriaTranslator
                            .convertToDetailedSearchSubCriteria(subCriteria);
            mainCriteria.addSubCriteria(detailedSearchSubCriteria);
        }
        return mainCriteria;
    }

    // exposed for tests
    static DetailedSearchSubCriteria convertToDetailedSearchSubCriteria(
            SearchSubCriteria subCriteria)
    {
        final SearchCriteria criteria = subCriteria.getCriteria();
        final SearchableEntityKind targetEntityKind = subCriteria.getTargetEntityKind();
        final DetailedSearchCriteria detailedSearchCriteria = convert(targetEntityKind, criteria);
        return new DetailedSearchSubCriteria(convertToAssociatedEntityKind(targetEntityKind),
                detailedSearchCriteria);
    }

    public SearchCriteriaToDetailedSearchCriteriaTranslator(SearchCriteria searchCriteria,
            SearchableEntityKind entityKind)
    {
        this(searchCriteria, translatorFor(entityKind));
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
                new DetailedSearchCriterion(extractDetailedSearchField(matchClause),
                        matchClause.getDesiredValue());
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
