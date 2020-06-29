package ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.SortOrder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.GlobalSearchObject;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.global.search.GlobalSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.AuthorisationInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.auth.ISQLAuthorisationInformationProviderDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.dao.ISQLSearchDAO;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.*;

import java.util.*;
import java.util.stream.Collectors;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.global.fetchoptions.GlobalSearchObjectSortOptions.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.GlobalSearchCriteriaTranslator.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;

public class GlobalSearchManager implements IGlobalSearchManager
{

    private static final String PERM_ID_FIELD_NAME = "Perm ID";

    private static final String DATA_SET_KIND_FIELD_NAME = "DataSet kind";

    private static final String IDENTIFIER_FIELD_NAME = "Identifier";

    private static final String CODE_FIELD_NAME = "Code";

    private static final String PROPERTY_NAME = "Property";

    private static final Map<String, String> SORTING_NAME_TO_RESULT_KEY_MAP = new HashMap<>();

    static
    {
        SORTING_NAME_TO_RESULT_KEY_MAP.put(SCORE, RANK_ALIAS);
        SORTING_NAME_TO_RESULT_KEY_MAP.put(OBJECT_KIND, OBJECT_KIND_ALIAS);
        SORTING_NAME_TO_RESULT_KEY_MAP.put(OBJECT_PERM_ID, PERM_ID_COLUMN);
        SORTING_NAME_TO_RESULT_KEY_MAP.put(OBJECT_IDENTIFIER, IDENTIFIER_ALIAS);
    }

    protected final ISQLAuthorisationInformationProviderDAO authProvider;

    private final ISQLSearchDAO searchDAO;

    public GlobalSearchManager(final ISQLAuthorisationInformationProviderDAO authProvider, final ISQLSearchDAO searchDAO)
    {
        this.searchDAO = searchDAO;
        this.authProvider = authProvider;
    }

