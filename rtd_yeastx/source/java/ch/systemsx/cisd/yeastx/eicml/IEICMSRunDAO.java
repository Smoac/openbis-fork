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

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.Select;

/**
 * Interface for querying / updating the metabol database.
 * 
 * @author Bernd Rinn
 */
public interface IEICMSRunDAO extends BaseQuery
{
    final String ALL_EIC_MSRUN_COLUMNS =
            "eicmsruns.eicMsRunId, eicmsruns.permId, eicmsruns.rawDataFileName, eicmsruns.rawDataFilePath, "
                    + "eicmsruns.acquisitionDate, eicmsruns.instrumentType, eicmsruns.instrumentManufacturer, "
                    + "eicmsruns.instrumentModel, eicmsruns.methodIonisation, eicmsruns.methodSeparation, "
                    + "eicmsruns.setId, eicmsruns.operator, "
                    + "eicmsruns.startTime, eicmsruns.endTime";

    @Select("INSERT INTO eicmsruns (permId, rawDataFileName, rawDataFilePath, acquisitionDate, "
            + "instrumentType, instrumentManufacturer, instrumentModel, methodIonisation, "
            + "methodSeparation, setId, operator, startTime, endTime) values (?{1.permId}, ?{1.rawDataFileName}, "
            + "?{1.rawDataFilePath}, ?{1.acquisitionDate}, ?{1.instrumentType}, "
            + "?{1.instrumentManufacturer}, ?{1.instrumentModel}, ?{1.methodIonisation}, "
            + "?{1.methodSeparation}, ?{1.setId}, ?{1.operator}, ?{1.startTime}, ?{1.endTime}) returning eicMsRunId")
    public long addMSRun(MSRunDTO msRun);

    @Select("INSERT INTO chromatograms (eicMsRunId, Q1MZ, Q3LowMz, Q3HighMz, label, polarity, runTimes, "
            + "intensities) values (?{1.eicMsRunId}, ?{1.q1Mz}, "
            + "?{1.q3LowMz}, ?{1.q3HighMz}, ?{1.label}, "
            + "?{1.polarity}, ?{1.runTimes}, ?{1.intensities}) returning chromId")
    public long addChromatogram(ChromatogramDTO chromatogram);

    @Select(sql = "SELECT * from eicmsruns", rubberstamp = true)
    public DataIterator<MSRunDTO> getMsRuns();

    @Select(sql = "SELECT * from eicmsruns where rawDataFileName=?{1}", rubberstamp = true)
    public DataIterator<MSRunDTO> getMsRunsForRawDataFile(String rawDataFileName);

    @Select("SELECT eicmsruns.*, count(chromatograms.*) AS chromCount from eicmsruns "
            + "LEFT JOIN chromatograms USING(eicMsRunId) where eicmsruns.eicMsRunId=?{1} GROUP BY "
            + ALL_EIC_MSRUN_COLUMNS)
    public MSRunDTO getMSRunById(long id);

    @Select("SELECT eicmsruns.*, count(chromatograms.*) AS chromCount from eicmsruns "
            + "LEFT JOIN chromatograms USING(eicMsRunId) where eicmsruns.permId=?{1} GROUP BY "
            + ALL_EIC_MSRUN_COLUMNS)
    public MSRunDTO getMSRunByPermId(String permId);

    @Select("SELECT eicmsruns.* FROM msrun LEFT JOIN chromatograms USING(eicMsRunId) "
            + "where chromatograms.chromId = ?{1.chromId}")
    public MSRunDTO getMSRunForChromatogram(ChromatogramDTO chromatogram);

    @Select("SELECT chromatograms.* FROM chromatograms where chromId=?{1}")
    public ChromatogramDTO getChromatogramById(long id);
    
    @Select("SELECT chromatograms.* FROM chromatograms where label=?{1}")
    public ChromatogramDTO getChromatogramByLabel(String label);

    @Select(sql = "SELECT chromatograms.* FROM chromatograms LEFT JOIN eicmsruns USING(eicMsRunId) "
            + "where eicMsRunId=?{1.eicMsRunId}", rubberstamp = true)
    public DataIterator<ChromatogramDTO> getChromatogramsForRun(MSRunDTO msRun);

    @Select(sql = "SELECT chromId, eicMsRunId, Q1Mz, Q3LowMz, Q3HighMz, label, polarity FROM chromatograms "
            + "LEFT JOIN eicmsruns USING(eicMsRunId) " + "where eicMsRunId=?{1.eicMsRunId}", rubberstamp = true)
    public DataIterator<ChromatogramDTO> getChromatogramsForRunNoData(MSRunDTO msRun);

}
