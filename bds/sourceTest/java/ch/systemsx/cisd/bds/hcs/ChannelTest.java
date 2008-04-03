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

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

/**
 * Test cases for corresponding {@link Channel} class.
 * 
 * @author Christian Ribeaud
 */
public final class ChannelTest
{

    @Test
    public final void testComparable()
    {
        final List<Channel> channels = new ArrayList<Channel>();
        channels.add(new Channel(2, 123));
        channels.add(new Channel(1, 456));
        assertEquals(123, channels.get(0).getWavelength());
        Collections.sort(channels);
        assertEquals(456, channels.get(0).getWavelength());
    }
}
