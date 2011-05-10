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

package ch.systemsx.cisd.openbis.plugin.screening.server;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import net.lemnik.eodsql.DataIterator;
import net.lemnik.eodsql.QueryTool;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.mail.MailClient;
import ch.systemsx.cisd.common.mail.MailClientParameters;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.IPropertiesBatchManager;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.business.bo.common.DatabaseContextUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSamplesWithTypes;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.plugin.generic.shared.IGenericServer;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ExperimentFeatureVectorSummaryLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.FeatureVectorValuesLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.LogicalImageLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.MaterialFeatureVectorSummaryLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.PlateContentLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningApiImpl;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellContentLoader;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetEnrichedReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageSampleContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.LogicalImageInfo;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReplicaImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;

/**
 * The concrete {@link IScreeningServer} implementation.
 * 
 * @author Tomasz Pylak
 */
@Component(ResourceNames.SCREENING_PLUGIN_SERVER)
public final class ScreeningServer extends AbstractServer<IScreeningServer> implements
        IScreeningServer, IScreeningApiServer
{
    /**
     * The minor version of this service.
     */
    public static final int MINOR_VERSION = 7;

    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10, 360,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    @Resource(name = ResourceNames.SCREENING_BUSINESS_OBJECT_FACTORY)
    private IScreeningBusinessObjectFactory businessObjectFactory;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    private ICommonServer commonServer;

    @Resource(name = ch.systemsx.cisd.openbis.plugin.generic.shared.ResourceNames.GENERIC_PLUGIN_SERVER)
    private IGenericServer genericServer;

    @Resource(name = ResourceNames.MAIL_CLIENT_PARAMETERS)
    private MailClientParameters mailClientParameters;

    public ScreeningServer()
    {
    }

    @Private
    ScreeningServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            IPropertiesBatchManager propertiesBatchManager,
            final IScreeningBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, propertiesBatchManager, sampleTypeSlaveServerPlugin,
                dataSetTypeSlaveServerPlugin);
        this.businessObjectFactory = businessObjectFactory;
    }

    //
    // IInvocationLoggerFactory
    //

    /**
     * Creates a logger used to log invocations of objects of this class.
     */
    public final IScreeningServer createLogger(IInvocationLoggerContext context)
    {
        return new ScreeningServerLogger(getSessionManager(), context);
    }

    //
    // IScreeningServer
    //

    public final SampleParentWithDerived getSampleInfo(final String sessionToken,
            final TechId sampleId)
    {
        final Session session = getSession(sessionToken);
        final ISampleBO sampleBO = businessObjectFactory.createSampleBO(session);
        sampleBO.loadDataByTechId(sampleId);
        final SamplePE sample = sampleBO.getSample();
        return SampleTranslator.translate(getSampleTypeSlaveServerPlugin(sample.getSampleType())
                .getSampleInfo(session, sample), session.getBaseIndexURL());
    }

    public PlateContent getPlateContent(String sessionToken, TechId plateId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadImagesAndMetadata(session, businessObjectFactory, plateId);
    }

    public FeatureVectorDataset getFeatureVectorDataset(String sessionToken,
            DatasetReference dataset, CodeAndLabel featureName)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadFeatureVectorDataset(session, businessObjectFactory, dataset,
                featureName);
    }

    public PlateImages getPlateContentForDataset(String sessionToken, TechId datasetId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadImagesAndMetadataForDataset(session, businessObjectFactory,
                datasetId);
    }

    public List<WellContent> listPlateWells(String sessionToken, WellSearchCriteria materialCriteria)
    {
        Session session = getSession(sessionToken);
        return WellContentLoader.load(session, businessObjectFactory, getDAOFactory(),
                materialCriteria);
    }

    public List<WellReplicaImage> listWellImages(String sessionToken, TechId materialId,
            TechId experimentId)
    {
        Session session = getSession(sessionToken);
        return WellContentLoader.loadWithImages(session, businessObjectFactory, getDAOFactory(),
                materialId, experimentId, createDefaultSettings());
    }

    public List<Material> listMaterials(String sessionToken, WellSearchCriteria materialCriteria)
    {
        Session session = getSession(sessionToken);
        return WellContentLoader.loadMaterials(session, businessObjectFactory, getDAOFactory(),
                materialCriteria);
    }

    public FeatureVectorValues getWellFeatureVectorValues(String sessionToken, String datasetCode,
            String datastoreCode, WellLocation wellLocation)
    {
        getSession(sessionToken);
        return FeatureVectorValuesLoader.loadFeatureVectorValues(businessObjectFactory,
                datasetCode, datastoreCode, wellLocation);
    }

    public LogicalImageInfo getImageDatasetInfo(String sessionToken, String datasetCode,
            String datastoreCode, WellLocation wellLocationOrNull)
    {
        Session session = getSession(sessionToken);
        return LogicalImageLoader.loadLogicalImageInfo(session, businessObjectFactory, datasetCode,
                datastoreCode, wellLocationOrNull);
    }

    public ImageDatasetEnrichedReference getImageDatasetReference(String sessionToken,
            String datasetCode, String datastoreCode)
    {
        Session session = getSession(sessionToken);
        return LogicalImageLoader.getImageDatasetReference(session, businessObjectFactory,
                datasetCode, datastoreCode);
    }

    public ImageSampleContent getImageDatasetInfosForSample(String sessionToken, TechId sampleId,
            WellLocation wellLocationOrNull)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.getImageDatasetInfosForSample(session, businessObjectFactory,
                sampleId, wellLocationOrNull);
    }

    public ExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        return commonServer.getDataSetInfo(sessionToken, datasetId);
    }

    public Material getMaterialInfo(String sessionToken, TechId materialId)
    {
        return genericServer.getMaterialInfo(sessionToken, materialId);
    }

    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException
    {
        checkSession(sessionToken);
        IVocabularyDAO vocabularyDAO = getDAOFactory().getVocabularyDAO();
        VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode(code);
        return VocabularyTranslator.translate(vocabulary);
    }

    public void registerLibrary(String sessionToken, String userEmail,
            List<NewMaterial> newGenesOrNull, List<NewMaterial> newOligosOrNull,
            List<NewSamplesWithTypes> newSamplesWithType)
    {
        IMailClient mailClient = new MailClient(mailClientParameters);
        executor.submit(new LibraryRegistrationTask(sessionToken, userEmail, newGenesOrNull,
                newOligosOrNull, newSamplesWithType, commonServer, genericServer, getDAOFactory(),
                mailClient));
    }

    public List<Material> listExperimentMaterials(String sessionToken, TechId experimentId,
            MaterialType materialType)
    {
        // TODO 2010-09-01, Piotr Buczek: move it to some BO when we have more queries like that
        IScreeningQuery dao = createDAO(getDAOFactory());
        DataIterator<Long> materialIdsIterator =
                dao.getMaterialsForExperimentWells(experimentId.getId(), materialType.getId());
        Collection<Long> materialIds = new ArrayList<Long>();
        for (Long id : materialIdsIterator)
        {
            materialIds.add(id);
        }
        return commonServer
                .listMaterials(sessionToken, new ListMaterialCriteria(materialIds), true);
    }

    public ExperimentFeatureVectorSummary getExperimentFeatureVectorSummary(String sessionToken,
            TechId experimentId)
    {
        Session session = getSession(sessionToken);
        // NOTE: we want the settings t be passed form the client in future
        MaterialSummarySettings settings = createDefaultSettings();
        return ExperimentFeatureVectorSummaryLoader.loadExperimentFeatureVectors(session,
                businessObjectFactory, getDAOFactory(), experimentId, settings);
    }

    public static MaterialSummarySettings createDefaultSettings()
    {
        MaterialSummarySettings settings = new MaterialSummarySettings();
        settings.setAggregationType(MaterialReplicaSummaryAggregationType.MEDIAN);
        settings.setFeatureCodes(new ArrayList<String>());
        settings.setReplicaMatrialTypePatterns(ScreeningConstants.REPLICA_METERIAL_TYPE_PATTERN);
        settings.setBiologicalReplicatePropertyTypeCodes("CONCENTRATION", "SIRNA");
        return settings;
    }

    public MaterialReplicaFeatureSummaryResult getMaterialFeatureVectorSummary(String sessionToken,
            TechId experimentId, TechId materialId)
    {
        Session session = getSession(sessionToken);
        // NOTE: we want the settings to be passed form the client in future
        MaterialSummarySettings settings = createDefaultSettings();
        return MaterialFeatureVectorSummaryLoader.loadMaterialFeatureVectors(session,
                businessObjectFactory, getDAOFactory(), materialId, experimentId, settings);
    }

    // --------- IScreeningOpenbisServer - method signature should be changed with care

    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        return createScreeningApiImpl(sessionToken).listFeatureVectorDatasets(plates);
    }

    public List<ImageDatasetReference> listImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        return createScreeningApiImpl(sessionToken).listImageDatasets(plates);
    }

    public List<ImageDatasetReference> listRawImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        return createScreeningApiImpl(sessionToken).listRawImageDatasets(plates);
    }

    public List<ImageDatasetReference> listSegmentationImageDatasets(String sessionToken,
            List<? extends PlateIdentifier> plates)
    {
        return createScreeningApiImpl(sessionToken).listSegmentationImageDatasets(plates);
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return createScreeningApiImpl(sessionToken).listPlateWells(experimentIdentifer,
                materialIdentifier, findDatasets);
    }

    public List<PlateWellReferenceWithDatasets> listPlateWells(String sessionToken,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return createScreeningApiImpl(sessionToken)
                .listPlateWells(materialIdentifier, findDatasets);
    }

    public List<WellIdentifier> listPlateWells(String sessionToken, PlateIdentifier plateIdentifier)
    {
        return createScreeningApiImpl(sessionToken).listPlateWells(plateIdentifier);
    }

    public Sample getWellSample(String sessionToken, WellIdentifier wellIdentifier)
    {
        return createScreeningApiImpl(sessionToken).getWellSample(wellIdentifier, true);
    }

    public Sample getPlateSample(String sessionToken, PlateIdentifier plateIdentifier)
    {
        return createScreeningApiImpl(sessionToken).getPlateSample(plateIdentifier);
    }

    public List<Plate> listPlates(String sessionToken)
    {
        return createScreeningApiImpl(sessionToken).listPlates();
    }

    public List<Plate> listPlates(String sessionToken, ExperimentIdentifier experiment)
    {
        return createScreeningApiImpl(sessionToken).listPlates(experiment);
    }

    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier> listExperiments(
            String sessionToken)
    {
        return createScreeningApiImpl(sessionToken).listExperiments();
    }

    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier> listExperiments(
            String sessionToken, String userId)
    {
        return createScreeningApiImpl(sessionToken).listExperiments(userId);
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            List<String> datasetCodes)
    {
        return createScreeningApiImpl(sessionToken).getDatasetIdentifiers(datasetCodes);
    }

    public List<PlateWellMaterialMapping> listPlateMaterialMapping(String sessionToken,
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        return createScreeningApiImpl(sessionToken).listPlateMaterialMapping(plates,
                materialTypeIdentifierOrNull);
    }

    private static IScreeningQuery createDAO(IDAOFactory daoFactory)
    {
        Connection connection = DatabaseContextUtils.getConnection(daoFactory);
        return QueryTool.getQuery(connection, IScreeningQuery.class);
    }

    private ScreeningApiImpl createScreeningApiImpl(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return new ScreeningApiImpl(session, businessObjectFactory, getDAOFactory());
    }

    public void logoutScreening(String sessionToken)
    {
        logout(sessionToken);
    }

    public String tryLoginScreening(String userId, String userPassword)
    {
        SessionContextDTO sessionContext = tryToAuthenticate(userId, userPassword);
        if (sessionContext != null)
        {
            return sessionContext.getSessionToken();
        } else
        {
            return null;
        }
    }

    public int getMajorVersion()
    {
        return MAJOR_VERSION;
    }

    public int getMinorVersion()
    {
        return MINOR_VERSION;
    }

}
