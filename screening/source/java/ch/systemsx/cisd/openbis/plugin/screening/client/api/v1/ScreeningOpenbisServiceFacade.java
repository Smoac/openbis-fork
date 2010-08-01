package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.common.api.MinimalMinorVersion;
import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A client side facade of openBIS and Datastore Server API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningOpenbisServiceFacade implements IScreeningOpenbisServiceFacade
{
    static final int MAJOR_VERSION_AS = 1;

    static final int MAJOR_VERSION_DSS = 1;

    static final String DSS_SCREENING_API =
            "/rmi-datastore-server-screening-api-v" + MAJOR_VERSION_DSS;

    private static final String OPENBIS_SCREENING_API = "/rmi-screening-api-v" + MAJOR_VERSION_AS;

    static final int SERVER_TIMEOUT_MIN = 5;

    private static final IDssServiceFactory DSS_SERVICE_FACTORY = new IDssServiceFactory()
        {
            public DssServiceRpcScreeningHolder createDssService(String serverUrl)
            {
                return new DssServiceRpcScreeningHolder(serverUrl);
            }
        };

    private final IScreeningApiServer openbisScreeningServer;

    private final DataStoreMultiplexer<PlateImageReference> plateImageReferencesMultiplexer;

    private final DataStoreMultiplexer<IFeatureVectorDatasetIdentifier> featureVectorDataSetIdentifierMultiplexer;

    private final DataStoreMultiplexer<FeatureVectorDatasetReference> featureVectorDataSetReferenceMultiplexer;

    private final DataStoreMultiplexer<FeatureVectorDatasetWellReference> featureVectorDataSetWellReferenceMultiplexer;

    private final DataStoreMultiplexer<IImageDatasetIdentifier> metaDataMultiplexer;

    private final String sessionToken;

    private final int minorVersionApplicationServer;

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
        final int minorVersion = openbisServer.getMinorVersion();
        final String sessionToken = openbisServer.tryLoginScreening(userId, userPassword);
        if (sessionToken == null)
        {
            return null;
        }
        return new ScreeningOpenbisServiceFacade(sessionToken, openbisServer, minorVersion,
                DSS_SERVICE_FACTORY);
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
        final IScreeningApiServer openbisServer = createScreeningOpenbisServer(serverUrl);
        final int minorVersion = openbisServer.getMinorVersion();
        return new ScreeningOpenbisServiceFacade(sessionToken, openbisServer, minorVersion,
                DSS_SERVICE_FACTORY);
    }

    private static IScreeningApiServer createScreeningOpenbisServer(String serverUrl)
    {
        ServiceFinder serviceFinder = new ServiceFinder("openbis", OPENBIS_SCREENING_API);
        return serviceFinder.createService(IScreeningApiServer.class, serverUrl);
    }

    ScreeningOpenbisServiceFacade(String sessionToken, IScreeningApiServer screeningServer,
            int minorVersion, final IDssServiceFactory dssServiceFactory)
    {
        this.openbisScreeningServer = screeningServer;
        this.sessionToken = sessionToken;
        this.minorVersionApplicationServer = minorVersion;
        IDssServiceFactory dssServiceCache = new IDssServiceFactory()
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

    /**
     * Return the list of all visible plates assigned to any experiment, along with their
     * hierarchical context (space, project, experiment).
     */
    public List<Plate> listPlates()
    {
        checkASMinimalMinorVersion("listPlates");
        return openbisScreeningServer.listPlates(sessionToken);
    }

    public List<ExperimentIdentifier> listExperiments()
    {
        checkASMinimalMinorVersion("listExperiments");
        return openbisScreeningServer.listExperiments(sessionToken);
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
     * For a given set of plates provides the list of all connected data sets containing images.
     */
    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        checkASMinimalMinorVersion("listImageDatasets", List.class);
        return openbisScreeningServer.listImageDatasets(sessionToken, plates);
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
     * Converts a given list of dataset codes to dataset identifiers which can be used in other API
     * calls.
     */
    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
        checkASMinimalMinorVersion("getDatasetIdentifiers", List.class);
        return openbisScreeningServer.getDatasetIdentifiers(sessionToken, datasetCodes);
    }

    /**
     * For a given set of feature vector data sets provides the list of all available features. This
     * is just the name of the feature. If for different data sets different sets of features are
     * available, provides the union of the feature names of all data sets.
     */
    public List<String> listAvailableFeatureNames(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets)
    {
        final Set<String> result = new HashSet<String>();
        featureVectorDataSetIdentifierMultiplexer.process(featureDatasets,
                new IReferenceHandler<IFeatureVectorDatasetIdentifier>()
                    {
                        public void handle(DssServiceRpcScreeningHolder dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            checkDSSMinimalMinorVersion(dssService, "listAvailableFeatureNames",
                                    List.class);
                            result.addAll(dssService.getService().listAvailableFeatureNames(
                                    sessionToken, references));
                        }
                    });
        return new ArrayList<String>(result);
    }

    /**
     * For a given set of data sets and a set of features (given by their name), provide all the
     * feature vectors.
     * 
     * @return The list of {@link FeatureVectorDataset}s, each element corresponds to one of the
     *         <var>featureDatasets</var>.
     */
    public List<FeatureVectorDataset> loadFeatures(
            List<FeatureVectorDatasetReference> featureDatasets, final List<String> featureNames)
    {
        if (featureNames.size() == 0)
        {
            throw new IllegalArgumentException("no feature names has been specified");
        }
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
                new ArrayList<FeatureVectorDatasetWellReference>(plateWellReferenceWithDataSets
                        .size());
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
        return new FeatureVectorDatasetWellReference(fvdr.getDatasetCode(), fvdr
                .getDatastoreServerUrl(), fvdr.getPlate(), fvdr.getExperimentIdentifier(), fvdr
                .getPlateGeometry(), fvdr.getRegistrationDate(), fvdr.getParentImageDataset(),
                wellPosition);
    }

    public List<FeatureVectorWithDescription> loadFeaturesForDatasetWellReferences(
            final List<FeatureVectorDatasetWellReference> datasetWellReferences,
            final List<String> featureNamesOrNull)
    {
        final List<String> featureNames =
                (isEmpty(featureNamesOrNull)) ? listAvailableFeatureNames(datasetWellReferences)
                        : featureNamesOrNull;

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

    private boolean isEmpty(final List<String> featureNamesOrNull)
    {
        return featureNamesOrNull == null || featureNamesOrNull.isEmpty();
    }

    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            ExperimentIdentifier experimentIdentifer, MaterialIdentifier materialIdentifier,
            List<String> featureNamesOrNull)
    {
        final List<PlateWellReferenceWithDatasets> plateWellRefs =
                listPlateWells(experimentIdentifer, materialIdentifier, true);
        final List<String> featureNames =
                (isEmpty(featureNamesOrNull)) ? listAvailableFeatureNamesForPlateWells(plateWellRefs)
                        : featureNamesOrNull;
        final List<FeatureVectorDatasetWellReference> datasetWellReferences =
                convertToFeatureVectorDatasetWellIdentifier(plateWellRefs);
        final List<FeatureVectorWithDescription> featureVectors =
                loadFeaturesForDatasetWellReferences(datasetWellReferences, featureNames);
        return featureVectors;
    }

    public List<FeatureVectorWithDescription> loadFeaturesForPlateWells(
            MaterialIdentifier materialIdentifier, List<String> featureNamesOrNull)
    {
        final List<PlateWellReferenceWithDatasets> plateWellRefs =
                listPlateWells(materialIdentifier, true);
        final List<String> featureNames =
                (isEmpty(featureNamesOrNull)) ? listAvailableFeatureNamesForPlateWells(plateWellRefs)
                        : featureNamesOrNull;
        final List<FeatureVectorDatasetWellReference> datasetWellReferences =
                convertToFeatureVectorDatasetWellIdentifier(plateWellRefs);
        final List<FeatureVectorWithDescription> featureVectors =
                loadFeaturesForDatasetWellReferences(datasetWellReferences, featureNames);
        return featureVectors;
    }

    private List<String> listAvailableFeatureNamesForPlateWells(
            final List<PlateWellReferenceWithDatasets> plateWellRefs)
    {
        final List<String> featureNames;
        final List<FeatureVectorDatasetReference> featureVectorDatasetReferences =
                new ArrayList<FeatureVectorDatasetReference>(plateWellRefs.size());
        for (PlateWellReferenceWithDatasets plateWellRef : plateWellRefs)
        {
            featureVectorDatasetReferences.addAll(plateWellRef.getFeatureVectorDatasetReferences());
        }
        final List<String> availableFeatureNames =
                listAvailableFeatureNames(featureVectorDatasetReferences);
        featureNames = availableFeatureNames;
        return featureNames;
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

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the provided output streams. Output streams will not be closed
     * automatically.<br>
     * <p>
     * If there is an image reference specified which is not referring to the existing image on the
     * server, nothing will be written to the output stream returned by the output streams provider.
     * No exception will be thrown.
     * </p>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams
     *             fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            final IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        try
        {
            plateImageReferencesMultiplexer.process(imageReferences,
                    new IReferenceHandler<PlateImageReference>()
                        {
                            public void handle(DssServiceRpcScreeningHolder dssService,
                                    List<PlateImageReference> references)
                            {
                                checkDSSMinimalMinorVersion(dssService, "loadImages", List.class);
                                final InputStream stream =
                                        dssService.getService()
                                                .loadImages(sessionToken, references);
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

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
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
                    result.addAll(dssService.getService().listImageMetadata(sessionToken,
                            references));
                }
            });
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
        if (serviceHolder.getMinorVersion() < minimalMinorVersion)
        {
            final String paramString = Arrays.asList(parameterTypes).toString();
            throw new UnsupportedOperationException(String.format(
                    "Method '%s(%s)' requires minor version %d, "
                            + "but server '%s' has only minor version %d.", methodName, paramString
                            .substring(1, paramString.length() - 1), minimalMinorVersion,
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
                            + "but server has only minor version %d.", methodName, paramString
                            .substring(1, paramString.length() - 1), minimalMinorVersion,
                    minorVersionApplicationServer));
        }
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
