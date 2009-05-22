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

import net.lemnik.eodsql.BaseQuery;
import net.lemnik.eodsql.Select;
import net.lemnik.eodsql.Update;

/**
 * Interface for querying / updating the metabol database.
 * 
 * @author Bernd Rinn
 */
public interface IFIAMSRunDAO extends BaseQuery
{
    final String ALL_FIA_MSRUN_COLUMNS =
            "fiamsruns.fiaMsRunId, fiamsruns.permId, fiamsruns.rawDataFileName, "
                    + "fiamsruns.rawDataFilePath, fiamsruns.acquisitionDate, "
                    + "fiamsruns.instrumentType, fiamsruns.instrumentManufacturer, "
                    + "fiamsruns.instrumentModel, fiamsruns.methodIonisation, "
                    + "fiamsruns.methodSeparation, fiamsruns.polarity, fiamsruns.lowMz, "
                    + "fiamsruns.highMz, fiamsruns.internalStandard, fiamsruns.od, fiamsruns.operator";

    @Select("INSERT INTO fiamsruns (permId, rawDataFileName, rawDataFilePath, acquisitionDate, "
            + "instrumentType, instrumentManufacturer, instrumentModel, methodIonisation, "
            + "methodSeparation, polarity, lowMz, highMz, internalStandard, od, operator) values "
            + "(?{1.permId}, ?{1.rawDataFileName}, ?{1.rawDataFilePath}, ?{1.acquisitionDate}, "
            + "?{1.instrumentType}, ?{1.instrumentManufacturer}, ?{1.instrumentModel}, "
            + "?{1.methodIonisation}, ?{1.methodSeparation}, ?{1.polarity}, ?{1.lowMz}, ?{1.highMz}, "
            + "?{1.internalStandard}, ?{1.od}, ?{1.operator}) returning fiaMsRunId")
    public long addMSRun(FIAMSRunDTO msRun);

    // Too slow as eodsql 2.0 doesn't support batch updates.
    @Update("INSERT INTO profiles (fiaMsRunId, mz, intensity) values (?{1}, ?{2}, ?{3})")
    public void addProfileEntry(long fiaMsRunId, float mz, float intensity);

    // Too slow as eodsql 2.0 doesn't support batch updates.
    @Update("INSERT INTO centroids (fiaMsRunId, mz, intensity, correlation) "
            + "values (?{1}, ?{2}, ?{3}, ?{4})")
    public void addCentroidEntry(long fiaMsRunId, float mz, float intensity, float correlation);
}
