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

import java.util.List;
import java.util.Map;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.StringFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.helper.sample.SampleIdentifierParts;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.FullEntityIdentifier;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.AND;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IS_NOT_NULL;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.NAME_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.OWNER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PART_OF_SAMPLE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.USER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.PERSONS_TABLE;

public class CodeSearchConditionTranslator implements IConditionTranslator<StringFieldSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final StringFieldSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final StringFieldSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        switch (criterion.getFieldType())
        {
            case ATTRIBUTE:
            {
                final String columnName = (criterion.getFieldName().equals(NAME_COLUMN)) ?  NAME_COLUMN : CODE_COLUMN;
                final AbstractStringValue value = criterion.getFieldValue();

                if (value != null && value.getValue() != null)
                {
                    final String innerValue = value.getValue();

                    final FullEntityIdentifier fullObjectIdentifier = new FullEntityIdentifier(innerValue, null);
                    final SampleIdentifierParts identifierParts = fullObjectIdentifier.getParts();

                    if (identifierParts.getProjectCodeOrNull() != null)
                    {
                        throw new IllegalArgumentException("There cannot be project code for this entity.");
                    }

                    final String entityCode = fullObjectIdentifier.getEntityCode();
                    final String spaceCode = identifierParts.getSpaceCodeOrNull();
                    final String containerCode = identifierParts.getContainerCodeOrNull();

                    if (spaceCode != null)
                    {
                        sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(OWNER_COLUMN).append(SP).append(EQ).append(SP).
                                append(LP);
                        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP).
                                append(FROM).append(SP).append(PERSONS_TABLE).append(SP).
                                append(WHERE).append(SP).append(USER_COLUMN).append(SP);
                        TranslatorUtils.appendStringComparatorOp(value.getClass(), spaceCode.toLowerCase(), sqlBuilder, args);
                        sqlBuilder.append(RP);
                        sqlBuilder.append(SP).append(AND).append(SP);
                    }

                    if (containerCode != null)
                    {
                        sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(PART_OF_SAMPLE_COLUMN).append(SP).
                                append(EQ).append(SP).append(LP).
                                append(SELECT).append(SP).append(ID_COLUMN).append(SP).append(FROM).append(SP).
                                append(tableMapper.getEntitiesTable()).append(SP).
                                append(WHERE).append(SP).append(columnName).append(SP);
                        TranslatorUtils.appendStringComparatorOp(value.getClass(), containerCode, sqlBuilder, args);

                        sqlBuilder.append(RP).append(SP).append(AND).append(SP);
                    }

                    sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP);
                    TranslatorUtils.appendStringComparatorOp(value.getClass(), entityCode, sqlBuilder, args);
                } else
                {
                    sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(IS_NOT_NULL);
                }
                break;
            }

            case PROPERTY:
                // Fall through.
            case ANY_PROPERTY:
                // Fall through.
            case ANY_FIELD:
            {
                throw new IllegalArgumentException();
            }
        }
    }

}
