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

package ch.systemsx.cisd.openbis.plugin.screening;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.lf5.util.StreamUtils;

import ch.systemsx.cisd.openbis.plugin.screening.client.api.v1.ScreeningOpenbisServiceFacade;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.IDatasetIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateImageReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.PlateSingleImage;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto.WellPosition;

/**
 * @author Tomasz Pylak
 */
public class ScreeningClientApiTest
{
    private static final String USER_ID = "a";

    private static final String USER_PASSWORD = "x";

    private static final String OPENBIS_SERVER_URL = "http://localhost:8888/openbis";

    public static void main(String[] args) throws IOException
    {
        ScreeningOpenbisServiceFacade facade =
                ScreeningOpenbisServiceFacade.tryCreate(USER_ID, USER_PASSWORD, OPENBIS_SERVER_URL);
        // List<Plate> plates = facade.listPlates();
        // System.out.println("Plates: " + plates);
        // List<ImageDatasetReference> imageDatasets = facade.listImageDatasets(plates);
        // System.out.println("Image datasets: " + imageDatasets);
        // List<FeatureVectorDatasetReference> featureVectorDatasets =
        // facade.listFeatureVectorDatasets(plates);
        // System.out.println("Feature vector datasets: " + featureVectorDatasets);

        // test for feature vector dataset
        // String datasetCode = "20091214153212961-474922"; // feature vector
        String datasetCode = "20091216162628729-475111"; // image
        IDatasetIdentifier datasetIdentifier = getDatasetIdentifier(facade, datasetCode);
        loadImages(facade, datasetIdentifier);

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
        for (int channel = 1; channel <= 2; channel++)
        {
            for (int tile = 4; tile <= 6; tile++)
            {
                List<PlateImageReference> imageRefs = new ArrayList<PlateImageReference>();
                imageRefs.add(new PlateImageReference(1, 3, tile, channel, datasetIdentifier));
                List<PlateSingleImage> images = facade.loadImages(imageRefs);
                saveImages(images);
            }
        }

    }

    private static void saveImages(List<PlateSingleImage> images) throws FileNotFoundException,
            IOException
    {
        for (PlateSingleImage image : images)
        {
            FileOutputStream out = new FileOutputStream(new File(createImageFileName(image)));
            InputStream in = image.getImage();
            StreamUtils.copyThenClose(in, out);
            out.close();
            in.close();
        }
    }

    private static String createImageFileName(PlateSingleImage image)
    {
        WellPosition well = image.getWellPosition();
        return "img_r" + well.getWellRow() + "_c" + well.getWellColumn() + "_ch"
                + image.getChannel() + "_t" + image.getTile() + ".png";
    }
}
