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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.EnumFieldSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.CriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.Attributes;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType.ATTRIBUTE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.*;

public class EnumFieldSearchConditionTranslator implements IConditionTranslator<EnumFieldSearchCriteria<?>>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final EnumFieldSearchCriteria<?> criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final EnumFieldSearchCriteria<?> criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyName)
    {
        if (criterion.getFieldType() == ATTRIBUTE)
        {
            final Enum<?> value = criterion.getFieldValue();
            final String criterionFieldName = criterion.getFieldName();
            final String columnName = Attributes.getColumnName(criterionFieldName, tableMapper.getEntitiesTable(), criterion.getFieldName());

            sqlBuilder.append(CriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(columnName).append(SP).append(EQ).append(SP).append(QU);
            args.add(value.toString());
        } else
        {
            throw new IllegalArgumentException();
        }
    }

}
