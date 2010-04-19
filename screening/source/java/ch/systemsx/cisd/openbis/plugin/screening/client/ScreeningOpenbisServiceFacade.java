package ch.systemsx.cisd.openbis.plugin.screening.client;

import java.util.List;

import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.IScreeningOpenbisServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.Dataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IPlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateSingleImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.WellFeaturesReference;

/**
 * A facade of openBIS API for Genedata Screener integration.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningOpenbisServiceFacade
{
    private static final int SERVER_TIMEOUT_MIN = 5;

    private final IScreeningOpenbisServer screeningServer;

    private final String sessionToken;

    /**
     * Creates a service facade which communicates with the openBIS server at the specified URL.
     * Authenticates the user.
     */
    public static ScreeningOpenbisServiceFacade create(String userId, String userPassword,
            String serverUrl)
    {
        IScreeningOpenbisServer server = createScreeningServer(serverUrl);
        String sessionToken = server.tryLoginScreening(userId, userPassword);
        if (sessionToken == null)
        {
            return null;
        }
        return new ScreeningOpenbisServiceFacade(server, sessionToken);
    }

    private static IScreeningOpenbisServer createScreeningServer(String serverUrl)
    {
        return HttpInvokerUtils.createServiceStub(IScreeningOpenbisServer.class, serverUrl
                + "/rmi-screening", SERVER_TIMEOUT_MIN);
    }

    private ScreeningOpenbisServiceFacade(IScreeningOpenbisServer screeningServer,
            String sessionToken)
    {
        this.screeningServer = screeningServer;
        this.sessionToken = sessionToken;
    }

    /** Closes connection with the server. After calling this method this facade cannot be used. */
    public void logout()
    {
        screeningServer.logoutScreening(sessionToken);
    }

    /**
     * Return the list of all visible plates, along with their hierarchical context (space, project,
     * experiment).
     */
    public List<Plate> listPlates()
    {
        return screeningServer.listPlates(sessionToken);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing feature
     * vectors.
     */
    public List<Dataset> listFeatureVectorDatasets(List<IPlateIdentifier> plates)
    {
        return screeningServer.listFeatureVectorDatasets(sessionToken, plates);
    }

    /**
     * For a given set of plates provides the list of all connected data sets containing images.
     */
    public List<Dataset> listImageDatasets(List<IPlateIdentifier> plates)
    {
        return screeningServer.listImageDatasets(sessionToken, plates);
    }

    /**
     * For a given set of feature vector data sets provides the list of all available features. This
     * is just the name of the feature. If for different data sets different sets of features are
     * available, provides the union of the feature names of all data sets.
     */
    public List<String> listAvailableFeatureNames(List<IDatasetIdentifier> featureDatasets)
    {
        // TODO 2010-04-16, Tomasz Pylak:
        return null;
    }

    /**
     * For a given set of data sets and a set of features (given by their name), provide all the
     * feature vectors.
     */
    // Q: what result structure do you prefer? The one below is the easiest to use in Java,
    // but it could be also a simple String[][] table like:
    // plate-barcode well-row well-column feature1 feature2 ....
    public List<FeatureVectorDataset> loadFeatures(List<IDatasetIdentifier> featureDatasets,
            List<String> featureNames)
    {
        // TODO 2010-04-16, Tomasz Pylak:
        return null;
    }

    /**
     * For a given set of wells (given by feature vector data set code and well position), provide
     * all images for all channels and tiles.
     */
    public List<PlateSingleImage> loadWellImages(List<WellFeaturesReference> wells)
    {
        // TODO 2010-04-16, Tomasz Pylak:
        return null;
    }

    /**
     * For a given set of image data sets, provide all image channels that have been acquired and
     * the available (natural) image size(s).
     */
    public List<ImageDatasetMetadata> listImageMetadata(List<IDatasetIdentifier> imageDatasets)
    {
        // TODO 2010-04-16, Tomasz Pylak:
        return null;
    }

}
