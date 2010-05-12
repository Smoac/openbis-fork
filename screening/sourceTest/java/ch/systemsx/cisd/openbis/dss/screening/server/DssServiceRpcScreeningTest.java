/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.screening.server;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDataset;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetReference;

/**
 * Test cases for the {@link DssServiceRpcScreening}.
 * 
 * @author Piotr Buczek
 */
public class DssServiceRpcScreeningTest extends AbstractFileSystemTestCase
{

    private static final String EXAMPLE1 =
            "wellName;f0;f1;f2\n" + "A01;1.0;1.1;1.2\n" + "C7;2.0;2.1;2.2\n" + "D7;3.0;3.1;3.2\n"
                    + "D8;A3.0;3.1;3.2\n";

    private static final String EXAMPLE2 =
            "row;col;f0;f1;f2\n" + "A;01;1.0;1.1;1.2\n" + "C;7;2.0;2.1;2.2\n" + "D;7;3.0;3.1;3.2\n"
                    + "D;8;A3.0;3.1;3.2\n";

    @Test
    public void testExtractFeatureNames()
    {
        File datasetFile = new File(workingDirectory, "scrDataset");
        FileUtilities.writeToFile(datasetFile, EXAMPLE1);
        try
        {
            String[] names = DssServiceRpcScreening.extractFeatureNames(datasetFile);
            assertEquals("[f0, f1, f2]", Arrays.toString(names));
        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    @Test
    public void testCreateFeatureVectorDataset()
    {
        File datasetFile = new File(workingDirectory, "scrDataset");
        final String datasetCode = "CODE";
        final String url = "URL";
        final String[] fileData =
            { EXAMPLE1, EXAMPLE2 };
        try
        {
            for (String data : fileData)
            {
                FileUtilities.writeToFile(datasetFile, data);
                FeatureVectorDatasetReference dataset =
                        createFeatureVectorDatasetReference(datasetCode, url);
                FeatureVectorDataset result =
                        DssServiceRpcScreening.createFeatureVectorDataset(datasetFile, dataset,
                                Arrays.asList(new String[]
                                    { "f2", "f0", "f3" }));

                assertEquals(dataset, result.getDataset());
                assertEquals("[f2, f0]", result.getFeatureNames().toString());
                assertEquals(3, result.getFeatureVectors().size());

                assertEquals("[1, 1]", result.getFeatureVectors().get(0).getWellPosition()
                        .toString());
                assertEquals("[1.2, 1.0]", Arrays.toString(result.getFeatureVectors().get(0)
                        .getValues()));

                assertEquals("[3, 7]", result.getFeatureVectors().get(1).getWellPosition()
                        .toString());
                assertEquals("[2.2, 2.0]", Arrays.toString(result.getFeatureVectors().get(1)
                        .getValues()));

                assertEquals("[4, 7]", result.getFeatureVectors().get(2).getWellPosition()
                        .toString());
                assertEquals("[3.2, 3.0]", Arrays.toString(result.getFeatureVectors().get(2)
                        .getValues()));
            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }

    private FeatureVectorDatasetReference createFeatureVectorDatasetReference(
            final String datasetCode, final String url)
    {
        return new FeatureVectorDatasetReference(datasetCode, url, null, null);
    }

}
