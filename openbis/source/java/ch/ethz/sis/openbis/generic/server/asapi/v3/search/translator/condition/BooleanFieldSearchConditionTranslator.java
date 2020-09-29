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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.BooleanFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.AttributesMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.PSQLTypes.BOOLEAN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

public class BooleanFieldSearchConditionTranslator implements IConditionTranslator<BooleanFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final BooleanFieldSearchCriteria criterion, final TableMapper tableMapper,
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
    public void translate(final BooleanFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String criterionFieldName = criterion.getFieldName();
                final String columnName = AttributesMapper.getColumnName(criterionFieldName, tableMapper.getValuesTable(), criterionFieldName);
                final Boolean value = criterion.getFieldValue();

                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(EQ).append(SP).append(QU);
                args.add(value);
                break;
            }

            case PROPERTY:
            {
                final Boolean value = criterion.getFieldValue();
                final String propertyName = TranslatorUtils.normalisePropertyName(criterion.getFieldName());
                final String casting = dataTypeByPropertyCode.get(propertyName);

                if (!DataTypeCode.BOOLEAN.toString().equals(casting))
                {
                    throw new UserFailureException(String.format(
                            "The data type of property %s has to be %s instead of %s.", propertyName,
                            DataTypeCode.BOOLEAN, casting));
                }

                final boolean internalProperty = TranslatorUtils.isPropertyInternal(criterion.getFieldName());

                translateBooleanProperty(tableMapper, args, sqlBuilder, aliases, value, propertyName, internalProperty);
                break;
            }

            case ANY_PROPERTY:
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static void translateBooleanProperty(final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases, final Boolean value,
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
        sqlBuilder.append(SP).append(AND);

        sqlBuilder.append(SP).append(entityTypesSubTableAlias).append(PERIOD).append(ColumnNames.CODE_COLUMN).append(SP)
                .append(EQ).append(SP).append(QU);
        args.add(propertyName);

        if (value != null)
        {
            sqlBuilder.append(SP).append(THEN).append(SP);
            sqlBuilder.append(aliases.get(tableMapper.getValuesTable())
                    .getSubTableAlias()).append(PERIOD).append(ColumnNames.VALUE_COLUMN).append(DOUBLE_COLON)
                    .append(BOOLEAN).append(SP).append(EQ).append(SP).append(QU).append(SP).append(END);
            args.add(value);
        }

        sqlBuilder.append(RP);
    }

}
