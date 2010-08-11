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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.IScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacadeFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.client.cli.Login;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellMaterialMapping;
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
 * that avoids this is to call the
 * {@link ch.systemsx.cisd.openbis.plugin.screening.client.cli.Login} class. Logging in on the
 * console will grant this class access to the openBIS server.</i>
 * 
 * @author Bernd Rinn
 */
public class OpenBISScreeningML
{
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
    public static final String REQUIRES_OPENBIS_AS_API = "1.2";

    /**
     * The required version ("major.minor") of the screening API on the openBIS datastore server.
     */
    public static final String REQUIRES_OPENBIS_DSS_API = "1.1";

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
        openbis = ScreeningOpenbisServiceFacadeFactory.tryCreate(user, password, url);
        if (openbis == null)
        {
            throw new RuntimeException("Login failed.");
        }
        init();
    }

    private static void init()
    {
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
        openbis = null;
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
     * @return Each row contains information about one plate:
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
        final Object[][] result = new Object[plates.size()][9];
        for (int i = 0; i < plates.size(); ++i)
        {
            final Object[] annotations =
                    new Object[]
                        { plates.get(i).getAugmentedCode(), plates.get(i).getPermId(),
                                plates.get(i).tryGetSpaceCode(), plates.get(i).getPlateCode(),
                                plates.get(i).getExperimentIdentifier().getAugmentedCode(),
                                plates.get(i).getExperimentIdentifier().getPermId(),
                                plates.get(i).getExperimentIdentifier().getSpaceCode(),
                                plates.get(i).getExperimentIdentifier().getProjectCode(),
                                plates.get(i).getExperimentIdentifier().getExperimentCode(), };
            System.arraycopy(annotations, 0, result[i], 0, annotations.length);
        }
        return result;
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
        final Object[][] result = new Object[experimentPlates.size()][9];
        for (int i = 0; i < experimentPlates.size(); ++i)
        {
            final Object[] annotations =
                    new Object[]
                        {
                                experimentPlates.get(i).getAugmentedCode(),
                                plates.get(i).getPermId(),
                                experimentPlates.get(i).tryGetSpaceCode(),
                                plates.get(i).getPlateCode(),
                                experimentPlates.get(i).getExperimentIdentifier()
                                        .getAugmentedCode(),
                                experimentPlates.get(i).getExperimentIdentifier().getPermId(),
                                experimentPlates.get(i).getExperimentIdentifier().getSpaceCode(),
                                experimentPlates.get(i).getExperimentIdentifier().getProjectCode(),
                                experimentPlates.get(i).getExperimentIdentifier()
                                        .getExperimentCode(), };
            System.arraycopy(annotations, 0, result[i], 0, annotations.length);
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
                openbis.listImageDatasets(experimentPlates);
        if (imageDatasets.isEmpty())
        {
            return new Object[0][];
        }
        final List<ImageDatasetMetadata> meta =
                openbis.listImageMetadata(Arrays.asList(imageDatasets.get(0)));
        if (meta.isEmpty())
        {
            return new Object[0][];
        }
        final List<String> channels = meta.get(0).getChannelNames();
        Object[][] result = new Object[channels.size()][1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i][0] = channels.get(i);
        }
        return result;
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
        final List<String> features =
                openbis.listAvailableFeatureNames(Arrays.asList(featureDatasets.get(0)));
        Object[][] result = new Object[features.size()][1];
        for (int i = 0; i < result.length; ++i)
        {
            result[i][0] = features.get(i);
        }
        return result;
    }

    //
    // Images
    //

    /**
     * Loads the TIFF images for the given well location and all channels and stores them in
     * temporary files. The temporary files will be removed automatically when the Java Virtual
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
     * Loads the TIFF images for the given well location and list of channels and stores them in
     * temporary files. The temporary files will be removed automatically when the Java Virtual
     * Machine exits.
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
        checkLoggedIn();
        final Plate plateId = plateCodeToPlateMap.get(plate);
        if (plateId == null)
        {
            throw new RuntimeException("No plate with that code found.");
        }
        final List<String> imageChannels;
        if (channels == null || channels.length == 0)
        {
            final List<ImageDatasetReference> imageDatasets =
                    openbis.listImageDatasets(Arrays.asList(plateId));
            final List<ImageDatasetMetadata> meta = openbis.listImageMetadata(imageDatasets);
            if (meta.isEmpty())
            {
                return new Object[][][]
                    { new Object[0][], new Object[0][] };
            }
            imageChannels = meta.get(0).getChannelNames();
        } else
        {
            imageChannels = Arrays.asList(channels);
        }
        final List<ImageDatasetReference> imageDatasets =
                openbis.listImageDatasets(Arrays.asList(plateId));
        if (imageDatasets.isEmpty())
        {
            return new Object[][][]
                { new Object[0][], new Object[0][] };
        }
        final List<PlateImageReference> imageReferences =
                new ArrayList<PlateImageReference>(imageDatasets.size());
        final List<ImageDatasetMetadata> meta = openbis.listImageMetadata(imageDatasets);
        if (meta.isEmpty())
        {
            return new Object[][][]
                { new Object[0][], new Object[0][] };
        }
        final List<File> imageFiles =
                new ArrayList<File>(imageDatasets.size() * imageChannels.size()
                        * meta.get(0).getNumberOfTiles());
        final Object[][][] result = new Object[2][][];
        result[0] =
                new Object[imageDatasets.size() * imageChannels.size()
                        * meta.get(0).getNumberOfTiles()][1];
        result[1] =
                new Object[imageDatasets.size() * imageChannels.size()
                        * meta.get(0).getNumberOfTiles()][15];
        int dsIdx = 0;
        int resultIdx = 0;
        for (ImageDatasetReference ds : imageDatasets)
        {
            final ImageDatasetMetadata m = meta.get(dsIdx);
            for (String channel : imageChannels)
            {
                for (int tile = 0; tile < m.getNumberOfTiles(); ++tile)
                {
                    final PlateImageReference ref =
                            new PlateImageReference(row, col, tile, channel, ds);
                    imageReferences.add(ref);
                    final File imageFile = createImageFileName(plateId, ref);
                    imageFiles.add(imageFile);
                    result[0][resultIdx][0] = imageFile.getPath();
                    final Object[] annotations =
                            new Object[]
                                { channel, tile,
                                        createPlateWellDescription(ds.getPlate(), row, col),
                                        ds.getPlate().getAugmentedCode(),
                                        ds.getPlate().getPermId(), ds.getPlate().tryGetSpaceCode(),
                                        ds.getPlate().getPlateCode(), row, col,
                                        ds.getExperimentIdentifier().getAugmentedCode(),
                                        ds.getExperimentIdentifier().getPermId(),
                                        ds.getExperimentIdentifier().getSpaceCode(),
                                        ds.getExperimentIdentifier().getProjectCode(),
                                        ds.getExperimentIdentifier().getExperimentCode(),
                                        ds.getPermId(), };
                    System.arraycopy(annotations, 0, result[1][resultIdx], 0, annotations.length);
                    resultIdx++;
                }
            }
            dsIdx++;
        }
        try
        {
            loadImages(imageReferences, imageFiles);
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
        return result;
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the specified files.<br>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the files fails
     */
    private static void loadImages(List<PlateImageReference> imageReferences,
            List<File> imageOutputFiles) throws IOException
    {
        final Map<PlateImageReference, OutputStream> imageRefToFileMap =
                createImageToFileMap(imageReferences, imageOutputFiles);
        try
        {
            openbis.loadImages(imageReferences, new IImageOutputStreamProvider()
                {
                    public OutputStream getOutputStream(PlateImageReference imageReference)
                            throws IOException
                    {
                        return imageRefToFileMap.get(imageReference);
                    }
                });
        } finally
        {
            closeOutputStreams(imageRefToFileMap.values());
        }
    }

    private static void closeOutputStreams(Collection<OutputStream> streams) throws IOException
    {
        for (OutputStream stream : streams)
        {
            stream.close();
        }
    }

    private static Map<PlateImageReference, OutputStream> createImageToFileMap(
            List<PlateImageReference> imageReferences, List<File> imageOutputFiles)
            throws FileNotFoundException
    {
        assert imageReferences.size() == imageOutputFiles.size() : "there should be one file specified for each image reference";
        final Map<PlateImageReference, OutputStream> map =
                new HashMap<PlateImageReference, OutputStream>();
        for (int i = 0; i < imageReferences.size(); i++)
        {
            OutputStream out =
                    new BufferedOutputStream(new FileOutputStream(imageOutputFiles.get(i)));
            map.put(imageReferences.get(i), out);
        }
        return map;
    }

    private static File createImageFileName(Plate plate, PlateImageReference image)
    {
        try
        {
            final WellPosition well = image.getWellPosition();
            final File f =
                    File.createTempFile("img_", "_" + plate.getPlateCode() + "_"
                            + image.getDatasetCode() + "_row" + well.getWellRow() + "_col"
                            + well.getWellColumn() + "_channel" + image.getChannel() + "_tile"
                            + image.getTile() + ".tiff");
            f.deleteOnExit();
            return f;
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
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
     * @param gene The gene name as stored as material code in openBIS
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
     * @param gene The gene name as stored as material code
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
                        MaterialTypeIdentifier.GENE, gene), (features == null) ? null : Arrays
                        .asList(features));
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
     * @param gene The gene name as stored as material code in openBIS
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
     * @param gene The gene name as stored as material code
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
                        MaterialTypeIdentifier.GENE, gene), (features == null) ? null : Arrays
                        .asList(features));
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
     * @param plate The gene name as stored as material code
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
     * @param plate The gene name as stored as material code
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
                openbis.loadFeaturesForPlates(Arrays.asList(PlateIdentifier
                        .createFromAugmentedCode(plate)), (features == null) ? null : Arrays
                        .asList(features));
        final List<String> featureNameList =
                featureVectors.get(featureVectors.size() - 1).getFeatureNames();
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
        result[0] = new Object[numberOfRows][featureNameList.size()];
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
        result[2] = new Object[featureNameList.size()][1];
        for (int i = 0; i < featureNameList.size(); ++i)
        {
            result[2][i][0] = featureNameList.get(i);
        }
        return result;
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
                openbis
                        .listPlateMaterialMapping(toPlates(platesCodes),
                                MaterialTypeIdentifier.GENE);
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
                    openbis = ScreeningOpenbisServiceFacadeFactory.tryCreate(token, serverUrl);
                    if (openbis == null)
                    {
                        throw new RuntimeException("Login failed.");
                    }
                    init();
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
