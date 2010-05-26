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

package eu.basysbio.cisd.dss;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface ITimeSeriesDAO extends BaseQuery
{
    @Select("select id from experiments where perm_id = ?{1}")
    public Long tryToGetExperimentIDByPermID(String experimentPermID);

    @Select("insert into experiments (perm_id) values (?{1}) returning id")
    public long createExperiment(String experimentPermID);

    @Select("select id from data_sets where perm_id = ?{1}")
    public Long tryToGetDataSetIDByPermID(String dataSetID);
    
    @Select("insert into data_sets (perm_id, expe_id) values (?{1}, ?{2}) returning id")
    public long createDataSet(String dataSetPermID, long experimentID);

    @Select("insert into columns (header, dase_id) values (?{1}, ?{2}) returning id")
    public long createColumn(String header, long dataSetID);

    @Select("insert into rows default values returning id")
    public long createRow();

    @Update("insert into column_values (colu_id, row_id, value) values (?{1}, ?{2}, ?{3})")
    public void createValue(long columnID, long rowID, String value);

    @Select("select id from samples where perm_id = ?{1}")
    public Long tryToGetSampleIDByPermID(String samplePermID);
    
    @Select("insert into samples (perm_id) values (?{1}) returning id")
    public long createSample(String samplePermID);
    
    @Select("insert into data_columns (experiment_code, cultivation_method, "
            + "   biological_replicate_code, time_point, time_point_type, technical_replicate_code, "
            + "   celloc, time_series_data_set_type, value_type, scale, bi_id, controlled_gene, "
            + "   growth_phase, genotype, dase_id, samp_id) "
            + "values (?{1.experimentCode}, ?{1.cultivationMethod}, ?{1.biologicalReplicateCode}, "
            + "   ?{1.timePoint}, ?{1.timePointType}, ?{1.technicalReplicateCode}, ?{1.celLoc}, "
            + "   ?{1.timeSeriesDataSetType}, ?{1.valueType}, ?{1.scale}, ?{1.biID}, ?{1.controlledGene}, "
            + "   ?{1.growthPhase}, ?{1.genotype}, ?{2}, ?{3}) returning id")
    public long createDataColumn(DataColumnHeader dataColumnHeader, long dataSetID, Long sampleID);
    
    @Update("insert into data_column_values (daco_id, row_id, value) values (?{1}, ?{2}, ?{3})")
    public void createDataValue(long columnID, long rowID, Double value);

    @Select("select perm_id from data_sets")
    public DataSet<String> findDataSets();
    
}
