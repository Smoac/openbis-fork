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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetRelatedEntities;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;

/**
 *
 * @author Franz-Josef Elmer
 */
public abstract class AbstractImageDropboxTestCase extends AbstractScreeningSystemTestCase
{
    protected ImageChecker imageChecker;

    @BeforeTest
    public void dropAnExampleDataSet() throws Exception
    {
        registerAdditionalOpenbisMetaData();
        File exampleDataSet = createTestDataContents();
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported(FINISHED_POST_REGISTRATION_CONDITION);
    }
    
    @BeforeMethod
    public void setUpImageChecker()
    {
        imageChecker = new ImageChecker(new File("tmp/wrong_images/" + getClass().getSimpleName()));
    }
    
    @AfterMethod
    public void assertImageChecker()
    {
        imageChecker.assertNoFailures();
    }
    
    protected void registerAdditionalOpenbisMetaData()
    {
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
        return 120;
    }

    protected AbstractExternalData getRegisteredContainerDataSet()
    {
        List<AbstractExternalData> dataSets = getRegisteredDataSets();
        for (AbstractExternalData dataSet : dataSets)
        {
            if (dataSet.getDataSetType().getDataSetKind().equals(DataSetKind.CONTAINER))
            {
                return dataSet;
            }
        }
        fail("No container data set found: " + dataSets);
        return null; // never reached but needed for the compiler
    }

    protected List<AbstractExternalData> getRegisteredDataSets()
    {
        String code = translateIntoCamelCase(getClass().getSimpleName()).toUpperCase();
        ExperimentIdentifier identifier = ExperimentIdentifierFactory.parse("/TEST/TEST-PROJECT/" + code);
        Experiment experiment = commonServer.getExperimentInfo(sessionToken, identifier);
        List<AbstractExternalData> dataSets 
                = commonServer.listRelatedDataSets(sessionToken, new DataSetRelatedEntities(Arrays.asList(experiment)), false);
        return dataSets;
    }
    
    private String translateIntoCamelCase(String string)
    {
        StringBuilder builder = new StringBuilder();
        for (char c : string.toCharArray())
        {
            if (Character.isUpperCase(c))
            {
                if (builder.length() > 0)
                {
                    builder.append('_');
                }
                builder.append(Character.toLowerCase(c));
            } else
            {
                builder.append(c);
            }
        }
        return builder.toString();
    }

}
