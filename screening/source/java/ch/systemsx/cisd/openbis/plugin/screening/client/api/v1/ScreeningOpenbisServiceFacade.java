package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.common.api.client.ServiceFinder;
import ch.systemsx.cisd.common.io.ConcatenatedFileOutputStreamWriter;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.dss.screening.shared.api.v1.IDssServiceRpcScreening;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.IScreeningApiServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IFeatureVectorDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;

/**
 * A client side facade of openBIS and Datastore Server API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningOpenbisServiceFacade implements IScreeningOpenbisServiceFacade
{
    private static final String DSS_SCREENING_API = "/rmi-datastore-server-screening-api-v1/";

    private static final String OPENBIS_SCREENING_API = "/rmi-screening-api-v1";

    private static final int SERVER_TIMEOUT_MIN = 5;

    private static final IDssServiceFactory DSS_SERVICE_FACTORY = new IDssServiceFactory()
        {
            public IDssServiceRpcScreening createDssService(String serverUrl)
            {
                return HttpInvokerUtils.createStreamSupportingServiceStub(
                        IDssServiceRpcScreening.class, serverUrl + DSS_SCREENING_API,
                        SERVER_TIMEOUT_MIN);
            }
        };

    private final IScreeningApiServer openbisScreeningServer;

    private final DataStoreMultiplexer<PlateImageReference> plateImageReferencesMultiplexer;

    private final DataStoreMultiplexer<IFeatureVectorDatasetIdentifier> featureVectorDataSetIdentifierMultiplexer;

    private final DataStoreMultiplexer<FeatureVectorDatasetReference> featureVectorDataSetReferenceMultiplexer;

    private final DataStoreMultiplexer<IImageDatasetIdentifier> metaDataMultiplexer;

    private final String sessionToken;

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL.
     * Authenticates the user.
     * 
     * @return null if the user could not be authenticated.
     */
    public static IScreeningOpenbisServiceFacade tryCreate(String userId, String userPassword,
            String serverUrl)
    {
        IScreeningApiServer openbisServer = createScreeningOpenbisServer(serverUrl);
        String sessionToken = openbisServer.tryLoginScreening(userId, userPassword);
        if (sessionToken == null)
        {
            return null;
        }
        return new ScreeningOpenbisServiceFacade(sessionToken, openbisServer, DSS_SERVICE_FACTORY);
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
        IScreeningApiServer openbisServer = createScreeningOpenbisServer(serverUrl);
        return new ScreeningOpenbisServiceFacade(sessionToken, openbisServer, DSS_SERVICE_FACTORY);
    }

    private static IScreeningApiServer createScreeningOpenbisServer(String serverUrl)
    {
        ServiceFinder serviceFinder = new ServiceFinder("openbis", OPENBIS_SCREENING_API);
        return serviceFinder.createService(IScreeningApiServer.class, serverUrl);
    }

    ScreeningOpenbisServiceFacade(String sessionToken, IScreeningApiServer screeningServer,
            final IDssServiceFactory dssServiceFactory)
    {
        this.openbisScreeningServer = screeningServer;
        this.sessionToken = sessionToken;
        IDssServiceFactory dssServiceCache = new IDssServiceFactory()
            {
                private final Map<String/* url */, IDssServiceRpcScreening> cache =
                        new HashMap<String, IDssServiceRpcScreening>();

                public IDssServiceRpcScreening createDssService(String serverUrl)
                {
                    IDssServiceRpcScreening dssService = cache.get(serverUrl);
                    if (dssService == null)
                    {
                        dssService = dssServiceFactory.createDssService(serverUrl);
                        cache.put(serverUrl, dssService);
                    }
                    return dssService;
                }
            };
        plateImageReferencesMultiplexer =
                new DataStoreMultiplexer<PlateImageReference>(dssServiceCache);
        metaDataMultiplexer = new DataStoreMultiplexer<IImageDatasetIdentifier>(dssServiceCache);
        featureVectorDataSetIdentifierMultiplexer =
                new DataStoreMultiplexer<IFeatureVectorDatasetIdentifier>(dssServiceCache);
        featureVectorDataSetReferenceMultiplexer =
                new DataStoreMultiplexer<FeatureVectorDatasetReference>(dssServiceCache);
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
        openbisScreeningServer.logoutScreening(sessionToken);
    }

    /**
     * Return the list of all visible plates assigned to any experiment, along with their
     * hierarchical context (space, project, experiment).
     */
    public List<Plate> listPlates()
    {
        return openbisScreeningServer.listPlates(sessionToken);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing feature
     * vectors.
     */
    public List<FeatureVectorDatasetReference> listFeatureVectorDatasets(
            List<? extends PlateIdentifier> plates)
    {
        return openbisScreeningServer.listFeatureVectorDatasets(sessionToken, plates);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing images.
     */
    public List<ImageDatasetReference> listImageDatasets(List<? extends PlateIdentifier> plates)
    {
        return openbisScreeningServer.listImageDatasets(sessionToken, plates);
    }

    /**
     * Converts a given list of dataset codes to dataset identifiers which can be used in other API
     * calls.
     */
    public List<IDatasetIdentifier> getDatasetIdentifiers(List<String> datasetCodes)
    {
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
                        public void handle(IDssServiceRpcScreening dssService,
                                List<IFeatureVectorDatasetIdentifier> references)
                        {
                            result.addAll(dssService.listAvailableFeatureNames(sessionToken,
                                    references));
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
                        public void handle(IDssServiceRpcScreening dssService,
                                List<FeatureVectorDatasetReference> references)
                        {

                            result.addAll(dssService.loadFeatures(sessionToken, references,
                                    featureNames));
                        }
                    });
        return result;
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
                            public void handle(IDssServiceRpcScreening dssService,
                                    List<PlateImageReference> references)
                            {
                                InputStream stream =
                                        dssService.loadImages(sessionToken, references);
                                try
                                {
                                    ConcatenatedFileOutputStreamWriter imagesWriter =
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
                public void handle(IDssServiceRpcScreening dssService,
                        List<IImageDatasetIdentifier> references)
                {
                    result.addAll(dssService.listImageMetadata(sessionToken, references));
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

    private static interface IReferenceHandler<R extends IDatasetIdentifier>
    {
        public void handle(IDssServiceRpcScreening dssService, List<R> references);
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
                IDssServiceRpcScreening dssService =
                        dssServiceFactory.createDssService(entry.getKey());
                handler.handle(dssService, entry.getValue());
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

}
