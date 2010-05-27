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

package eu.basysbio.cisd.dss;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.Properties;

import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.cifex.CifexExtractorHelper;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class LcaMicDataSetUploaderTest extends AbstractFileSystemTestCase
{
    private static FilenameFilter TXT_FILTER = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".txt");
            }
        };

    private static FilenameFilter PROPERTIES_FILTER = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.equals(CifexExtractorHelper.REQUEST_PROPERTIES_FILE);
            }
        };

    private Mockery context;
    private ITimeSeriesDAO dao;
    private IEncapsulatedOpenBISService service;
    private LcaMicDataSetUploader uploader;
    private IDropBoxFeeder feeder;
    private File dropBox;

    @BeforeMethod
    public void beforeMethod()
    {
        context = new Mockery();
        dao = context.mock(ITimeSeriesDAO.class);
        service = context.mock(IEncapsulatedOpenBISService.class);
        feeder = context.mock(IDropBoxFeeder.class);
        dropBox = new File(workingDirectory, "drop-box");
        dropBox.mkdirs();
        Properties properties = new Properties();
        properties.setProperty(
                TimeSeriesDataSetUploaderParameters.TIME_SERIES_DATA_SET_DROP_BOX_PATH, dropBox
                        .toString());
        uploader =
                new LcaMicDataSetUploader(dao, service, new TimeSeriesDataSetUploaderParameters(
                        properties));
    }
    
    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one does not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void test()
    {
        File tsvFile = new File(workingDirectory, "data.tsv");
        FileUtilities.writeToFile(tsvFile, LcaMicDataSetPropertiesExtractorTest.EXAMPLE 
                + "12\t2.5\t5.5\tN/A\n42\t42.5\t45.5\t3.25\n");
        
        DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setExperimentIdentifier(new ExperimentIdentifier("p1", "e1"));
        dataSetInformation.setDataSetCode("abc-1");
        dataSetInformation.setUploadingUserEmail("ab@c.de");
        uploader.handleTSVFile(tsvFile, dataSetInformation, feeder);
        
        List<String> data = getData(1);
        assertEquals("BBA ID\t" +
        		"Ma::MS::B1::12::EX::T1::NC::LcaMicCfd::Value[um]::LIN::NB::NC\t" +
        		"Ma::MS::B1::42::EX::T1::NC::LcaMicCfd::Value[um]::LIN::NB::NC", data.get(0));
        assertEquals("BBA9001#A_S20090325-2\t2.5\t42.5", data.get(1));
        checkProperties(1);
        data = getData(2);
        assertEquals("BBA ID\t" +
                "Ma::MS::B1::12::EX::T1::NC::LcaMicAbsFl::Mean[Au]::LIN::NB::NC\t" +
                "Ma::MS::B1::42::EX::T1::NC::LcaMicAbsFl::Mean[Au]::LIN::NB::NC", data.get(0));
        assertEquals("BBA9001#A_S20090325-2\t5.5\t45.5", data.get(1));
        checkProperties(2);
        data = getData(3);
        assertEquals("BBA ID\t" +
                "Ma::MS::B1::12::EX::T1::NC::LcaMicAbsFl::Std[Au]::LIN::NB::NC\t" +
                "Ma::MS::B1::42::EX::T1::NC::LcaMicAbsFl::Std[Au]::LIN::NB::NC", data.get(0));
        assertEquals("BBA9001#A_S20090325-2\tN/A\t3.25", data.get(1));
        checkProperties(3);
        context.assertIsSatisfied();
    }

    private List<String> getData(int number)
    {
        File ds = new File(dropBox, DataSetHandler.LCA_MIC_TIME_SERIES + number);
        List<String> data = FileUtilities.loadToStringList(ds.listFiles(TXT_FILTER)[0]);
        assertEquals(2, data.size());
        return data;
    }

    private void checkProperties(int number)
    {
        File ds = new File(dropBox, DataSetHandler.LCA_MIC_TIME_SERIES + number);
        List<String> data = FileUtilities.loadToStringList(ds.listFiles(PROPERTIES_FILTER)[0]);
        assertEquals(2, data.size());
        assertEquals("comment=null,p1/e1,abc-1,LCA_MIC_TIME_SERIES,TSV", data.get(0));
        assertEquals("user-email=ab@c.de", data.get(1));
    }
    
}
