/*
 * Copyright 2008 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.bds.Format;
import ch.systemsx.cisd.bds.IAnnotations;
import ch.systemsx.cisd.bds.IFormattedData;
import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * @author Franz-Josef Elmer
 */
public class HCSImageAnnotations implements IAnnotations
{
    private static final Set<Format> FORMATS =
            Collections.unmodifiableSet(new HashSet<Format>(Arrays
                    .asList(HCSImageFormatV1_0.HCS_IMAGE_1_0)));

    private String deviceID;

    private List<Channel> channels = new ArrayList<Channel>();

    public void addChannels(Iterable<Channel> channelIterable)
    {
        for (Channel channel : channelIterable)
        {
            addChannel(channel);
        }
    }

    public void addChannel(Channel channel)
    {
        channels.add(channel);
    }

    public List<Channel> getChannels()
    {
        return Collections.unmodifiableList(channels);
    }

    public final String getDeviceID()
    {
        return deviceID;
    }

    public final void setDeviceID(String deviceID)
    {
        this.deviceID = deviceID;
    }

    public void assertValid(IFormattedData formattedData) throws DataStructureException
    {
        Format format = formattedData.getFormat();
        if (FORMATS.contains(format) == false)
        {
            throw new DataStructureException("One of the following formats expected instead of '"
                    + format + "': " + FORMATS);
        }
        // TODO 2008-01-15, Franz-Josef Elmer also checks number of channels
    }

    public void saveTo(IDirectory directory)
    {
        if (deviceID != null)
        {
            directory.addKeyValuePair("device_id", deviceID);
        }
        Collections.sort(channels, new Comparator<Channel>()
            {
                public int compare(Channel c1, Channel c2)
                {
                    return c1.getCounter() - c2.getCounter();
                }
            });
        for (Channel channel : channels)
        {
            IDirectory channelDir = directory.makeDirectory("channel" + channel.getCounter());
            channelDir.addKeyValuePair("wavelength", Integer.toString(channel.getWavelength()));
        }
    }
}
