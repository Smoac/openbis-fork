/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.dss.client.api.v1.IDataSetDss;
import ch.systemsx.cisd.openbis.dss.generic.shared.api.v1.NewDataSetMetadataDTO;
import ch.systemsx.cisd.openbis.generic.client.cli.Login;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * Simple Matlab interface for openBIS for Screening. It is meant to be used in one Matlab session
 * at a time, i.e. it is <i>not</i> multi-threading safe.
 * <p>
 * While written in Java, the API is idiomatic for Matlab, i.e. values are returned as
 * multi-dimensional arrays. For the <code>get...</code> and <code>load...</code> methods the first
 * index will contain the actual data, while the second index will contain per-row annotations. For
 * <code>getFeatureMatrix</code>, the third index contains per-column annotations. This allows
 * simple access with Matlab's slicing operator, see doc of e.g. {@link #getFeatureMatrix(String)}.
 * <p>
 * A typical Matlab session looks like:
 * 
 * <pre>
 * % Add the API jar file to the classpath
 * javaaddpath('/home/brinn/matlab/openbis_screening_api-batteries_included.jar')
 * % Login to server
 * OpenBISScreeningML.login('user', 'secret', 'https://www.infectome.org')
 * 
 * % ...perform calls on the server...
 * 
 * % Logout to close the session on the server
 * OpenBISScreeningML.logout()
 * </pre>
 * 
 * <i>Note: using this login your password will end up in the Matlab command history. An alternative
 * that avoids this is to call the {@link ch.systemsx.cisd.openbis.generic.client.cli.Login} class.
 * Logging in on the console will grant this class access to the openBIS server.</i>
 * <p>
 * To learn the API one needs to understand three basic notions: code, augmented code and perm id.
 * Space, project, experiment, plate and well have their own <b>code</b>, which is unique only in
 * the context of the parent.<br>
 * That's why one needs <b>augmented code</b> to point e.g. to one experiment, because two different
 * projects can have experiments with the same code.<br>
 * Such an augmented code for experiment has a form of "/space-code/project-code/experiment-code".<br>
 * For plate it has a form of "/space-code/plate-code" (note that plate code is unique on the space
 * level). <br>
 * The drawback of an augmented code is that it's not persistent. If someone e.g. moves the
 * experiment from one space to the other augmented code of the experiment becomes invalid. That is
 * why experiments, plates and datasets have <b>perm id</b> (permament identifier) which never
 * change and allow to refer to them with one "magic" identifier, e.g. 20110516124520378-737166.
 * </p>
 * 
 * @author Bernd Rinn
 */
public class OpenBISScreeningML
{
    private static interface ITileNumberIterable extends Iterable<Integer>
    {
        public void setMaximumNumberOfTiles(int numberOfTiles);

        public int getMaximumNumberOfTiles();
    }

    private static final class ImageReferenceAndFile
    {
        private final PlateImageReference imageReference;

        private final File imageFile;

        private BufferedOutputStream outputStream;

        ImageReferenceAndFile(PlateImageReference imageReference, File imageFile)
        {
            this.imageReference = imageReference;
            this.imageFile = imageFile;
        }

        public PlateImageReference getImageReference()
        {
            return imageReference;
        }

        public File getImageFile()
        {
            return imageFile;
        }

        public OutputStream open() throws IOException
        {
            if (outputStream == null)
            {
                outputStream = new BufferedOutputStream(new FileOutputStream(imageFile));
            }
            return outputStream;
        }

        public void close() throws IOException
        {
            if (outputStream != null)
            {
                outputStream.close();
            }
            outputStream = null;
        }
    }

    static final String DATASETS_FOLDER = "openbis_datasets";

    private static File temporarySessionDir;

    private static Map<PlateImageReference, File> loadedImages;

    static IScreeningOpenbisServiceFacadeFactory facadeFactory =
            ScreeningOpenbisServiceFacadeFactory.INSTANCE;

    private static IScreeningOpenbisServiceFacade openbis = null;

    private static List<ExperimentIdentifier> experiments = null;

    private static List<Plate> plates = null;

    private static Map<String, List<Plate>> experimentToPlateMap =
            new HashMap<String, List<Plate>>();

    private static Map<String, ExperimentIdentifier> experimentCodeToExperimentMap =
            new HashMap<String, ExperimentIdentifier>();

    private static Map<String, Plate> plateCodeToPlateMap = new HashMap<String, Plate>();

    private OpenBISScreeningML()
    {
        // Not to be constructed.
    }

    //
    // Versioning
    //

    /**
     * The version of the API.
     */
    public static final String VERSION = "1";

    /**
     * The required version ("major.minor") of the screening API on the openBIS application server.
     */
    public static final String REQUIRES_OPENBIS_AS_API = "1.7";

    /**
     * The required version ("major.minor") of the screening API on the openBIS datastore server.
     */
    public static final String REQUIRES_OPENBIS_DSS_API = "1.1";

    private static File dataSetsDir;

    /**
     * Root temporary directory for data sets and images. By default <code>java.io.tmpdir</code> is
     * used.
     */
    static File tempDir = new File(System.getProperty("java.io.tmpdir"));

    static final String TEMP_DIR_PREFIX = "openbis_";

    static final String TEMP_DIR_POSTFIX = "_temp_dir";

    //
    // Authentication methods
    //

    /**
     * Login to the openBIS server given as <var>url</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * OpenBISScreeningML.login('user', 'secret', 'https://www.infectome.org')
     * </pre>
     * 
     * @param user The user id on the server
     * @param password The password on the server
     * @param url The URL, e.g. <code>https://www.infectome.org</var>
     */
    public static void login(String user, String password, String url)
    {
        IScreeningOpenbisServiceFacade facade = facadeFactory.tryToCreate(user, password, url);
        if (facade == null)
        {
            throw new RuntimeException("Login failed.");
        }
        init(facade);
    }

    private static void init(IScreeningOpenbisServiceFacade openBisFacade)
    {
        openbis = openBisFacade;
        dataSetsDir = new File(tempDir, DATASETS_FOLDER);
        if (dataSetsDir.isDirectory() == false && dataSetsDir.mkdirs() == false)
        {
            throw new RuntimeException("Couldn't create a data set directory.");
        }
        temporarySessionDir =
                new File(tempDir, TEMP_DIR_PREFIX + System.currentTimeMillis() / 1000
                        + TEMP_DIR_POSTFIX);
        if (temporarySessionDir.mkdirs() == false)
        {
            throw new RuntimeException("Couldn't create a temporary directory.");
        }
        loadedImages = new HashMap<PlateImageReference, File>();
        experiments = openbis.listExperiments();
        experimentCodeToExperimentMap.clear();
        for (ExperimentIdentifier e : experiments)
        {
            experimentCodeToExperimentMap.put(e.getAugmentedCode(), e);
        }
        plates = openbis.listPlates();
        plateCodeToPlateMap.clear();
        experimentToPlateMap.clear();
        for (Plate p : plates)
        {
            final String plateCode = p.getAugmentedCode();
            plateCodeToPlateMap.put(plateCode, p);
            final String experimentCode = p.getExperimentIdentifier().getAugmentedCode();
            List<Plate> experimentPlates = experimentToPlateMap.get(experimentCode);
            if (experimentPlates == null)
            {
                experimentPlates = new ArrayList<Plate>();
                experimentToPlateMap.put(experimentCode, experimentPlates);
            }
            experimentPlates.add(p);
        }
    }

