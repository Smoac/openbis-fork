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

package ch.systemsx.cisd.bds.hcs;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormattedDataFactory;
import ch.systemsx.cisd.bds.IFormatParameters;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.hcs.IHCSImageFormattedData.NodePath;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.IFile;
import ch.systemsx.cisd.bds.storage.ILink;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.bds.v1_0.DataStructureV1_0;

/**
 * Test cases for corresponding {@link HCSImageFormattedData} class.
 * 
 * @author Christian Ribeaud
 */
public class HCSImageFormattedDataTest extends AbstractFileSystemTestCase

{
    private static final int NUMBER_OF_CHANNELS = 1;

    private static final Geometry GEOMETRY = new Geometry(2, 2);

    private static final String IMAGE_ROOT_DIRECTORY_NAME = "CP001-1";

    private static final String ORIGINAL_IMAGE = "original.jpg";

    static final Location PLATE_LOCATION = new Location(2, 1);

    static final Location WELL_LOCATION = new Location(1, 2);

    private Mockery context;

    private HCSImageFormattedData formattedData;

    private IFormatParameters formatParameters;

    private IDirectory directory;

    private IDirectory standardNode;

    private IDirectory originalNode;

    private IDirectory standardLeafDirectory;

    private File imageRootDirectory;

    private final void prepareAndCreateFormattedData()
    {
        prepareAndCreateFormattedData(standardNode, originalNode);
    }

    private final void prepareAndCreateFormattedDataWithoutOriginal()
    {
        prepareAndCreateFormattedData(standardNode, null);
    }

    private final void prepareAndCreateFormattedData(final IDirectory standardDirNode,
            final IDirectory originalNodeOrNull)
    {
        directory = context.mock(IDirectory.class);
        formatParameters = context.mock(IFormatParameters.class);
        final Format format = HCSImageFormatV1_0.HCS_IMAGE_1_0;
        context.checking(new Expectations()
            {
                {
                    for (final String formatParameterName : format.getMandatoryParameterNames())
                    {
                        one(formatParameters).containsParameter(formatParameterName);
                        will(returnValue(true));
                    }
                    for (final String formatParameterName : format.getOptionalParameterNames())
                    {
                        allowing(formatParameters).containsParameter(formatParameterName);
                        will(returnValue(true));
                    }
                    addParameterCheckExpectations(this, GEOMETRY, GEOMETRY, NUMBER_OF_CHANNELS);
                    prepareGetDataDirectories(this, standardDirNode, originalNodeOrNull);
                }
            });
        formattedData =
                (HCSImageFormattedData) FormattedDataFactory.createFormattedData(directory, format,
                        null, formatParameters);
    }

    private void prepareGetDataDirectories(Expectations exp, IDirectory standardDirNode,
            IDirectory originalNodeOrNull)
    {
        exp.one(directory).tryGetNode(DataStructureV1_0.DIR_STANDARD);
        exp.will(Expectations.returnValue(standardDirNode));

        exp.one(formatParameters).getValue(HCSImageFormatV1_0.CONTAINS_ORIGINAL_DATA);
        boolean containsOriginalData = originalNodeOrNull != null;
        exp.will(Expectations.returnValue(Utilities.Boolean.fromBoolean(containsOriginalData)));

        exp.one(formatParameters).getValue(HCSImageFormatV1_0.IS_INCOMING_SYMBOLIC_LINK);
        exp.will(Expectations.returnValue(Utilities.Boolean.fromBoolean(false)));

        exp.one(formatParameters).getValue(HCSImageFormatV1_0.IMAGE_FILE_EXTENSION);
        exp.will(Expectations.returnValue("jpg"));

        if (containsOriginalData)
        {
            exp.one(directory).tryGetNode(DataStructureV1_0.DIR_ORIGINAL);
            exp.will(Expectations.returnValue(originalNodeOrNull));
        }
    }

    private final void createStandardNode()
    {
        final File standardDirectory = new File(workingDirectory, DataStructureV1_0.DIR_STANDARD);
        standardDirectory.mkdir();
        standardNode = NodeFactory.createDirectoryNode(standardDirectory);
        final IDirectory channel1 = standardNode.makeDirectory(Channel.CHANNEL + "1");
        final IDirectory row1 = channel1.makeDirectory(HCSImageFormattedData.ROW + "1");
        standardLeafDirectory = row1.makeDirectory(HCSImageFormattedData.COLUMN + "2");
    }

    private final void createOriginalNode()
    {
        final File originalDirectory = new File(workingDirectory, DataStructureV1_0.DIR_ORIGINAL);
        originalDirectory.mkdir();
        originalNode = NodeFactory.createDirectoryNode(originalDirectory);
    }

    private final void addParameterCheckExpectations(final Expectations exp,
            Geometry plateGeometry, Geometry wellGeometry, int numberOfChannels)
    {
        exp.one(formatParameters).getValue(HCSImageFormatV1_0.NUMBER_OF_CHANNELS);
        exp.will(Expectations.returnValue(new Integer(numberOfChannels)));
        exp.one(formatParameters).getValue(PlateGeometry.PLATE_GEOMETRY);
        exp.will(Expectations.returnValue(plateGeometry));
        exp.one(formatParameters).getValue(WellGeometry.WELL_GEOMETRY);
        exp.will(Expectations.returnValue(wellGeometry));
    }

