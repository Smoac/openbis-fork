/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntitySortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.EntityWithPropertiesSortOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.fetchoptions.Sorting;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.buildFullIdentifierConcatenationString;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.*;

public class OrderTranslator
{
    public static final String PROPERTY_CODE_ALIAS = "property_code";

    public static final String TYPE_CODE_ALIAS = "type_code";

    private static final String IDENTIFIER = "IDENTIFIER";

    private static final String UNIQUE_PREFIX = OrderTranslator.class.getName() + ":";

    /** Column name in select for sorting by identifier (which is a generated string). */
    private static final String IDENTIFIER_SORTING_COLUMN = "i";

    public static SelectQuery translateToOrderQuery(final TranslationContext translationContext)
    {
        if (translationContext.getSortOptions() == null)
        {
            throw new IllegalArgumentException("Null sort options provided.");
        }

        final String from = buildFrom(translationContext);
        final String where = buildWhere(translationContext);
        final String select = buildSelect(translationContext);
        final String orderBy = buildOrderBy(translationContext);

        return new SelectQuery(select  + NL + from + NL + where + NL + orderBy, translationContext.getArgs());
    }

    private static String buildOrderBy(final TranslationContext translationContext)
    {
        final StringBuilder orderByBuilder = translationContext.getSortOptions().getSortings().stream().collect(
                StringBuilder::new,
                (stringBuilder, sorting) ->
                {
                    stringBuilder.append(COMMA + SP);
                    appendSortingColumn(translationContext, stringBuilder, sorting, false);
                },
                StringBuilder::append
        );

        return ORDER_BY + orderByBuilder.substring(COMMA.length());
    }

    private static String buildSelect(final TranslationContext translationContext)
    {
        final StringBuilder sqlBuilder = new StringBuilder(
                SELECT + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN);

        translationContext.getSortOptions().getSortings().forEach((sorting) ->
        {
            sqlBuilder.append(COMMA).append(SP);
            appendSortingColumn(translationContext, sqlBuilder, sorting, true);
        });

        return sqlBuilder.toString();
    }

