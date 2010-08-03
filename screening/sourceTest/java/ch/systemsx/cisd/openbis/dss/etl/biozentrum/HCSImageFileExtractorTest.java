/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.openbis.dss.etl.AbstractHCSImageFileExtractor.ImageFileInfo;

/**
 * Test cases for {@link HCSImageFileExtractor}.
 * 
 * @author Izabela Adamczyk
 */
@Friend(toClasses = HCSImageFileExtractor.class)
public class HCSImageFileExtractorTest extends AssertJUnit
{
    @Test
    public void testExtractFileInfoCorrectFileName() throws Exception
    {
        ImageFileInfo info =
                HCSImageFileExtractor
                        .extractFileInfo("SM100719invasomes_plt-1_bc-UNK_wp-A01_s-10_t-1_wl-Cy3_001");
        assertEquals("plate location token", "A01", info.getPlateLocationToken());
        assertEquals("channel token", "Cy3", info.getChannelToken());
        assertEquals("time point token", "1", info.getTimepointToken());
        assertEquals("well location token", "10", info.getWellLocationToken());
    }
}
