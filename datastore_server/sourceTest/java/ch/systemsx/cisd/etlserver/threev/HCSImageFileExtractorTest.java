/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.etlserver.threev;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;

/**
 * Test cases for the {@link HCSImageFileExtractor}.
 * 
 * @author Christian Ribeaud
 */
public final class HCSImageFileExtractorTest extends AbstractFileSystemTestCase
{

    private static final String WELL_GEOMETRY = "3x3";

    private HCSImageFileExtractor fileExtractor;

    private Mockery context;

    private IHCSImageFileAccepter fileAccepter;

    private BufferedAppender logRecorder;

    private final IDirectory workingDirectoryNode;

    public HCSImageFileExtractorTest()
    {
        super();
        this.workingDirectoryNode = NodeFactory.createDirectoryNode(workingDirectory);
    }

    private final void prepareFileExtractor()
    {
        context = new Mockery();
        fileAccepter = context.mock(IHCSImageFileAccepter.class);
        logRecorder = new BufferedAppender("%m", Level.WARN);
        fileExtractor = new HCSImageFileExtractor(createProperties());
    }

    private final static Properties createProperties()
    {
        final Properties props = new Properties();
        props.setProperty(WellGeometry.WELL_GEOMETRY, WELL_GEOMETRY);
        return props;
    }

    private final IFile createFile(final String fileName) throws IOException
    {
        final File file = new File(workingDirectory, fileName);
        FileUtils.touch(file);
        assertTrue(file.exists());
        return NodeFactory.createFileNode(file);
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        prepareFileExtractor();
        logRecorder.resetLogContent();
    }

    @AfterMethod
    public final void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void processWithNull()
    {
        boolean exceptionThrown = false;
        try
        {
            fileExtractor.process(null, null, null);
        } catch (AssertionError ex)
        {
            exceptionThrown = true;
        }
        assertTrue("Null values not allowed here.", exceptionThrown);
        context.assertIsSatisfied();
    }

    @Test
    public final void testProcessWithNoCorrectPrefix() throws IOException
    {
        final String imagePath = "H24_s6_w1_[UUID].tif";
        createFile(imagePath);
        assertEquals(0, fileExtractor.process(workingDirectoryNode, null, fileAccepter)
                .getInvalidFiles().size());
    }

    @Test
    public final void testProcessHappyCase() throws IOException
    {
        final String imagePath = "Screening_H24_s6_w1_[UUID].tif";
        final IFile file = createFile(imagePath);
        final int channel = 1;
        final Location plateLocation = new Location(24, 8);
        final Location wellLocation = new Location(3, 2);
        context.checking(new Expectations()
            {
                {
                    one(fileAccepter).accept(channel, plateLocation, wellLocation, file);
                }
            });
        assertEquals("", logRecorder.getLogContent());
        assertTrue(fileExtractor.process(workingDirectoryNode, null, fileAccepter).getInvalidFiles()
                .isEmpty());
        context.assertIsSatisfied();
    }

    @Test
    public final void testProcessWithNotEnoughTokens() throws IOException
    {
        final String imagePath = "Screening_H24_s6_w1.tif";
        createFile(imagePath);
        fileExtractor.process(workingDirectoryNode, null, fileAccepter);
        assertEquals(0, fileExtractor.process(workingDirectoryNode, null, fileAccepter)
                .getInvalidFiles().size());
    }

    @Test
    public final void testProcessWithNoRightPlateCoordinate() throws IOException
    {
        final String imagePath = "Screening_XX_s6_w1_UUID.tif";
        createFile(imagePath);
        fileExtractor.process(workingDirectoryNode, null, fileAccepter);
        assertEquals(1, fileExtractor.process(workingDirectoryNode, null, fileAccepter)
                .getInvalidFiles().size());
    }

    @Test
    public final void testProcessWithNoRightWellCoordinate() throws IOException
    {
        final String imagePath = "Screening_H24_6_w1_UUID.tif";
        createFile(imagePath);
        fileExtractor.process(workingDirectoryNode, null, fileAccepter);
        assertEquals(1, fileExtractor.process(workingDirectoryNode, null, fileAccepter)
                .getInvalidFiles().size());
    }
}