    private static String buildFrom(final TranslationContext translationContext)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final StringBuilder sqlBuilder = new StringBuilder(FROM + SP + tableMapper.getEntitiesTable() + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
        final AtomicInteger indexCounter = new AtomicInteger(1);

        translationContext.getSortOptions().getSortings().forEach((sorting) ->
        {
            final String sortingCriterionFieldName = sorting.getField();
            final Map<Object, Map<String, JoinInformation>> aliases = translationContext.getAliases();
            final Map<String, JoinInformation> joinInformationMap;
            final Object aliasesMapKey;
            if (TranslatorUtils.isPropertySearchFieldName(sortingCriterionFieldName))
            {
                final String propertyName = sortingCriterionFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length()).toLowerCase();
                joinInformationMap = TranslatorUtils.getPropertyJoinInformationMap(tableMapper, () -> getOrderingAlias(indexCounter));
                aliasesMapKey = propertyName;
            } else if (isTypeSearchCriterion(sortingCriterionFieldName) || isSortingByMaterialPermId(translationContext, sortingCriterionFieldName))
            {
                joinInformationMap = TranslatorUtils.getTypeJoinInformationMap(tableMapper, () -> getOrderingAlias(indexCounter));
                aliasesMapKey = EntityWithPropertiesSortOptions.TYPE;
            } else if (isSortingByIdentifierCriterion(sortingCriterionFieldName) && tableMapper != TableMapper.SAMPLE)
            {
                joinInformationMap = TranslatorUtils.getIdentifierJoinInformationMap(tableMapper, () -> getOrderingAlias(indexCounter),
                        UNIQUE_PREFIX);
                aliasesMapKey = UNIQUE_PREFIX;
            } else
            {
                joinInformationMap = null;
                aliasesMapKey = null;
            }

            if (joinInformationMap != null)
            {
                joinInformationMap.values().forEach((joinInformation) -> TranslatorUtils.appendJoin(sqlBuilder, joinInformation));
                aliases.put(aliasesMapKey, joinInformationMap);
            }
        });
        return sqlBuilder.toString();
    }

    private static String buildWhere(final TranslationContext translationContext)
    {
        final StringBuilder sqlBuilder = new StringBuilder(WHERE + SP + SearchCriteriaTranslator.MAIN_TABLE_ALIAS + PERIOD + ID_COLUMN + SP + IN + SP +
                LP + SELECT + SP + UNNEST + LP + QU + RP + RP);
        final Map<Object, Map<String, JoinInformation>> aliases = translationContext.getAliases();
        final List<Object> args = translationContext.getArgs();

        args.add(translationContext.getIds().toArray(new Long[0]));

        translationContext.getSortOptions().getSortings().forEach((sorting) ->
        {
            final String sortingCriteriaFieldName = sorting.getField();
            if (TranslatorUtils.isPropertySearchFieldName(sortingCriteriaFieldName))
            {
                final String fullPropertyName = sortingCriteriaFieldName.substring(
                        EntityWithPropertiesSortOptions.PROPERTY.length());
                final String attributeTypesTableAlias = aliases.get(fullPropertyName.toLowerCase())
                        .get(TableNames.DATA_TYPES_TABLE).getMainTableAlias();

                sqlBuilder.append(SP).append(AND).append(SP);
                TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, attributeTypesTableAlias,
                        TranslatorUtils.isPropertyInternal(fullPropertyName));

                sqlBuilder.append(SP).append(AND).append(SP).append(attributeTypesTableAlias).append(PERIOD)
                        .append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
                args.add(TranslatorUtils.normalisePropertyName(fullPropertyName));
            }
        });

        return sqlBuilder.toString();
    }

    /**
     * Appends sorting column to SQL builder. In some cases it can be more columns. Adds type casting when needed.
     *
     * @param translationContext order translation context.
     * @param sqlBuilder string builder to which the column should be appended.
     * @param sorting sorting parameters.
     * @param inSelect {@code true} if this method is used in the {@code SELECT} clause.
     */
    private static void appendSortingColumn(final TranslationContext translationContext, final StringBuilder sqlBuilder, final Sorting sorting,
            final boolean inSelect)
    {
        final String sortingCriteriaFieldName = sorting.getField();
        final Map<String, JoinInformation> aliases = translationContext.getAliases().get(UNIQUE_PREFIX);
        final TableMapper tableMapper = translationContext.getTableMapper();
        if (TranslatorUtils.isPropertySearchFieldName(sortingCriteriaFieldName))
        {
            final String propertyName = sortingCriteriaFieldName.substring(EntityWithPropertiesSortOptions.PROPERTY.length());
            final String propertyNameLowerCase = propertyName.toLowerCase();
            final String valuesTableAlias = translationContext.getAliases().get(propertyNameLowerCase).get(tableMapper.getEntityTypesAttributeTypesTable()).getMainTableAlias();
            sqlBuilder.append(valuesTableAlias).append(PERIOD).append(VALUE_COLUMN);

            final String casting = translationContext.getDataTypeByPropertyName().get(propertyName);
            if (casting != null)
            {
                sqlBuilder.append(DOUBLE_COLON).append(casting.toLowerCase());
            }
        } else if (isTypeSearchCriterion(sortingCriteriaFieldName))
        {
            final String typesTableAlias = translationContext.getAliases().get(EntityWithPropertiesSortOptions.TYPE).get(tableMapper.getEntityTypesTable()).
                    getSubTableAlias();
            sqlBuilder.append(typesTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingByIdentifierCriterion(sortingCriteriaFieldName))
        {
            if (inSelect)
            {
                if (tableMapper != TableMapper.SAMPLE)
                {
                    final JoinInformation entitiesTableAlias = aliases.get(tableMapper.getEntitiesTable());
                    final JoinInformation spacesTableAlias = aliases.get(UNIQUE_PREFIX + SPACES_TABLE);
                    final JoinInformation projectsTableAlias = aliases.get(UNIQUE_PREFIX + PROJECTS_TABLE);
                    buildFullIdentifierConcatenationString(sqlBuilder,
                            (spacesTableAlias != null) ? spacesTableAlias.getSubTableAlias() : null,
                            (projectsTableAlias != null) ? projectsTableAlias.getSubTableAlias() : null,
                            (entitiesTableAlias != null) ? entitiesTableAlias.getSubTableAlias() : null, false);
                } else
                {
                    sqlBuilder.append(MAIN_TABLE_ALIAS).append(PERIOD).append(SAMPLE_IDENTIFIER_COLUMN);
                }

                sqlBuilder.append(SP);
            }
            sqlBuilder.append(IDENTIFIER_SORTING_COLUMN);
        } else if (isSortingByMaterialPermId(translationContext, sortingCriteriaFieldName))
        {
            final String materialTypeTableAlias = translationContext.getAliases().get(EntityWithPropertiesSortOptions.TYPE)
                    .get(tableMapper.getEntityTypesTable()).getSubTableAlias();
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN);

            if (!inSelect)
            {
                sqlBuilder.append(SP).append(sorting.getOrder());
            }

            sqlBuilder.append(COMMA).append(SP).append(materialTypeTableAlias).append(PERIOD).append(CODE_COLUMN);
        } else if (isSortingBySpaceModificationDate(translationContext, sortingCriteriaFieldName))
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(REGISTRATION_TIMESTAMP_COLUMN.toLowerCase());
        } else
        {
            final String lowerCaseSortingCriteriaFieldName = sortingCriteriaFieldName.toLowerCase();
            final String fieldName = AttributesMapper.getColumnName(lowerCaseSortingCriteriaFieldName, tableMapper.getEntitiesTable(),
                    lowerCaseSortingCriteriaFieldName);
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(fieldName);
        }

        if (!inSelect)
        {
            sqlBuilder.append(SP).append(sorting.getOrder());
        }
    }

    private static boolean isSortingBySpaceModificationDate(final TranslationContext translationContext, final String sortingCriteriaFieldName)
    {
        return EntitySortOptions.MODIFICATION_DATE.equals(sortingCriteriaFieldName) && translationContext.getTableMapper() == TableMapper.SPACE;
    }

    private static boolean isSortingByMaterialPermId(final TranslationContext translationContext, final String sortingCriteriaFieldName)
    {
        return EntitySortOptions.PERM_ID.equals(sortingCriteriaFieldName) && translationContext.getTableMapper() == TableMapper.MATERIAL;
    }

    private static String getOrderingAlias(final AtomicInteger num)
    {
        return "o" + num.getAndIncrement();
    }

    private static boolean isSortingByIdentifierCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(IDENTIFIER);
    }

    private static boolean isTypeSearchCriterion(final String sortingCriteriaFieldName)
    {
        return sortingCriteriaFieldName.equals(EntityWithPropertiesSortOptions.TYPE);
    }

    public static SelectQuery translateToSearchTypeQuery(final TranslationContext translationContext)
    {
        final TableMapper tableMapper = translationContext.getTableMapper();
        final String queryString = SELECT + SP + DISTINCT + SP + "o3" + PERIOD + CODE_COLUMN + SP + PROPERTY_CODE_ALIAS + COMMA + SP +
                "o4" + PERIOD + CODE_COLUMN + SP + TYPE_CODE_ALIAS + NL +
                FROM + SP + tableMapper.getAttributeTypesTable() + SP + "o3" + SP + NL +
                INNER_JOIN + SP + DATA_TYPES_TABLE + SP + "o4" + SP +
                ON + SP + "o3" + PERIOD + tableMapper.getAttributeTypesTableDataTypeIdField() + SP + EQ + SP + "o4" + PERIOD + ID_COLUMN + NL +
                WHERE + SP + "o4" + PERIOD + CODE_COLUMN + SP + IN + SP + LP + SELECT + SP + UNNEST + LP + QU + RP + RP;

        return new SelectQuery(queryString, Collections.singletonList(translationContext.getTypesToFilter()));
    }

}
