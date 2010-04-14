/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.yeastx.db;

import java.util.List;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.TransactionQuery;
import net.lemnik.eodsql.Update;

/**
 * Interface for the "generic" methods (i.e. experiment, sample and data set).
 * 
 * @author Bernd Rinn
 */
public interface IGenericDAO extends TransactionQuery
{

    @Select("select * from EXPERIMENTS where PERM_ID = ?{1}")
    public DMExperimentDTO getExperimentByPermId(String experimentPermId);

    @Select("insert into EXPERIMENTS (PERM_ID, NAME) values (?{1.permId}, ?{1.name}) returning ID")
    public long addExperiment(DMExperimentDTO experiment);

    @Select("select * from SAMPLES where PERM_ID = ?{1}")
    public DMSampleDTO getSampleByPermId(String samplePermId);

    @Select("select DS.* from DATA_SETS DS JOIN SAMPLES S on DS.SAMP_ID = S.ID where S.PERM_ID = ?{1}")
    public DMDataSetDTO[] listDataSetsForSample(String samplePermId);

    @Select("select * from SAMPLES where ID = ?{1}")
    public DMSampleDTO getSampleById(long id);

    @Select("insert into SAMPLES (PERM_ID, NAME, EXPE_ID) values (?{1.permId}, ?{1.name}, ?{1.experiment.id}) "
            + "returning ID")
    public long addSample(DMSampleDTO sample);

    @Select("select * from DATA_SETS where PERM_ID = ?{1}")
    public DMDataSetDTO getDataSetByPermId(String dataSetPermId);

    @Select("select * from DATA_SETS where ID = ?{1}")
    public DMDataSetDTO getDataSetById(long id);

    @Select("insert into DATA_SETS (PERM_ID, EXPE_ID, SAMP_ID) values "
            + "(?{1.permId}, ?{1.experimentId}, ?{1.sampleId}) returning ID")
    public long addDataSet(DMDataSetDTO dataSet);

    @Update(sql = "delete from DATA_SETS where PERM_ID=?{1.permId}", batchUpdate = true)
    public void deleteDataSets(List<DMDataSetDTO> dataSets);

    @Update(sql = "delete from DATA_SETS where PERM_ID=?{1}")
    public void deleteDataSet(String permId);

}
