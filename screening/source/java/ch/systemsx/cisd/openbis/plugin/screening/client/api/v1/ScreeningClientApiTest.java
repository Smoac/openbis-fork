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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.lf5.util.StreamUtils;

import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetMetadata;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.ImageDatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.Plate;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateSingleImage;
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
            System.err
                    .println("Example parameters: test-user my-password http://localhost:8888/openbis/openbis");
            return;
        }
        String userId = args[0];
        String userPassword = args[1];
        String serverUrl = args[2];

        System.out.println(String.format("Connecting to the server '%s' as a user '%s.", serverUrl,
                userId));
        ScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacade.tryCreate(userId, userPassword, serverUrl);
        if (facade == null)
        {
            System.err.println("Cannot connect with the server, is it running?");
            return;
        }
        List<Plate> plates = facade.listPlates();
        System.out.println("Plates: " + plates);
        List<ImageDatasetReference> imageDatasets = facade.listImageDatasets(plates);
        System.out.println("Image datasets: " + imageDatasets);
        List<FeatureVectorDatasetReference> featureVectorDatasets =
                facade.listFeatureVectorDatasets(plates);
        System.out.println("Feature vector datasets: " + featureVectorDatasets);

        // test for feature vector dataset
        String featureVectorDatasetCode = featureVectorDatasets.get(0).getDatasetCode(); // feature
        // vector
        IDatasetIdentifier datasetIdentifier =
                getDatasetIdentifier(facade, featureVectorDatasetCode);
        loadImages(facade, datasetIdentifier);

        String imageDatasetCode = imageDatasets.get(0).getDatasetCode(); // image
        datasetIdentifier = getDatasetIdentifier(facade, imageDatasetCode);
        loadImages(facade, datasetIdentifier);

        List<String> featureNames = facade.listAvailableFeatureNames(featureVectorDatasets);
        System.out.println("Feature names: " + featureNames);
        List<FeatureVectorDataset> features =
                facade.loadFeatures(featureVectorDatasets, featureNames);
        System.out.println("Features: " + features);

        List<ImageDatasetMetadata> imageMetadata = facade.listImageMetadata(imageDatasets);
        System.out.println("Image metadata: " + imageMetadata);

        facade.logout();
    }

    private static IDatasetIdentifier getDatasetIdentifier(ScreeningOpenbisServiceFacade facade,
            String datasetCode)
    {
        IDatasetIdentifier datasetIdentifier =
                facade.getDatasetIdentifiers(Arrays.asList(datasetCode)).get(0);
        return datasetIdentifier;
    }

    private static void loadImages(ScreeningOpenbisServiceFacade facade,
            IDatasetIdentifier datasetIdentifier) throws FileNotFoundException, IOException
    {
        for (int well = 1; well <= 5; well++)
        {
            for (int channel = 1; channel <= 2; channel++)
            {
                for (int tile = 1; tile <= 1; tile++)
                {
                    List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
                    imageRefs.add(new PlateImageReference(well, well, tile, channel,
                            datasetIdentifier));
                    List<PlateSingleImage> images = facade.loadImages(imageRefs);
                    saveImages(images, datasetIdentifier.getDatasetCode());
                }
            }
        }
    }

    private static void saveImages(List<PlateSingleImage> images, String dirName)
            throws FileNotFoundException, IOException
    {
        File dir = new File(dirName);
        dir.mkdir();
        for (PlateSingleImage image : images)
        {
            FileOutputStream out = new FileOutputStream(new File(dir, createImageFileName(image)));
            InputStream in = image.getImage();
            StreamUtils.copyThenClose(in, out);
            out.close();
            in.close();
        }
    }

    private static String createImageFileName(PlateSingleImage image)
    {
        WellPosition well = image.getWellPosition();
        return "img_row" + well.getWellRow() + "_col" + well.getWellColumn() + "_channel"
                + image.getChannel() + "_tile" + image.getTile() + ".png";
    }
}
