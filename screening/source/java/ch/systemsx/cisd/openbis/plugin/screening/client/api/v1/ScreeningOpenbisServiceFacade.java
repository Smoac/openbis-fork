package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.time.DateUtils;

import ch.systemsx.cisd.base.image.IImageTransformerFactory;
import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.openbis.dss.client.api.v1.DssComponentFactory;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IDssComponent;
import ch.systemsx.cisd.openbis.dss.client.api.v1.IOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.client.api.v1.impl.OpenbisServiceFacade;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssBuilder;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.FileInfoDssDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwner;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetDTO.DataSetOwnerType;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationChangingService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClause;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchCriteria.MatchClauseAttribute;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SearchSubCriteria;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.IDataSetFilter;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.filter.TypeBasedDataSetFilter;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.WellImageCache.CachedImage;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.WellImageCache.WellImages;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageSize;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * A client side facade of openBIS and Datastore Server API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningOpenbisServiceFacade implements IScreeningOpenbisServiceFacade
{
    static final int MAJOR_VERSION_AS = 1;

    static final int MAJOR_VERSION_DSS = 1;

    static final String DSS_SCREENING_API = "/rmi-datastore-server-screening-api-v"
            + MAJOR_VERSION_DSS + "/";

    private static final String OPENBIS_SCREENING_API = "/rmi-screening-api-v" + MAJOR_VERSION_AS;

    static final long SERVER_TIMEOUT_MILLIS = 5 * DateUtils.MILLIS_PER_MINUTE;

    private static final IDssServiceFactory DSS_SERVICE_FACTORY = new IDssServiceFactory()
        {
            public DssServiceRpcScreeningHolder createDssService(String serverUrl)
            {
                return new DssServiceRpcScreeningHolder(serverUrl);
            }
        };
        
    private final IScreeningApiServer openbisScreeningServer;

    private final IGeneralInformationService generalInformationService;

    private final IGeneralInformationChangingService generalInformationChangingService;

    private final IDssComponent dssComponent;

    private final DataStoreMultiplexer<PlateImageReference> plateImageReferencesMultiplexer;

    private final DataStoreMultiplexer<IFeatureVectorDatasetIdentifier> featureVectorDataSetIdentifierMultiplexer;

    private final DataStoreMultiplexer<FeatureVectorDatasetReference> featureVectorDataSetReferenceMultiplexer;

    private final DataStoreMultiplexer<FeatureVectorDatasetWellReference> featureVectorDataSetWellReferenceMultiplexer;

    private final DataStoreMultiplexer<IImageDatasetIdentifier> metaDataMultiplexer;

    private final String sessionToken;

    private final int minorVersionApplicationServer;

    private final Map<IImageDatasetIdentifier, ImageDatasetMetadata> imageMetadataCache =
            new ConcurrentHashMap<IImageDatasetIdentifier, ImageDatasetMetadata>();

    private final WellImageCache imageCache = new WellImageCache();

    private IDssServiceFactory dssServiceCache;

    private final IOpenbisServiceFacade openbisServiceFacade;


    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL.
     * Authenticates the user.
     * 
     * @return null if the user could not be authenticated.
     */
    public static IScreeningOpenbisServiceFacade tryCreate(String userId, String userPassword,
            String serverUrl)
    {
        final IScreeningApiServer openbisServer = createScreeningOpenbisServer(serverUrl);
        final String sessionToken = openbisServer.tryLoginScreening(userId, userPassword);
        if (sessionToken == null)
        {
            return null;
        }
        return tryCreate(sessionToken, serverUrl, openbisServer);
    }

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL for
     * an authenticated user.
     * 
     * @param sessionToken The session token for the authenticated user
     * @param serverUrl The URL for the openBIS application server
     */
    public static IScreeningOpenbisServiceFacade tryCreate(String sessionToken, String serverUrl)
    {
        return tryCreate(sessionToken, serverUrl, createScreeningOpenbisServer(serverUrl));
    }

    private static IScreeningOpenbisServiceFacade tryCreate(String sessionToken, String serverUrl,
            final IScreeningApiServer openbisServer)
    {
        final IGeneralInformationService generalInformationService =
                createGeneralInformationService(serverUrl);
        IGeneralInformationChangingService generalInformationChangingService =
                createGeneralInformationChangingService(serverUrl);
        final int minorVersion = openbisServer.getMinorVersion();
        final IDssComponent dssComponent =
                DssComponentFactory.tryCreate(sessionToken, serverUrl, SERVER_TIMEOUT_MILLIS);
        return new ScreeningOpenbisServiceFacade(sessionToken, openbisServer, minorVersion,
                DSS_SERVICE_FACTORY, dssComponent, generalInformationService,
                generalInformationChangingService);
    }

    private static IScreeningApiServer createScreeningOpenbisServer(String serverUrl)
    {
        ServiceFinder serviceFinder = new ServiceFinder("openbis", OPENBIS_SCREENING_API);
        return serviceFinder.createService(IScreeningApiServer.class, serverUrl);
    }

    private static IGeneralInformationService createGeneralInformationService(String serverUrl)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationService.SERVICE_URL);
        IGeneralInformationService service =
                generalInformationServiceFinder.createService(IGeneralInformationService.class,
                        serverUrl);
        return service;
    }

    private static IGeneralInformationChangingService createGeneralInformationChangingService(
            String serverUrl)
    {
        ServiceFinder generalInformationServiceFinder =
                new ServiceFinder("openbis", IGeneralInformationChangingService.SERVICE_URL);
        IGeneralInformationChangingService service =
                generalInformationServiceFinder.createService(
                        IGeneralInformationChangingService.class, serverUrl);
        return service;
    }

    ScreeningOpenbisServiceFacade(String sessionToken, IScreeningApiServer screeningServer,
            int minorVersion, final IDssServiceFactory dssServiceFactory,
            IDssComponent dssComponent, IGeneralInformationService generalInformationService,
            IGeneralInformationChangingService generalInformationChangingService)
    {
        this.openbisScreeningServer = screeningServer;
        this.generalInformationService = generalInformationService;
        this.generalInformationChangingService = generalInformationChangingService;
        this.dssComponent = dssComponent;
        this.sessionToken = sessionToken;
        openbisServiceFacade = new OpenbisServiceFacade(sessionToken, generalInformationService, dssComponent);

        this.minorVersionApplicationServer = minorVersion;
        dssServiceCache = new IDssServiceFactory()
            {
                private final Map<String/* url */, DssServiceRpcScreeningHolder> cache =
                        new HashMap<String, DssServiceRpcScreeningHolder>();

                public DssServiceRpcScreeningHolder createDssService(String serverUrl)
                {
                    DssServiceRpcScreeningHolder dssServiceHolder = cache.get(serverUrl);
                    if (dssServiceHolder == null)
                    {
                        dssServiceHolder = dssServiceFactory.createDssService(serverUrl);
                        cache.put(serverUrl, dssServiceHolder);
                    }
                    return dssServiceHolder;
                }
            };
        plateImageReferencesMultiplexer =
                new DataStoreMultiplexer<PlateImageReference>(dssServiceCache);
        metaDataMultiplexer = new DataStoreMultiplexer<IImageDatasetIdentifier>(dssServiceCache);
        featureVectorDataSetIdentifierMultiplexer =
                new DataStoreMultiplexer<IFeatureVectorDatasetIdentifier>(dssServiceCache);
        featureVectorDataSetReferenceMultiplexer =
                new DataStoreMultiplexer<FeatureVectorDatasetReference>(dssServiceCache);
        featureVectorDataSetWellReferenceMultiplexer =
                new DataStoreMultiplexer<FeatureVectorDatasetWellReference>(dssServiceCache);
    }

    /**
     * Return the session token for this authenticated user.
     */
    public String getSessionToken()
    {
        return sessionToken;
    }

    /** Closes connection with the server. After calling this method this facade cannot be used. */
    public void logout()
    {
        checkASMinimalMinorVersion("logoutScreening");
        openbisScreeningServer.logoutScreening(sessionToken);
    }
    
    public void clearWellImageCache()
    {
        imageCache.clear();
    }

    /**
     * Return the list of all visible plates assigned to any experiment, along with their
     * hierarchical context (space, project, experiment).
     */
    public List<Plate> listPlates()
    {
        checkASMinimalMinorVersion("listPlates");
        return openbisScreeningServer.listPlates(sessionToken);
    }

    /**
     * Return the list of all plates for the given <var>experiment</var>.
     */
    public List<Plate> listPlates(ExperimentIdentifier experiment)
    {
        if (hasASMethod("listPlates", ExperimentIdentifier.class))
        {
            return openbisScreeningServer.listPlates(sessionToken, experiment);
        } else
        {
            final List<Plate> allPlates = listPlates();
            final List<Plate> result = new ArrayList<Plate>(allPlates.size());
            for (Plate plate : allPlates)
            {
                if (plate.getExperimentIdentifier().getPermId().equals(experiment.getPermId())
                        || plate.getExperimentIdentifier().getAugmentedCode()
                                .equals(experiment.getAugmentedCode()))
                {
                    result.add(plate);
                }
            }
            return result;
        }
    }

    public List<Plate> listPlates(ExperimentIdentifier experiment, String analysisProcedure)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        searchCriteria.addMatchClause(MatchClause.createAttributeMatch(MatchClauseAttribute.TYPE,
                ScreeningConstants.DEFAULT_PLATE_SAMPLE_TYPE_CODE));
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, experiment.getExperimentCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PROJECT, experiment.getProjectCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.SPACE, experiment.getSpaceCode()));
        searchCriteria.addSubCriteria(SearchSubCriteria
                .createExperimentCriteria(experimentCriteria));
        List<Sample> samples = openbisServiceFacade.searchForSamples(searchCriteria);
        List<ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet> dataSets =
                openbisServiceFacade.listDataSets(samples, null);
        Set<String> sampleIdentifiers = new HashSet<String>();
        for (ch.systemsx.cisd.openbis.dss.client.api.v1.DataSet dataSet : dataSets)
        {
            if (analysisProcedure.equals(dataSet.getProperties().get(
                    ScreeningConstants.ANALYSIS_PROCEDURE)))
            {
                sampleIdentifiers.add(dataSet.getSampleIdentifierOrNull());
            }
        }

        List<Plate> plates = new ArrayList<Plate>();
        for (Sample sample : samples)
        {
            String sampleIdentifier = sample.getIdentifier();
            if (sampleIdentifiers.contains(sampleIdentifier))
            {
                String spaceCode =
                        SampleIdentifierFactory.parse(sampleIdentifier).getSpaceLevel()
                                .getSpaceCode();
                String permID = sample.getPermId();
                String experimentIdentifierOrNull = sample.getExperimentIdentifierOrNull();
                ExperimentIdentifier expermientIdentifier =
                        experimentIdentifierOrNull == null ? null : ExperimentIdentifier
                                .createFromAugmentedCode(experimentIdentifierOrNull);
                plates.add(new Plate(sample.getCode(), spaceCode, permID, expermientIdentifier));
            }
        }
        return plates;
    }

    public List<ExperimentIdentifier> listExperiments()
    {
        checkASMinimalMinorVersion("listExperiments");
        return openbisScreeningServer.listExperiments(sessionToken);
    }

    public List<ExperimentIdentifier> listExperiments(String userId)
    {
        checkASMinimalMinorVersion("listExperiments", String.class);
        return openbisScreeningServer.listExperiments(sessionToken, userId);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing feature
     * vectors.
     */
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates)
    {
        checkASMinimalMinorVersion("listFeatureVectorDatasets", List.class);
        return openbisScreeningServer.listFeatureVectorDatasets(sessionToken, plates);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing raw images.
     * 
     * @deprecated Use {@link #listRawImageDatasets(List)} instead.
     */
    @Deprecated
    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        checkASMinimalMinorVersion("listImageDatasets", List.class);
        return openbisScreeningServer.listImageDatasets(sessionToken, plates);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing raw images.
     */
    public List<ImageDatasetReference> listRawImageDatasets(List<? extends PlateIdentifier> plates)
    {
        if (hasASMethod("listRawImageDatasets", List.class))
        {
            return openbisScreeningServer.listRawImageDatasets(sessionToken, plates);
        } else
        {
            checkASMinimalMinorVersion("listImageDatasets", List.class);
            return openbisScreeningServer.listImageDatasets(sessionToken, plates);
        }
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing images.
     */
    public List<ImageDatasetReference> listSegmentationImageDatasets(
            List<? extends PlateIdentifier> plates)
    {
        if (hasASMethod("listSegmentationImageDatasets", List.class))
        {
            return openbisScreeningServer.listSegmentationImageDatasets(sessionToken, plates);
        }
        return Collections.emptyList();
    }

    /**
     * For the given <var>experimentIdentifier</var> find all plate locations that are connected to
     * the specified <var>materialIdentifier</var>. If <code>findDatasets == true</code>, find also
     * the connected image and image analysis data sets for the relevant plates.
     */
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            boolean findDatasets)
    {
        checkASMinimalMinorVersion("listPlateWells", ExperimentIdentifier.class,
                MaterialIdentifier.class, boolean.class);
        return openbisScreeningServer.listPlateWells(sessionToken, experimentIdentifer,
                materialIdentifier, findDatasets);
    }

    /**
     * For the given <var>materialIdentifier</var> find all plate locations that are connected to
     * it. If <code>findDatasets == true</code>, find also the connected image and image analysis
     * data sets for the relevant plates.
     */
    public List<PlateWellReferenceWithDatasets> listPlateWells(
            MaterialIdentifier materialIdentifier, boolean findDatasets)
    {
        checkASMinimalMinorVersion("listPlateWells", MaterialIdentifier.class, boolean.class);
        return openbisScreeningServer
                .listPlateWells(sessionToken, materialIdentifier, findDatasets);
    }

    /**
     * For the given <var>plateIdentifier</var> find all wells that are connected to it.
     */
    public List<WellIdentifier> listPlateWells(PlateIdentifier plateIdentifier)
    {
        checkASMinimalMinorVersion("listPlateWells", PlateIdentifier.class);
        return openbisScreeningServer.listPlateWells(sessionToken, plateIdentifier);
    }

    public Map<String, String> getWellProperties(WellIdentifier wellIdentifier)
    {
        Sample wellSample = openbisScreeningServer.getWellSample(sessionToken, wellIdentifier);
        Map<String, String> properties = wellSample.getProperties();
        return properties;
    }

    public void updateWellProperties(WellIdentifier wellIdentifier, Map<String, String> properties)
    {
        Sample wellSample = openbisScreeningServer.getWellSample(sessionToken, wellIdentifier);
        generalInformationChangingService.updateSampleProperties(sessionToken, wellSample.getId(),
                properties);
    }

    /**
     * Get proxies to the data sets owned by specified well.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    public List<IDataSetDss> getDataSets(WellIdentifier wellIdentifier,
            String datasetTypeCodePattern) throws IllegalStateException,
            EnvironmentFailureException
    {
        return getDataSets(wellIdentifier, new TypeBasedDataSetFilter(datasetTypeCodePattern));
    }

    public List<IDataSetDss> getDataSets(WellIdentifier wellIdentifier, IDataSetFilter dataSetFilter)
            throws IllegalStateException, EnvironmentFailureException
    {
        final Sample wellSample = getWellSample(wellIdentifier);
        return getDataSets(wellSample, dataSetFilter);
    }

    public IDataSetDss getDataSet(String dataSetCode) throws IllegalStateException,
            EnvironmentFailureException
    {
        return dssComponent.getDataSet(dataSetCode);
    }

    /**
     * Get proxies to the data sets owned by specified plate.
     * 
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     */
    public List<IDataSetDss> getDataSets(PlateIdentifier plateIdentifier,
            final String datasetTypeCodePattern) throws IllegalStateException,
            EnvironmentFailureException
    {
        return getDataSets(plateIdentifier, new TypeBasedDataSetFilter(datasetTypeCodePattern));
    }

    public List<IDataSetDss> getDataSets(PlateIdentifier plateIdentifier,
            IDataSetFilter dataSetFilter) throws IllegalStateException, EnvironmentFailureException
    {
        checkASMinimalMinorVersion("getPlateSample", PlateIdentifier.class);
        Sample sample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        return getDataSets(sample, dataSetFilter);
    }

    private List<IDataSetDss> getDataSets(final Sample sample, IDataSetFilter filter)
    {
        final List<DataSet> dataSets =
                generalInformationService.listDataSetsForSample(sessionToken, sample, true);
        final List<IDataSetDss> result = new ArrayList<IDataSetDss>();
        for (DataSet dataSet : dataSets)
        {
            if (filter.pass(dataSet))
            {
                result.add(dssComponent.getDataSet(dataSet.getCode()));
            }
        }
        return result;
    }

    /**
     * Upload a new data set to the DSS for a well.
     * 
     * @param wellIdentifier Identifier of a well that should become owner of the new data set
     * @param dataSetFile A file or folder containing the data
     * @param dataSetMetadataOrNull The optional metadata overriding server defaults for the new
     *            data set
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     * @throws IOException when accessing the data set file or folder fails
     */
    public IDataSetDss putDataSet(WellIdentifier wellIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException
    {
        final Sample wellSample = getWellSample(wellIdentifier);
        return createDataSetDss(wellSample, dataSetMetadataOrNull, dataSetFile);
    }

    private Sample getWellSample(WellIdentifier wellIdentifier)
    {
        checkASMinimalMinorVersion("getWellSample", WellIdentifier.class);
        return openbisScreeningServer.getWellSample(sessionToken, wellIdentifier);
    }

    /**
     * Upload a new data set to the DSS for a plate.
     * 
     * @param plateIdentifier Identifier of a plate that should become owner of the new data set
     * @param dataSetFile A file or folder containing the data
     * @param dataSetMetadataOrNull The optional metadata overriding server defaults for the new
     *            data set
     * @return A proxy to the newly added data set
     * @throws IllegalStateException Thrown if the user has not yet been authenticated.
     * @throws EnvironmentFailureException Thrown in cases where it is not possible to connect to
     *             the server.
     * @throws IOException when accessing the data set file or folder fails
     */
    public IDataSetDss putDataSet(PlateIdentifier plateIdentifier, File dataSetFile,
            NewDataSetMetadataDTO dataSetMetadataOrNull) throws IllegalStateException,
            EnvironmentFailureException, IOException
    {
        Sample sample = openbisScreeningServer.getPlateSample(sessionToken, plateIdentifier);
        return createDataSetDss(sample, dataSetMetadataOrNull, dataSetFile);
    }

    private IDataSetDss createDataSetDss(Sample sample,
            NewDataSetMetadataDTO dataSetMetadataOrNull, File dataSetFile) throws IOException
    {
        final NewDataSetMetadataDTO dataSetMetadata =
                (dataSetMetadataOrNull == null) ? new NewDataSetMetadataDTO()
                        : dataSetMetadataOrNull;
        final DataSetOwner dataSetOwner =
                new DataSetOwner(DataSetOwnerType.SAMPLE, sample.getIdentifier());
        final String dataSetFolderNameOrNull = dataSetFile.isDirectory() ? dataSetFile.getName() : null;
        final List<FileInfoDssDTO> fileInfos = getFileInfosForPath(dataSetFile);
        final NewDataSetDTO newDataSet =
                new NewDataSetDTO(dataSetMetadata, dataSetOwner, dataSetFolderNameOrNull, fileInfos);
        return dssComponent.putDataSet(newDataSet, dataSetFile);
    }

    private List<FileInfoDssDTO> getFileInfosForPath(File file) throws IOException
    {
        ArrayList<FileInfoDssDTO> fileInfos = new ArrayList<FileInfoDssDTO>();
        if (false == file.exists())
        {
            return fileInfos;
        }

        String path = file.getCanonicalPath();
        if (false == file.isDirectory())
        {
            path = file.getParentFile().getCanonicalPath();
        }

        FileInfoDssBuilder builder = new FileInfoDssBuilder(path, path);
        builder.appendFileInfosForFile(file, fileInfos, true);
        return fileInfos;
    }

    /**
     * Converts a given list of dataset codes to dataset identifiers which can be used in other API
     * calls.
     */
    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
        checkASMinimalMinorVersion("getDatasetIdentifiers", List.class);
        return openbisScreeningServer.getDatasetIdentifiers(sessionToken, datasetCodes);
    }

    public List<String> listAvailableFeatureNames(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        return listAvailableFeatureCodes(featureDatasets);
    }

    /**
     * For a given set of feature vector data sets provides the list of all available features. This
     * is just the code of the feature. If for different data sets different sets of features are
     * available, provides the union of the feature names of all data sets.
     */
    public List<String> listAvailableFeatureCodes(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        final Set<String> result = new HashSet<String>();
        featureVectorDataSetIdentifierMultiplexer.process(featureDatasets,
                new IReferenceHandler<IFeatureVectorDatasetIdentifier>()
                    {
                        @SuppressWarnings("deprecation")
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "listAvailableFeatureNames",
                                    List.class);
                            // Use old method in order to allow accessing older servers.
                            result.addAll(dssService.getService().listAvailableFeatureNames(
                                    sessionToken, references));
                        }
                    });
        return new ArrayList<String>(result);
    }

    /**
     * For a given set of plates and a set of features (given by their code), provide all the
     * feature vectors.
     * 
     * @param plates The plates to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all
     *            available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the
     *         <var>featureDatasets</var>.
     */
    public List<FeatureVectorDataset> loadFeaturesForPlates(List<? extends PlateIdentifier> plates,
            final List<String> featureCodesOrNull)
    {
        final List<FeatureVectorDatasetReference> datasets = listFeatureVectorDatasets(plates);
        return loadFeatures(datasets, featureCodesOrNull);
    }

    /**
     * For a given set of data sets and a set of features (given by their code), provide all the
     * feature vectors.
     * 
     * @param featureDatasets The data sets to get the feature vectors for
     * @param featureCodesOrNull The codes of the features to load, or <code>null</code>, if all
     *            available features should be loaded.
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the
     *         <var>featureDatasets</var>.
     */
    public List<FeatureVectorDataset> loadFeatures(
            List<FeatureVectorDatasetReference> featureDatasets,
            final List<String> featureCodesOrNull)
    {
        final List<String> featureNames =
                (isEmpty(featureCodesOrNull)) ? listAvailableFeatureNames(featureDatasets)
                        : featureCodesOrNull;

        final List<FeatureVectorDataset> result = new ArrayList<FeatureVectorDataset>();
        featureVectorDataSetReferenceMultiplexer.process(featureDatasets,
                new IReferenceHandler<FeatureVectorDatasetReference>()
                    {
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<FeatureVectorDatasetReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadFeatures", List.class,
                                    List.class);
                            result.addAll(dssService.getService().loadFeatures(sessionToken,
                                    references, featureNames));
                        }
                    });
        return result;
    }

    public List<FeatureVectorDatasetWellReference> convertToFeatureVectorDatasetWellIdentifier(
            List<PlateWellReferenceWithDatasets> plateWellReferenceWithDataSets)
    {
        final List<FeatureVectorDatasetWellReference> result =
                new ArrayList<FeatureVectorDatasetWellReference>(
                        plateWellReferenceWithDataSets.size());
        for (PlateWellReferenceWithDatasets plateWellRef : plateWellReferenceWithDataSets)
        {
            for (FeatureVectorDatasetReference fvdr : plateWellRef
                    .getFeatureVectorDatasetReferences())
            {
                result.add(createFVDatasetReference(fvdr, plateWellRef.getWellPosition()));
            }
        }
        return result;
    }

    private FeatureVectorDatasetWellReference createFVDatasetReference(
            FeatureVectorDatasetReference fvdr, WellPosition wellPosition)
    {
        return new FeatureVectorDatasetWellReference(fvdr.getDatasetCode(), fvdr.getDataSetType(), 
                fvdr.getDatastoreServerUrl(), fvdr.getPlate(), fvdr.getExperimentIdentifier(),
                fvdr.getPlateGeometry(), fvdr.getRegistrationDate(), fvdr.getParentImageDataset(),
                fvdr.getProperties(), wellPosition);
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            final List<FeatureVectorDatasetWellReference> datasetWellReferences,
            final List<String> featureCodesOrNull)
    {
        final List<String> featureNames =
                (isEmpty(featureCodesOrNull)) ? listAvailableFeatureNames(datasetWellReferences)
                        : featureCodesOrNull;

        final List<FeatureVectorWithDescription> result =
                new ArrayList<FeatureVectorWithDescription>();
        featureVectorDataSetWellReferenceMultiplexer.process(datasetWellReferences,
                new IReferenceHandler<FeatureVectorDatasetWellReference>()
                    {
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<FeatureVectorDatasetWellReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService,
                                    "loadFeaturesForDatasetWellReferences", List.class, List.class);
                            result.addAll(dssService.getService()
                                    .loadFeaturesForDatasetWellReferences(sessionToken, references,
                                            featureNames));
                        }
                    });
        return result;
    }

    private boolean isEmpty(final List<String> featureCodeOrNull)
    {
        return featureCodeOrNull == null || featureCodeOrNull.isEmpty();
    }

    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            List<String> featureCodesOrNull)
    {
        final List<PlateWellReferenceWithDatasets> plateWellRefs =
                listPlateWells(experimentIdentifer, materialIdentifier, true);
        return loadFeatureVectors(featureCodesOrNull, plateWellRefs);
    }

    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, List<String> featureCodesOrNull)
    {
        final List<PlateWellReferenceWithDatasets> plateWellRefs =
                listPlateWells(materialIdentifier, true);
        return loadFeatureVectors(featureCodesOrNull, plateWellRefs);
    }

    private List<FeatureVectorWithDescription> loadFeatureVectors(List<String> featureCodesOrNull,
            final List<PlateWellReferenceWithDatasets> plateWellRefs)
    {
        final List<String> featureCodes =
                isEmpty(featureCodesOrNull) ? listAvailableFeatureCodesForPlateWells(plateWellRefs)
                        : featureCodesOrNull;
        final List<FeatureVectorDatasetWellReference> datasetWellReferences =
                convertToFeatureVectorDatasetWellIdentifier(plateWellRefs);
        final List<FeatureVectorWithDescription> featureVectors =
                loadFeaturesForDatasetWellReferences(datasetWellReferences, featureCodes);
        return featureVectors;
    }

    private List<String> listAvailableFeatureCodesForPlateWells(
            final List<PlateWellReferenceWithDatasets> plateWellRefs)
    {
        final List<FeatureVectorDatasetReference> featureVectorDatasetReferences =
                new ArrayList<FeatureVectorDatasetReference>(plateWellRefs.size());
        for (PlateWellReferenceWithDatasets plateWellRef : plateWellRefs)
        {
            featureVectorDatasetReferences.addAll(plateWellRef.getFeatureVectorDatasetReferences());
        }
        final List<String> availableFeatureCodes =
                listAvailableFeatureCodes(featureVectorDatasetReferences);
        return availableFeatureCodes;
    }

    /**
     * An interface to provide mapping between image references and output streams where the images
     * should be saved.
     */
    public static interface IImageOutputStreamProvider
    {
        /**
         * @return output stream where the image for the specified reference should be saved.
         * @throws IOException when creating the output stream fails
         */
        OutputStream getOutputStream(PlateImageReference imageReference) throws IOException;
    }

    public List<WellPosition> convertToWellPositions(List<WellIdentifier> wellIds)
    {
        final List<WellPosition> result = new ArrayList<WellPosition>(wellIds.size());
        for (WellIdentifier id : wellIds)
        {
            result.add(id.getWellPosition());
        }
        return result;
    }

    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef)
    {
        return createPlateImageReferences(imageDatasetRef, null, null, null);
    }

    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef, List<String> channelCodesOrNull,
            List<WellPosition> wellsOrNull)
    {
        return createPlateImageReferences(imageDatasetRef, null, channelCodesOrNull, wellsOrNull);
    }

    public List<PlateImageReference> createPlateImageReferences(
            ImageDatasetReference imageDatasetRef, ImageDatasetMetadata metadataOrNull,
            List<String> channelCodesOrNull, List<WellPosition> wellsOrNull)
    {
        final List<WellPosition> wellsToUse =
                (wellsOrNull == null || wellsOrNull.isEmpty()) ? createWellPositions(imageDatasetRef
                        .getPlateGeometry()) : wellsOrNull;
        return createPlateImageReferences((IImageDatasetIdentifier) imageDatasetRef,
                metadataOrNull, channelCodesOrNull, wellsToUse);
    }

    public List<PlateImageReference> createPlateImageReferences(
            IImageDatasetIdentifier imageDatasetId, List<String> channeldCodesOrNull,
            List<WellPosition> wellsToUse)
    {
        return createPlateImageReferences(imageDatasetId, null, channeldCodesOrNull, wellsToUse);
    }

    public List<PlateImageReference> createPlateImageReferences(
            IImageDatasetIdentifier imageDatasetId, ImageDatasetMetadata metadataOrNull,
            List<String> channelCodesOrNull, List<WellPosition> wellsToUse)
    {
        final ImageDatasetMetadata metadata = getImageMetadata(imageDatasetId, metadataOrNull);

        final List<String> channelsToUse =
                (channelCodesOrNull == null || channelCodesOrNull.isEmpty()) ? metadata
                        .getChannelCodes() : channelCodesOrNull;
        final List<PlateImageReference> result =
                new ArrayList<PlateImageReference>(wellsToUse.size()
                        * metadata.getNumberOfChannels() * metadata.getNumberOfTiles());
        for (WellPosition well : wellsToUse)
        {
            for (String channel : channelsToUse)
            {
                for (int tile = 0; tile < metadata.getNumberOfTiles(); ++tile)
                {
                    result.add(new PlateImageReference(tile, channel, well, metadata
                            .getImageDataset()));
                }
            }
        }
        return result;
    }

    private ImageDatasetMetadata getImageMetadata(IImageDatasetIdentifier imageDatasetRef,
            ImageDatasetMetadata metadataOrNull)
    {
        if (metadataOrNull != null)
        {
            return metadataOrNull;
        }
        return listImageMetadata(imageDatasetRef);
    }

    private List<WellPosition> createWellPositions(Geometry plateGeometry)
    {
        final List<WellPosition> result =
                new ArrayList<WellPosition>(plateGeometry.getNumberOfRows()
                        * plateGeometry.getNumberOfColumns());
        for (int row = 1; row <= plateGeometry.getNumberOfRows(); ++row)
        {
            for (int col = 1; col <= plateGeometry.getNumberOfColumns(); ++col)
            {
                result.add(new WellPosition(row, col));
            }
        }
        return result;
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the provided output streams. Output streams will not be closed
     * automatically.<br/>
     * <p>
     * If there is an image reference specified which is not referring to the existing image on the
     * server, nothing will be written to the output stream returned by the output streams provider.
     * No exception will be thrown.
     * </p>
     * The images will be converted to PNG format before being shipped.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams
     *             fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        loadImages(imageReferences, outputStreamProvider, true);
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the provided output streams. Output streams will not be closed
     * automatically.<br/>
     * <p>
     * If there is an image reference specified which is not referring to the existing image on the
     * server, nothing will be written to the output stream returned by the output streams provider.
     * No exception will be thrown.
     * </p>
     * If <code>convertToPng==true</code>, the images will be converted to PNG format before being
     * shipped, otherwise they will be shipped in the format that they are stored on the server.<br/>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams
     *             fails
     */
    public void loadImages(final List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider, final boolean convertToPNG)
            throws IOException
    {
        try
        {
            plateImageReferencesMultiplexer.process(imageReferences,
                    new IReferenceHandler<PlateImageReference>()
                        {
                            public void handle(DssServiceRpcScreeningHolder dssService,
                                    List<PlateImageReference> references)
                            {
                                final InputStream stream;
                                if (hasDSSMethod(dssService, "loadImages", List.class,
                                        boolean.class))
                                {
                                    // Only available since v1.3
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references, convertToPNG);
                                } else
                                {
                                    checkDSSMinimalMinorVersion(dssService, "loadImages",
                                            List.class);
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references);
                                }
                                try
                                {
                                    final ConcatenatedFileOutputStreamWriter imagesWriter =
                                            new ConcatenatedFileOutputStreamWriter(stream);
                                    for (PlateImageReference imageRef : references)
                                    {
                                        OutputStream output =
                                                outputStreamProvider.getOutputStream(imageRef);
                                        imagesWriter.writeNextBlock(output);
                                    }
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                } finally
                                {
                                    try
                                    {
                                        stream.close();
                                    } catch (IOException ex)
                                    {
                                        throw new WrappedIOException(ex);
                                    }
                                }

                            }
                        });
        } catch (WrappedIOException ex)
        {
            throw ex.getIoException();
        }
    }

    public void loadImages(List<PlateImageReference> imageReferences, final boolean convertToPNG,
            final IPlateImageHandler plateImageHandler) throws IOException
    {
        try
        {
            plateImageReferencesMultiplexer.process(imageReferences,
                    new IReferenceHandler<PlateImageReference>()
                        {
                            public void handle(DssServiceRpcScreeningHolder dssService,
                                    List<PlateImageReference> references)
                            {
                                final InputStream stream;
                                if (hasDSSMethod(dssService, "loadImages", List.class,
                                        boolean.class))
                                {
                                    // Only available since v1.3
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references, convertToPNG);
                                } else
                                {
                                    checkDSSMinimalMinorVersion(dssService, "loadImages",
                                            List.class);
                                    stream =
                                            dssService.getService().loadImages(sessionToken,
                                                    references);
                                }
                                try
                                {
                                    final ConcatenatedFileOutputStreamWriter imagesWriter =
                                            new ConcatenatedFileOutputStreamWriter(stream);
                                    int index = 0;
                                    long size;
                                    do
                                    {
                                        final ByteArrayOutputStream outputStream =
                                                new ByteArrayOutputStream();
                                        size = imagesWriter.writeNextBlock(outputStream);
                                        if (size > 0)
                                        {
                                            plateImageHandler.handlePlateImage(
                                                    references.get(index),
                                                    outputStream.toByteArray());
                                        }
                                        index++;
                                    } while (size >= 0);
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                } finally
                                {
                                    try
                                    {
                                        stream.close();
                                    } catch (IOException ex)
                                    {
                                        throw new WrappedIOException(ex);
                                    }
                                }

                            }
                        });
        } catch (WrappedIOException ex)
        {
            throw ex.getIoException();
        }
    }

    public List<byte[]> loadImages(IDatasetIdentifier dataSetIdentifier,
            List<WellPosition> wellPositions, String channel, ImageSize thumbnailSizeOrNull)
            throws IOException
    {
        DssServiceRpcScreeningHolder dssServiceHolder =
                dssServiceCache.createDssService(dataSetIdentifier.getDatastoreServerUrl());
        InputStream stream =
                dssServiceHolder.getService().loadImages(sessionToken, dataSetIdentifier,
                        wellPositions, channel, thumbnailSizeOrNull);
        ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(stream);
        List<byte[]> result = new ArrayList<byte[]>();
        long size;
        do
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            size = imagesWriter.writeNextBlock(outputStream);
            if (size > 0)
            {
                result.add(outputStream.toByteArray());
            }
        } while (size >= 0);
        return result;
    }

    public void loadImages(IDatasetIdentifier dataSetIdentifier, List<WellPosition> wellPositions,
            String channel, ImageSize thumbnailSizeOrNull, IPlateImageHandler plateImageHandler)
            throws IOException
    {
        final DssServiceRpcScreeningHolder dssServiceHolder =
                dssServiceCache.createDssService(dataSetIdentifier.getDatastoreServerUrl());
        final IDssServiceRpcScreening service = dssServiceHolder.getService();
        checkDSSMinimalMinorVersion(dssServiceHolder, "listPlateImageReferences",
                IDatasetIdentifier.class, List.class, String.class);
        final List<PlateImageReference> plateImageReferences =
                service.listPlateImageReferences(sessionToken, dataSetIdentifier, wellPositions,
                        channel);
        checkDSSMinimalMinorVersion(dssServiceHolder, "loadImages", List.class, ImageSize.class);
        final InputStream stream =
                service.loadImages(sessionToken, plateImageReferences, thumbnailSizeOrNull);
        final ConcatenatedFileOutputStreamWriter imagesWriter =
                new ConcatenatedFileOutputStreamWriter(stream);
        int index = 0;
        long size;
        do
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            size = imagesWriter.writeNextBlock(outputStream);
            if (size > 0)
            {
                plateImageHandler.handlePlateImage(plateImageReferences.get(index),
                        outputStream.toByteArray());
            }
            index++;
        } while (size >= 0);
    }

    public byte[] loadImageWellCaching(final PlateImageReference imageReference,
            final ImageSize imageSizeOrNull) throws IOException
    {
        // PlateImageReference should really implement IImageDatasetIdentifier, however it doesn't,
        // so we need to convert to ImageDatasetReference here.
        final IImageDatasetIdentifier imageDatasetId =
                new ImageDatasetReference(imageReference.getDatasetCode(), null,
                        imageReference.getDatastoreServerUrl(), null, null, null, null, null, null);
        final ImageDatasetMetadata imageMetadata = listImageMetadata(imageDatasetId);
        final ImageSize size =
                (imageSizeOrNull == null) ? new ImageSize(imageMetadata.getWidth(),
                        imageMetadata.getHeight()) : imageSizeOrNull;
        final WellImages images = imageCache.getWellImages(imageReference, size, imageMetadata);
        if (images.isLoaderCall())
        {
            try
            {
                final List<PlateImageReference> imageReferences =
                        createPlateImageReferences(imageDatasetId, imageMetadata, null,
                                Collections.singletonList(imageReference.getWellPosition()));
                loadImages(imageReferences, imageSizeOrNull, new IPlateImageHandler()
                    {
                        public void handlePlateImage(PlateImageReference plateImageReference,
                                byte[] imageFileBytes)
                        {
                            images.putImage(plateImageReference, imageFileBytes);
                        }
                    });
            } catch (IOException ex)
            {
                images.cancel(ex);
                throw ex;
            } catch (RuntimeException ex)
            {
                images.cancel(ex);
                throw ex;
            }
        }
        final CachedImage imageOrNull = images.getImage(imageReference);
        if (imageOrNull == null)
        {
            throw new IOException(imageReference + " doesn't exist.");
        }
        return imageOrNull.getImageData();
    }

    public void loadImages(List<PlateImageReference> imageReferences, final ImageSize sizeOrNull,
            final IPlateImageHandler plateImageHandler) throws IOException
    {
        plateImageReferencesMultiplexer.process(imageReferences,
                new IReferenceHandler<PlateImageReference>()
                    {
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadImages", List.class,
                                    ImageSize.class);
                            final InputStream stream =
                                    dssService.getService().loadImages(sessionToken, references,
                                            sizeOrNull);
                            try
                            {
                                final ConcatenatedFileOutputStreamWriter imagesWriter =
                                        new ConcatenatedFileOutputStreamWriter(stream);
                                int index = 0;
                                long size;
                                do
                                {
                                    final ByteArrayOutputStream outputStream =
                                            new ByteArrayOutputStream();
                                    size = imagesWriter.writeNextBlock(outputStream);
                                    if (size > 0)
                                    {
                                        plateImageHandler.handlePlateImage(references.get(index),
                                                outputStream.toByteArray());
                                    }
                                    index++;
                                } while (size >= 0);
                            } catch (IOException ex)
                            {
                                throw new WrappedIOException(ex);
                            } finally
                            {
                                try
                                {
                                    stream.close();
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                }
                            }

                        }
                    });
    }

    public byte[] loadThumbnailImageWellCaching(final PlateImageReference imageReference)
            throws IOException
    {
        // PlateImageReference should really implement IImageDatasetIdentifier, however it doesn't,
        // so we need to convert to ImageDatasetReference here.
        final IImageDatasetIdentifier imageDatasetId =
                new ImageDatasetReference(imageReference.getDatasetCode(), null,
                        imageReference.getDatastoreServerUrl(), null, null, null, null, null, null);
        final ImageDatasetMetadata imageMetadata = listImageMetadata(imageDatasetId);
        final WellImages images =
                imageCache.getWellImages(
                        imageReference,
                        new ImageSize(imageMetadata.getThumbnailWidth(), imageMetadata
                                .getThumbnailHeight()), imageMetadata);
        if (images.isLoaderCall())
        {
            try
            {
                final List<PlateImageReference> imageReferences =
                        createPlateImageReferences(imageDatasetId, imageMetadata, null,
                                Collections.singletonList(imageReference.getWellPosition()));
                loadThumbnailImages(imageReferences, new IPlateImageHandler()
                    {
                        public void handlePlateImage(PlateImageReference plateImageReference,
                                byte[] imageFileBytes)
                        {
                            images.putImage(plateImageReference, imageFileBytes);
                        }
                    });
            } catch (IOException ex)
            {
                images.cancel(ex);
                throw ex;
            } catch (RuntimeException ex)
            {
                images.cancel(ex);
                throw ex;
            }
        }
        final CachedImage imageOrNull = images.getImage(imageReference);
        if (imageOrNull == null)
        {
            throw new IOException(imageReference + " doesn't exist.");
        }
        return imageOrNull.getImageData();
    }

    public void loadThumbnailImages(List<PlateImageReference> imageReferences,
            final IPlateImageHandler plateImageHandler) throws IOException
    {
        plateImageReferencesMultiplexer.process(imageReferences,
                new IReferenceHandler<PlateImageReference>()
                    {
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadThumbnailImages",
                                    List.class);
                            final InputStream stream =
                                    dssService.getService().loadThumbnailImages(sessionToken,
                                            references);
                            try
                            {
                                final ConcatenatedFileOutputStreamWriter imagesWriter =
                                        new ConcatenatedFileOutputStreamWriter(stream);
                                int index = 0;
                                long size;
                                do
                                {
                                    final ByteArrayOutputStream outputStream =
                                            new ByteArrayOutputStream();
                                    size = imagesWriter.writeNextBlock(outputStream);
                                    if (size > 0)
                                    {
                                        plateImageHandler.handlePlateImage(references.get(index),
                                                outputStream.toByteArray());
                                    }
                                    index++;
                                } while (size >= 0);
                            } catch (IOException ex)
                            {
                                throw new WrappedIOException(ex);
                            } finally
                            {
                                try
                                {
                                    stream.close();
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                }
                            }

                        }
                    });
    }

    public void loadThumbnailImages(List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        plateImageReferencesMultiplexer.process(imageReferences,
                new IReferenceHandler<PlateImageReference>()
                    {
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<PlateImageReference> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "loadThumbnailImages",
                                    List.class);
                            final InputStream stream =
                                    dssService.getService().loadThumbnailImages(sessionToken,
                                            references);
                            try
                            {
                                final ConcatenatedFileOutputStreamWriter imagesWriter =
                                        new ConcatenatedFileOutputStreamWriter(stream);
                                for (PlateImageReference imageRef : references)
                                {
                                    OutputStream output =
                                            outputStreamProvider.getOutputStream(imageRef);
                                    imagesWriter.writeNextBlock(output);
                                }
                            } catch (IOException ex)
                            {
                                throw new WrappedIOException(ex);
                            } finally
                            {
                                try
                                {
                                    stream.close();
                                } catch (IOException ex)
                                {
                                    throw new WrappedIOException(ex);
                                }
                            }

                        }
                    });
    }

    public void saveImageTransformerFactory(List<IDatasetIdentifier> dataSetIdentifiers,
            String channel, IImageTransformerFactory transformerFactoryOrNull)
    {
        Map<String, List<IDatasetIdentifier>> map = getReferencesPerDss(dataSetIdentifiers);
        Set<Entry<String, List<IDatasetIdentifier>>> entrySet = map.entrySet();
        for (Entry<String, List<IDatasetIdentifier>> entry : entrySet)
        {
            String serverUrl = entry.getKey();
            IDssServiceRpcScreening service =
                    dssServiceCache.createDssService(serverUrl).getService();
            service.saveImageTransformerFactory(sessionToken, entry.getValue(), channel,
                    transformerFactoryOrNull);
        }
    }

    public IImageTransformerFactory getImageTransformerFactoryOrNull(
            List<IDatasetIdentifier> dataSetIdentifiers, String channel)
    {
        Map<String, List<IDatasetIdentifier>> map = getReferencesPerDss(dataSetIdentifiers);
        Set<Entry<String, List<IDatasetIdentifier>>> entrySet = map.entrySet();
        if (entrySet.size() != 1)
        {
            throw new IllegalArgumentException("Only one data store expected instead of "
                    + map.keySet());
        }
        Entry<String, List<IDatasetIdentifier>> entry = entrySet.iterator().next();
        IDssServiceRpcScreening service =
                dssServiceCache.createDssService(entry.getKey()).getService();
        return service.getImageTransformerFactoryOrNull(sessionToken, dataSetIdentifiers, channel);
    }

    public ImageDatasetMetadata listImageMetadata(IImageDatasetIdentifier imageDataset)
    {
        final List<ImageDatasetMetadata> metadataList =
                listImageMetadata(Collections.singletonList(imageDataset));
        if (metadataList.isEmpty())
        {
            throw new IllegalArgumentException("Cannot find metadata for image data set '"
                    + imageDataset + "'.");
        }
        return metadataList.get(0);
    }

    public List<ImageDatasetMetadata> listImageMetadata(
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        final List<ImageDatasetMetadata> result = new ArrayList<ImageDatasetMetadata>();
        metaDataMultiplexer.process(imageDatasets, new IReferenceHandler<IImageDatasetIdentifier>()
            {
                public void handle(DssServiceRpcScreeningHolder dssService,
                        List<IImageDatasetIdentifier> references)
                {
                    checkDSSMinimalMinorVersion(dssService, "listImageMetadata", List.class);
                    final Iterator<IImageDatasetIdentifier> it = references.iterator();
                    while (it.hasNext())
                    {
                        final IImageDatasetIdentifier ref = it.next();
                        final ImageDatasetMetadata cached = imageMetadataCache.get(ref);
                        if (cached != null)
                        {
                            result.add(cached);
                            it.remove();
                        }
                    }
                    if (references.isEmpty())
                    {
                        return;
                    }
                    final List<ImageDatasetMetadata> metadata =
                            dssService.getService().listImageMetadata(sessionToken, references);
                    for (ImageDatasetMetadata md : metadata)
                    {
                        imageMetadataCache.put(md.getImageDataset(), md);
                    }
                    result.addAll(metadata);
                }
            });
        return result;
    }

    public List<PlateWellMaterialMapping> listPlateMaterialMapping(
            List<? extends PlateIdentifier> plates,
            MaterialTypeIdentifier materialTypeIdentifierOrNull)
    {
        return openbisScreeningServer.listPlateMaterialMapping(sessionToken, plates,
                materialTypeIdentifierOrNull);
    }

    public List<String> listAnalysisProcedures(ExperimentIdentifier experimentIdentifier)
    {
        SearchCriteria searchCriteria = new SearchCriteria();
        SearchCriteria experimentCriteria = new SearchCriteria();
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.CODE, experimentIdentifier.getExperimentCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.PROJECT, experimentIdentifier.getProjectCode()));
        experimentCriteria.addMatchClause(MatchClause.createAttributeMatch(
                MatchClauseAttribute.SPACE, experimentIdentifier.getSpaceCode()));
        searchCriteria.addSubCriteria(SearchSubCriteria.createExperimentCriteria(experimentCriteria));
        List<DataSet> dataSets = generalInformationService.searchForDataSets(sessionToken, searchCriteria);
        Set<String> procedures = new HashSet<String>();
        for (DataSet dataSet : dataSets)
        {
            HashMap<String, String> properties = dataSet.getProperties();
            String analysisProcedure = properties.get(ScreeningConstants.ANALYSIS_PROCEDURE);
            if (analysisProcedure != null)
            {
                procedures.add(analysisProcedure);
            }
        }
        ArrayList<String> result = new ArrayList<String>(procedures);
        Collections.sort(result);
        return result;
    }
    
    // --------- helpers -----------

    private static final class WrappedIOException extends RuntimeException
    {
        private static final long serialVersionUID = 1L;

        private final IOException ioException;

        WrappedIOException(IOException cause)
        {
            super(cause);
            ioException = cause;
        }

        public final IOException getIoException()
        {
            return ioException;
        }

    }

    private interface IReferenceHandler<R extends IDatasetIdentifier>
    {
        public void handle(DssServiceRpcScreeningHolder dssService, List<R> references);
    }

    private static final class DataStoreMultiplexer<R extends IDatasetIdentifier>
    {
        private final IDssServiceFactory dssServiceFactory;

        public DataStoreMultiplexer(IDssServiceFactory dssServiceFactory)
        {
            this.dssServiceFactory = dssServiceFactory;
        }

        public void process(List<? extends R> references, IReferenceHandler<R> handler)
        {
            Map<String, List<R>> referencesPerDss = getReferencesPerDss(cast(references));
            Set<Entry<String, List<R>>> entrySet = referencesPerDss.entrySet();
            for (Entry<String, List<R>> entry : entrySet)
            {
                final DssServiceRpcScreeningHolder dssServiceHolder =
                        dssServiceFactory.createDssService(entry.getKey());
                handler.handle(dssServiceHolder, entry.getValue());
            }
        }

        @SuppressWarnings("unchecked")
        private List<R> cast(List<? extends R> references)
        {
            return (List<R>) references;
        }

    }

    private static <R extends IDatasetIdentifier> Map<String, List<R>> getReferencesPerDss(
            List<R> references)
    {
        HashMap<String, List<R>> referencesPerDss = new HashMap<String, List<R>>();
        for (R reference : references)
        {
            String url = reference.getDatastoreServerUrl();
            List<R> list = referencesPerDss.get(url);
            if (list == null)
            {
                list = new ArrayList<R>();
                referencesPerDss.put(url, list);
            }
            list.add(reference);
        }
        return referencesPerDss;
    }

    private void checkDSSMinimalMinorVersion(final DssServiceRpcScreeningHolder serviceHolder,
            final String methodName, final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IDssServiceRpcScreening.class, methodName, parameterTypes);
        if (hasDSSMethod(serviceHolder, methodName, parameterTypes) == false)
        {
            final String paramString = Arrays.asList(parameterTypes).toString();
            throw new UnsupportedOperationException(String.format(
                    "Method '%s(%s)' requires minor version %d, "
                            + "but server '%s' has only minor version %d.", methodName,
                    paramString.substring(1, paramString.length() - 1), minimalMinorVersion,
                    serviceHolder.getServerUrl(), serviceHolder.getMinorVersion()));
        }
    }

    private void checkASMinimalMinorVersion(final String methodName,
            final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IScreeningApiServer.class, methodName, parameterTypes);
        if (minorVersionApplicationServer < minimalMinorVersion)
        {
            final String paramString = Arrays.asList(parameterTypes).toString();
            throw new UnsupportedOperationException(String.format(
                    "Method '%s(%s)' requires minor version %d, "
                            + "but server has only minor version %d.", methodName,
                    paramString.substring(1, paramString.length() - 1), minimalMinorVersion,
                    minorVersionApplicationServer));
        }
    }

    private boolean hasDSSMethod(final DssServiceRpcScreeningHolder serviceHolder,
            final String methodName, final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IDssServiceRpcScreening.class, methodName, parameterTypes);
        return serviceHolder.getMinorVersion() >= minimalMinorVersion;
    }

    private boolean hasASMethod(final String methodName, final Class<?>... parameterTypes)
    {
        final int minimalMinorVersion =
                getMinimalMinorVersion(IScreeningApiServer.class, methodName, parameterTypes);
        return minorVersionApplicationServer >= minimalMinorVersion;
    }

    private static int getMinimalMinorVersion(final Class<?> clazz, final String methodName,
            final Class<?>... parameterTypes)
    {
        assert clazz != null : "Unspecified class.";
        assert methodName != null : "Unspecified method name.";

        final Class<?>[] actualParameterTypes = new Class<?>[parameterTypes.length + 1];
        actualParameterTypes[0] = String.class; // The token field
        System.arraycopy(parameterTypes, 0, actualParameterTypes, 1, parameterTypes.length);
        final Method method;
        try
        {
            method = clazz.getMethod(methodName, actualParameterTypes);
        } catch (Exception ex)
        {
            throw new Error("Method not found.", ex);
        }
        final MinimalMinorVersion minimalMinorVersion =
                method.getAnnotation(MinimalMinorVersion.class);
        if (minimalMinorVersion == null)
        {
            return 0;
        } else
        {
            return minimalMinorVersion.value();
        }
    }

}
