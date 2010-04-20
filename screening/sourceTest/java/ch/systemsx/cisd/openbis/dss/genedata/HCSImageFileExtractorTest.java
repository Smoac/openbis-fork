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

package ch.systemsx.cisd.openbis.dss.genedata;

import java.io.IOException;
import java.util.Arrays;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.AssertJUnit;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.etlserver.HCSImageFileExtractionResult;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
@Friend(toClasses=HCSImageFileExtractor.class)
public class HCSImageFileExtractorTest extends AssertJUnit
{
    private Mockery context;

    private IHCSImageFileAccepter fileAccepter;

    private HCSImageFileExtractor extractor;

    private IDirectory directory;

    private DataSetInformation dataSetInformation;

    private IFile file1;

    private IFile file2;
    
    @BeforeMethod
    public final void setUp() throws IOException
    {
        context = new Mockery();
        directory = context.mock(IDirectory.class);
        file1 = createImageFileMock("001001000-0.jpeg");
        file2 = createImageFileMock("002005000-1.jpeg");
        fileAccepter = context.mock(IHCSImageFileAccepter.class);
        extractor = new HCSImageFileExtractor(new WellGeometry(1, 1));
        dataSetInformation = new DataSetInformation();
    }
    
    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }
    
    @Test
    public void test()
    {
        context.checking(new Expectations()
            {
                {
                    one(directory).listFiles(null, false);
                    will(returnValue(Arrays.asList(file1, file2)));
                    
                    one(fileAccepter).accept(1, new Location(1, 1), new Location(1, 1), file1);
                    one(fileAccepter).accept(2, new Location(2, 5), new Location(1, 1), file2);
                }
            });
        
        HCSImageFileExtractionResult result = extractor.process(directory, dataSetInformation, fileAccepter);
        
        assertEquals("[channel1[1=1], channel2[2=2]]", result.getChannels().toString());
        assertEquals(2, result.getTotalFiles());
        assertEquals(0, result.getInvalidFiles().size());
        context.assertIsSatisfied();
    }
    
    private IFile createImageFileMock(final String fileName)
    {
        final IFile file = context.mock(IFile.class, fileName);
        context.checking(new Expectations()
        {
            {
                one(file).getPath();
                will(returnValue(fileName));
            }
        });
        return file;
    }
}

