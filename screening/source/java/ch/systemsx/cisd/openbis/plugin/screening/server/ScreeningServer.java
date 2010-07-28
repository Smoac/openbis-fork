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

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.business.bo.ISampleBO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.plugin.IDataSetTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.server.plugin.ISampleTypeSlaveServerPlugin;
import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleParentWithDerived;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.translator.SampleTranslator;
import ch.systemsx.cisd.openbis.generic.shared.translator.VocabularyTranslator;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.GenePlateLocationsLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.PlateContentLoader;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.ScreeningApiImpl;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.ResourceNames;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;

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
    static final int MINOR_VERSION = 1;

    @Resource(name = ResourceNames.SCREENING_BUSINESS_OBJECT_FACTORY)
    private IScreeningBusinessObjectFactory businessObjectFactory;

    @Resource(name = ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER)
    protected ICommonServer commonServer;

    public ScreeningServer()
    {
    }

    @Private
    ScreeningServer(final ISessionManager<Session> sessionManager, final IDAOFactory daoFactory,
            final IScreeningBusinessObjectFactory businessObjectFactory,
            final ISampleTypeSlaveServerPlugin sampleTypeSlaveServerPlugin,
            final IDataSetTypeSlaveServerPlugin dataSetTypeSlaveServerPlugin)
    {
        super(sessionManager, daoFactory, sampleTypeSlaveServerPlugin, dataSetTypeSlaveServerPlugin);
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

    public PlateImages getPlateContentForDataset(String sessionToken, TechId datasetId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadImagesAndMetadataForDataset(session, businessObjectFactory,
                datasetId);
    }

    public List<WellContent> getPlateLocations(String sessionToken, TechId geneMaterialId,
            ExperimentIdentifier experimentIdentifier)
    {
        Session session = getSession(sessionToken);
        return GenePlateLocationsLoader.load(session, businessObjectFactory, getDAOFactory(),
                geneMaterialId, experimentIdentifier, true);
    }

    public List<WellContent> listPlateLocations(String sessionToken,
            PlateMaterialsSearchCriteria materialCriteria)
    {
        Session session = getSession(sessionToken);
        return GenePlateLocationsLoader.load(session, businessObjectFactory, getDAOFactory(),
                materialCriteria, true);
    }

    public TableModel loadImageAnalysisForExperiment(String sessionToken, TechId experimentId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader.loadImageAnalysisForExperiment(session, businessObjectFactory,
                experimentId);
    }

    public TableModel loadImageAnalysisForPlate(String sessionToken, TechId plateId)
    {
        Session session = getSession(sessionToken);
        return PlateContentLoader
                .loadImageAnalysisForPlate(session, businessObjectFactory, plateId);
    }

    public ExternalData getDataSetInfo(String sessionToken, TechId datasetId)
    {
        return commonServer.getDataSetInfo(sessionToken, datasetId);
    }

    public Vocabulary getVocabulary(String sessionToken, String code) throws UserFailureException
    {
        checkSession(sessionToken);
        IVocabularyDAO vocabularyDAO = getDAOFactory().getVocabularyDAO();
        VocabularyPE vocabulary = vocabularyDAO.tryFindVocabularyByCode(code);
        return VocabularyTranslator.translate(vocabulary);
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

    public List<PlateWellReferenceWithDatasets> listPlateWells(
            String sessionToken,
            ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier experimentIdentifer,
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        return createScreeningApiImpl(sessionToken).listPlateWells(experimentIdentifer,
                materialIdentifier, findDatasets);
    }

    public List<Plate> listPlates(String sessionToken)
    {
        return createScreeningApiImpl(sessionToken).listPlates();
    }

    public List<ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier> listExperiments(
            String sessionToken)
    {
        return createScreeningApiImpl(sessionToken).listExperiments();
    }

    public List<IDatasetIdentifier> getDatasetIdentifiers(String sessionToken,
            List<String> datasetCodes)
    {
        return createScreeningApiImpl(sessionToken).getDatasetIdentifiers(datasetCodes);
    }

    private ScreeningApiImpl createScreeningApiImpl(String sessionToken)
    {
        final Session session = getSession(sessionToken);
        return new ScreeningApiImpl(session, businessObjectFactory, getDAOFactory(),
                getDataStoreBaseURL());
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
