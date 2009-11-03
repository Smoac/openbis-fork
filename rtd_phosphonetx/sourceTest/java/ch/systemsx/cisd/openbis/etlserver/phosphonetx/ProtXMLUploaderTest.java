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

package ch.systemsx.cisd.openbis.etlserver.phosphonetx;

import java.io.File;
import java.util.Properties;

import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummary;
import ch.systemsx.cisd.openbis.etlserver.phosphonetx.dto.ProteinSummaryHeader;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class ProtXMLUploaderTest extends ProtXMLTestCase
{
    private static final class MockUploader extends ProtXMLUploader
    {
        private final IEncapsulatedOpenBISService openbisService;
        private DataSetInformation dataSetInformation;
        private ProteinSummary proteinSummary;
        
        public MockUploader(Properties properties, IEncapsulatedOpenBISService openbisService)
        {
            super(properties, openbisService);
            this.openbisService = openbisService;
        }

        @Override
        protected ResultDataSetUploader createUploader()
        {
            return new ResultDataSetUploader(null, null, null)
                {
                    @Override
                    void upload(DataSetInformation dataSetInfo, ProteinSummary summary)
                    {
                        dataSetInformation = dataSetInfo;
                        proteinSummary = summary;
                    }
                };
        }

        public IEncapsulatedOpenBISService getService()
        {
            return openbisService;
        }
        
        public DataSetInformation getDataSetInformation()
        {
            return dataSetInformation;
        }

        public ProteinSummary getProteinSummary()
        {
            return proteinSummary;
        }
    }
    
    private Mockery context;
    private IEncapsulatedOpenBISService service;
    private MockUploader uploader;

    @BeforeMethod
    public void startUp()
    {
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        Properties properties = new Properties();
        properties.setProperty("database.kind", "test");
        uploader = new MockUploader(properties, service);
    }
    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testDataSetIsProtXMLFile()
    {
        File file = new File(workingDirectory, "test.xml");
        FileUtilities.writeToFile(file, EXAMPLE);
        DataSetInformation dataSetInformation = new DataSetInformation();
        
        uploader.upload(file, dataSetInformation);
        
        assertSame(service, uploader.getService());
        assertSame(dataSetInformation, uploader.getDataSetInformation());
        ProteinSummaryHeader header = uploader.getProteinSummary().getSummaryHeader();
        assertEquals("some/path/uniprot.HUMAN.v125.fasta", header.getReferenceDatabase());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDataSetIsFolderWithProtXMLFile()
    {
        File dataSet = new File(workingDirectory, "data-set");
        dataSet.mkdir();
        FileUtilities.writeToFile(new File(dataSet, "test-prot.xml"), EXAMPLE);
        DataSetInformation dataSetInformation = new DataSetInformation();
        
        uploader.upload(dataSet, dataSetInformation);
        
        assertSame(service, uploader.getService());
        assertSame(dataSetInformation, uploader.getDataSetInformation());
        ProteinSummaryHeader header = uploader.getProteinSummary().getSummaryHeader();
        assertEquals("some/path/uniprot.HUMAN.v125.fasta", header.getReferenceDatabase());
        context.assertIsSatisfied();
    }
    
    @Test
    public void testDataSetIsFolderWithoutProtXMLFile()
    {
        File dataSet = new File(workingDirectory, "data-set");
        dataSet.mkdir();
        FileUtilities.writeToFile(new File(dataSet, "pep.xml"), "peptides");
        DataSetInformation dataSetInformation = new DataSetInformation();
        
        try
        {
            uploader.upload(dataSet, dataSetInformation);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "No *prot.xml file found in data set "
                            + "'targets/unit-test-wd/ch.systemsx.cisd.openbis.etlserver.phosphonetx.ProtXMLUploaderTest/data-set'.",
                    ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
}
