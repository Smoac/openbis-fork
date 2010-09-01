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

package ch.systemsx.cisd.openbis.plugin.screening.client.api.v1;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade.IImageOutputStreamProvider;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.DatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorWithDescription;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Geometry;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IImageDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.MaterialTypeIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateWellReferenceWithDatasets;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;

/**
 * A test class which shows how to use API.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningClientApiTest
{
    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            System.err.println("Usage: <user> <password> <openbis-server-url>");
            System.err.println("Example parameters: test-user my-password http://localhost:8888");
            return;
        }
        configureLogging();

        String userId = args[0];
        String userPassword = args[1];
        String serverUrl = args[2];

        System.out.println(String.format("Connecting to the server '%s' as a user '%s.", serverUrl,
                userId));
        IScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacadeFactory.tryCreate(userId, userPassword, serverUrl);
        if (facade == null)
        {
            System.err.println("Authentication failed: check the user name and password.");
            return;
        }
        List<ExperimentIdentifier> experiments = facade.listExperiments();
        System.out.println("Experiments: " + experiments);

        MaterialIdentifier gene = new MaterialIdentifier(MaterialTypeIdentifier.GENE, "OPRS1");
        ExperimentIdentifier experimentIdentifer = experiments.get(0);
        List<PlateWellReferenceWithDatasets> plateWells =
                facade.listPlateWells(experimentIdentifer, gene, true);
        System.out.println(String.format("Wells with gene '%s' in experiment '%s': %s", gene,
                experimentIdentifer, plateWells));

        List<FeatureVectorWithDescription> featuresForPlateWells =
                facade.loadFeaturesForPlateWells(experimentIdentifer, gene, null);
        System.out.println("Features for wells: " + featuresForPlateWells);

        List<FeatureVectorWithDescription> featuresForPlateWellsCheck =
                facade.loadFeaturesForDatasetWellReferences(facade
                        .convertToFeatureVectorDatasetWellIdentifier(plateWells), null);

        if (featuresForPlateWellsCheck.equals(featuresForPlateWells) == false)
        {
            throw new IllegalStateException(String.format(
                    "Inconsistent results to fetch feature vectors, expected:\n%s\nbut got:\n%s",
                    featuresForPlateWells, featuresForPlateWellsCheck));
        }

        List<Plate> plates = facade.listPlates();
        System.out.println("Plates: " + plates);
        List<ImageDatasetReference> imageDatasets = facade.listImageDatasets(plates);
        System.out.println("Image datasets: " + imageDatasets);
        List<FeatureVectorDatasetReference> featureVectorDatasets =
                facade.listFeatureVectorDatasets(plates);
        System.out.println("Feature vector datasets: " + featureVectorDatasets);
        loadImages(facade, getFirstFive(facade, imageDatasets));
        List<String> featureCodes = facade.listAvailableFeatureCodes(featureVectorDatasets);
        System.out.println("Feature codes: " + featureCodes);
        List<FeatureVectorDataset> features =
                facade.loadFeatures(featureVectorDatasets, featureCodes);
        System.out.println("Features: " + features);

        Map<String, List<ImageDatasetReference>> imageDataSetReferencesPerDss =
                new HashMap<String, List<ImageDatasetReference>>();
        for (ImageDatasetReference imageDataset : imageDatasets)
        {
            String url = imageDataset.getDatastoreServerUrl();
            List<ImageDatasetReference> list = imageDataSetReferencesPerDss.get(url);
            if (list == null)
            {
                list = new ArrayList<ImageDatasetReference>();
                imageDataSetReferencesPerDss.put(url, list);
            }
            list.add(imageDataset);
        }
        Collection<List<ImageDatasetReference>> bundle = imageDataSetReferencesPerDss.values();
        for (List<ImageDatasetReference> imageDataSets : bundle)
        {
            List<ImageDatasetMetadata> imageMetadata = facade.listImageMetadata(imageDataSets);
            System.out.println("Image metadata: " + imageMetadata);
        }
        loadImagesFromFeatureVectors(facade, getFirstFive(facade, featureVectorDatasets));

        facade.logout();
    }

    private static <T extends DatasetIdentifier> List<T> getFirstFive(
            IScreeningOpenbisServiceFacade facade, List<T> identfiers)
    {
        List<T> result = new ArrayList<T>();
        for (int i = 0; i < Math.min(5, identfiers.size()); i++)
        {
            T ident = identfiers.get(i);
            result.add(ident);
            IDatasetIdentifier fetchedIdent = getDatasetIdentifier(facade, ident.getDatasetCode());
            if (fetchedIdent.getPermId().equals(ident.getPermId()) == false)
            {
                throw new IllegalStateException(
                        "Fetched dataset identifier is not the same as the expected one. It is "
                                + fetchedIdent + " instead of " + ident);
            }
        }
        return result;
    }

    private static IDatasetIdentifier getDatasetIdentifier(IScreeningOpenbisServiceFacade facade,
            String datasetCode)
    {
        IDatasetIdentifier datasetIdentifier =
                facade.getDatasetIdentifiers(Arrays.asList(datasetCode)).get(0);
        return datasetIdentifier;
    }

    private static void loadImages(IScreeningOpenbisServiceFacade facade,
            List<ImageDatasetReference> datasetIdentifiers) throws FileNotFoundException,
            IOException
    {
        List<PlateImageReference> imageRefs = createAllImagesReferences(facade, datasetIdentifiers);
        List<File> imageFiles = createImageFiles(imageRefs);

        loadImages(facade, imageRefs, imageFiles);
    }

    private static List<PlateImageReference> createAllImagesReferences(
            IScreeningOpenbisServiceFacade facade, List<ImageDatasetReference> datasetIdentifiers)
    {
        Map<IImageDatasetIdentifier, ImageDatasetMetadata> metadataMap =
                fetchMetadataMap(facade, datasetIdentifiers);
        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        for (ImageDatasetReference datasetIdentifier : datasetIdentifiers)
        {
            ImageDatasetMetadata metadata = metadataMap.get(datasetIdentifier);
            List<PlateImageReference> datasetImageRefs =
                    createWholePlateImageReferences(metadata, datasetIdentifier);
            imageRefs.addAll(datasetImageRefs);
        }
        return imageRefs;
    }

    private static List<File> createImageFiles(List<PlateImageReference> imageRefs)
    {
        List<File> imageFiles = new ArrayList<File>();
        for (PlateImageReference imageRef : imageRefs)
        {
            File dir = new File(imageRef.getDatasetCode());
            dir.mkdir();
            imageFiles.add(new File(dir, createImageFileName(imageRef)));
        }
        return imageFiles;
    }

    private static List<PlateImageReference> createWholePlateImageReferences(
            ImageDatasetMetadata metadata, ImageDatasetReference datasetIdentifier)
    {
        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        Geometry plateGeometry = datasetIdentifier.getPlateGeometry();
        for (int wellRow = 1; wellRow <= plateGeometry.getNumberOfRows(); wellRow++)
        {
            for (int wellCol = 1; wellCol <= plateGeometry.getNumberOfColumns(); wellCol++)
            {
                for (String channel : metadata.getChannelCodes())
                {
                    for (int tile = 0; tile < metadata.getNumberOfTiles(); tile++)
                    {
                        PlateImageReference imageRef =
                                new PlateImageReference(wellRow, wellCol, tile, channel,
                                        datasetIdentifier);
                        imageRefs.add(imageRef);
                    }
                }
            }
        }
        return imageRefs;
    }

    private static Map<IImageDatasetIdentifier, ImageDatasetMetadata> fetchMetadataMap(
            IScreeningOpenbisServiceFacade facade, List<ImageDatasetReference> datasetIdentifiers)
    {
        Map<IImageDatasetIdentifier, ImageDatasetMetadata> map =
                new HashMap<IImageDatasetIdentifier, ImageDatasetMetadata>();
        List<ImageDatasetMetadata> metadatum = facade.listImageMetadata(datasetIdentifiers);
        for (ImageDatasetMetadata metadata : metadatum)
        {
            map.put(metadata.getImageDataset(), metadata);
        }
        return map;
    }

    private static void loadImagesFromFeatureVectors(IScreeningOpenbisServiceFacade facade,
            List<FeatureVectorDatasetReference> datasetIdentifiers) throws FileNotFoundException,
            IOException
    {
        List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
        List<File> imageFiles = new ArrayList<File>();
        for (IDatasetIdentifier datasetIdentifier : datasetIdentifiers)
        {
            File dir = new File(datasetIdentifier.getDatasetCode());
            dir.mkdir();

            PlateImageReference imageRef =
                    new PlateImageReference(1, 1, 0, "DAPI", datasetIdentifier);
            imageRefs.add(imageRef);
            imageFiles.add(new File(dir, createImageFileName(imageRef)));
        }
        loadImages(facade, imageRefs, imageFiles);
    }

    /**
     * Saves images for a given list of image references (given by data set code, well position,
     * channel and tile) in the specified files.<br>
     * The number of image references has to be the same as the number of files.
     * 
     * @throws IOException when reading images from the server or writing them to the files fails
     */
    private static void loadImages(IScreeningOpenbisServiceFacade facade,
            List<PlateImageReference> imageReferences, List<File> imageOutputFiles)
            throws IOException
    {
        final Map<PlateImageReference, OutputStream> imageRefToFileMap =
                createImageToFileMap(imageReferences, imageOutputFiles);
        try
        {
            facade.loadImages(imageReferences, new IImageOutputStreamProvider()
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
        Map<PlateImageReference, OutputStream> map =
                new HashMap<PlateImageReference, OutputStream>();
        for (int i = 0; i < imageReferences.size(); i++)
        {
            OutputStream out =
                    new BufferedOutputStream(new FileOutputStream(imageOutputFiles.get(i)));
            map.put(imageReferences.get(i), out);
        }
        return map;
    }

    private static String createImageFileName(PlateImageReference image)
    {
        WellPosition well = image.getWellPosition();
        return "img_row" + well.getWellRow() + "_col" + well.getWellColumn() + "_"
                + image.getChannel() + "_tile" + image.getTile() + ".png";
    }

    private static void configureLogging()
    {
        Properties props = new Properties();
        props.put("log4j.appender.STDOUT", "org.apache.log4j.ConsoleAppender");
        props.put("log4j.appender.STDOUT.layout", "org.apache.log4j.PatternLayout");
        props.put("log4j.appender.STDOUT.layout.ConversionPattern", "%d %-5p [%t] %c - %m%n");
        props.put("log4j.rootLogger", "INFO, STDOUT");
        PropertyConfigurator.configure(props);
    }
}
