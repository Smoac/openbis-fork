/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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

import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.AbstractStringValue;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.planner.DataSetKindSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.TranslatorUtils;
import ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames;

import java.util.List;
import java.util.Map;

import static ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchFieldType.ATTRIBUTE;

public class DataSetKindSearchConditionTranslator implements IConditionTranslator<DataSetKindSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final DataSetKindSearchCriteria criterion, final TableMapper tableMapper,
            final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final DataSetKindSearchCriteria criterion, final TableMapper tableMapper, final List<Object> args,
            final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        if (criterion.getFieldType() == ATTRIBUTE)
        {
            final AbstractStringValue value = criterion.getFieldValue();
            TranslatorUtils.translateStringComparison(SearchCriteriaTranslator.MAIN_TABLE_ALIAS,
                    ColumnNames.DATA_SET_KIND_COLUMN, value, criterion.isUseWildcards(), null, sqlBuilder, args);
        } else
        {
            throw new IllegalArgumentException();
        }
    }

}
