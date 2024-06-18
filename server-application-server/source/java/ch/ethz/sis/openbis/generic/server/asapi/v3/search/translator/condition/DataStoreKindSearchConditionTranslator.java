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
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.FROM;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.IN;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.LP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.NE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.OR;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.PERIOD;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.QU;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.RP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SELECT;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SP;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.SQ;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.TRUE;
import static ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SQLLexemes.WHERE;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.CODE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.DATA_STORE_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.PERM_ID_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_IDENTIFIER_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.ColumnNames.SAMPLE_PROP_COLUMN;
import static ch.systemsx.cisd.openbis.generic.shared.dto.TableNames.SAMPLES_VIEW;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.Restrictions;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreKindSearchCriteria;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.datastore.search.DataStoreSearchCriteria;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.mapper.TableMapper;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.SearchCriteriaTranslator;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinInformation;
import ch.ethz.sis.openbis.generic.server.asapi.v3.search.translator.condition.utils.JoinType;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.DataStoreDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.TableNames;

public class DataStoreKindSearchConditionTranslator implements IConditionTranslator<DataStoreKindSearchCriteria>
{

    @Override
    public Map<String, JoinInformation> getJoinInformationMap(final DataStoreKindSearchCriteria criterion,
            final TableMapper tableMapper, final IAliasFactory aliasFactory)
    {
//        final Map<String, JoinInformation> joinInformationMap = new LinkedHashMap<>();
//        final String dataStoresTableAlias = aliasFactory.createAlias();
//
//        final JoinInformation joinInformation = new JoinInformation();
//        joinInformation.setJoinType(JoinType.LEFT);
//        joinInformation.setMainTable(TableNames.DATA_ALL_TABLE);
//        joinInformation.setMainTableAlias(SearchCriteriaTranslator.MAIN_TABLE_ALIAS);
//        joinInformation.setMainTableIdField(DATA_STORE_COLUMN);
//        joinInformation.setSubTable(TableNames.DATA_STORES_TABLE);
//        joinInformation.setSubTableAlias(dataStoresTableAlias);
//        joinInformation.setSubTableIdField(ID_COLUMN);
//        joinInformationMap.put(TableNames.DATA_STORES_TABLE, joinInformation);
//
//        return joinInformationMap;

        return null;
    }

    @Override
    public void translate(final DataStoreKindSearchCriteria criterion, final TableMapper tableMapper,
            final List<Object> args, final StringBuilder sqlBuilder, final Map<String, JoinInformation> aliases,
            final Map<String, String> dataTypeByPropertyCode)
    {
//        final String name = criterion.getFieldName();
//        final String value = criterion.getFieldValue();
//        final String valuesTableAlias = aliases.get(tableMapper.getValuesTable()).getSubTableAlias();
//
//        TranslatorUtils.appendPropertiesExist(sqlBuilder, valuesTableAlias);
//        sqlBuilder.append(SP).append(AND).append(SP).append(LP);
//
//        if (tableMapper == TableMapper.SAMPLE || tableMapper == TableMapper.EXPERIMENT
//                || tableMapper == TableMapper.DATA_SET)
//        {
//            sqlBuilder.append(aliases.get(tableMapper.getAttributeTypesTable()).getSubTableAlias()).append(PERIOD)
//                    .append(CODE_COLUMN).append(SP).append(EQ).append(SP).append(QU);
//            args.add(name);
//            sqlBuilder.append(SP).append(AND).append(SP);
//            appendSampleSubselectConstraint(args, sqlBuilder, value, valuesTableAlias);
//        } else
//        {
//            throw new IllegalArgumentException("Sample properties are not supported for " + tableMapper);
//        }
//
//        sqlBuilder.append(RP);

        final Set<DataStoreKind> dataStoreKinds = criterion.getDataStoreKinds();
        final boolean includeAfs = dataStoreKinds.contains(DataStoreKind.AFS);
        final boolean includeDss = dataStoreKinds.contains(DataStoreKind.DSS);

        if (includeAfs)
        {
            if (!includeDss)
            {
                sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP).append(EQ).append(SP)
                        .append(SQ).append(DataStoreDAO.AFS_DATA_STORE_CODE).append(SQ);
            } else
            {
                sqlBuilder.append(TRUE);
            }
        } else if (includeDss)
        {
            sqlBuilder.append(SearchCriteriaTranslator.MAIN_TABLE_ALIAS).append(PERIOD).append(CODE_COLUMN).append(SP).append(NE).append(SP)
                    .append(SQ).append(DataStoreDAO.AFS_DATA_STORE_CODE).append(SQ);
        } else
        {
            sqlBuilder.append(TRUE);
        }
    }

//    public static void appendSampleSubselectConstraint(final List<Object> args, final StringBuilder sqlBuilder,
//            final String value, final String propertyTableAlias)
//    {
//        sqlBuilder.append(propertyTableAlias).append(PERIOD)
//                .append(SAMPLE_PROP_COLUMN).append(SP).append(IN).append(SP);
//        sqlBuilder.append(LP);
//        sqlBuilder.append(SELECT).append(SP).append(ID_COLUMN).append(SP)
//                .append(FROM).append(SP).append(SAMPLES_VIEW).append(SP);
//
//        if (value != null)
//        {
//            sqlBuilder.append(WHERE).append(SP);
//            translateStringComparison(CODE_COLUMN, value, sqlBuilder, args);
//            sqlBuilder.append(SP).append(OR).append(SP);
//            translateStringComparison(PERM_ID_COLUMN, value, sqlBuilder, args);
//            sqlBuilder.append(SP).append(OR).append(SP);
//            translateStringComparison(SAMPLE_IDENTIFIER_COLUMN, value, sqlBuilder, args);
//        }
//
//        sqlBuilder.append(RP);
//    }
//
//    private static void translateStringComparison(final String columnName, final String value,
//            final StringBuilder sqlBuilder, final List<Object> args)
//    {
//        sqlBuilder.append(columnName).append(SP).append(EQ).append(SP).append(QU);
//        args.add(value);
//    }

}
