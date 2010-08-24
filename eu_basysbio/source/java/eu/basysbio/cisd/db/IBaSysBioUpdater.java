/*
 * Copyright 2010 ETH Zuerich, CISD
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

package eu.basysbio.cisd.db;

import java.util.List;

import eu.basysbio.cisd.db.IOpenBISDataSetQuery.DataSetContainer;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * An updater / inserter for the BaSysBio result database.
 * 
 * @author Bernd Rinn
 */
public interface IBaSysBioUpdater extends TransactionQuery
{
    @Select("insert into data_sets                                       "
            + "    (code, uploader_email, exp_code, exp_perm_id) "
            + "  values (?{1.ds_code}, ?{1.uploader_email}, ?{1.exp_code}, ?{1.exp_perm_id}) "
            + "  returning id")
    long insertDataSet(DataSetContainer dataSet);

    @Update(sql = "insert into time_series                                       "
            + "    (dase_id, identifier, identifier_type, identifier_human_readable, data_set_type, "
            + "              experiment_type, cultivation_method, biological_replicates, technical_replicates,"
            + "              time_point, time_point_type, cell_location, value, value_type, unit, scale, "
            + "              row_id, value_group_id)                                          "
            + "  values (?{1}, ?{4}, ?{2}, ?{5}, ?{3.dataSetType}, ?{3.experimentType}, "
            + "            ?{3.cultivationMethod}, ?{3.biologicalReplicates}, "
            + "            ?{3.technicalReplicates}, ?{3.timePoint}, ?{3.timePointType}, ?{3.cellLocation},"
            + "            ?{6}, ?{3.valueType}, ?{3.unit}, ?{3.scale}, ?{7}, ?{8})", batchUpdate = true)
    void insertTimeSeriesEntry(long dataSetId, String identifierType,
            List<TimeSeriesColumnDescriptor> columnDescriptor, List<String> identifier,
            List<String> humanReadableOrNull, List<Double> value, List<Long> rowId,
            List<Long> valueGroupId);

    @Select("select nextval('time_series_row_id_seq')")
    long nextRowId();

    @Select("select nextval('time_series_value_group_id_seq')")
    long nextValueGroupId();
}
