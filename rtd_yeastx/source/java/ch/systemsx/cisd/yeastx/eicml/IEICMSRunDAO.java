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

package ch.systemsx.cisd.yeastx.eicml;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.yeastx.db.generic.IDMGenericDAO;

/**
 * Interface for querying / updating the metabol database.
 * 
 * @author Bernd Rinn
 */
public interface IEICMSRunDAO extends IDMGenericDAO
{
    final String ALL_EIC_MSRUN_COLUMNS =
            "EIC_MS_RUNS.ID, EIC_MS_RUNS.EXPE_ID, EIC_MS_RUNS.SAMP_ID, EIC_MS_RUNS.DS_ID, "
                    + "EIC_MS_RUNS.RAW_DATA_FILE_NAME, EIC_MS_RUNS.RAW_DATA_FILE_PATH, "
                    + "EIC_MS_RUNS.ACQUISITION_DATE, EIC_MS_RUNS.INSTRUMENT_TYPE, EIC_MS_RUNS.INSTRUMENT_MANUFACTURER, "
                    + "EIC_MS_RUNS.INSTRUMENT_MODEL, EIC_MS_RUNS.METHOD_IONISATION, EIC_MS_RUNS.METHOD_SEPARATION, "
                    + "EIC_MS_RUNS.MS_RUN_ID, EIC_MS_RUNS.SET_ID, EIC_MS_RUNS.OPERATOR, "
                    + "EIC_MS_RUNS.START_TIME, EIC_MS_RUNS.END_TIME";

    @Select("insert into EIC_MS_RUNS (EXPE_ID, SAMP_ID, DS_ID, "
            + "RAW_DATA_FILE_NAME, RAW_DATA_FILE_PATH, ACQUISITION_DATE, "
            + "INSTRUMENT_TYPE, INSTRUMENT_MANUFACTURER, INSTRUMENT_MODEL, METHOD_IONISATION, "
            + "METHOD_SEPARATION, MS_RUN_ID, SET_ID, OPERATOR, START_TIME, END_TIME) values "
            + "(?{1.experimentId}, ?{1.sampleId}, ?{1.dataSetId}, ?{1.rawDataFileName}, "
            + "?{1.rawDataFilePath}, ?{1.acquisitionDate}, ?{1.instrumentType}, "
            + "?{1.instrumentManufacturer}, ?{1.instrumentModel}, ?{1.methodIonisation}, "
            + "?{1.methodSeparation}, ?{1.msRunId}, ?{1.setId}, ?{1.operator}, "
            + "?{1.startTime}, ?{1.endTime}) returning ID")
    public long addMSRun(EICMSRunDTO msRun);

    @Update(sql = "insert into EIC_CHROMATOGRAMS (EIC_MS_RUN_ID, Q1_MZ, Q3_LOW_MZ, Q3_HIGH_MZ, LABEL, POLARITY, RUN_TIMES, "
            + "intensities) values (?{1}, ?{2.q1Mz}, ?{2.q3LowMz}, ?{2.q3HighMz}, ?{2.label}, "
            + "?{2.polarity}, ?{2.runTimes}, ?{2.intensities})", batchUpdate = true)
    public void addChromatograms(long EIC_MS_RUN_ID, Iterable<ChromatogramDTO> chromatogram);

    @Select(sql = "select EIC_MS_RUNS.*,count(EIC_CHROMATOGRAMS.*) as chromCount from EIC_MS_RUNS "
            + "left join EIC_CHROMATOGRAMS on EIC_MS_RUN_ID = EIC_MS_RUNS.ID group by "
            + ALL_EIC_MSRUN_COLUMNS)
    public DataIterator<EICMSRunDTO> getMsRuns();

    @Select(sql = "select * from EIC_MS_RUNS where RAW_DATA_FILE_NAME=?{1}")
    public DataIterator<EICMSRunDTO> getMsRunsForRawDataFile(String rawDataFileName);

    @Select(sql = "select EIC_MS_RUNS.* from EIC_MS_RUNS left join DATA_SETS on DATA_SETS.id = EIC_MS_RUNS.ds_id "
            + "where DATA_SETS.perm_id = ?{1} group by " + ALL_EIC_MSRUN_COLUMNS)
    public EICMSRunDTO getMSRunByDatasetPermId(String datasetPermId);

    @Select("select * FROM EIC_CHROMATOGRAMS where EIC_MS_RUN_ID=?{1.id}")
    public DataIterator<ChromatogramDTO> getChromatogramsForRun(EICMSRunDTO msRun);

    @Select("select * FROM EIC_CHROMATOGRAMS where ID=?{1}")
    public ChromatogramDTO getChromatogramById(long chromatogramId);

    @Select(sql = "select ID, EIC_MS_RUN_ID, Q1_MZ, Q3_LOW_MZ, Q3_HIGH_MZ, LABEL, POLARITY FROM EIC_CHROMATOGRAMS "
            + "LEFT JOIN EIC_MS_RUNS on EIC_MS_RUN_ID = EIC_MS_RUNS.ID where EIC_MS_RUN_ID=?{1.id}")
    public DataIterator<ChromatogramDTO> getChromatogramsForRunNoData(EICMSRunDTO msRun);

}
