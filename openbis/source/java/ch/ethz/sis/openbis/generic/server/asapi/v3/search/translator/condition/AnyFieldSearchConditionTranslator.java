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

package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.*;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.SimplePropertyValidator;

import java.util.*;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.DateFieldSearchCriteria.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils.appendTsVectorMatch;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.*;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;

public class AnyFieldSearchConditionTranslator implements IConditionTranslator<AnyFieldSearchCriteria>
{

    private static final String UNIQUE_PREFIX = AnyFieldSearchConditionTranslator.class.getName();

    private static final String REGISTRATOR_JOIN_INFORMATION_KEY = "registrator";

    private static final String MODIFIER_JOIN_INFORMATION_KEY = "modifier";

    private static final String ENTITY_TYPE_JOIN_INFORMATION_KEY = "entity_type";

    private static final Map<Class<? extends IDateFormat>, String> TRUNCATION_INTERVAL_BY_DATE_FORMAT =
            new HashMap<>(3);

    static
    {
        TRUNCATION_INTERVAL_BY_DATE_FORMAT.put(SHORT_DATE_FORMAT.getClass(), "day");
        TRUNCATION_INTERVAL_BY_DATE_FORMAT.put(NORMAL_DATE_FORMAT.getClass(), "minute");
        TRUNCATION_INTERVAL_BY_DATE_FORMAT.put(LONG_DATE_FORMAT.getClass(), "second");
    }

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final AnyFieldSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        final Map<String, JoinInformation> result = TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
        TranslatorUtils.appendIdentifierJoinInformationMap(result, tableMapper, aliasFactory, UNIQUE_PREFIX);

        if (tableMapper.hasRegistrator())
        {
            final JoinInformation registratorJoinInformation = new JoinInformation();
            registratorJoinInformation.setJoinType(JoinType.LEFT);
            registratorJoinInformation.setMainTable(tableMapper.getEntitiesTable());
            registratorJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
            registratorJoinInformation.setMainTableIdField(PERSON_REGISTERER_COLUMN);
            registratorJoinInformation.setSubTable(PERSONS_TABLE);
            registratorJoinInformation.setSubTableAlias(aliasFactory.createAlias());
            registratorJoinInformation.setSubTableIdField(ID_COLUMN);
            result.put(REGISTRATOR_JOIN_INFORMATION_KEY, registratorJoinInformation);
        }

        if (tableMapper.hasModifier())
        {
            final JoinInformation registratorJoinInformation = new JoinInformation();
            registratorJoinInformation.setJoinType(JoinType.LEFT);
            registratorJoinInformation.setMainTable(tableMapper.getEntitiesTable());
            registratorJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
            registratorJoinInformation.setMainTableIdField(PERSON_MODIFIER_COLUMN);
            registratorJoinInformation.setSubTable(PERSONS_TABLE);
            registratorJoinInformation.setSubTableAlias(aliasFactory.createAlias());
            registratorJoinInformation.setSubTableIdField(ID_COLUMN);
            result.put(MODIFIER_JOIN_INFORMATION_KEY, registratorJoinInformation);
        }

        final JoinInformation typeJoinInformation = new JoinInformation();
        typeJoinInformation.setJoinType(JoinType.LEFT);
        typeJoinInformation.setMainTable(tableMapper.getEntitiesTable());
        typeJoinInformation.setMainTableAlias(MAIN_TABLE_ALIAS);
        typeJoinInformation.setMainTableIdField(tableMapper.getEntitiesTableEntityTypeIdField());
        typeJoinInformation.setSubTable(tableMapper.getEntityTypesTable());
        typeJoinInformation.setSubTableAlias(aliasFactory.createAlias());
        typeJoinInformation.setSubTableIdField(ID_COLUMN);
        result.put(ENTITY_TYPE_JOIN_INFORMATION_KEY, typeJoinInformation);

