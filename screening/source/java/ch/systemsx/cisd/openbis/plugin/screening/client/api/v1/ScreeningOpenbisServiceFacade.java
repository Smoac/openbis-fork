package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class ScreeningOpenbisServiceFacade
{
    private static final String DSS_SCREENING_API = "/rmi-datastore-server-screening-api-v1/";

    private static final String OPENBIS_SCREENING_API = "/rmi-screening-api-v1";

    private static final int SERVER_TIMEOUT_MIN = 5;

    private final IScreeningApiServer openbisScreeningServer;

    private final Map<String/* url */, IDssServiceRpcScreening> dssScreeningServerCache;

    private final String sessionToken;

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL.
     * Authenticates the user.
     * 
     * @return null if the user could not be authenticated.
     */
    public static ScreeningOpenbisServiceFacade tryCreate(String userId, String userPassword,
            String serverUrl)
    {
        IScreeningApiServer openbisServer = createScreeningOpenbisServer(serverUrl);
        String sessionToken = openbisServer.tryLoginScreening(userId, userPassword);
        if (sessionToken == null)
        {
            return null;
        }
        return new ScreeningOpenbisServiceFacade(openbisServer, sessionToken);
    }

    private static IScreeningApiServer createScreeningOpenbisServer(String serverUrl)
    {
        return HttpInvokerUtils.createServiceStub(IScreeningApiServer.class, serverUrl
                + OPENBIS_SCREENING_API, SERVER_TIMEOUT_MIN);
    }

    private static IDssServiceRpcScreening createScreeningDssServer(String serverUrl)
    {
        return HttpInvokerUtils.createStreamSupportingServiceStub(IDssServiceRpcScreening.class,
                serverUrl + DSS_SCREENING_API, SERVER_TIMEOUT_MIN);
    }

    private ScreeningOpenbisServiceFacade(IScreeningApiServer screeningServer, String sessionToken)
    {
        this.openbisScreeningServer = screeningServer;
        this.dssScreeningServerCache = new HashMap<String, IDssServiceRpcScreening>();
        this.sessionToken = sessionToken;
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
        if (featureDatasets.size() == 0)
        {
            return new ArrayList<String>();
        }
        IDssServiceRpcScreening dssServer = getScreeningDssServer(featureDatasets);
        return dssServer.listAvailableFeatureNames(sessionToken, featureDatasets);
    }

    /**
     * For a given set of data sets and a set of features (given by their name), provide all the
     * feature vectors.
     */
    public List<FeatureVectorDataset> loadFeatures(
            List<? extends IFeatureVectorDatasetIdentifier> featureDatasets,
            List<String> featureNames)
    {
        if (featureDatasets.size() == 0)
        {
            return new ArrayList<FeatureVectorDataset>();
        }
        if (featureNames.size() == 0)
        {
            throw new IllegalArgumentException("no feature names has been specified");
        }
        IDssServiceRpcScreening dssServer = getScreeningDssServer(featureDatasets);
        return dssServer.loadFeatures(sessionToken, featureDatasets, featureNames);
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
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the output streams
     *             fails
     */
    public void loadImages(List<PlateImageReference> imageReferences,
            IImageOutputStreamProvider outputStreamProvider) throws IOException
    {
        if (imageReferences.size() == 0)
        {
            return;
        }
        String datastoreServerUrl = extractDatastoreServerUrl(imageReferences);
        IDssServiceRpcScreening dssServer = getScreeningDssServer(datastoreServerUrl);

        InputStream stream = dssServer.loadImages(sessionToken, imageReferences);
        try
        {
            ConcatenatedFileOutputStreamWriter imagesWriter = new ConcatenatedFileOutputStreamWriter(stream);
            for (PlateImageReference imageRef : imageReferences)
            {
                OutputStream output = outputStreamProvider.getOutputStream(imageRef);
                imagesWriter.writeNextBlock(output);
            }
        } finally
        {
            stream.close();
        }
    }

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    public List<ImageDatasetMetadata> listImageMetadata(
            List<? extends IImageDatasetIdentifier> imageDatasets)
    {
        if (imageDatasets.size() == 0)
        {
            return new ArrayList<ImageDatasetMetadata>();
        }
        IDssServiceRpcScreening dssServer = getScreeningDssServer(imageDatasets);
        return dssServer.listImageMetadata(sessionToken, imageDatasets);
    }

    // --------- helpers -----------

    private static String extractDatastoreServerUrl(List<? extends IDatasetIdentifier> datasets)
    {
        assert datasets.size() > 0 : "no datasets specified";
        String datastoreServerUrl = null;
        for (IDatasetIdentifier dataset : datasets)
        {
            String url = dataset.getDatastoreServerUrl();
            if (datastoreServerUrl == null)
            {
                datastoreServerUrl = url;
            } else
            {
                if (datastoreServerUrl.equals(url) == false)
                {
                    throw new IllegalArgumentException(
                            "Only datasets from one datastore server can be specified in one call. Datasets from two different servers have been found.");
                }
            }
        }
        return datastoreServerUrl;
    }

    private IDssServiceRpcScreening getScreeningDssServer(String serverUrl)
    {
        IDssServiceRpcScreening dssService = dssScreeningServerCache.get(serverUrl);
        if (dssService == null)
        {
            dssService = createScreeningDssServer(serverUrl);
            dssScreeningServerCache.put(serverUrl, dssService);
        }
        return dssService;
    }

    private IDssServiceRpcScreening getScreeningDssServer(
            List<? extends IDatasetIdentifier> datasets)
    {
        String datastoreServerUrl = extractDatastoreServerUrl(datasets);
        return getScreeningDssServer(datastoreServerUrl);
    }
}
