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

package ch.systemsx.cisd.yeastx.mzxml;

import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

import ch.systemsx.cisd.yeastx.db.generic.DMDataSetDTO;
import ch.systemsx.cisd.yeastx.db.generic.IGenericDAO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzInstrumentDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzPrecursorDTO;
import ch.systemsx.cisd.yeastx.mzxml.dto.MzScanDTO;

/**
 * Creates MS Raw Data (mzXML) records .
 * 
 * @author Tomasz Pylak
 */
public interface IMzXmlDAO extends IGenericDAO
{
    @Select(sql = "insert into MZ_MS_RUNS "
            + "           ( EXPE_ID, SAMP_ID, DS_ID, INSTRUMENT_TYPE, INSTRUMENT_MANUFACTURER, "
            + "             INSTRUMENT_MODEL, METHOD_IONISATION ) "
            + "    values (?{1.experimentId}, ?{1.sampleId}, ?{1.id}, ?{2.instrumentType.value}, ?{2.instrumentManufacturer.value}, "
            + "            ?{2.instrumentModel.value}, ?{2.methodIonisation.value} )                       "
            + "    returning ID")
    public long addRun(DMDataSetDTO dataSet, MzInstrumentDTO instrument);

    @Select(sql = "insert into MZ_SCANS "
            + "           ( MZ_MS_RUN_ID, NUMBER, LEVEL, PEAKS_COUNT, POLARITY, "
            + "             SCAN_TYPE, COLLISION_ENERGY, LOW_MZ, HIGH_MZ,       "
            + "             RETENTION_TIME,                                     "
            + "             PRECURSOR1_MZ, PRECURSOR1_INTENSITY, PRECURSOR1_CHARGE,        "
            + "             PRECURSOR2_MZ, PRECURSOR2_INTENSITY, PRECURSOR2_CHARGE )       "
            + "    values ( ?{1}, ?{2.number}, ?{2.level}, ?{2.peaksCount}, ?{2.polarity}, "
            + "             ?{2.scanType}, ?{2.collisionEnergy}, ?{2.lowMz}, ?{2.highMz},  "
            + "             ?{2.retentionTimeInSeconds},                                   "
            + "             ?{3.mz}, ?{3.intensity}, ?{3.charge},                          "
            + "             ?{4.mz}, ?{4.intensity}, ?{4.charge} )                         "
            + "    returning ID")
    public long addScan(long runId, MzScanDTO scan, MzPrecursorDTO precursor1,
            MzPrecursorDTO precursor2);

    @Update(sql = "insert into MZ_PEAKS (MZ_SCAN_ID, MZ, INTENSITY) "
            + "                  values (?{1}, ?{2}, ?{3})", batchUpdate = true)
    public void addPeaks(long scanId, Iterable<Float> mzArray, Iterable<Float> intensityArray);

}
