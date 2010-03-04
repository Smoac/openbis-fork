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

package ch.systemsx.cisd.yeastx.eicml;

import static ch.systemsx.cisd.yeastx.eicml.EICMLChromatogramImagesReporter.getMz1;
import static ch.systemsx.cisd.yeastx.eicml.EICMLChromatogramImagesReporter.getMz2;
import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;

/**
 * Test of {@link EICMLChromatogramImagesReporter}
 * 
 * @author Tomasz Pylak
 */
@Friend(toClasses = EICMLChromatogramImagesReporter.class)
public class EICMLChromatogramImagesReporterTest
{
    @Test
    public void testLabelParsing()
    {
        assertEquals(10, getMz1("-EIC 10>23"));
        assertEquals(10, getMz1("EIC 10>23"));
        assertEquals(10, getMz1("-EIC 10"));
        assertEquals(23, getMz2("-EIC 10>23"));
        assertEquals(23, getMz2("EIC 10>23"));
        assertEquals(-1, getMz2("-EIC 10"));

    }
}
