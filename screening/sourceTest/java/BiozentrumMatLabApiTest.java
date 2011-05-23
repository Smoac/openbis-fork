/*
 * Copyright 2011 ETH Zuerich, CISD
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

/**
 * Integration tests of MatLab API on Biozentrum server.
 * 
 * @author Tomasz Pylak
 */
public class BiozentrumMatLabApiTest
{
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            System.err
                    .println("Specify the password and optionally the plate identifier, mount point and dataset type pattern!");
        }
        String passwd = args[0];
        String chosenPlate = "/TEST/KB01";
        String mountPoint = null;
        String datasetTypePattern = "HCS_ANALYSIS_CELL_CLASSIFICATIONS_MAT";
        if (args.length > 1)
        {
            chosenPlate = args[1];
        }
        if (args.length > 2)
        {
            mountPoint = args[2];
        }

        OpenBISScreeningML.login("admin", passwd, "http://bc2-openbis01.bc2.unibas.ch:8443");

        Object[][] meta = OpenBISScreeningML.getImagesMetadata(chosenPlate);
        System.out.println(String.format("Number of tiles: %s. Plate geometry %sx%s", meta[1][3],
                meta[1][6], meta[1][7]));

        // fetch 3ed tile of well (1,1). The second call to fetch this image will return it from the
        // local cache.
        // The last optional parameter can specify a list of channels to load (otherwise all
        // channels are loaded).
        Object[][][] images = OpenBISScreeningML.loadImages(chosenPlate, 1, 1, 3);
        System.out.println("Image path: " + images[1][1]);

        // properties of well (2,4)
        Object[][] props = OpenBISScreeningML.getWellProperties(chosenPlate, 2, 4);
        for (int i = 0; i < props.length; i++)
        {
            System.out.println(String.format("Property %s = %s ", props[i][0], props[i][1]));
        }

        // save description of the well (2,4)
        props = new Object[][]
            {
                { "DESCRIPTION", "hello example" } };
        OpenBISScreeningML.updateWellProperties(chosenPlate, 2, 4, props);

        // Loads dataset with classification results.
        // OpenBIS store diretcory is mounted locally in "/mount/openbis/store", so no data are
        // copied and just a path to the appropriate location
        // is returned.
        Object[][] datasets =
                OpenBISScreeningML.loadDataSets(chosenPlate, datasetTypePattern, mountPoint);
        System.out.println("Path to the first downloaded dataset: " + datasets[0][1]);
    }
}