    @Override
    public Set<Map<String, Object>> searchForIDs(final Long userId, final AuthorisationInformation authorisationInformation, final GlobalSearchCriteria criteria,
            final String idsColumnName, final TableMapper tableMapper, final GlobalSearchObjectFetchOptions fetchOptions)
    {
        final Set<Map<String, Object>> mainCriteriaIntermediateResults = searchDAO.queryDBWithNonRecursiveCriteria(userId,
                criteria, tableMapper, idsColumnName, authorisationInformation, fetchOptions);

        // If we have results, we use them
        // If we don't have results and criteria are not empty, there are no results.
        final Set<Map<String, Object>> resultBeforeFiltering =
                containsValues(mainCriteriaIntermediateResults) ? mainCriteriaIntermediateResults : Collections.emptySet();

        return filterResultsByUserRights(authorisationInformation, resultBeforeFiltering, tableMapper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> sortRecords(final Set<Map<String, Object>> records,
            final SortOptions<GlobalSearchObject> sortOptions)
    {
        final ArrayList<Map<String, Object>> result = new ArrayList<>(records);
        final List<Sorting> sortingList = sortOptions.getSortings();
        result.sort((o1, o2) ->
        {
            for (final Sorting sorting : sortingList)
            {
                final String resultKey = SORTING_NAME_TO_RESULT_KEY_MAP.get(sorting.getField());
                final Comparable<Object> v1 = (Comparable<Object>) o1.get(resultKey);
                final Object v2 = o2.get(resultKey);

                if (v1 != null && v2 != null && !v1.equals(v2))
                {
                    return sorting.getOrder().isAsc() ? v1.compareTo(v2) : -v1.compareTo(v2);
                } else if (v1 == null && v2 != null)
                {
                    return sortOrderToInt(sorting.getOrder());
                } else if (v1 != null && v2 == null)
                {
                    return -sortOrderToInt(sorting.getOrder());
                }
            }
            return 0;
        });
        return result;
    }

    private static int sortOrderToInt(final SortOrder sortOrder)
    {
        return sortOrder.isAsc() ? 1 : -1;
    }

    /**
     * Checks whether a collection contains any values.
     *
     * @param collection collection to be checked for values.
     * @return {@code false} if collection is {@code null} or empty, true otherwise.
     */
    protected static boolean containsValues(final Collection<?> collection)
    {
        return collection != null && !collection.isEmpty();
    }

    private Set<Map<String, Object>> filterResultsByUserRights(final AuthorisationInformation authorisationInformation,
            final Set<Map<String, Object>> result, final TableMapper tableMapper)
    {
        if (authorisationInformation.isInstanceRole())
        {
            return result;
        } else
        {
            final Set<Long> allIds = result.stream().map(fieldMap -> (Long) fieldMap.get(ID_COLUMN))
                    .collect(Collectors.toSet());
            final Set<Long> filteredIds = doFilterIDsByUserRights(allIds, authorisationInformation, tableMapper);
            return result.stream().filter(fieldMap -> filteredIds.contains((Long) fieldMap.get(ID_COLUMN)))
                    .collect(Collectors.toSet());
        }
    }

    protected Set<Long> doFilterIDsByUserRights(final Set<Long> ids,
            final AuthorisationInformation authorisationInformation, final TableMapper tableMapper)
    {
        switch (tableMapper)
        {
            case SAMPLE:
            {
                return authProvider.getAuthorisedSamples(ids, authorisationInformation);
            }
            
            case EXPERIMENT:
            {
                return authProvider.getAuthorisedExperiments(ids, authorisationInformation);
            }
            
            case DATA_SET:
            {
                return authProvider.getAuthorisedDatasets(ids, authorisationInformation);
            }
            
            case MATERIAL:
            {
                return ids;
            }
            
            default:
            {
                throw new IllegalArgumentException("Full text search does not support this table mapper: "
                        + tableMapper);
            }
        }
    }

    @Override
    public Collection<MatchingEntity> map(final Collection<Map<String, Object>> records, final boolean withMatches)
    {
        return records.stream().map((fieldsMap) ->
        {
            final MatchingEntity matchingEntity = new MatchingEntity();
            matchingEntity.setCode((String) fieldsMap.get(CODE_COLUMN));
            final EntityKind entityKind = EntityKind.valueOf((String) fieldsMap.get(OBJECT_KIND_ALIAS));
            matchingEntity.setEntityKind(entityKind);
            matchingEntity.setId((Long) fieldsMap.get(ID_COLUMN));
            matchingEntity.setPermId((String) fieldsMap.get(PERM_ID_COLUMN));

            final String entityTypesCode = (String) fieldsMap.get(ENTITY_TYPES_CODE_ALIAS);
            if (entityTypesCode != null)
            {
                matchingEntity.setEntityType(new BasicEntityType(entityTypesCode));
            }

            matchingEntity.setIdentifier((String) fieldsMap.get(IDENTIFIER_ALIAS));

            if (entityKind == EntityKind.EXPERIMENT || entityKind == EntityKind.DATA_SET)
            {
                final Space space = new Space();
                space.setCode((String) fieldsMap.get(SPACE_CODE_ALIAS));
                matchingEntity.setSpace(space);
            }

            matchingEntity.setScore((Float) fieldsMap.get(RANK_ALIAS));

            final List<PropertyMatch> matches = new ArrayList<>();

            if (withMatches)
            {
                mapPropertyMatches(fieldsMap, matches);
                mapAttributeMatches(fieldsMap, entityKind, matches);
            }

            matchingEntity.setMatches(matches);
            return matchingEntity;
        }).collect(Collectors.toList());
    }

    private void mapAttributeMatches(final Map<String, Object> fieldsMap, final EntityKind entityKind, final List<PropertyMatch> matches)
    {
        switch (entityKind)
        {
            case MATERIAL:
            {
                mapAttributeMatch(fieldsMap, matches, IDENTIFIER_ALIAS, IDENTIFIER_FIELD_NAME);
                break;
            }

            case EXPERIMENT:
            case SAMPLE:
            {
                mapAttributeMatch(fieldsMap, matches, CODE_MATCH_ALIAS, CODE_FIELD_NAME);
                mapAttributeMatch(fieldsMap, matches, PERM_ID_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                break;
            }

            case DATA_SET:
            {
                mapAttributeMatch(fieldsMap, matches, DATA_SET_KIND_MATCH_ALIAS, DATA_SET_KIND_FIELD_NAME);
                mapAttributeMatch(fieldsMap, matches, CODE_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                mapAttributeMatch(fieldsMap, matches, PERM_ID_MATCH_ALIAS, PERM_ID_FIELD_NAME);
                break;
            }
        }
    }

    private void mapPropertyMatches(final Map<String, Object> fieldsMap, final List<PropertyMatch> matches)
    {
        final String propertyValueMatch = (String) fieldsMap.get(PROPERTY_VALUE_ALIAS);
        if (propertyValueMatch != null)
        {
            addPropertyMatch(propertyValueMatch, fieldsMap, matches);
        }

        final String cvLabelMatch = (String) fieldsMap.get(CV_LABEL_ALIAS);
        if (cvLabelMatch != null)
        {
            addPropertyMatch(cvLabelMatch, fieldsMap, matches);
        }

        final String cvCodeMatch = (String) fieldsMap.get(CV_CODE_ALIAS);
        if (cvCodeMatch != null)
        {
            addPropertyMatch(cvCodeMatch, fieldsMap, matches);
        }
    }

    private void addPropertyMatch(final String codeMatchString, final Map<String, Object> fieldsMap,
            final List<PropertyMatch> matches)
    {
        final PropertyMatch propertyMatch = new PropertyMatch();
        propertyMatch.setCode(PROPERTY_NAME + " '" + fieldsMap.get(PROPERTY_TYPE_LABEL_ALIAS) + "'");
        propertyMatch.setValue(codeMatchString);

        final String headline = coalesceMap(fieldsMap, VALUE_HEADLINE_ALIAS, LABEL_HEADLINE_ALIAS,
                CODE_HEADLINE_ALIAS, DESCRIPTION_HEADLINE_ALIAS);

        if (headline != null)
        {
            computeSpans(propertyMatch, headline);
        }

        matches.add(propertyMatch);
    }

    /**
     * Computes matching text spans for given property match.
     * @param propertyMatch property match to which the spans should be added.
     * @param headline headline string which contains markers of start and stop of matches.
     */
    private void computeSpans(final PropertyMatch propertyMatch, final String headline)
    {
        final List<Span> spans = new ArrayList<>();
        final int startSelLength = START_SEL.length();
        final int stopSelLength = STOP_SEL.length();
        final int combinedSelLength = startSelLength + stopSelLength;
        int cursorIndex = headline.indexOf(START_SEL);
        int matchesCount = 0;
        while (cursorIndex >= 0)
        {
            final int matchStartIndex = cursorIndex;
            final int matchEndIndex = headline.indexOf(STOP_SEL, matchStartIndex + startSelLength);
            final Span span = new Span();
            span.setStart(matchStartIndex - matchesCount * combinedSelLength);
            span.setEnd(matchEndIndex - startSelLength - matchesCount * combinedSelLength);
            spans.add(span);

            cursorIndex = headline.indexOf(START_SEL, matchEndIndex + stopSelLength);
            matchesCount++;
        }

        propertyMatch.setSpans(spans);
    }

    /**
     * Returns first not null value.
     * @param keys keys in the order how the values should be checked.
     * @return the first not null value from map casted to string.
     */
    private String coalesceMap(final Map<String, ?> map, final String... keys)
    {
        return (String) Arrays.stream(keys).map(map::get)
                .filter(Objects::nonNull).findFirst().orElse(null);
    }

    private void mapAttributeMatch(final Map<String, Object> fieldsMap, final List<PropertyMatch> matches,
            final String matchAlias, final String code)
    {
        final Object fieldMatch = fieldsMap.get(matchAlias);
        if (fieldMatch != null)
        {
            final String codeMatchString = (String) fieldMatch;
            final PropertyMatch propertyMatch = new PropertyMatch();
            propertyMatch.setCode(code);
            propertyMatch.setValue(codeMatchString);

            final Span span = new Span();
            span.setStart(0);
            span.setEnd(codeMatchString.length());
            propertyMatch.setSpans(Collections.singletonList(span));
            matches.add(propertyMatch);
        }
    }

}
