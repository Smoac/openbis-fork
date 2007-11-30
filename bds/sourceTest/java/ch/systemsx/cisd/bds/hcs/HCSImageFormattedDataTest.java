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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.FormatedDataFactory;
import ch.systemsx.cisd.bds.IFormatParameters;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link HCSImageFormattedData} class.
 * 
 * @author Christian Ribeaud
 */
public class HCSImageFormattedDataTest extends AbstractFileSystemTestCase

{
    static final Location PLATE_LOCATION = new Location(2, 1);

    static final Location WELL_LOCATION = new Location(1, 2);

    private Mockery context;

    private HCSImageFormattedData formattedData;

    private IFormatParameters formatParameters;

    private IDirectory directory;

    private IDirectory standardNode;

    private IDirectory leafDirectory;

    private final void prepareAndCreateFormattedData()
    {
        directory = context.mock(IDirectory.class);
        formatParameters = context.mock(IFormatParameters.class);
        context.checking(new Expectations()
            {
                {
                    for (final String formatParameterName : HCSImageFormattedData.MANDATORY_FORMAT_PARAMETERS)
                    {
                        one(formatParameters).containsParameter(formatParameterName);
                        will(returnValue(true));
                    }
                }
            });
        formattedData =
                (HCSImageFormattedData) FormatedDataFactory.createFormatedData(directory,
                        HCSImageFormat1_0.HCS_IMAGE_1_0, null, formatParameters);
    }

    private final void prepareStandardNode()
    {
        standardNode = NodeFactory.createDirectoryNode(workingDirectory);
        final IDirectory channel1 = standardNode.makeDirectory("channel1");
        final IDirectory row1 = channel1.makeDirectory("row1");
        leafDirectory = row1.makeDirectory("column2");
    }

    private final void addParameterCheckExpectations(final Expectations exp)
    {
        final ChannelList channelList = createChannelList();
        final Geometry geometry = new Geometry(2, 2);
        exp.one(formatParameters).getValue(ChannelList.NUMBER_OF_CHANNELS);
        exp.will(exp.returnValue(channelList));
        exp.one(formatParameters).getValue(PlateGeometry.PLATE_GEOMETRY);
        exp.will(exp.returnValue(geometry));
        exp.one(formatParameters).getValue(WellGeometry.WELL_GEOMETRY);
        exp.will(exp.returnValue(geometry));
    }

    //
    // AbstractFileSystemTestCase
    //

    @Override
    @BeforeMethod
    public final void setup() throws IOException
    {
        super.setup();
        context = new Mockery();
        prepareAndCreateFormattedData();
        prepareStandardNode();
    }

    @AfterMethod
    public void tearDown()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    private final ChannelList createChannelList()
    {
        final int wavelength = 987;
        final int channelCounter = 1;
        final Channel channel = new Channel(channelCounter, wavelength);
        final List<Channel> channels = Arrays.asList(new Channel[]
            { channel });
        return new ChannelList(channels);
    }

    @Test
    public final void testCreateWellFileName()
    {
        try
        {
            HCSImageFormattedData.createWellFileName(null);
            fail("Location can not be null.");
        } catch (AssertionError ex)
        {
            // Nothing to do here.
        }
        final Location location = WELL_LOCATION;
        final String expected = "row2_column1.tiff";
        assertEquals(expected, HCSImageFormattedData.createWellFileName(location));
        context.assertIsSatisfied();
    }

    @Test
    public final void testGetFormat()
    {
        assertEquals(HCSImageFormat1_0.HCS_IMAGE_1_0, formattedData.getFormat());
        context.assertIsSatisfied();
    }

    @Test
    public final void testAddStandardNodeWithOriginalData() throws IOException
    {
        final String originalFileName = "original.txt";
        FileUtils.touch(new File(workingDirectory, originalFileName));
        final int channelIndex = 1;
        final IDirectory workingDirectoryNode = NodeFactory.createDirectoryNode(workingDirectory);
        context.checking(new Expectations()
            {
                {
                    addParameterCheckExpectations(this);

                    exactly(2).of(directory).tryGetNode(DataStructureV1_0.DIR_STANDARD);
                    will(returnValue(standardNode));

                    one(directory).tryGetNode(DataStructureV1_0.DIR_ORIGINAL);
                    will(returnValue(workingDirectoryNode));

                    one(formatParameters).getValue(HCSImageFormat1_0.CONTAINS_ORIGINAL_DATA);
                    will(returnValue(Boolean.FALSE));
                }
            });
        final INode node = formattedData.addStandardNode(originalFileName, channelIndex, PLATE_LOCATION, WELL_LOCATION);
        assertNotNull(node);
        assertEquals(originalFileName, node.getName());
        final INode standardFile = leafDirectory.tryGetNode(originalFileName);
        assertNotNull(standardFile);
        assertEquals(originalFileName, standardFile.getName());
        context.assertIsSatisfied();
    }

    @Test
    public final void testTryGetStandardNodeAt()
    {

        context.assertIsSatisfied();
    }
}