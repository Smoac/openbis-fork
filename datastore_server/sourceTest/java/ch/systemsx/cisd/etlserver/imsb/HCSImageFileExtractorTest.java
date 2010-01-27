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

package ch.systemsx.cisd.etlserver.imsb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.bds.hcs.WellGeometry;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.etlserver.IHCSImageFileAccepter;
import ch.systemsx.cisd.etlserver.plugins.AbstractHCSImageFileExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Test cases for the {@link HCSImageFileExtractor}.
 * 
 * @author Christian Ribeaud
 */
@Friend(toClasses =
    { HCSImageFileExtractor.class, AbstractHCSImageFileExtractor.class })
public final class HCSImageFileExtractorTest extends AbstractFileSystemTestCase
{

    private final IDirectory workingDirectoryNode;

    public HCSImageFileExtractorTest()
    {
        super();
        this.workingDirectoryNode = NodeFactory.createDirectoryNode(workingDirectory);
    }

    static class TestHCSImageFileExtractor extends HCSImageFileExtractor
    {

        private List<IFile> files;

        @Override
        List<IFile> listImageFiles(final IDirectory directory)
        {
            return files;
        }

        public TestHCSImageFileExtractor(final Properties properties)
        {
            super(properties);
        }

        void setFiles(final List<IFile> files)
        {
            this.files = files;
        }

    }

    private static final String WELL_GEOMETRY = "3x3";

    private static final String SAMPLE_CODE = "CP042-1ab";

    private final DataSetInformation dataSetInformation = createDataSetInformation();

    private TestHCSImageFileExtractor fileExtractor;

    private Mockery context;

    private IHCSImageFileAccepter fileAccepter;

    private BufferedAppender logRecorder;

    private final void prepareFileExtractor()
    {
        context = new Mockery();
        fileAccepter = context.mock(IHCSImageFileAccepter.class);
        logRecorder = new BufferedAppender("%m", Level.WARN);
        fileExtractor = new TestHCSImageFileExtractor(createProperties());
    }

    private final static DataSetInformation createDataSetInformation()
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();
        dataSetInformation.setSampleCode(SAMPLE_CODE);
        return dataSetInformation;
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
        try
        {
            fileExtractor.process((IDirectory) null, null, null);
            fail("Null values not allowed here.");
        } catch (final AssertionError ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testProcessWithIncorrectSample() throws IOException
    {
        final String imagePath = "CP002-2bc_H24_6_w460.tif";
        final List<IFile> files = new ArrayList<IFile>();
        fileExtractor.setFiles(files);
        files.add(createFile(imagePath));
        assertEquals("", logRecorder.getLogContent());
        assertEquals(1, fileExtractor.process(NodeFactory.createDirectoryNode(workingDirectory),
                dataSetInformation, fileAccepter).getInvalidFiles().size());
    }

    @Test
    public final void testProcessHappyCase() throws IOException
    {
        final String imagePath1 = "Some_trash_" + SAMPLE_CODE + "_H24_6_w530.tif";
        final String imagePath2 = "Some_trash_" + SAMPLE_CODE + "_H24_6_w460.tif";
        final List<IFile> files = new ArrayList<IFile>();
        fileExtractor.setFiles(files);
        final IFile file1 = createFile(imagePath1);
        files.add(file1);
        final IFile file2 = createFile(imagePath2);
        files.add(file2);
        final int channel1 = 2;
        final int channel2 = 1;
        final Location plateLocation = new Location(24, 8);
        final Location wellLocation = new Location(1, 2);
        context.checking(new Expectations()
            {
                {
                    one(fileAccepter).accept(channel1, plateLocation, wellLocation, file1);
                    one(fileAccepter).accept(channel2, plateLocation, wellLocation, file2);
                }
            });
        assertEquals("", logRecorder.getLogContent());
        final List<IFile> invalidFiles =
                fileExtractor.process(workingDirectoryNode, dataSetInformation, fileAccepter)
                        .getInvalidFiles();
        assertEquals(0, invalidFiles.size());
        context.assertIsSatisfied();
    }

    @Test
    public final void testProcessWithNotEnoughTokens() throws IOException
    {
        final String imagePath = "H24_6_w460.tif";
        final List<IFile> files = new ArrayList<IFile>();
        fileExtractor.setFiles(files);
        final IFile file1 = createFile(imagePath);
        files.add(file1);
        createFile(imagePath);
        fileExtractor.process(workingDirectoryNode, dataSetInformation, fileAccepter);
        assertEquals(1, fileExtractor.process(workingDirectoryNode, dataSetInformation,
                fileAccepter).getInvalidFiles().size());
    }

    @Test
    public final void testProcessWithIncorrectPlateCoordinate() throws IOException
    {
        final String imagePath = "Screening_" + SAMPLE_CODE + "_XX_6_w530.tiff";
        final List<IFile> files = new ArrayList<IFile>();
        fileExtractor.setFiles(files);
        final IFile file1 = createFile(imagePath);
        files.add(file1);
        createFile(imagePath);
        fileExtractor.process(workingDirectoryNode, dataSetInformation, fileAccepter);
        assertEquals(1, fileExtractor.process(workingDirectoryNode, dataSetInformation,
                fileAccepter).getInvalidFiles().size());
    }

    @Test
    public final void testProcessWithIncorrectWellCoordinate() throws IOException
    {
        final String imagePath = "Doesnt_matter_" + SAMPLE_CODE + "_H24_s6_w530.tif";
        final List<IFile> files = new ArrayList<IFile>();
        fileExtractor.setFiles(files);
        final IFile file1 = createFile(imagePath);
        files.add(file1);
        fileExtractor.process(workingDirectoryNode, dataSetInformation, fileAccepter);
        assertEquals(1, fileExtractor.process(workingDirectoryNode, dataSetInformation,
                fileAccepter).getInvalidFiles().size());
    }

    @Test
    public final void testZigZagTileConvertion() throws IOException
    {
        WellGeometry geom = new WellGeometry(4, 3);
        HCSImageFileExtractor extractor = new HCSImageFileExtractor(geom);
        assertLocation(extractor, 8, new Location(2, 2), geom);
        assertLocation(extractor, 12, new Location(1, 1), geom);
        assertLocation(extractor, 1, new Location(1, 4), geom);
        assertLocation(extractor, 5, new Location(2, 3), geom);
    }

    private void assertLocation(HCSImageFileExtractor extractor, int tileNumber,
            Location expectedLocation, WellGeometry wellGeometry)
    {
        Location location =
                AbstractHCSImageFileExtractor.tryGetZigZagWellLocation("" + tileNumber,
                        wellGeometry);
        assertEquals(expectedLocation, location);
    }
}