    private final void createImageRootDirectory() throws IOException
    {
        imageRootDirectory = new File(workingDirectory, IMAGE_ROOT_DIRECTORY_NAME);
        imageRootDirectory.mkdir();
        final File originalFile = new File(imageRootDirectory, ORIGINAL_IMAGE);
        FileUtils.writeStringToFile(originalFile, "This is my original file...");
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setUp() throws IOException
    {
        super.setUp();
        context = new Mockery();
        createStandardNode();
        createOriginalNode();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public final void testCreateWellFileName()
    {
        prepareAndCreateFormattedDataWithoutOriginal();
        boolean fail = true;
        try
        {
            HCSImageFormattedData.createWellFileName(null, null);
        } catch (AssertionError ex)
        {
            fail = false;
        }
        assertEquals(false, fail);
        final Location location = WELL_LOCATION;
        final String expected = "row2_column1.tiff";
        assertEquals(expected, HCSImageFormattedData.createWellFileName(location,
                HCSImageFormatV1_0.DEFAULT_FILE_EXTENSION));
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetFormat()
    {
        prepareAndCreateFormattedDataWithoutOriginal();
        assertEquals(HCSImageFormatV1_0.HCS_IMAGE_1_0, formattedData.getFormat());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAddStandardNodeWithOriginalData() throws IOException
    {
        createImageRootDirectory();
        prepareAndCreateFormattedData();

        final int channelIndex = 1;
        final NodePath nodePath =
                formattedData.addStandardNode(imageRootDirectory, ORIGINAL_IMAGE, channelIndex,
                        PLATE_LOCATION, WELL_LOCATION);
        final String standardFileName = "row2_column1.jpg";
        assertNotNull(nodePath);
        assertTrue(nodePath.getNode() instanceof ILink);
        assertEquals(standardFileName, nodePath.getNode().getName());
        // Look at the standard leaf directory if the node is there as well.
        final INode standardFile = standardLeafDirectory.tryGetNode(standardFileName);
        assertNotNull(standardFile);
        assertTrue(standardFile instanceof IFile);
        assertEquals(standardFileName, standardFile.getName());
        // Node should still be in the 'original' directory.
        assertNotNull(((IDirectory) originalNode.tryGetNode(IMAGE_ROOT_DIRECTORY_NAME))
                .tryGetNode(ORIGINAL_IMAGE));
        context.assertIsSatisfied();
    }

    @Test
    public final void testAddStandardNodeWithoutOriginalData() throws IOException
    {
        createImageRootDirectory();
        prepareAndCreateFormattedDataWithoutOriginal();

        final int channelIndex = 1;
        final NodePath nodePath =
                formattedData.addStandardNode(imageRootDirectory, ORIGINAL_IMAGE, channelIndex,
                        PLATE_LOCATION, WELL_LOCATION);
        final String standardFileName = "row2_column1.jpg";
        assertNotNull(nodePath);
        assertTrue(nodePath.getNode() instanceof IFile);
        assertEquals(standardFileName, nodePath.getNode().getName());
        // Look at the standard leaf directory if the node is there as well.
        final INode standardFile = standardLeafDirectory.tryGetNode(standardFileName);
        assertNotNull(standardFile);
        assertTrue(standardFile instanceof IFile);
        assertEquals(standardFileName, standardFile.getName());
        // Node should no longer be in the 'original' directory.
        assertNull(originalNode.tryGetNode(IMAGE_ROOT_DIRECTORY_NAME));
        context.assertIsSatisfied();

    }

    @Test
    public final void testTryGetStandardNodeAtWithNoFile()
    {
        prepareAndCreateFormattedDataWithoutOriginal();

        final int channelIndex = 1;
        final INode node =
                formattedData.tryGetStandardNodeAt(channelIndex, PLATE_LOCATION, WELL_LOCATION);
        assertNull(node);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTryGetStandardNodeAtWithFile() throws IOException
    {
        prepareAndCreateFormattedDataWithoutOriginal();

        final File file = new File(workingDirectory, "row2_column1.jpg");
        FileUtils.writeStringToFile(file, "This is my original file...");
        standardLeafDirectory.addFile(file, null, true);
        final int channelIndex = 1;
        final INode node =
                formattedData.tryGetStandardNodeAt(channelIndex, PLATE_LOCATION, WELL_LOCATION);
        assertNotNull(node);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTryGetStandardNodeAtWithWrongChannelIndex()
    {
        prepareAndCreateFormattedDataWithoutOriginal();
        try
        {
            formattedData.tryGetStandardNodeAt(2, null, null);
            fail("2 > 1");
        } catch (final IndexOutOfBoundsException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testTryGetStandardNodeAtWithWrongPlateLocation()
    {
        prepareAndCreateFormattedDataWithoutOriginal();
        try
        {
            final Location location = new Location(2, 3);
            formattedData.tryGetStandardNodeAt(1, location, location);
            fail("Given geometry '2x2' does not contain location '[x=2,y=3]'");
        } catch (final IllegalArgumentException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }

    @Test
    public final void testTryGetStandardNodeAtWithWrongWellLocation()
    {
        prepareAndCreateFormattedDataWithoutOriginal();
        try
        {
            formattedData.tryGetStandardNodeAt(1, new Location(2, 2), new Location(3, 2));
            fail("Given geometry '2x2' does not contain location '[x=3,y=2]'");
        } catch (final IllegalArgumentException ex)
        {
            // Nothing to do here.
        }
        context.assertIsSatisfied();
    }
}