    /**
     * Logs out and closes the session on the server.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * OpenBISScreeningML.logout()
     * </pre>
     */
    public static void logout()
    {
        if (openbis == null)
        {
            return;
        }
        openbis.logout();
        if (Login.OPENBIS_TOKEN_FILE.exists())
        {
            Login.OPENBIS_TOKEN_FILE.delete();
        }
        delete(temporarySessionDir);
        openbis = null;
    }

    private static void delete(File file)
    {
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File child : files)
            {
                delete(child);
            }
        }
        file.delete();
    }

    //
    // Information methods
    //

    /**
     * Lists all experiment.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the experiments
     * exps = OpenBISScreeningML.listExperiments();
     * % How many experiments do we have?
     * length(exps)
     * % Get all information about experiment 3
     * exp3 = exps(3,:)
     * % Get the perm ids for all experiments
     * permids = exps(:,2)
     * </pre>
     * 
     * @return Each row contains information about one experiment:
     *         <p>
     *         <code>{ experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listExperiments()
    {
        checkLoggedIn();
        final Object[][] result = new Object[experiments.size()][5];
        for (int i = 0; i < experiments.size(); ++i)
        {
            final Object[] annotations =
                    new Object[]
                        { experiments.get(i).getAugmentedCode(), experiments.get(i).getPermId(),
                                experiments.get(i).getSpaceCode(),
                                experiments.get(i).getProjectCode(),
                                experiments.get(i).getExperimentCode() };
            System.arraycopy(annotations, 0, result[i], 0, annotations.length);
        }
        return result;
    }

    /**
     * Lists all plates.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the plates
     * plates = OpenBISScreeningML.listPlates();
     * % How many plates do we have?
     * length(plates)
     * % Get all information about plate 2
     * plate2 = plates(2,:)
     * % Get the simple plate codes for all plates
     * codes = plates(:,4)
     * </pre>
     * 
     * @return Each row contains information about one plate:
     *         <p>
     *         <code>{ plate augmented code, plate perm id, plate space code, plate code, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listPlates()
    {
        checkLoggedIn();
        return listPlates(plates);
    }

    /**
     * Lists the plates of <var>experiment</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the plates of experiment MYEXP in project PROJ of space SPACE
     * plates = OpenBISScreeningML.listPlates('/SPACE/PROJ/MYEXP');
     * % How many plates do we have?
     * length(plates)
     * % Get all information about plate 2
     * plate2 = plates(2,:)
     * % Get the augmented plate codes for all plates
     * acodes = plates(:,1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the plates for
     * @return Each row contains information about one plate:
     *         <p>
     *         <code>{ plate augmented code, plate perm id, plate space code, plate code, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code }</code>
     */
    public static Object[][] listPlates(String experiment)
    {
        checkLoggedIn();
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        return listPlates(experimentPlates);
    }

    private static Object[][] listPlates(final List<Plate> list)
    {
        final Object[][] result = new Object[list.size()][9];
        for (int i = 0; i < list.size(); ++i)
        {
            final Object[] annotations =
                    new Object[]
                        { list.get(i).getAugmentedCode(), plates.get(i).getPermId(),
                                list.get(i).tryGetSpaceCode(), plates.get(i).getPlateCode(),
                                list.get(i).getExperimentIdentifier().getAugmentedCode(),
                                list.get(i).getExperimentIdentifier().getPermId(),
                                list.get(i).getExperimentIdentifier().getSpaceCode(),
                                list.get(i).getExperimentIdentifier().getProjectCode(),
                                list.get(i).getExperimentIdentifier().getExperimentCode(), };
            System.arraycopy(annotations, 0, result[i], 0, annotations.length);
        }
        return result;
    }

    /**
     * Returns the properties of specified well for specified plate.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get properties for well A03 of plate P005 in space SPACE
     * properties = OpenBISScreeningML.getWellProperties('/SPACE/P005', 1, 3)
     * % Get property type code of first property
     * properties(1,1)
     * % Get property value of first property
     * properties(1,2)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code
     * @param row The row in the plate to get the well properties for
     * @param column The column in the plate to get the well properties for
     * @return A two dimensional array where the first column contains the property codes and the
     *         second column the corresponding property values.
     */
    public static Object[][] getWellProperties(String augmentedPlateCode, int row, int column)
    {
        checkLoggedIn();
        WellPosition wellPosition = new WellPosition(row, column);
        WellIdentifier wellIdentifier = getWell(augmentedPlateCode, wellPosition);
        List<Map.Entry<String, String>> list =
                new ArrayList<Map.Entry<String, String>>(openbis.getWellProperties(wellIdentifier)
                        .entrySet());
        Object[][] result = new Object[list.size()][2];
        for (int i = 0; i < list.size(); i++)
        {
            result[i] = new Object[]
                { list.get(i).getKey(), list.get(i).getValue() };
        }
        return result;
    }

    /**
     * Updates properties of specified well for specified plate.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Updates properties DESCRIPTION and NUMBER for well A03 of plate P005 in space SPACE
     * properties = {'DESCRIPTION' 'hello example'; 'NUMBER' 3.14}
     * OpenBISScreeningML.updateWellProperties('/SPACE/P005', 1, 3, properties)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code
     * @param row The row in the plate to get the well properties for
     * @param column The column in the plate to get the well properties for
     * @param properties A two dimensional array where the first column contains the property codes
     *            and the second column the corresponding property values.
     */
    public static void updateWellProperties(String augmentedPlateCode, int row, int column,
            Object[][] properties)
    {
        checkLoggedIn();
        WellPosition wellPosition = new WellPosition(row, column);
        WellIdentifier wellIdentifier = getWell(augmentedPlateCode, wellPosition);
        openbis.updateWellProperties(wellIdentifier, createMap(properties));
    }

    private static WellIdentifier getWell(String augmentedPlateCode, WellPosition wellPosition)
    {
        Plate plate = getPlate(augmentedPlateCode);
        List<WellIdentifier> wells = openbis.listPlateWells(plate);
        for (WellIdentifier wellIdentifier : wells)
        {
            if (wellIdentifier.getWellPosition().equals(wellPosition))
            {
                return wellIdentifier;
            }
        }
        throw new RuntimeException("Plate '" + augmentedPlateCode + "' has no well at "
                + wellPosition + ".");
    }

    /**
     * Fetches metadata of the image datasets for the specified <var>plate</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the metadata of image datasets of plate P005 from space SPACE
     * imagesMetadata = OpenBISScreeningML.getImagesMetadata('/SPACE/P005');
     * % How many image datasets do we have? Usually there will be just one.
     * length(imagesMetadata)
     * % What is the number of tiles in the first image dataset?
     * imagesMetadata(1, 3)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @return <code>{ images width, images height, number of tiles in the well, 
     *                 number of tiles rows, number of tiles columns, number of plate rows, number of plate columns }</code>
     *         .
     */
    public static Object[][] getImagesMetadata(String augmentedPlateCode)
    {
        checkLoggedIn();
        final Plate plateId = getPlate(augmentedPlateCode);
        final List<ImageDatasetReference> imageDatasets = listRawImageDatasets(plateId);
        final List<ImageDatasetMetadata> metaList = openbis.listImageMetadata(imageDatasets);

        Object[][] result = new Object[metaList.size()][];
        for (int i = 0; i < metaList.size(); ++i)
        {
            ImageDatasetMetadata meta = metaList.get(i);
            ImageDatasetReference imageDatasetReference = imageDatasets.get(i);
            Geometry plateGeometry = imageDatasetReference.getPlateGeometry();
            result[i] =
                    new Object[]
                        { meta.getWidth(), meta.getHeight(), meta.getNumberOfTiles(),
                                meta.getTilesRows(), meta.getTilesCols(),
                                plateGeometry.getNumberOfRows(), plateGeometry.getNumberOfColumns() };
        }
        return result;
    }

    /**
     * Lists all channels measured in <var>experiment</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the channels of experiment MYEXP in project PROJ of space SPACE
     * channels = OpenBISScreeningML.listChannels('/SPACE/PROJ/MYEXP');
     * % How many channels do we have?
     * length(channels)
     * % What is the name of channel 1?
     * channels(1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the channels for
     * @return Each row contains information about one channel. Currently the only information
     *         available is the channel name.
     */
    public static Object[][] listChannels(String experiment)
    {
        checkLoggedIn();
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        if (experimentPlates.isEmpty())
        {
            return new Object[0][];
        }
        final List<ImageDatasetReference> imageDatasets =
                openbis.listRawImageDatasets(experimentPlates);
        if (imageDatasets.isEmpty())
        {
            return new Object[0][];
        }
        return extractChannels(imageDatasets.get(0));
    }

    private static List<String> getChannelCodes(final List<ImageDatasetMetadata> meta)
    {
        return meta.get(0).getChannelCodes();
    }

    /**
     * Lists all features computed for <var>experiment</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the features of experiment MYEXP in project PROJ of space SPACE
     * features = OpenBISScreeningML.listFeatures('/SPACE/PROJ/MYEXP');
     * % How many features do we have?
     * length(features)
     * % What is the name of features 1?
     * features(1)
     * </pre>
     * 
     * @param experiment The augmented code of the experiment to list the features for
     * @return Each row contains information about one feature. Currently the only information
     *         available is the feature name.
     */
    public static Object[][] listFeatures(String experiment)
    {
        checkLoggedIn();
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        if (experimentPlates.isEmpty())
        {
            return new Object[0][];
        }
        final List<FeatureVectorDatasetReference> featureDatasets =
                openbis.listFeatureVectorDatasets(experimentPlates);
        if (featureDatasets.isEmpty())
        {
            return new Object[0][];
        }
        final List<String> features = listAvailableFeatureCodes(featureDatasets);
        Object[][] result = new Object[features.size()][1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i][0] = features.get(i);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private static List<String> listAvailableFeatureCodes(
            final List<FeatureVectorDatasetReference> featureDatasets)
    {
        return openbis.listAvailableFeatureNames(Arrays.asList(featureDatasets.get(0)));
    }

    //
    // Data Sets
    //

    /**
     * Loads data sets for specified plate code. For each data set the path to the root of the data
     * set is returned. If it is possible the path points directly into the data set store. No data
     * is copied. Otherwise the data is retrieved from the data store server.<br>
     * If the same dataset is loaded for the second time in one session it will be immediately
     * returned from the local cache.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load all data sets of plate P005 in space SPACE
     * dsinfo = OpenBISScreeningML.loadDataSets('/SPACE/P005')
     * % Get the data set codes
     * dsinfo(:,1)
     * % Get root path of first data set (assuming there is at least one)
     * dsginfo(1,2)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param datasetTypeCodePattern only datasets of the type which matche the specified pattern
     *            will be returned. To fetch all datasets specify ".*".
     * @param overrideStoreRootPathOrNull A path, in the context of the local file system mounts, to
     *            the DSS' store root. If null, paths are returned in the context of the DSS' file
     *            system mounts.
     * @return Each row contains information about one data set:
     *         <p>
     *         <code>{ data set code, data set root path  }</code>
     */
    public static Object[][] loadDataSets(String augmentedPlateCode, String datasetTypeCodePattern,
            String overrideStoreRootPathOrNull)
    {
        checkLoggedIn();
        Plate plateIdentifier = getPlate(augmentedPlateCode);

        List<IDataSetDss> dataSets = openbis.getDataSets(plateIdentifier, datasetTypeCodePattern);
        Object[][] result = new Object[dataSets.size()][];
        try
        {
            for (int i = 0; i < dataSets.size(); i++)
            {
                IDataSetDss dataSet = dataSets.get(i);
                String code = dataSet.getCode();
                File file = new File(dataSetsDir, code);
                if (file.exists() == false)
                {
                    file =
                            dataSet.getLinkOrCopyOfContents(overrideStoreRootPathOrNull,
                                    dataSetsDir);
                }
                result[i] = new Object[]
                    { code, file.getPath() };
            }
            return result;
        } catch (Exception ex)
        {
            throw new RuntimeException("Loading data sets for plate '" + augmentedPlateCode
                    + "' failed: " + ex, ex);
        }
    }

    /**
     * Uploads specified data set for specified plate. The data set code will be returned.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Upload data set /path/to/my-data-set with properties DESCRIPTION and NUMBER for 
     * % plate P005 in space SPACE
     * properties = {'DESCRIPTION' 'hello example'; 'NUMBER' 3.14}
     * datasetcode = OpenBISScreeningML.uploadDataSet('/SPACE/P005', '/path/to/my-data-set', 'HCS_IMAGE', properties)
     * </pre>
     * 
     * @param augmentedPlateCode The augmented plate code.
     * @param dataSetFilePath Path to the data set file/folder to be uploaded.
     * @param dataSetType Data set type.
     * @param dataSetProperties A two dimensional array where the first column contains the property
     *            codes and the second column the corresponding property values.
     */
    public static Object uploadDataSet(String augmentedPlateCode, String dataSetFilePath,
            String dataSetType, Object[][] dataSetProperties)
    {
        checkLoggedIn();
        Plate plateIdentifier = getPlate(augmentedPlateCode);
        File dataSetFile = new File(dataSetFilePath);
        if (dataSetFile.exists() == false)
        {
            throw new RuntimeException("Unknown data set file path '" + dataSetFilePath + "'.");
        }
        try
        {
            Map<String, String> map = createMap(dataSetProperties);
            IDataSetDss dataSet =
                    openbis.putDataSet(plateIdentifier, dataSetFile, new NewDataSetMetadataDTO(
                            dataSetType, map));
            return dataSet.getCode();
        } catch (Exception ex)
        {
            throw new RuntimeException("Couldn't upload data set for plate '" + augmentedPlateCode
                    + "'.", ex);
        }
    }

    private static Map<String, String> createMap(Object[][] properties)
    {
        Map<String, String> map = new HashMap<String, String>();
        for (Object[] objects : properties)
        {
            if (objects.length == 2)
            {
                Object value = objects[1];
                map.put(objects[0].toString(), value == null ? null : value.toString());
            }
        }
        return map;
    }

    //
    // Images
    //

    /**
     * Loads the TIFF images for the given well location, all tiles and all channels and stores them
     * in temporary files. The temporary files will be removed automatically when the Java Virtual
     * Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for all channels of well B10 of plate P005 in space SPACE
     * imginfo = OpenBISScreeningML.loadImages('/SPACE/P005', 2, 10)
     * % Get the plate-well descriptions of all locations
     * imginfo(2,:,3)
     * % Show the third image (assuming there are at least three images)
     * imtool(imginfo(1,3))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a
     *         vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains
     *         <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col)
    {
        return loadImages(plate, row, col, (String[]) null);
    }

    /**
     * Loads the TIFF images for the given well location, tile number, and all channels and stores
     * them in temporary files. The temporary files will be removed automatically when the Java
     * Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for all channels of well B10 and tile 3 of plate P005 in space SPACE
     * imginfo = OpenBISScreeningML.loadImages('/SPACE/P005', 2, 10, 3)
     * % Get the plate-well descriptions of all locations
     * imginfo(2,:,3)
     * % Show the third image (assuming there are at least three images)
     * imtool(imginfo(1,3))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @param tile The tile number. Starts with 0.
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a
     *         vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains
     *         <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col, int tile)
    {
        return loadImages(plate, row, col, tile, (String[]) null);
    }

    /**
     * Loads the TIFF images for the given well location, list of channels, and all tiles and stores
     * them in temporary files. The temporary files will be removed automatically when the Java
     * Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for channel DAPI of well H10 of plate P005 in space SPACE
     * imginfo=OpenBISScreeningML.loadImages('/SPACE/P005', 8, 10, 'DAPI')
     * % Get the channel names and tile numbers of all locations
     * imginfo(2,:,1:2)
     * % Show the second image (assuming there are at least two images)
     * imtool(imginfo(1,2))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @param channels The names of the channels to get the images for
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a
     *         vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains
     *         <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col, String[] channels)
    {
        return loadRawImages(plate, row, col, channels, createAllTilesIterator());
    }

    /**
     * Has the same effect as {@link #loadImages(String, int, int, String[])}, but instead of
     * loading raw images loads their segmentation results if available.
     */
    public static Object[][][] loadSegmentationImages(String plate, int row, int col,
            String[] objectNames)
    {
        return loadSegmentationImages(plate, row, col, objectNames, createAllTilesIterator());
    }

    private static ITileNumberIterable createAllTilesIterator()
    {
        return new ITileNumberIterable()
            {
                private int maximumNumberOfTiles;

                public void setMaximumNumberOfTiles(int numberOfTiles)
                {
                    this.maximumNumberOfTiles = numberOfTiles;
                }

                public int getMaximumNumberOfTiles()
                {
                    return maximumNumberOfTiles;
                }

                public Iterator<Integer> iterator()
                {
                    return new Iterator<Integer>()
                        {
                            private int index;

                            public boolean hasNext()
                            {
                                return index < maximumNumberOfTiles;
                            }

                            public Integer next()
                            {
                                return index++;
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    /**
     * Loads the TIFF images for the given well location, tile number, and list of channels and
     * stores them in temporary files. The temporary files will be removed automatically when the
     * Java Virtual Machine exits.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Load the images for channel DAPI of well H10 and tile 3 of plate P005 in space SPACE
     * imginfo=OpenBISScreeningML.loadImages('/SPACE/P005', 8, 10, 3, 'DAPI')
     * % Get the channel names of all locations
     * imginfo(2,:,1)
     * % Show the second image (assuming there are at least two images)
     * imtool(imginfo(1,2))
     * </pre>
     * 
     * @param plate The augmented plate code
     * @param row The row in the plate to get the images for
     * @param col The column in the plate to get the images for
     * @param tile The tile number. Starts with 0.
     * @param channels The names of the channels to get the images for
     * @return <code>{ names of TIFF files, image annotation }</code>
     *         <p>
     *         Each of <code>names of TIFF files</code> and <code>image annotation</code> is a
     *         vector of length of the number of images.
     *         <p>
     *         <code>image annotation</code> contains
     *         <code>{ channel name, tile number, plate well description, 
     *         plate augmented code, plate perm id, plate space code, plate code, row, column, 
     *         experiment augmented code, experiment perm id, experiment space code, 
     *         experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] loadImages(String plate, int row, int col, final int tile,
            String[] channels)
    {
        return loadRawImages(plate, row, col, channels, createSingleTileIterator(tile));
    }

    /**
     * Has the same effect as {@link #loadImages(String, int, int, int, String[])}, but instead of
     * loading raw images loads their segmentation results if available.
     */
    public static Object[][][] loadSegmentationImages(String plate, int row, int col,
            final int tile, String[] objectNames)
    {
        return loadSegmentationImages(plate, row, col, objectNames, createSingleTileIterator(tile));
    }

    private static ITileNumberIterable createSingleTileIterator(final int tile)
    {
        return new ITileNumberIterable()
            {
                public void setMaximumNumberOfTiles(int numberOfTiles)
                {
                    if (tile >= numberOfTiles)
                    {
                        throw new IllegalArgumentException("Tile number " + tile
                                + " is not less than number of tiles " + numberOfTiles + ".");
                    }
                }

                public int getMaximumNumberOfTiles()
                {
                    return 1;
                }

                public Iterator<Integer> iterator()
                {
                    return new Iterator<Integer>()
                        {
                            private boolean delivered;

                            public boolean hasNext()
                            {
                                return delivered == false;
                            }

                            public Integer next()
                            {
                                delivered = true;
                                return tile;
                            }

                            public void remove()
                            {
                                throw new UnsupportedOperationException();
                            }
                        };
                }
            };
    }

    /**
     * Lists all segmentation objects for the <var>plate</var>.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get the segmentation objects of plate P005 in space SPACE.
     * segmentationObjects = OpenBISScreeningML.listSegmentationObjects('/SPACE/P005');
     * % How many segmentation objects do we have?
     * length(segmentationObjects)
     * % What is the name of segmentation objects 1?
     * segmentationObjects(1)
     * </pre>
     * 
     * @param plate augmented code of the plate
     * @return Each row contains information about one segmentation object. Currently the only
     *         information available is the segmentation object name.
     */
    public static Object[][] listSegmentationObjects(String plate)
    {
        checkLoggedIn();
        Plate plateId = getPlate(plate);
        final List<ImageDatasetReference> imageDatasets = listSegmentationImageDatasets(plateId);
        if (imageDatasets.isEmpty())
        {
            return new Object[0][];
        }
        return extractChannels(imageDatasets.get(0));
    }

    private static Object[][] extractChannels(ImageDatasetReference imageDatasetReference)
    {
        final List<ImageDatasetMetadata> meta =
                openbis.listImageMetadata(Arrays.asList(imageDatasetReference));
        if (meta.isEmpty())
        {
            return new Object[0][];
        }
        final List<String> channels = getChannelCodes(meta);
        Object[][] result = new Object[channels.size()][1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i][0] = channels.get(i);
        }
        return result;
    }

    private static Object[][][] loadRawImages(String plate, int row, int col, String[] channels,
            ITileNumberIterable tileNumberIterable)
    {
        checkLoggedIn();
        final Plate plateId = getPlate(plate);
        final List<ImageDatasetReference> imageDatasets = listRawImageDatasets(plateId);
        return loadImages(plateId, imageDatasets, row, col, channels, tileNumberIterable);
    }

    private static Object[][][] loadSegmentationImages(String plate, int row, int col,
            String[] channels, ITileNumberIterable tileNumberIterable)
    {
        checkLoggedIn();
        final Plate plateId = getPlate(plate);
        final List<ImageDatasetReference> imageDatasets = listSegmentationImageDatasets(plateId);
        return loadImages(plateId, imageDatasets, row, col, channels, tileNumberIterable);
    }

    private static Object[][][] loadImages(Plate plate, List<ImageDatasetReference> imageDatasets,
            int row, int col, String[] channels, ITileNumberIterable tileNumberIterable)
    {
        final List<ImageDatasetMetadata> meta = openbis.listImageMetadata(imageDatasets);
        if (meta.isEmpty())
        {
            return new Object[][][]
                { new Object[0][], new Object[0][] };
        }
        final List<String> imageChannels;
        if (channels == null || channels.length == 0)
        {
            imageChannels = getChannelCodes(meta);
        } else
        {
            imageChannels = Arrays.asList(channels);
        }
        final List<ImageReferenceAndFile> imageReferencesAndFiles =
                new ArrayList<ImageReferenceAndFile>(imageDatasets.size());
        final Object[][][] result = new Object[2][][];
        tileNumberIterable.setMaximumNumberOfTiles(meta.get(0).getNumberOfTiles());
        int numberOfTiles = tileNumberIterable.getMaximumNumberOfTiles();
        result[0] = new Object[imageDatasets.size() * imageChannels.size() * numberOfTiles][1];
        result[1] = new Object[imageDatasets.size() * imageChannels.size() * numberOfTiles][15];
        int dsIdx = 0;
        int resultIdx = 0;
        for (ImageDatasetReference ds : imageDatasets)
        {
            for (String channel : imageChannels)
            {
                for (Integer tile : tileNumberIterable)
                {
                    final PlateImageReference ref =
                            new PlateImageReference(row, col, tile, channel, ds);
                    final File imageFile = createImageFileName(plate, ref);
                    imageReferencesAndFiles.add(new ImageReferenceAndFile(ref, imageFile));
                    result[0][resultIdx][0] = imageFile.getPath();
                    PlateIdentifier plateIdentifier = ds.getPlate();
                    ExperimentIdentifier expIdentifier = ds.getExperimentIdentifier();
                    final Object[] annotations =
                            new Object[]
                                { channel, tile,
                                        createPlateWellDescription(plateIdentifier, row, col),
                                        plateIdentifier.getAugmentedCode(),
                                        plateIdentifier.getPermId(),
                                        plateIdentifier.tryGetSpaceCode(),
                                        plateIdentifier.getPlateCode(), row, col,
                                        expIdentifier.getAugmentedCode(),
                                        expIdentifier.getPermId(), expIdentifier.getSpaceCode(),
                                        expIdentifier.getProjectCode(),
                                        expIdentifier.getExperimentCode(), ds.getPermId(), };
                    System.arraycopy(annotations, 0, result[1][resultIdx], 0, annotations.length);
                    resultIdx++;
                }
            }
            dsIdx++;
        }
        try
        {
            loadImages(imageReferencesAndFiles);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        return result;
    }

    private static List<ImageDatasetReference> listRawImageDatasets(final Plate plateId)
    {
        return openbis.listRawImageDatasets(Arrays.asList(plateId));
    }

    private static List<ImageDatasetReference> listSegmentationImageDatasets(final Plate plateId)
    {
        return openbis.listSegmentationImageDatasets(Arrays.asList(plateId));
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) and files.
     * 
     * @throws IOException when reading images from the server or writing them to the files fails
     */
    private static void loadImages(List<ImageReferenceAndFile> imageReferencesAndFiles)
            throws IOException
    {
        List<PlateImageReference> imageReferences = new ArrayList<PlateImageReference>();
        final Map<PlateImageReference, ImageReferenceAndFile> imageRefToFileMap =
                new HashMap<PlateImageReference, ImageReferenceAndFile>();
        for (ImageReferenceAndFile imageReferenceAndFile : imageReferencesAndFiles)
        {
            PlateImageReference imageReference = imageReferenceAndFile.getImageReference();
            File file = loadedImages.get(imageReference);
            if (file == null)
            {
                imageReferences.add(imageReference);
                imageRefToFileMap.put(imageReference, imageReferenceAndFile);
            }
        }
        try
        {
            openbis.loadImages(imageReferences, new IImageOutputStreamProvider()
                {
                    public OutputStream getOutputStream(PlateImageReference imageReference)
                            throws IOException
                    {
                        return imageRefToFileMap.get(imageReference).open();
                    }
                }, false);
        } finally
        {
            Collection<ImageReferenceAndFile> values = imageRefToFileMap.values();
            for (ImageReferenceAndFile imageReferenceAndFile : values)
            {
                imageReferenceAndFile.close();
                PlateImageReference imageReference = imageReferenceAndFile.getImageReference();
                loadedImages.put(imageReference, imageReferenceAndFile.getImageFile());
            }
        }
    }

    private static File createImageFileName(Plate plate, PlateImageReference image)
    {
        final WellPosition well = image.getWellPosition();
        File imageDir = new File(temporarySessionDir, "images");
        imageDir.mkdirs();
        final File f =
                new File(imageDir, "img_" + plate.getPlateCode() + "_" + image.getDatasetCode()
                        + "_row" + well.getWellRow() + "_col" + well.getWellColumn() + "_"
                        + image.getChannel() + "_tile" + image.getTile() + ".tiff");
        f.deleteOnExit();
        return f;
    }

    //
    // Feature matrix
    //

    /**
     * Returns the feature matrix of all features for all locations in <var>experiment</var> (a
     * location is one well position in one feature vector data set) connected to <var>gene</var> in
     * <code>[0]</code>, location annotations in <code>[1]</code> and feature annotation in
     * <code>[2]</code>.
     * <p>
     * One row in the matrix corresponds to one location (i.e. one well and one feature vector
     * dataset), one column corresponds to one feature.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for experiment /SPACE/PROJ/MYEXP for locations connected to GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('/SPACE/PROJ/MYEXP', 'GENENAME');
     * % Get the feature vector for the second location (assuming there are at least two locations)
     * loc2 = fmatrix(1,2,:)
     * % Get the values of the fifth feature for all locations (assuming there are at least 5 features)
     * feature5 = fmatrix(1,:,5)
     * % What are the features?
     * featureNames = fmatrix(3,:)
     * % Get the plate-well descriptions of the locations
     * locationDescriptions = fmatrix(2,:,1)
     * </pre>
     * 
     * @param experiment The augmented experiment code
     * @param gene The gene code (stored as material code in openBIS, usually it is gene id)
     * @return <code>{ feature matrix, annotations per location, feature names }</code> where
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column, experiment augmented code, experiment perm
     *         id, experiment space code, experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] getFeatureMatrix(String experiment, String gene)
    {
        return getFeatureMatrix(experiment, gene, (String[]) null);
    }

    /**
     * Returns the feature matrix of the specified features for all locations in
     * <var>experiment</var> (a location is one well position in one feature vector data set) in
     * <var>experiment</var> connected to <var>gene</var> in <code>[0]</code>, location annotations
     * in <code>[1]</code> and feature annotation in <code>[2]</code>.
     * <p>
     * One row in the matrix corresponds to one location (i.e. one well and one feature vector
     * dataset), one column corresponds to one feature.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE for 
     * % experiment /SPACE/PROJ/MYEXP for locations connected to GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('/SPACE/PROJ/MYEXP', 'GENENAME', ('FEATURE1','FEATURE2','FEATURE3'));
     * % Get the feature vector for the second location (assuming there are at least two locations)
     * loc2 = fmatrix(1,2,:)
     * % Get the values of the fourth feature for all locations (assuming there are at least 4 features)
     * feature5 = fmatrix(1,:,4)
     * % What are the features?
     * featureNames = fmatrix(3,:)
     * % Get the plate-well descriptions of the locations
     * locationDescriptions = fmatrix(2,:,1)
     * </pre>
     * 
     * @param experiment The augmented experiment code
     * @param gene The gene code (stored as material code in openBIS, usually it is gene id)
     * @param features The names of the features to contain the feature matrix
     * @return <code>{ feature matrix, annotations per location, feature names }</code> where
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column, experiment augmented code, experiment perm
     *         id, experiment space code, experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] getFeatureMatrix(String experiment, String gene, String[] features)
    {
        checkLoggedIn();
        final ExperimentIdentifier experimentId = experimentCodeToExperimentMap.get(experiment);
        if (experimentId == null)
        {
            throw new RuntimeException("No experiment with that code found.");
        }
        final List<Plate> experimentPlates = experimentToPlateMap.get(experiment);
        if (experimentPlates == null || experimentPlates.isEmpty())
        {
            return new Object[][][]
                { new Object[0][], new Object[0][], new Object[0][] };
        }
        final List<FeatureVectorWithDescription> featureVectors =
                openbis.loadFeaturesForPlateWells(experimentId, new MaterialIdentifier(
                        MaterialTypeIdentifier.GENE, gene),
                        (features == null) ? null : Arrays.asList(features));
        final List<String> featureNameList =
                featureVectors.get(featureVectors.size() - 1).getFeatureNames();
        final Object[][][] result = new Object[3][][];
        if (featureVectors.isEmpty())
        {
            return result;
        }
        result[0] = new Object[featureVectors.size()][featureNameList.size()];
        result[1] = new Object[featureVectors.size()][13];
        int resultIdx = 0;
        for (FeatureVectorWithDescription f : featureVectors)
        {
            arraycopy(f.getValues(), result[0][resultIdx]);
            final Object[] annotations =
                    new Object[]
                        {
                                createPlateWellDescription(f),
                                f.getDatasetWellReference().getPlate().getAugmentedCode(),
                                f.getDatasetWellReference().getPlate().getPermId(),
                                f.getDatasetWellReference().getPlate().tryGetSpaceCode(),
                                f.getDatasetWellReference().getPlate().getPlateCode(),
                                f.getWellPosition().getWellRow(),
                                f.getWellPosition().getWellColumn(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getAugmentedCode(),
                                f.getDatasetWellReference().getExperimentIdentifier().getPermId(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getSpaceCode(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getProjectCode(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getExperimentCode(),
                                f.getDatasetWellReference().getDatasetCode(), };
            System.arraycopy(annotations, 0, result[1][resultIdx], 0, annotations.length);
            resultIdx++;
        }
        result[2] = new Object[featureNameList.size()][1];
        for (int i = 0; i < featureNameList.size(); ++i)
        {
            result[2][i][0] = featureNameList.get(i);
        }
        return result;
    }

    /**
     * Returns the feature matrix of all features for all locations (a location is one well position
     * in one feature vector data set) connected to <var>gene</var> in <code>[0]</code>, location
     * annotations in <code>[1]</code> and feature annotation in <code>[2]</code>.
     * <p>
     * One row in the matrix corresponds to one location (i.e. one well and one feature vector
     * dataset), one column corresponds to one feature.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('GENENAME');
     * % Get the feature vector for the second location (assuming there are at least two locations)
     * loc2 = fmatrix(1,2,:)
     * % Get the values of the fifth feature for all locations (assuming there are at least 5 features)
     * feature5 = fmatrix(1,:,5)
     * % What are the features?
     * featureNames = fmatrix(3,:)
     * % Get the plate-well descriptions of the locations
     * locationDescriptions = fmatrix(2,:,1)
     * </pre>
     * 
     * @param gene The gene code (stored as material code in openBIS, usually it is gene id)
     * @return <code>{ feature matrix, annotations per location, feature names }</code> where
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column, experiment augmented code, experiment perm
     *         id, experiment space code, experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] getFeatureMatrix(String gene)
    {
        return getFeatureMatrix(gene, (String[]) null);
    }

    /**
     * Returns the feature matrix of the specified features for all locations (a location is one
     * well position in one feature vector data set) in <var>experiment</var> connected to
     * <var>gene</var> in <code>[0]</code>, location annotations in <code>[1]</code> and feature
     * annotation in <code>[2]</code>.
     * <p>
     * One row in the matrix corresponds to one location (i.e. one well and one feature vector
     * dataset), one column corresponds to one feature.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE for GENENAME
     * fmatrix = OpenBISScreeningML.getFeatureMatrix('GENENAME', ('FEATURE1','FEATURE2','FEATURE3'));
     * % Get the feature vector for the second location (assuming there are at least two locations)
     * loc2 = fmatrix(1,2,:)
     * % Get the values of the second feature ('FEATURE2' here) for all locations
     * feature2 = fmatrix(1,:,2)
     * % What are the features?
     * featureNames = fmatrix(3,:)
     * % Get the plate-well descriptions of the locations
     * locationDescriptions = fmatrix(2,:,1)
     * </pre>
     * 
     * @param gene The gene code (stored as material code in openBIS, usually it is gene id)
     * @param features The names of the features to contain the feature matrix
     * @return <code>{ feature matrix, annotations per location, feature names }</code> where
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column, experiment augmented code, experiment perm
     *         id, experiment space code, experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] getFeatureMatrix(String gene, String[] features)
    {
        checkLoggedIn();
        final List<FeatureVectorWithDescription> featureVectors =
                openbis.loadFeaturesForPlateWells(new MaterialIdentifier(
                        MaterialTypeIdentifier.GENE, gene),
                        (features == null) ? null : Arrays.asList(features));
        final List<String> featureNameList =
                featureVectors.get(featureVectors.size() - 1).getFeatureNames();
        final Object[][][] result = new Object[3][][];
        if (featureVectors.isEmpty())
        {
            return result;
        }
        result[0] = new Object[featureVectors.size()][featureNameList.size()];
        result[1] = new Object[featureVectors.size()][13];
        int resultIdx = 0;
        for (FeatureVectorWithDescription f : featureVectors)
        {
            arraycopy(f.getValues(), result[0][resultIdx]);
            final Object[] annotations =
                    new Object[]
                        {
                                createPlateWellDescription(f),
                                f.getDatasetWellReference().getPlate().getAugmentedCode(),
                                f.getDatasetWellReference().getPlate().getPermId(),
                                f.getDatasetWellReference().getPlate().tryGetSpaceCode(),
                                f.getDatasetWellReference().getPlate().getPlateCode(),
                                f.getWellPosition().getWellRow(),
                                f.getWellPosition().getWellColumn(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getAugmentedCode(),
                                f.getDatasetWellReference().getExperimentIdentifier().getPermId(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getSpaceCode(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getProjectCode(),
                                f.getDatasetWellReference().getExperimentIdentifier()
                                        .getExperimentCode(),
                                f.getDatasetWellReference().getDatasetCode(), };
            System.arraycopy(annotations, 0, result[1][resultIdx], 0, annotations.length);
            resultIdx++;
        }
        result[2] = new Object[featureNameList.size()][1];
        for (int i = 0; i < featureNameList.size(); ++i)
        {
            result[2][i][0] = featureNameList.get(i);
        }
        return result;
    }

    /**
     * Returns the feature matrix of all available features for all locations (a location is one
     * well position in one feature vector data set) of all feature vector data sets of the given
     * <var>plate</var> in <code>[0]</code>, location annotations in <code>[1]</code> and feature
     * annotation in <code>[2]</code>.
     * <p>
     * One row in the matrix corresponds to one location (i.e. one well and one feature vector
     * dataset), one column corresponds to one feature.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for PLATECODE
     * fmatrix = OpenBISScreeningML.getFeatureMatrixForPlate('PLATECODE');
     * % Get the feature vector for the second location (assuming there are at least two locations)
     * loc2 = fmatrix(1,2,:)
     * % Get the values of the fourth feature for all locations (assuming there are at least 4 features)
     * feature5 = fmatrix(1,:,4)
     * % What are the features?
     * featureNames = fmatrix(3,:)
     * % Get the plate-well descriptions of the locations
     * locationDescriptions = fmatrix(2,:,1)
     * </pre>
     * 
     * @param plate augmented code of the plate for which features should be loaded
     * @return <code>{ feature matrix, annotations per location, feature names }</code> where
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column, experiment augmented code, experiment perm
     *         id, experiment space code, experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] getFeatureMatrixForPlate(String plate)
    {
        return getFeatureMatrixForPlate(plate, (String[]) null);
    }

    /**
     * Returns the feature matrix of the specified features for all locations (a location is one
     * well position in one feature vector data set) of all feature vector data sets of the given
     * <var>plate</var> in <code>[0]</code>, location annotations in <code>[1]</code> and feature
     * annotation in <code>[2]</code>.
     * <p>
     * One row in the matrix corresponds to one location (i.e. one well and one feature vector
     * dataset), one column corresponds to one feature.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE for PLATECODE
     * fmatrix = OpenBISScreeningML.getFeatureMatrixForPlate('PLATECODE', ('FEATURE1','FEATURE2','FEATURE3'));
     * % Get the feature vector for the second location (assuming there are at least two locations)
     * loc2 = fmatrix(1,2,:)
     * % Get the values of the second feature for all locations
     * feature5 = fmatrix(1,:,2)
     * % What are the features?
     * featureNames = fmatrix(3,:)
     * % Get the plate-well descriptions of the locations
     * locationDescriptions = fmatrix(2,:,1)
     * </pre>
     * 
     * @param plate augmented code of the plate for which features should be loaded
     * @param features The names of the features to contain the feature matrix
     * @return <code>{ feature matrix, annotations per location, feature names }</code> where
     *         <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column, experiment augmented code, experiment perm
     *         id, experiment space code, experiment project code, experiment code, data set code }</code>
     */
    public static Object[][][] getFeatureMatrixForPlate(String plate, String[] features)
    {
        checkLoggedIn();
        final List<FeatureVectorDataset> featureVectors =
                openbis.loadFeaturesForPlates(
                        Arrays.asList(PlateIdentifier.createFromAugmentedCode(plate)),
                        (features == null) ? null : Arrays.asList(features));
        FeatureVectorDataset last = featureVectors.get(featureVectors.size() - 1);
        final List<String> featureCodeList = getFeatureCodes(last);
        final Object[][][] result = new Object[3][][];
        if (featureVectors.isEmpty())
        {
            return result;
        }
        int numberOfRows = 0;
        for (FeatureVectorDataset fvds : featureVectors)
        {
            numberOfRows += fvds.getFeatureVectors().size();
        }
        result[0] = new Object[numberOfRows][featureCodeList.size()];
        result[1] = new Object[numberOfRows][13];
        int resultIdx = 0;
        for (FeatureVectorDataset fvds : featureVectors)
        {
            final FeatureVectorDatasetReference datasetRef = fvds.getDataset();
            for (FeatureVector f : fvds.getFeatureVectors())
            {
                arraycopy(f.getValues(), result[0][resultIdx]);
                final Object[] annotations =
                        new Object[]
                            { createPlateWellDescription(datasetRef.getPlate(), f),
                                    datasetRef.getPlate().getAugmentedCode(),
                                    datasetRef.getPlate().getPermId(),
                                    datasetRef.getPlate().tryGetSpaceCode(),
                                    datasetRef.getPlate().getPlateCode(),
                                    f.getWellPosition().getWellRow(),
                                    f.getWellPosition().getWellColumn(),
                                    datasetRef.getExperimentIdentifier().getAugmentedCode(),
                                    datasetRef.getExperimentIdentifier().getPermId(),
                                    datasetRef.getExperimentIdentifier().getSpaceCode(),
                                    datasetRef.getExperimentIdentifier().getProjectCode(),
                                    datasetRef.getExperimentIdentifier().getExperimentCode(),
                                    datasetRef.getDatasetCode(), };
                System.arraycopy(annotations, 0, result[1][resultIdx], 0, annotations.length);
                resultIdx++;
            }
        }
        result[2] = new Object[featureCodeList.size()][1];
        for (int i = 0; i < featureCodeList.size(); ++i)
        {
            result[2][i][0] = featureCodeList.get(i);
        }
        return result;
    }

    @SuppressWarnings("deprecation")
    private static List<String> getFeatureCodes(FeatureVectorDataset last)
    {
        return last.getFeatureNames();
    }

    /**
     * Returns the gene mapping for the given <var>plateCodes</var> in <code>[0]</code> and location
     * annotations in <code>[1]</code>.
     * <p>
     * One row in the matrix corresponds to one well.
     * <p>
     * Matlab example:
     * 
     * <pre>
     * % Get feature matrix for features FEATURE1, FEATURE2 and FEATURE for PLATECODE
     * genes = getGeneMappingForPlate('PLATECODE');
     * % Get the plate well location description of the 10th wells
     * loc2 = genes(2,10,1)
     * % Get the gene ids that are in the 10th well
     * geneIds = genes(1,10,:)
     * </pre>
     * 
     * @param platesCodes The augmented codes of the plates to get the mapping for
     * @return <code>{ gene ids, annotations per well }</code> where <code>gene ids</code> can be 0,
     *         1 or more gene ids. <code>annotations per location</code> contain:
     *         <p>
     *         <code>{ plate well description, plate augmented code, plate perm id,
     *         plate space code, plate code, row, column }</code>
     */
    public static Object[][][] getGeneMappingForPlates(String[] platesCodes)
    {
        checkLoggedIn();
        final List<PlateWellMaterialMapping> mappingList =
                openbis.listPlateMaterialMapping(toPlates(platesCodes), MaterialTypeIdentifier.GENE);
        int size = 0;
        for (PlateWellMaterialMapping mapping : mappingList)
        {
            size +=
                    mapping.getPlateGeometry().getNumberOfRows()
                            * mapping.getPlateGeometry().getNumberOfColumns();
        }
        final Object[][][] result = new Object[2][size][];
        int resultIdx = 0;
        for (PlateWellMaterialMapping mapping : mappingList)
        {
            for (int row = 1; row <= mapping.getPlateGeometry().getNumberOfRows(); ++row)
            {
                for (int col = 1; col <= mapping.getPlateGeometry().getNumberOfColumns(); ++col)
                {
                    final List<MaterialIdentifier> genes = mapping.getMaterialsForWell(row, col);
                    result[0][resultIdx] = new Object[genes.size()];
                    for (int i = 0; i < genes.size(); ++i)
                    {
                        result[0][resultIdx][i] = genes.get(i).getMaterialCode();
                    }
                    final PlateIdentifier plate = mapping.getPlateIdentifier();
                    result[1][resultIdx] =
                            new Object[]
                                { createPlateWellDescription(plate, row, col),
                                        plate.getAugmentedCode(), plate.getPermId(),
                                        plate.tryGetSpaceCode(), plate.getPlateCode(), row, col, };
                    ++resultIdx;
                }
            }
        }
        return result;
    }

    //
    // Helper methods
    //

    private static List<PlateIdentifier> toPlates(String[] augmentedPlateCodes)
    {
        final List<PlateIdentifier> result =
                new ArrayList<PlateIdentifier>(augmentedPlateCodes.length);
        for (String plateCode : augmentedPlateCodes)
        {
            result.add(PlateIdentifier.createFromAugmentedCode(plateCode));
        }
        return result;
    }

    private static Plate getPlate(String augmentedPlateCode)
    {
        Plate plateIdentifier = plateCodeToPlateMap.get(augmentedPlateCode);
        if (plateIdentifier == null)
        {
            throw new RuntimeException("No plate with that code '" + augmentedPlateCode
                    + "' found.");
        }
        return plateIdentifier;
    }

    private static void arraycopy(double[] src, Object[] dest)
    {
        for (int i = 0; i < dest.length; ++i)
        {
            dest[i] = src[i];
        }
    }

    private static String createPlateWellDescription(FeatureVectorWithDescription f)
    {
        return createPlateWellDescription(f.getDatasetWellReference().getPlate(), f
                .getWellPosition().getWellRow(), f.getWellPosition().getWellColumn());
    }

    private static String createPlateWellDescription(PlateIdentifier p, FeatureVector f)
    {
        return createPlateWellDescription(p, f.getWellPosition().getWellRow(), f.getWellPosition()
                .getWellColumn());
    }

    private static String createPlateWellDescription(PlateIdentifier p, int row, int col)
    {
        return p.getPlateCode() + ":" + translateRowNumberIntoLetterCode(row) + col;
    }

    /**
     * Translates a row number into letter code. Thus, 1 -> A, 2 -> B, 26 -> Z, 27 -> AA, 28 -> AB,
     * etc.
     */
    private static String translateRowNumberIntoLetterCode(int rowNumber)
    {
        int rowIndex = rowNumber - 1;
        String code = "";
        while (rowIndex >= 0)
        {
            code = (char) (rowIndex % 26 + 'A') + code;
            rowIndex = rowIndex / 26 - 1;
        }
        return code;
    }

    private static void checkLoggedIn()
    {
        if (openbis == null)
        {
            if (Login.OPENBIS_TOKEN_FILE.exists())
            {
                BufferedReader br = null;
                try
                {
                    br = new BufferedReader(new FileReader(Login.OPENBIS_TOKEN_FILE));
                    final String token = br.readLine();
                    br.close();
                    br = new BufferedReader(new FileReader(Login.OPENBIS_SERVER_URL_FILE));
                    final String serverUrl = br.readLine();
                    br.close();
                    br = null;
                    IScreeningOpenbisServiceFacade facade =
                            facadeFactory.tryToCreate(token, serverUrl);
                    if (facade == null)
                    {
                        throw new RuntimeException("Login failed.");
                    }
                    init(facade);
                } catch (IOException ex)
                {
                    if (openbis == null)
                    {
                        throw new RuntimeException("Login failed.", ex);
                    }
                } finally
                {
                    if (br != null)
                    {
                        try
                        {
                            br.close();
                        } catch (IOException ex)
                        {
                            // Silence this.
                        }
                    }
                }
            }
            if (openbis == null)
            {
                throw new RuntimeException("Not logged in.");
            }
        }
    }
}
