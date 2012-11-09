/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.datastoreserver.systemtests;

import java.io.File;
import java.io.IOException;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.EntityHelper;

/**
 * 
 *
 * @author Jakub Straszewski
 */
public class VocabularyDropboxApiSystemTest  extends SystemTestCase
{
    // for jython script locate
    // vocabularies-api.py

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-vocabularies-api-test");
    }

    @Test
    public void testControlledVocabulariesApi() throws Exception
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        runDropbox();

        Sample sample =
                openBISService
.tryGetSampleWithExperiment(SampleIdentifier.create("VOC",
                        "CELL_PLATE"));

        IEntityProperty property = EntityHelper.tryFindProperty(sample.getProperties(), "ORGANISM");

        assertEquals("RAT", property.getVocabularyTerm().getCode());

    }

    private void runDropbox() throws IOException, Exception
    {
        File exampleDataSet = new File(workingDirectory, "my-data");
        createExampleDataSet(exampleDataSet);
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
        FileUtilities.writeToFile(new File(exampleDataSet, "file.txt"), "hello world");
    }

}
