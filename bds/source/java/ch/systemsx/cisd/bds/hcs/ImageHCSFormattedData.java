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

import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.bds.AbstractFormattedData;
import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.DataStructureV1_0;
import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.FormattedDataContext;
import ch.systemsx.cisd.bds.IFormatParameters;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.Utilities;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * {@link IFormattedData} implementation for <i>HCS (High-Content Screening) with Images</i>. It is associated with
 * {@link ImageHCSFormat1_0}.
 * 
 * @author Christian Ribeaud
 */
// TODO 2007-11-15, Christian Ribeaud: Make a test of this class. Maybe we can extract some of
// 'HCSDataStructureV1_0Test' class.
public final class ImageHCSFormattedData extends AbstractFormattedData implements IHCSFormattedData
{

    /** The <i>column</i> (or <i>x</i>) coordinate. */
    private static final String COLUMN = "column";

    /** The <i>row</i> (or <i>y</i>) coordinate. */
    private static final String ROW = "row";

    /**
     * The mandatory format parameters that must be defined so that this implementation is able to work properly.
     */
    private final static String[] MANDATORY_FORMAT_PARAMETERS =
            new String[]
                { PlateGeometry.PLATE_GEOMETRY, WellGeometry.WELL_GEOMETRY, ChannelList.NUMBER_OF_CHANNELS,
                        ImageHCSFormat1_0.CONTAINS_ORIGINAL_DATA, ImageHCSFormat1_0.DEVICE_ID };

    public ImageHCSFormattedData(final FormattedDataContext context)
    {
        super(context);
    }

    private final Geometry getWellGeometry()
    {
        return (Geometry) getFormatParameters().getValue(WellGeometry.WELL_GEOMETRY);
    }

    private final Geometry getPlateGeometry()
    {
        return (Geometry) getFormatParameters().getValue(PlateGeometry.PLATE_GEOMETRY);
    }

    private final int getChannelCount()
    {
        return getChannelList().getChannelCount();
    }

    private final ChannelList getChannelList()
    {
        return (ChannelList) getFormatParameters().getValue(ChannelList.NUMBER_OF_CHANNELS);
    }

    private final static void checkLocation(final Geometry geometry, final Location location)
    {
        if (geometry.contains(location) == false)
        {
            throw new IllegalArgumentException(String.format("Given geometry '%s' does not contain location '%s'",
                    geometry, location));
        }

    }

    private final void checkChannel(final int channel)
    {
        if (channel < 1)
        {
            throw new IndexOutOfBoundsException(String.format("Channel index starts at 1.", channel));
        }
        final int channelCount = getChannelCount();
        if (channel > channelCount)
        {
            throw new IndexOutOfBoundsException(String.format("%d > %d", channel, channelCount));
        }
    }

    private final static String createWellFileName(final Location wellLocation)
    {
        return ROW + wellLocation.y + COLUMN + wellLocation.x + ".tiff";
    }

    //
    // IHCSFormattedData
    //

    // TODO 2007-11-15, Christian Ribeaud: Should we consider to use 'standard_original_mapping' here?
    public final INode getNodeAt(final int channel, final Location plateLocation, final Location wellLocation)
            throws DataStructureException
    {
        checkChannel(channel);
        checkLocation(getPlateGeometry(), plateLocation);
        checkLocation(getWellGeometry(), wellLocation);
        final IDirectory standardDir = Utilities.getSubDirectory(dataDirectory, DataStructureV1_0.DIR_STANDARD);
        final IDirectory channelDir = Utilities.getSubDirectory(standardDir, Channel.CHANNEL + channel);
        final IDirectory plateRowDir = Utilities.getSubDirectory(channelDir, ROW + plateLocation.y);
        final IDirectory plateColumnDir = Utilities.getSubDirectory(plateRowDir, COLUMN + plateLocation.x);
        final INode node = plateColumnDir.tryToGetNode(createWellFileName(wellLocation));
        if (node == null)
        {
            throw new DataStructureException(String.format(
                    "No node could be found at channel %d, plate location '%s' and well location '%s'.", channel,
                    plateLocation, wellLocation));
        }
        return node;
    }

    //
    // AbstractFormattedData
    //

    public final Format getFormat()
    {
        return ImageHCSFormat1_0.IMAGE_HCS_1_0;
    }

    @Override
    protected final void assertValidFormatAndFormatParameters()
    {
        super.assertValidFormatAndFormatParameters();
        final IFormatParameters formatParameters = getFormatParameters();
        final Set<String> notPresent = new HashSet<String>();
        for (final String formatParameterName : MANDATORY_FORMAT_PARAMETERS)
        {
            if (formatParameters.containsParameter(formatParameterName) == false)
            {
                notPresent.add(formatParameterName);
            }
        }
        if (notPresent.isEmpty() == false)
        {
            throw new DataStructureException(String.format(
                    "Following format parameters '%s' could not be found in the structure.", notPresent));
        }

    }
}
