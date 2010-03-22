/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;

/**
 * A <i>Business Object</i> to load a list of {@link ExternalDataPE}.
 * 
 * @author Christian Ribeaud
 */
public interface IExternalDataTable
{
    /**
     * Loads data sets specified by their codes. Data set codes will be ignored if no
     * {@link ExternalDataPE} could be found. Optionally if <var>lockForUpdate</var> is
     * <var>true</var> a all updates to loaded data sets from other transactions will be blocked for
     * until current transaction is finished.
     */
    void loadByDataSetCodes(List<String> dataSetCodes, boolean lockForUpdate);

    /**
     * Loads data sets which are linked to the sample with given <var>sampleId</var>.
     */
    void loadBySampleTechId(final TechId sampleId);

    /**
     * Loads data sets which are linked to the experiment with given <var>experimentId</var>.
     */
    void loadByExperimentTechId(final TechId experimentId);

    /**
     * Returns the loaded {@link ExternalDataPE}.
     */
    List<ExternalDataPE> getExternalData();

    /**
     * Sets the specified external data as they were loaded.
     */
    void setExternalData(List<ExternalDataPE> externalData);

    /**
     * Deletes loaded data sets for specified reason.
     */
    void deleteLoadedDataSets(String reason);

    /**
     * Uploads loaded data sets to CIFEX server as specified in the upload context.
     * 
     * @return a message or an empty string.
     */
    String uploadLoadedDataSetsToCIFEX(DataSetUploadContext uploadContext);

    /** Schedules archivization of loaded data sets. Only active data sets will be archived. */
    void archiveDatasets();

    /** Schedules unarchivization of loaded data sets. Only archived datasets will be unarchived. */
    void unarchiveDatasets();

    /** Creates a report from specified datasets using the specified datastore service. */
    TableModel createReportFromDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes);

    /** Schedules processing of specified datasets using the specified datastore service. */
    void processDatasets(String datastoreServiceKey, String datastoreCode, List<String> datasetCodes);

    /**
     * Loads data sets that belong to chosen data store.
     */
    public void loadByDataStore(DataStorePE dataStore);

}
