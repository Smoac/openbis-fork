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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ch.systemsx.cisd.bds.DataStructureException;
import ch.systemsx.cisd.bds.FormatParameter;
import ch.systemsx.cisd.bds.IStorable;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.INode;

/**
 * The list of encapsulated <code>Channels</code> available here.
 * 
 * @author Christian Ribeaud
 */
public final class ChannelList implements IStorable, Iterable<Channel>
{

    /**
     * Unique identifier when used as {@link FormatParameter}.
     */
    static final String NUMBER_OF_CHANNELS = "number_of_channels";

    private final List<Channel> channels;

    public ChannelList(final List<Channel> channels)
    {
        assert channels != null && channels.size() > 0 : "At least one channel must be specified.";
        if (new HashSet<Channel>(channels).size() != channels.size())
        {
            throw new DataStructureException(String.format("Some duplicate channels in '%s'.", channels));
        }
        this.channels = channels;
    }

    /**
     * Loads all <code>Channels</code> from the specified directory.
     * 
     * @throws DataStructureException if the <code>Channels</code> could be loaded.
     */
    final static ChannelList loadFrom(final IDirectory directory)
    {
        final List<Channel> channels = new ArrayList<Channel>();
        for (INode node : directory)
        {
            if (node.getName().startsWith(Channel.CHANNEL))
            {
                assert node instanceof IDirectory : "Must be an IDirectory";
                channels.add(Channel.loadFrom((IDirectory) node));
            }
        }
        return new ChannelList(channels);
    }

    /** Returns the number of channels. */
    public final int getChannelCount()
    {
        return channels.size();
    }

    //
    // IStorable
    //

    public final void saveTo(final IDirectory directory)
    {
        directory.addKeyValuePair(NUMBER_OF_CHANNELS, getChannelCount() + "");
        for (Channel channel : channels)
        {
            channel.saveTo(directory);
        }
    }

    //
    // Iterable
    //

    public final Iterator<Channel> iterator()
    {
        return channels.iterator();
    }
}
