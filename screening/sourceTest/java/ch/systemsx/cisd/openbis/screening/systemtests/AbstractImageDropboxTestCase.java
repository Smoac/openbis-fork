/*
 * Copyright 2014 ETH Zuerich, SIS
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

package ch.systemsx.cisd.openbis.screening.systemtests;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.BeforeTest;

/**
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractImageDropboxTestCase extends AbstractScreeningSystemTestCase
{
    @BeforeTest
    public void dropAnExampleDataSet() throws Exception
    {
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported(FINISHED_POST_REGISTRATION_CONDITION);
    }

    private File createTestDataContents() throws IOException
    {
        File destination = new File(workingDirectory, "test-data");
        destination.mkdirs();
        FileUtils.copyDirectory(new File(getTestDataFolder(), getDataFolderToDrop()), destination);
        return destination;
    }

    protected abstract String getDataFolderToDrop();

    protected String getTestDataFolder()
    {
        return "../screening/resource/test-data/" + getClass().getSimpleName();
    }

    @Override
    protected int dataSetImportWaitDurationInSeconds()
    {
        return 60;
    }
    

}
