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

package ch.systemsx.cisd.yeastx.fiaml;

import java.util.Collection;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.yeastx.db.generic.IDMGenericDAO;

/**
 * Interface for querying / updating the metabol database.
 * 
 * @author Bernd Rinn
 */
public interface IFIAMSRunDAO extends IDMGenericDAO
{
    final String ALL_FIA_MSRUN_COLUMNS =
            "FIA_MS_RUNS.ID, FIA_MS_RUNS.EXPE_ID, "
                    + "FIA_MS_RUNS.SAMP_ID, FIA_MS_RUNS.DS_ID, FIA_MS_RUNS.RAW_DATA_FILE_NAME, "
                    + "FIA_MS_RUNS.RAW_DATA_FILE_PATH, FIA_MS_RUNS.ACQUISITION_DATE, "
                    + "FIA_MS_RUNS.INSTRUMENT_TYPE, FIA_MS_RUNS.INSTRUMENT_MANUFACTURER, "
                    + "FIA_MS_RUNS.INSTRUMENT_MODEL, FIA_MS_RUNS.METHOD_IONISATION, "
                    + "FIA_MS_RUNS.METHOD_SEPARATION, FIA_MS_RUNS.POLARITY, FIA_MS_RUNS.LOW_MZ, "
                    + "FIA_MS_RUNS.HIGH_MZ, FIA_MS_RUNS.INTERNAL_STANDARD, FIA_MS_RUNS.od, FIA_MS_RUNS.OPERATOR";

    @Select("insert into FIA_MS_RUNS (EXPE_ID, SAMP_ID, DS_ID, RAW_DATA_FILE_NAME, RAW_DATA_FILE_PATH, "
            + "ACQUISITION_DATE, INSTRUMENT_TYPE, INSTRUMENT_MANUFACTURER, INSTRUMENT_MODEL, METHOD_IONISATION, "
            + "METHOD_SEPARATION, POLARITY, LOW_MZ, HIGH_MZ, INTERNAL_STANDARD, od, OPERATOR) values "
            + "(?{1.experimentId}, ?{1.sampleId}, ?{1.dataSetId}, ?{1.rawDataFileName}, "
            + "?{1.rawDataFilePath}, ?{1.acquisitionDate}, "
            + "?{1.instrumentType}, ?{1.instrumentManufacturer}, ?{1.instrumentModel}, "
            + "?{1.methodIonisation}, ?{1.methodSeparation}, ?{1.polarity}, ?{1.lowMz}, ?{1.highMz}, "
            + "?{1.internalStandard}, ?{1.od}, ?{1.operator}) returning ID")
    public long addMSRun(FIAMSRunDTO msRun);

    @Update(sql = "insert into FIA_CENTROIDS (FIA_MS_RUN_ID, MZ, INTENSITY, CORRELATION) "
            + "values (?{1}, ?{2}, ?{3}, ?{4})", batchUpdate = true)
    public void addCentroids(long fiaMsRunId, float[] mz, float[] intensity, float[] correlation);

    @Update(sql = "insert into FIA_PROFILES (FIA_MS_RUN_ID, LOW_MZ, HIGH_MZ, MZ, INTENSITIES) "
            + "values (?{1}, ?{2.lowMz}, ?{2.highMz}, ?{2.mz}, ?{2.intensities})", batchUpdate = true)
    public void addProfiles(long fiaMsRunId, Collection<ProfileDTO> profiles);

    @Select(sql = "select FIA_MS_RUNS.*,count(FIA_PROFILES.*) as profileCount from FIA_MS_RUNS "
            + "left join FIA_PROFILES on FIA_MS_RUN_ID = FIA_MS_RUNS.ID group by "
            + ALL_FIA_MSRUN_COLUMNS)
    public DataIterator<FIAMSRunDTO> getMsRuns();

    @Select("select * from FIA_PROFILES where FIA_MS_RUN_ID=?{1.id}")
    public DataIterator<ProfileDTO> getProfilesForRun(FIAMSRunDTO msRun);

    @Select("select * from FIA_CENTROIDS where FIA_MS_RUN_ID=?{1.id}")
    public DataIterator<CentroidDTO> getCentroidsForRun(FIAMSRunDTO msRun);

}
