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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.servlet.IRequestContextProvider;
import ch.systemsx.cisd.common.spring.IUncheckedMultipartFile;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.IResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractClientService;
import ch.systemsx.cisd.openbis.generic.client.web.server.AbstractOriginalDataProviderWithoutHeaders;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.DataProviderAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.ITableModelProvider;
import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.MaterialDisambiguationProvider;
import ch.systemsx.cisd.openbis.generic.shared.IServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.BuildAndEnvironmentInfo;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientService;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.FeatureVectorSummaryProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.MaterialFeatureVectorsFromAllExperimentsProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.MaterialReplicaFeatureSummaryProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.PlateMetadataProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset.WellContentProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.AnalysisProcedures;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LibraryRegistrationInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;

/**
 * The {@link IScreeningClientService} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(value = ResourceNames.SCREENING_PLUGIN_SERVICE)
public final class ScreeningClientService extends AbstractClientService implements
        IScreeningClientService
{

    @Resource(name = ResourceNames.SCREENING_PLUGIN_SERVER)
    private IScreeningServer server;

    public ScreeningClientService()
    {
    }

    @Private
    ScreeningClientService(final IScreeningServer server,
            final IRequestContextProvider requestContextProvider)
    {
        super(requestContextProvider);
        this.server = server;
    }

    //
    // AbstractClientService
    //

    @Override
    protected final IServer getServer()
    {
        return server;
    }

    //
    // IScreeningClientService
    //

    @Override
    protected String getVersion()
    {
        return BuildAndEnvironmentInfo.INSTANCE.getFullVersion();
    }

    public final SampleParentWithDerived getSampleGenerationInfo(final TechId sampleId)
            throws UserFailureException
    {
        return server.getSampleInfo(getSessionToken(), sampleId);
    }

    public ExternalData getDataSetInfo(TechId datasetTechId)
    {
        return server.getDataSetInfo(getSessionToken(), datasetTechId);
    }

    public Material getMaterialInfo(TechId materialTechId) throws UserFailureException
    {
        return server.getMaterialInfo(getSessionToken(), materialTechId);
    }

    public PlateContent getPlateContent(TechId plateId) throws UserFailureException
    {
        return server.getPlateContent(getSessionToken(), plateId);
    }

    public FeatureVectorDataset getFeatureVectorDataset(DatasetReference dataset,
            CodeAndLabel featureName) throws UserFailureException
    {
        return server.getFeatureVectorDataset(getSessionToken(), dataset, featureName);
    }

    public FeatureVectorValues getWellFeatureVectorValues(String datasetCode, String datastoreCode,
            WellLocation location)
    {
        return server.getWellFeatureVectorValues(getSessionToken(), datasetCode, datastoreCode,
                location);
    }

    public PlateImages getPlateContentForDataset(TechId datasetId)
    {
        return server.getPlateContentForDataset(getSessionToken(), datasetId);
    }

    public TypedTableResultSet<WellContent> listPlateWells(
            IResultSetConfig<String, TableModelRowWithObject<WellContent>> gridCriteria,
            WellSearchCriteria materialCriteria)
    {
        final ITableModelProvider<WellContent> provider =
                new WellContentProvider(server, getSessionToken(), materialCriteria);
        ResultSet<TableModelRowWithObject<WellContent>> resultSet =
                listEntities(gridCriteria, new DataProviderAdapter<WellContent>(provider));
        return new TypedTableResultSet<WellContent>(resultSet);
    }

    public String prepareExportPlateWells(
            TableExportCriteria<TableModelRowWithObject<WellContent>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public List<WellReplicaImage> listWellImages(TechId materialId, TechId experimentId)
            throws UserFailureException
    {
        return server.listWellImages(getSessionToken(), materialId, experimentId);
    }

    public TypedTableResultSet<Material> listMaterials(
            IResultSetConfig<String, TableModelRowWithObject<Material>> gridCriteria,
            WellSearchCriteria materialCriteria)
    {
        List<Material> materials = server.listMaterials(getSessionToken(), materialCriteria);
        final ITableModelProvider<Material> provider =
                new MaterialDisambiguationProvider(materials);
        ResultSet<TableModelRowWithObject<Material>> resultSet =
                listEntities(gridCriteria, new DataProviderAdapter<Material>(provider));
        return new TypedTableResultSet<Material>(resultSet);
    }

    public String prepareExportMaterials(
            TableExportCriteria<TableModelRowWithObject<Material>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public TypedTableResultSet<WellMetadata> listPlateMetadata(
            IResultSetConfig<String, TableModelRowWithObject<WellMetadata>> criteria,
            TechId sampleId)
    {
        PlateMetadataProvider metaDataProvider =
                new PlateMetadataProvider(server, getSessionToken(), sampleId);
        DataProviderAdapter<WellMetadata> dataProvider =
                new DataProviderAdapter<WellMetadata>(metaDataProvider);
        ResultSet<TableModelRowWithObject<WellMetadata>> resultSet =
                listEntities(criteria, dataProvider);
        return new TypedTableResultSet<WellMetadata>(resultSet);
    }

    public String prepareExportPlateMetadata(
            TableExportCriteria<TableModelRowWithObject<WellMetadata>> criteria)
    {
        return prepareExportEntities(criteria);
    }

    public void registerLibrary(LibraryRegistrationInfo details) throws UserFailureException
    {
        final String sessionToken = getSessionToken();
        HttpSession session = getHttpSession();
        UploadedFilesBean uploadedFiles = null;
        String sessionKey = details.getSessionKey();
        String experiment = details.getExperiment();
        try
        {
            String space =
                    new ExperimentIdentifierFactory(experiment).createIdentifier().getSpaceCode();
            uploadedFiles = getUploadedFiles(sessionKey, session);
            for (IUncheckedMultipartFile file : uploadedFiles.iterable())
            {
                LibraryExtractor extractor =
                        new LibraryExtractor(file.getInputStream(), details.getSeparator(),
                                experiment, space, details.getPlateGeometry(), details.getScope());
                extractor.extract();
                server.registerLibrary(sessionToken, details.getUserEmail(),
                        extractor.getNewGenes(), extractor.getNewOligos(),
                        extractor.getNewSamplesWithType());
            }
        } finally
        {
            cleanUploadedFiles(sessionKey, session, uploadedFiles);
        }

    }

    public Vocabulary getPlateGeometryVocabulary() throws UserFailureException
    {
        final String sessionToken = getSessionToken();
        return server.getVocabulary(sessionToken, ScreeningConstants.PLATE_GEOMETRY);
    }

    public LogicalImageInfo getImageDatasetInfo(String datasetCode, String datastoreCode,
            WellLocation wellLocationOrNull)
    {
        final String sessionToken = getSessionToken();
        return server.getImageDatasetInfo(sessionToken, datasetCode, datastoreCode,
                wellLocationOrNull);
    }

    public ImageDatasetEnrichedReference getImageDatasetReference(String datasetCode,
            String datastoreCode)
    {
        final String sessionToken = getSessionToken();
        return server.getImageDatasetReference(sessionToken, datasetCode, datastoreCode);
    }

    public ImageSampleContent getImageDatasetInfosForSample(TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        final String sessionToken = getSessionToken();
        return server.getImageDatasetInfosForSample(sessionToken, sampleId, wellLocationOrNull);
    }

    public ResultSet<Material> listExperimentMaterials(final TechId experimentId,
            final ListMaterialDisplayCriteria displayCriteria)
    {
        return listEntities(displayCriteria,
                new AbstractOriginalDataProviderWithoutHeaders<Material>()
                    {
                        @Override
                        public List<Material> getFullOriginalData() throws UserFailureException
                        {
                            return server.listExperimentMaterials(getSessionToken(), experimentId,
                                    displayCriteria.getListCriteria().tryGetMaterialType());
                        }
                    });
    }

    public TypedTableResultSet<MaterialFeatureVectorSummary> listExperimentFeatureVectorSummary(
            IResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> criteria,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        FeatureVectorSummaryProvider provider =
                new FeatureVectorSummaryProvider(server, getSessionToken(), experimentId,
                        analysisProcedureCriteria);
        return listEntities(provider, criteria);

    }

    public TypedTableResultSet<MaterialReplicaFeatureSummary> listMaterialReplicaFeatureSummary(
            IResultSetConfig<String, TableModelRowWithObject<MaterialReplicaFeatureSummary>> resultSetConfig,
            TechId experimentId, TechId materialId)
    {
        MaterialReplicaFeatureSummaryProvider provider =
                new MaterialReplicaFeatureSummaryProvider(server, getSessionToken(), experimentId,
                        materialId);
        return listEntities(provider, resultSetConfig);
    }

    public String prepareExportFeatureVectorSummary(
            TableExportCriteria<TableModelRowWithObject<MaterialFeatureVectorSummary>> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public String prepareExportMaterialReplicaFeatureSummary(
            TableExportCriteria<TableModelRowWithObject<MaterialReplicaFeatureSummary>> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public TypedTableResultSet<MaterialSimpleFeatureVectorSummary> listMaterialFeaturesFromAllExperiments(
            IResultSetConfig<String, TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> resultSetConfig,
            TechId materialId, ExperimentSearchByProjectCriteria experimentSearchCriteria)
            throws UserFailureException
    {

        MaterialFeatureVectorsFromAllExperimentsProvider provider =
                new MaterialFeatureVectorsFromAllExperimentsProvider(server, getSessionToken(),
                        materialId, experimentSearchCriteria);
        return listEntities(provider, resultSetConfig);
    }

    public String prepareExportMaterialFeaturesFromAllExperiments(
            TableExportCriteria<TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> criteria)
            throws UserFailureException
    {
        return prepareExportEntities(criteria);
    }

    public AnalysisProcedures listAnalysisProcedures(
            ExperimentSearchCriteria experimentSearchCriteria)
    {
        return server.listAnalysisProcedures(getSessionToken(), experimentSearchCriteria);
    }

}
