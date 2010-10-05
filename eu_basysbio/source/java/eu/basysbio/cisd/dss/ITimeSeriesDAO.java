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

import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataSet;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * @author Franz-Josef Elmer
 */
public interface ITimeSeriesDAO extends BaseQuery
{
    @Select("select nextval('time_series_value_group_id_seq')")
    public long getNextValueGroupId();

    @Select("select id from data_sets where perm_id = ?{1}")
    public Long tryToGetDataSetIDByPermID(String dataSetID);

    @Select("insert into data_sets (perm_id, uploader_email, exp_code, exp_perm_id) "
            + "values (?{1}, ?{2}, ?{3.code}, ?{3.permId}) returning id")
    public long createDataSet(String dataSetPermID, String uploaderEMail, Experiment experiment);

    @Select("select perm_id from data_sets where id in (select dase_id from time_series where "
            + "experiment_type = ?{1.experimentCode} and cultivation_method = ?{1.cultivationMethod} "
            + "and biological_replicates = ?{1.biologicalReplicateCode} "
            + "and time_point = ?{1.timePoint} and time_point_type = ?{1.timePointType} "
            + "and technical_replicates = ?{1.technicalReplicateCode} and cell_location = ?{1.celLoc} "
            + "and data_set_type = ?{1.timeSeriesDataSetType} "
            + "and value_type = ?{1.valueType} and unit = ?{1.unit} and scale = ?{1.scale})")
    public List<String> listDataSetsByTimeSeriesDataColumnHeader(
            DataColumnHeader dataColumnHeader);

    @Select(" select distinct ts.identifier from time_series ts join data_sets ds on ds.id = ts.dase_id "
            + "where ds.perm_id = ANY(?{1})")
    public Set<String> getIdentifiersForTimeSeriesDataSet(String[] permId);

    @Update(sql = "insert into time_series "
            + "(dase_id, identifier_type, row_index, column_index, value_group_id, "
            + " identifier, identifier_human_readable, bsb_id, confidence_level, "
            + " controlled_gene, number_of_replicates, "
            + " experiment_type, cultivation_method, biological_replicates, time_point, "
            + " time_point_type, technical_replicates, cell_location, data_set_type, "
            + " value_type, unit, scale, value) "
            + "values (?{1}, ?{2}, ?{3.rowIndex}, ?{3.columnIndex}, ?{3.valueGroupId}, "
            + "        ?{3.identifier}, ?{3.humanReadable}, ?{3.bsbId}, ?{3.confidenceLevel}, "
            + "        ?{3.controlledGene}, ?{3.numberOfReplicates}, "
            + "        ?{3.descriptor.experimentType}, ?{3.descriptor.cultivationMethod}, "
            + "        ?{3.descriptor.biologicalReplicates}, ?{3.descriptor.timePoint}, "
            + "        ?{3.descriptor.timePointType}, ?{3.descriptor.technicalReplicates}, "
            + "        ?{3.descriptor.cellLocation}, ?{3.descriptor.dataSetType}, "
            + "        ?{3.descriptor.valueType}, ?{3.descriptor.unit}, ?{3.descriptor.scale}, "
            + "        ?{3.value})", batchUpdate = true)
    public void insertTimeSeriesValues(long dataSetID, String identifierType,
            List<TimeSeriesValue> timeSeriesValues);

    @Select("select perm_id from data_sets where id in (select dase_id from chip_chip_data where "
            + "experiment_type = ?{1.experimentCode} and cultivation_method = ?{1.cultivationMethod} "
            + "and biological_replicates = ?{1.biologicalReplicateCode} "
            + "and technical_replicates = ?{1.technicalReplicateCode} and cell_location = ?{1.celLoc} "
            + "and growth_phase = ?{1.growthPhase} and genotype = ?{1.genotype})")
    public List<String> listDataSetsByChipChipDataColumnHeader(DataColumnHeader dataColumnHeader);

    @Update(sql = "insert into chip_chip_data "
            + "(dase_id, row_index, bsu_identifier, gene_name, gene_function, "
            + " array_design, microarray_id, "
            + " experiment_type, cultivation_method, biological_replicates, technical_replicates, "
            + " cell_location, growth_phase, genotype, "
            + " chip_peak_position_value, chip_peak_position_scale, "
            + " chip_local_height_value, chip_local_height_scale, "
            + " chip_score_value, chip_score_scale, "
            + " intergenic, nearby_gene_names, nearby_gene_ids, distances_from_start) "
            + "values (?{1}, ?{2.rowIndex}, ?{2.bsuIdentifier}, ?{2.geneName}, ?{2.geneFunction}, "
            + "        ?{2.arrayDesign}, ?{2.microArrayID}, "
            + "        ?{2.descriptor.experimentType}, ?{2.descriptor.cultivationMethod}, "
            + "        ?{2.descriptor.biologicalReplicates}, ?{2.descriptor.technicalReplicates}, "
            + "        ?{2.descriptor.cellLocation}, ?{2.descriptor.growthPhase}, ?{2.descriptor.genotype}, "
            + "        ?{2.chipPeakPosition}, ?{2.chipPeakPositionScale}, "
            + "        ?{2.chipLocalHeight}, ?{2.chipLocalHeightScale}, "
            + "        ?{2.chipScore}, ?{2.chipScoreScale}, "
            + "        ?{2.intergenic}, ?{2.nearbyGeneNames}, ?{2.nearbyGeneIDs}, ?{2.distancesFromStart})", batchUpdate = true)
    public void insertChipChipValues(long dataSetID, List<ChipChipData> dataValues);

    @Select("select perm_id from data_sets")
    public DataSet<String> findDataSets();

}
