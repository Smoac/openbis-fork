/*
 * Copyright 2013 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.image;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import ch.systemsx.cisd.common.image.IntensityRescaling.Levels;

/**
 * @author Jakub Straszewski
 */
@Test
public class IntensityRescalingTest
{

    public void testRegularCaseOfIntensityRescaling()
    {
        int[] histogramArray =
                new int[]
                    { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 };
        Levels result = IntensityRescaling.computeLevels(5, histogramArray);
        assertEquals(5, result.minLevel);
        assertEquals(histogramArray.length - 6, result.maxLevel);
    }

    @Test
    public void testBorderCaseOfIntensityRescaling()
    {
        Levels result = IntensityRescaling.computeLevels(14, new int[]
            { 1098, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 });
        assertEquals(0, result.minLevel);
        assertEquals(1, result.maxLevel);
    }

    @Test
    public void testBorderCaseOfIntensityRescalingOtherWay()
    {
        Levels result = IntensityRescaling.computeLevels(14, new int[]
            { 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 19876 });
        assertEquals(11, result.minLevel);
        assertEquals(12, result.maxLevel);
    }

}
