/*
 *  Copyright ETH 2023 - 2024 Zürich, Scientific IT Services
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition;

import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.EQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.TRUE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKindSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.systemsx.cisd.openbis.generic.shared.Constants;

public class DataStoreKindSearchConditionTranslator implements IConditionTranslator<DataStoreKindSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final DataStoreKindSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
        return null;
    }

    @Override
    public void translate(final DataStoreKindSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
        final Set<DataStoreKind> dataStoreKinds = Set.of(criterion.getDataStoreKinds());
        final boolean includeAfs = dataStoreKinds.contains(DataStoreKind.AFS);
        final boolean includeDss = dataStoreKinds.contains(DataStoreKind.DSS);

        if (includeAfs)
        {
            if (!includeDss)
            {
                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP).append(EQ).append(SP)
                        .append(SQ).append(Constants.AFS_DATA_STORE_CODE).append(SQ);
            } else
            {
                sqlBuilder.append(TRUE);
            }
        } else if (includeDss)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP).append(NE).append(SP)
                    .append(SQ).append(Constants.AFS_DATA_STORE_CODE).append(SQ);
        } else
        {
            sqlBuilder.append(TRUE);
        }
    }

}
