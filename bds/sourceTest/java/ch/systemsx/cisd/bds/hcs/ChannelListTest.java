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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

import ch.systemsx.cisd.bds.exception.DataStructureException;
import ch.systemsx.cisd.bds.storage.IDirectory;
import ch.systemsx.cisd.bds.storage.filesystem.NodeFactory;
import ch.systemsx.cisd.common.utilities.AbstractFileSystemTestCase;

/**
 * Test cases for corresponding {@link ChannelList} class.
 * 
 * @author Christian Ribeaud
 */
public final class ChannelListTest extends AbstractFileSystemTestCase
{

    private final static ChannelList createChannelList()
    {
        final List<Channel> list = new ArrayList<Channel>();
        list.add(new Channel(1, 123));
        list.add(new Channel(2, 456));
        return new ChannelList(list);
    }

    private final static void checkFile(final File file)
    {
        assertTrue(file.getName().equals(ChannelList.NUMBER_OF_CHANNELS) || file.getName().startsWith(Channel.CHANNEL));
    }

    private final static void checkChannelFile(final File channelFile)
    {
        assertNotNull(channelFile);
        assertTrue(channelFile.isDirectory());
        final File[] files = channelFile.listFiles();
        assertEquals(1, files.length);
        assertEquals(files[0].getName(), Channel.WAVELENGTH);
    }

    @Test
    public final void testConstructor()
    {
        try
        {
            new ChannelList(null);
            fail("Channel list can not be null.");
        } catch (AssertionError ex)
        {
        }
        try
        {
            new ChannelList(new ArrayList<Channel>());
            fail("Channel list can not be empty.");
        } catch (AssertionError ex)
        {
        }
        final List<Channel> list = new ArrayList<Channel>();
        list.add(new Channel(1, 123));
        list.add(new Channel(1, 456));
        try
        {
            new ChannelList(list);
            fail("Duplicate channels are not allowed.");
        } catch (DataStructureException e)
        {
        }
    }

    @Test
    public final void testSaveTo()
    {
        final ChannelList channelList = createChannelList();
        final IDirectory dir = NodeFactory.createDirectoryNode(workingDirectory);
        channelList.saveTo(dir);
        final File[] files = workingDirectory.listFiles();
        assertEquals(3, files.length);
        for (File file : files)
        {
            checkFile(file);
            if (file.getName().startsWith(Channel.CHANNEL))
            {
                checkChannelFile(file);
            }
        }
    }

    @Test(dependsOnMethods = "testSaveTo")
    public final void testLoadFrom()
    {
        testSaveTo();
        final IDirectory dir = NodeFactory.createDirectoryNode(workingDirectory);
        final ChannelList channelList = ChannelList.loadFrom(dir);
        assertEquals(2, channelList.getChannelCount());
        final Iterator<Channel> iterator = channelList.iterator();
        Channel channel = iterator.next();
        assertEquals(123, channel.getWavelength());
        channel = iterator.next();
        assertEquals(456, channel.getWavelength());
    }
}