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

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.GenericTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericTableRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ChannelStackImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;

/**
 * Service interface for the <i>screening</i> <i>GWT</i> client.
 * <p>
 * Each method should throw {@link UserFailureException}. The authorisation framework can throw it
 * when the user has insufficient privileges. If it is not marked, the GWT client will report
 * unexpected exception.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public interface IScreeningClientService extends IClientService
{

    /**
     * For given {@link TechId} returns corresponding {@link SampleParentWithDerived}.
     */
    public SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws UserFailureException;

    /**
     * For given {@link TechId} returns corresponding {@link ExternalData}.
     */
    public ExternalData getDataSetInfo(TechId datasetTechId);

    /**
     * Fetches information about wells on a plate and their content.
     */
    public PlateContent getPlateContent(TechId sampleId) throws UserFailureException;

    /**
     * Fetches information about a plate: metadata and images for wells. The specified dataset is
     * supposed to be in BDS-HCS format.
     */
    public PlateImages getPlateContentForDataset(TechId datasetId);

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment(s)
     *         and have specified material(s) inside.
     */
    public ResultSet<WellContent> listPlateWells(
            DefaultResultSetConfig<String, WellContent> gridCriteria,
            PlateMaterialsSearchCriteria materialCriteria);

    /**
     * Like {@link ICommonClientService#prepareExportSamples(TableExportCriteria)}, but for
     * WellContent.
     */
    public String prepareExportPlateLocations(TableExportCriteria<WellContent> criteria);

    /**
     * Returns {@link GenericTableResultSet} containing plate metadata.
     */
    public GenericTableResultSet listPlateMetadata(
            IResultSetConfig<String, GenericTableRow> resultSetConfig, TechId sampleId)
            throws ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;

    /**
     * Like {@link ICommonClientService#prepareExportSamples(TableExportCriteria)}, but for
     * GenericTableRow.
     */
    public String prepareExportPlateMetadata(TableExportCriteria<GenericTableRow> exportCriteria)
            throws UserFailureException;

    /** Lists all images for a given well in the given dataset */
    public List<ChannelStackImageReference> listChannelStackImages(String datasetCode,
            String datastoreCode, WellLocation wellLocation);

    /**
     * Registers a new library.
     */
    public void registerLibrary(LibraryRegistrationInfo details) throws UserFailureException;

    /**
     * Returns plate geometry vocabulary.
     */
    public Vocabulary getPlateGeometryVocabulary() throws UserFailureException;
}
