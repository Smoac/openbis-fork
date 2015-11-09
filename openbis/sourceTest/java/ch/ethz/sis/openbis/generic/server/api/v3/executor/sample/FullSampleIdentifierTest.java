/*
 * Copyright 2015 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.api.v3.executor.sample;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.testng.annotations.Test;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class FullSampleIdentifierTest
{

    @Test
    public void testHappyCases()
    {
        assertSampId("/S1");
        assertSampId("/SPACE1/S2");
        assertSampId("/SPACE1/S2:A02");
        assertSampId("/SPACE1/PROJECT1/S1");
        assertSampId("/SPACE1/PROJECT1/S1:A02");
    }
    
    @Test
    public void testFailingCases()
    {
        assertInvalidSampId("Unspecified sample identifier.", null);
        assertInvalidSampId("Unspecified sample identifier.", "");
        assertInvalidSampId("Sample identifier has to start with a '/': A/BC", "A/BC");
        assertInvalidSampId("Sample identifier can not contain more than three '/': /A/B/C/D", "/A/B/C/D");
        assertInvalidSampId("Sample code can not contain more than one ':': /A/B:C:D", "/A/B:C:D");
    }
    
    private void assertInvalidSampId(String expectedErrorMsg, String identifier)
    {
        try
        {
            new FullSampleIdentifier(identifier);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException ex)
        {
            assertEquals(expectedErrorMsg, ex.getMessage());
        }
    }
    
    private void assertSampId(String identifier)
    {
        assertEquals(new FullSampleIdentifier(identifier).toString(), identifier);
    }

}
