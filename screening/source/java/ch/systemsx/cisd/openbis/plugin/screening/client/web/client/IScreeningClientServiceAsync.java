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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

/**
 * Service interface for the <i>screening</i> <i>GWT</i> client.
 * 
 * @author Christian Ribeaud
 */
public interface IScreeningClientServiceAsync extends IClientServiceAsync
{

    /** @see IScreeningClientService#getSampleGenerationInfo(TechId) */
    public void getSampleGenerationInfo(final TechId sampleId,
            final AsyncCallback<SampleParentWithDerived> callback);

    /** @see IScreeningClientService#getPlateContent(TechId) */
    public void getPlateContent(TechId sampleId, final AsyncCallback<PlateContent> callback);

    /** @see IScreeningClientService#getPlateContentForDataset(TechId) */
    public void getPlateContentForDataset(TechId datasetId,
            AsyncCallback<PlateImages> createDisplayPlateCallback);

    /** @see IScreeningClientService#getDataSetInfo(TechId) */
    public void getDataSetInfo(TechId datasetTechId, AsyncCallback<ExternalData> callback);

    /**
     * @see IScreeningClientService#listPlateWells(DefaultResultSetConfig,
     *      PlateMaterialsSearchCriteria)
     */
    public void listPlateWells(DefaultResultSetConfig<String, WellContent> gridCriteria,
            PlateMaterialsSearchCriteria materialCriteria,
            AsyncCallback<ResultSet<WellContent>> callback);

    /**
     * @see IScreeningClientService#prepareExportPlateLocations(TableExportCriteria)
     */
    public void prepareExportPlateLocations(TableExportCriteria<WellContent> criteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#listPlateMetadata(IResultSetConfig, TechId)
     */
    public void listPlateMetadata(IResultSetConfig<String, GenericTableRow> resultSetConfig,
            TechId sampleId, AsyncCallback<GenericTableResultSet> callback);

    /**
     * @see IScreeningClientService#prepareExportPlateMetadata(TableExportCriteria)
     */
    public void prepareExportPlateMetadata(TableExportCriteria<GenericTableRow> exportCriteria,
            AsyncCallback<String> callback);

    /**
     * @see IScreeningClientService#registerLibrary(LibraryRegistrationInfo)
     */
    public void registerLibrary(LibraryRegistrationInfo newLibraryInfo,
            AsyncCallback<Void> registerSamplesCallback);

    /**
     * @see IScreeningClientService#getPlateGeometryVocabulary()
     */
    public void getPlateGeometryVocabulary(AsyncCallback<Vocabulary> callback);
}
