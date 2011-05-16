/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl;

import java.util.Arrays;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.etl.ImagingDatabaseHelper.ImagingChannelsCreator;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.Channel;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.ChannelColor;

/**
 * Test of {@link ImagingChannelsCreator}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = ImagingChannelsCreator.class)
public class ImagingChannelsCreatorTest extends AssertJUnit
{
    @Test
    public void testFillMissingChannelColors()
    {
        List<Channel> channels =
                Arrays.asList(mkChannel(), mkChannel(ChannelColor.RED), mkChannel());
        ImagingChannelsCreator.fillMissingChannelColors(channels);

        assertEqual(ChannelColor.BLUE, channels.get(0));
        assertEqual(ChannelColor.RED, channels.get(1));
        assertEqual(ChannelColor.GREEN, channels.get(2));

    }

    private static void assertEqual(ChannelColor expectedColor, Channel channel)
    {
        if (expectedColor != channel.tryGetChannelColor())
        {
            fail("Expected " + expectedColor + " but got: " + channel.tryGetChannelColor());
        }
    }

    private static Channel mkChannel()
    {
        return mkChannel(null);
    }

    private static Channel mkChannel(ChannelColor colorOrNull)
    {
        return new Channel("code", "label", colorOrNull);
    }
}