        return result;
    }

    @Override
    public void translate(final AnyFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ANY_FIELD:
            {
                final String alias = SearchCriteriaTranslator.MAIN_TABLE_ALIAS;
                final AbstractStringValue value = criterion.getFieldValue();
                final boolean useWildcards = criterion.isUseWildcards();
                final String stringValue = TranslatorUtils.stripQuotationMarks(value.getValue());
                final Set<PSQLTypes> compatiblePSQLTypesForValue = findCompatibleSqlTypesForValue(stringValue);
                final boolean equalsToComparison = (value.getClass() == StringEqualToValue.class);
                final String separator = SP + OR + SP;
                final int separatorLength = separator.length();

                sqlBuilder.append(LP);

                AnyPropertySearchConditionTranslator.doTranslate(criterion, tableMapper, args, sqlBuilder, aliases);
                sqlBuilder.append(separator);

                if (value.getClass() != StringMatchesValue.class)
                {
                    IdentifierSearchConditionTranslator.doTranslate(criterion, tableMapper, args, sqlBuilder, aliases,
                            UNIQUE_PREFIX);
                    sqlBuilder.append(separator);

                    translateEntityTypeMatch(value, args, sqlBuilder, aliases, useWildcards);
                    sqlBuilder.append(separator);

                    if (tableMapper.hasRegistrator())
                    {
                        translateUserMatch(value, args, sqlBuilder, aliases, REGISTRATOR_JOIN_INFORMATION_KEY,
                                useWildcards);
                        sqlBuilder.append(separator);
                    }
                    if (tableMapper.hasModifier())
                    {
                        translateUserMatch(value, args, sqlBuilder, aliases, MODIFIER_JOIN_INFORMATION_KEY,
                                useWildcards);
                        sqlBuilder.append(separator);
                    }

                    final StringBuilder resultSqlBuilder = tableMapper.getFieldToSQLTypeMap().entrySet().stream()
                            .collect(
                            StringBuilder::new,
                            (stringBuilder, fieldToSQLTypesEntry) ->
                            {
                                final String fieldName = fieldToSQLTypesEntry.getKey();
                                final PSQLTypes fieldSQLType = fieldToSQLTypesEntry.getValue();
                                final boolean includeColumn = compatiblePSQLTypesForValue.contains(fieldSQLType);

                                if (CODE_COLUMN.equals(fieldName))
                                {
                                    stringBuilder.append(separator);
                                    CodeSearchConditionTranslator.translateSearchByCodeCondition(stringBuilder,
                                            tableMapper, value.getClass(), stringValue, useWildcards, args);
                                } else
                                {
                                    if (equalsToComparison || fieldSQLType == TIMESTAMP_WITH_TZ)
                                    {
                                        if (includeColumn)
                                        {
                                            if (fieldSQLType == TIMESTAMP_WITH_TZ)
                                            {
                                                final Optional<Object[]> dateFormatWithResultOptional = DATE_FORMATS
                                                        .stream().map(dateFormat ->

                                                            {final Date formattedValue = formatValue(stringValue,
                                                                    dateFormat);
                                                            return (formattedValue == null) ? null
                                                                    : new Object[]{TRUNCATION_INTERVAL_BY_DATE_FORMAT
                                                                    .get(dateFormat.getClass()), formattedValue};
                                                        }).filter(Objects::nonNull).findFirst();

                                                dateFormatWithResultOptional.ifPresent(dateFormatWithResult ->
                                                {
                                                    stringBuilder.append(separator).append(DATE_TRUNC).append(LP);
                                                    stringBuilder.append(SQ).append(dateFormatWithResult[0]).append(SQ)
                                                            .append(COMMA).append(SP).append(alias)
                                                            .append(PERIOD).append(fieldName);
                                                    stringBuilder.append(RP).append(SP).append(EQ).append(SP).append(QU);
                                                    args.add(dateFormatWithResult[1]);
                                                });
                                            } else
                                            {
                                                stringBuilder.append(separator).append(alias).append(PERIOD)
                                                        .append(fieldName);
                                                stringBuilder.append(SP).append(EQ).append(SP).append(QU)
                                                        .append(DOUBLE_COLON).append(fieldSQLType.toString());
                                                args.add(stringValue);
                                            }
                                        }
                                    } else
                                    {
                                        stringBuilder.append(separator);
                                        TranslatorUtils.translateStringComparison(alias, fieldName, value, useWildcards,
                                                VARCHAR, stringBuilder, args);
                                    }
                                }
                            },
                        StringBuilder::append);

                    if (resultSqlBuilder.length() > separatorLength)
                    {
                        sqlBuilder.append(resultSqlBuilder.substring(separatorLength));
                    }

                    if (args.isEmpty() || resultSqlBuilder.length() <= separatorLength)
                    {
                        // When there are no columns selected (no values added), then the query should return nothing
                        sqlBuilder.append(FALSE);
                    }
                } else
                {
                    appendTsVectorMatch(sqlBuilder, criterion.getFieldValue(), MAIN_TABLE_ALIAS, args);
                }

                sqlBuilder.append(RP).append(NL);

                break;
            }

            case PROPERTY:
            case ANY_PROPERTY:
            case ATTRIBUTE:
            {
                throw new IllegalArgumentException("Field type " + criterion.getFieldType() + " is not supported");
            }
        }
    }

    private void translateUserMatch(final AbstractStringValue value, final List<Object> args, final StringBuilder sqlBuilder,
            final Map<String, JoinInformation> aliases,
            final String personJoinInformationKey, final boolean useWildcards)
    {
        sqlBuilder.append(aliases.get(personJoinInformationKey).getSubTableAlias())
                .append(PERIOD).append(USER_COLUMN).append(SP);
        TranslatorUtils.appendStringComparatorOp(value.getClass(),
                TranslatorUtils.stripQuotationMarks(value.getValue()), useWildcards,
                sqlBuilder, args);
    }

    private void translateEntityTypeMatch(final AbstractStringValue value, final List<Object> args,
            final StringBuilder sqlBuilder,
            final Map<String, JoinInformation> aliases, final boolean useWildcards)
    {
        sqlBuilder.append(aliases.get(ENTITY_TYPE_JOIN_INFORMATION_KEY).getSubTableAlias())
                .append(PERIOD).append(CODE_COLUMN).append(SP);
        TranslatorUtils.appendStringComparatorOp(value.getClass(),
                TranslatorUtils.stripQuotationMarks(value.getValue()), useWildcards,
                sqlBuilder, args);
    }

    private static Set<PSQLTypes> findCompatibleSqlTypesForValue(final String value)
    {
        final SimplePropertyValidator validator = new SimplePropertyValidator();
        try
        {
            validator.validatePropertyValue(DataTypeCode.DATE, value);
            return EnumSet.of(PSQLTypes.DATE, TIMESTAMP_WITH_TZ);
        } catch (UserFailureException e1)
        {
            try
            {
                validator.validatePropertyValue(DataTypeCode.TIMESTAMP, value);
                return EnumSet.of(TIMESTAMP_WITH_TZ);
            } catch (UserFailureException e2)
            {
                try
                {
                    validator.validatePropertyValue(DataTypeCode.BOOLEAN, value);
                    return EnumSet.of(BOOLEAN);
                } catch (UserFailureException e3)
                {
                    try
                    {
                        validator.validatePropertyValue(DataTypeCode.INTEGER, value);
                        return EnumSet.of(INT8, INT4, INT2, FLOAT4, FLOAT8);
                    } catch (UserFailureException e4)
                    {
                        try
                        {
                            validator.validatePropertyValue(DataTypeCode.REAL, value);
                            return EnumSet.of(FLOAT4, FLOAT8);
                        } catch (UserFailureException e5)
                        {
                            validator.validatePropertyValue(DataTypeCode.VARCHAR, value);
                            return EnumSet.of(VARCHAR);
                        }
                    }
                }
            }
        }
    }

}
