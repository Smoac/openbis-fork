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
import java.util.Collections;

import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.ICommonServer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class MicroscopyImageDropboxTest extends AbstractImageDropboxTestCase
{

    @Override
    protected void registerAdditionalOpenbisMetaData()
    {
        System.out.println("MicroscopyImageDropboxTest.registerAdditionalOpenbisMetaData()");
        commonServer = (ICommonServer) applicationContext
                .getBean(ch.systemsx.cisd.openbis.generic.shared.ResourceNames.COMMON_SERVER);
        sessionToken = commonServer.tryAuthenticate("admin", "a").getSessionToken();
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("MICROSCOPY_EXPERIMENT");
        commonServer.registerExperimentType(sessionToken, experimentType);
        SampleType sampleType = new SampleType();
        sampleType.setCode("MICROSCOPY_SAMPLE");
        sampleType.setGeneratedCodePrefix("M-");
        commonServer.registerSampleType(sessionToken, sampleType);
        DataSetType dataSetType = new DataSetType("MICROSCOPY_IMG");
        dataSetType.setDataSetKind(DataSetKind.PHYSICAL);
        commonServer.registerDataSetType(sessionToken, dataSetType);
        dataSetType = new DataSetType("MICROSCOPY_IMG_CONTAINER");
        dataSetType.setDataSetKind(DataSetKind.CONTAINER);
        commonServer.registerDataSetType(sessionToken, dataSetType);
        commonServer.registerSpace(sessionToken, "TEST", null);
        commonServer.registerProject(sessionToken, ProjectIdentifierFactory.parse("/TEST/TEST-PROJECT"), "", 
                null, Collections.<NewAttachment>emptySet());
    }

    @Override
    protected String getDataFolderToDrop()
    {
        return "aarons_example";
    }
    
    @Test
    public void test()
    {
        AbstractExternalData dataSet = getRegisteredContainerDataSet();
        ImageChecker imageChecker = new ImageChecker();
        imageChecker.check(new File(getTestDataFolder(), "Merged_Default.png"), 
                new ImageLoader(dataSet, sessionToken));
        imageChecker.check(new File(getTestDataFolder(), "Merged_256x256.png"), 
                new ImageLoader(dataSet, sessionToken).mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "Merged_512x512.png"), 
                new ImageLoader(dataSet, sessionToken).mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "C1_Default.png"), 
                new ImageLoader(dataSet, sessionToken).channel("SERIES-0-CHANNEL-1"));
        imageChecker.check(new File(getTestDataFolder(), "C1_256x256.png"), 
                new ImageLoader(dataSet, sessionToken).channel("SERIES-0-CHANNEL-1").mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "C1_512x512.png"), 
                new ImageLoader(dataSet, sessionToken).channel("SERIES-0-CHANNEL-1").mode("thumbnail512x512"));
        imageChecker.check(new File(getTestDataFolder(), "C01_Default.png"), 
                new ImageLoader(dataSet, sessionToken).channel("SERIES-0-CHANNEL-0").channel("SERIES-0-CHANNEL-1"));
        imageChecker.check(new File(getTestDataFolder(), "C01_256x256.png"), 
                new ImageLoader(dataSet, sessionToken).channel("SERIES-0-CHANNEL-0").channel("SERIES-0-CHANNEL-1").mode("thumbnail256x256"));
        imageChecker.check(new File(getTestDataFolder(), "C01_512x512.png"), 
                new ImageLoader(dataSet, sessionToken).channel("SERIES-0-CHANNEL-0").channel("SERIES-0-CHANNEL-1").mode("thumbnail512x512"));

    }

}
