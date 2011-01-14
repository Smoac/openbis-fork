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

import ch.systemsx.cisd.openbis.generic.client.web.client.IClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * Service interface for the <i>screening</i> <i>GWT</i> client.
 * <p>
 * Each method should declare throwing {@link UserFailureException}. The authorisation framework can
 * throw it when the user has insufficient privileges. If it is not marked, the GWT client will
 * report unexpected exception.
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
    public ExternalData getDataSetInfo(TechId datasetTechId) throws UserFailureException;

    /**
     * Fetches information about wells on a plate and their content.
     */
    public PlateContent getPlateContent(TechId sampleId) throws UserFailureException;

    /**
     * Fetches information about a plate: metadata and images for wells. The specified dataset is
     * supposed to be in BDS-HCS format.
     */
    public PlateImages getPlateContentForDataset(TechId datasetId) throws UserFailureException;

    /**
     * @return well locations which belong to a parent plate connected to a specified experiment(s)
     *         and have specified material(s) inside.
     */
    public TypedTableResultSet<WellContent> listPlateWells(
            IResultSetConfig<String, TableModelRowWithObject<WellContent>> gridCriteria,
            WellSearchCriteria materialCriteria) throws UserFailureException;

    public String prepareExportPlateLocations(
            TableExportCriteria<TableModelRowWithObject<WellContent>> criteria)
            throws UserFailureException;

    /**
     * Returns {@link TypedTableResultSet} containing plate metadata.
     */
    public TypedTableResultSet<WellMetadata> listPlateMetadata(
            IResultSetConfig<String, TableModelRowWithObject<WellMetadata>> resultSetConfig,
            TechId sampleId) throws UserFailureException;

    /**
     * Lists {@link Material}s of specified type in experiment with specified id.
     */
    public ResultSet<Material> listExperimentMaterials(TechId experimentId,
            ListMaterialDisplayCriteria criteria) throws UserFailureException;

    /**
     * Like {@link ICommonClientService#prepareExportSamples(TableExportCriteria)}, but for
     * TypedTableResultSet.
     */
    public String prepareExportPlateMetadata(
            TableExportCriteria<TableModelRowWithObject<WellMetadata>> exportCriteria)
            throws UserFailureException;

    /**
     * Returns information about logical image in the given dataset. In HCS case the well location
     * should be specified.
     */
    public LogicalImageInfo getImageDatasetInfo(String datasetCode, String datastoreCode,
            WellLocation wellLocationOrNull) throws UserFailureException;

    /**
     * Returns information about image dataset for a given image dataset. Used to refresh
     * information about the dataset.
     */
    public ImageDatasetEnrichedReference getImageDatasetReference(String datasetCode,
            String datastoreCode);

    /**
     * Loads information about datasets connected to specified sample (microscopy) or a container
     * sample (HCS). In particular loads the logical images in datasets belonging to the specified
     * sample (restricted to one well in HCS case).
     */
    public ImageSampleContent getImageDatasetInfosForSample(TechId sampleId,
            WellLocation wellLocationOrNull);

    /**
     * Registers a new library.
     */
    public void registerLibrary(LibraryRegistrationInfo details) throws UserFailureException;

    /**
     * Returns plate geometry vocabulary.
     */
    public Vocabulary getPlateGeometryVocabulary() throws UserFailureException;

}
