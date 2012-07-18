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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TrackingDataSetCriteria;

/**
 * Test the java-based dropbox implementation
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class JavaDropboxSystemTest extends SystemTestCase
{

    // for configuration go to sourceTest/core-plugins/generic-test/1/dss/drop-boxes/java-dropbox

    File emailDirectory = new File(new File(new File(workingDirectory, "SystemTests"), "dss-root"),
            "email");

    @Override
    protected File getIncomingDirectory()
    {
        return new File(rootDir, "incoming-java-dropbox");
    }

    private HashSet<String> preexistingDataSetCodes;

    private void preparePreexistingDataSetCodes()
    {
        preexistingDataSetCodes = new HashSet<String>();
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<ExternalData> dataSets = listAllYoungDataSets(openBISService);
        for (ExternalData dto : dataSets)
        {
            preexistingDataSetCodes.add(dto.getCode());
        }
    }

    private LinkedList<String> newDataSetCodes()
    {
        LinkedList<String> newCodes = new LinkedList<String>();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        List<ExternalData> dataSets = listAllYoungDataSets(openBISService);
        for (ExternalData dto : dataSets)
        {
            String code = dto.getCode();

            if (false == preexistingDataSetCodes.contains(code))
            {
                newCodes.add(code);
            }
        }
        return newCodes;
    }

    private List<ExternalData> listAllYoungDataSets(IEncapsulatedOpenBISService openBISService)
    {
        List<ExternalData> dataSets =
                openBISService.listNewerDataSets(new TrackingDataSetCriteria(24));
        return dataSets;
    }

    @Test
    public void testJavaDropbox() throws Exception
    {
        preparePreexistingDataSetCodes();

        File exampleDataSet = new File(workingDirectory, "my-data");
        createExampleDataSet(exampleDataSet);
        moveFileToIncoming(exampleDataSet);
        waitUntilDataSetImported();

        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();

        List<String> codes = newDataSetCodes();

        List<ExternalData> x = openBISService.listDataSetsByCode(codes);

        assertEquals("Exactly one dataset should have been imported.", 1, x.size());

        for (ExternalData a : x)
        {
            assertEquals("/CISD/JAVA-TEST", a.getSampleIdentifier());
            assertEquals("/CISD/NEMO/EXP-TEST-1", a.getExperiment().getIdentifier());
        }
    }

    private void createExampleDataSet(File exampleDataSet)
    {
        exampleDataSet.mkdirs();
    }

}
