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

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.CASE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.DOUBLE_COLON;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.ELSE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.END;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.OR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.THEN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHEN;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractNumberValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.NumberFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

public class NumberFieldSearchConditionTranslator implements IConditionTranslator<NumberFieldSearchCriteria>
{

    private static final String INTEGER_DATA_TYPE_CODE = DataTypeCode.INTEGER.toString();

    private static final String REAL_DATA_TYPE_CODE = DataTypeCode.REAL.toString();

    private static final Set<String> VALID_DATA_TYPES = new HashSet<>(Arrays.asList(
            INTEGER_DATA_TYPE_CODE, REAL_DATA_TYPE_CODE));

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final NumberFieldSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                return null;
            }

            case PROPERTY:
            {
                return TranslatorUtils.getPropertyJoinInformationMap(tableMapper, aliasFactory);
            }
        }

        throw new IllegalArgumentException();
    }

    @Override
    public void translate(final NumberFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getValuesTable(), criterion.getFieldName());
                final AbstractNumberValue value = criterion.getFieldValue();

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP);
                TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
                sqlBuilder.append(NL);
                args.add(value.getValue());
                break;
            }

            case PROPERTY:
            {
                final AbstractNumberValue value = criterion.getFieldValue();
                final String propertyName = TranslatorUtils.normalisePropertyName(criterion.getFieldName());
                String casting = dataTypeByPropertyName.get(propertyName);
                if (VALID_DATA_TYPES.contains(casting) == false)
                {
                    throw new UserFailureException("The data type of property " + propertyName + " has to be one of "
                            + VALID_DATA_TYPES + " instead of " + casting + ".");
                }
                final boolean internalProperty = TranslatorUtils.isPropertyInternal(criterion.getFieldName());
                translateNumberProperty(tableMapper, args, sqlBuilder, aliases, value, propertyName, internalProperty);
                break;
            }

            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static void translateNumberProperty(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final AbstractNumberValue value,
            final String propertyName, final boolean internalProperty)
    {
        final JoinInformation joinInformation = aliases.get(tableMapper.getAttributeTypesTable());
        final String entityTypesSubTableAlias = joinInformation.getSubTableAlias();

        sqlBuilder.append(entityTypesSubTableAlias).append(PERIOD).append(joinInformation.getSubTableIdField())
                .append(SP).append(IS_NOT_NULL).append(SP).append(AND).append(SP).append(LP);

        if (value != null)
        {
            sqlBuilder.append(CASE).append(SP).append(WHEN).append(SP);
        }

        TranslatorUtils.appendInternalExternalConstraint(sqlBuilder, args, entityTypesSubTableAlias, internalProperty);

        sqlBuilder.append(SP).append(joinInformation.getSubTableAlias())
                .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(propertyName);

        sqlBuilder.append(SP).append(AND).append(SP).append(LP);

        sqlBuilder.append(aliases.get(TableNames.DATA_TYPES_TABLE).getSubTableAlias())
                .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(DataTypeCode.INTEGER.toString());

        sqlBuilder.append(SP).append(OR).append(SP);

        sqlBuilder.append(aliases.get(TableNames.DATA_TYPES_TABLE).getSubTableAlias())
                .append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
        args.add(DataTypeCode.REAL.toString());

        sqlBuilder.append(RP);

        if (value != null)
        {
            sqlBuilder.append(SP).append(THEN).append(SP);
            sqlBuilder.append(aliases.get(tableMapper.getValuesTable()).getSubTableAlias())
                    .append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(DOUBLE_COLON)
                    .append(PSQLTypes.NUMERIC.toString()).append(SP);
            TranslatorUtils.appendNumberComparatorOp(value, sqlBuilder);
            args.add(value.getValue());

            sqlBuilder.append(SP).append(ELSE).append(SP).append(false).append(SP).append(END);
        }
        sqlBuilder.append(RP);
    }

}
