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

package ch.systemsx.cisd.common.color;

import static org.testng.AssertJUnit.*;

import org.testng.annotations.Test;

/**
 * Test cases for the {@link WavelengthColor} class.
 *
 * @author Bernd Rinn
 */
public class WavelengthColorTest
{

    @Test
    public void testCommonDyes()
    {
        PureHSBColor dapi = WavelengthColor.getHSBColorForWavelength(461);
        assertEquals(214.8f, dapi.getHueDegree(), 0.05f);
        assertEquals(1.0f, dapi.getBrightness());
        PureHSBColor gfp = WavelengthColor.getHSBColorForWavelength(509);
        assertEquals(123.1f, gfp.getHueDegree(), 0.05f);
        assertEquals(1.0f, gfp.getBrightness());
        PureHSBColor fitc = WavelengthColor.getHSBColorForWavelength(521);
        assertEquals(110.6f, fitc.getHueDegree(), 0.05f);
        assertEquals(1.0f, fitc.getBrightness());
        PureHSBColor cy5 = WavelengthColor.getHSBColorForWavelength(660);
        assertEquals(0.0f, cy5.getHueDegree());
        assertEquals(1.0f, cy5.getBrightness());
    }
    
}
