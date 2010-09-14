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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample.SampleInitializer;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class SampleTest extends AssertJUnit
{
    private static final String SAMPLE_IDENTIFIER = "/space/sample-code";

    private Sample sample;

    @BeforeMethod
    public void setUp()
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        sample = new Sample(initializer);
    }

    @Test
    public void testEquals()
    {
        SampleInitializer initializer = new SampleInitializer();
        initializer.setIdentifier(SAMPLE_IDENTIFIER);
        Sample mySample = new Sample(initializer);

        assertTrue(sample.equals(mySample));
        assertEquals(sample.hashCode(), mySample.hashCode());
    }

    @Test
    public void testToString()
    {
        String stringRepresentation = sample.toString();
        assertEquals("Sample[/space/sample-code]", stringRepresentation);
    }
}
