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
 * Test cases fir {@link MixColors}.
 * 
 * @author Bernd Rinn
 */
public class MixColorsTest
{
    @Test
    public void testMixTwoPureColorsFullIntensity()
    {
        PureHSBColor color = MixColors.calcMixedColor(new PureHSBColor[]
            { new PureHSBColor(0.0f, 1.0f), new PureHSBColor(0.5f, 1.0f) }, new float[]
            { 1f, 1f });
        assertEquals(90.0f, color.getHueDegree());
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixTwoPureColorsFullAndHalfIntensity()
    {
        PureHSBColor color = MixColors.calcMixedColor(new PureHSBColor[]
            { new PureHSBColor(0.0f, 1.0f), new PureHSBColor(0.5f, 1.0f) }, new float[]
            { 1f, 0.5f });
        assertEquals(60.0f, color.getHueDegree());
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixTwoPureColorsFullAndHalfBrightness()
    {
        PureHSBColor color = MixColors.calcMixedColor(new PureHSBColor[]
            { new PureHSBColor(0.0f, 1.0f), new PureHSBColor(0.5f, 0.5f) }, new float[]
            { 1f, 1f });
        assertEquals(60.0f, color.getHueDegree());
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixTwoPureColorsZeroAndFullIntensity()
    {
        PureHSBColor color = MixColors.calcMixedColor(new PureHSBColor[]
            { new PureHSBColor(0.0f, 1.0f), new PureHSBColor(2f / 3f, 1.0f) }, new float[]
            { 0f, 1f });
        assertEquals(240.0f, color.getHueDegree());
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixThreePureColorsFullQuarterQuarterIntensity()
    {
        PureHSBColor color =
                MixColors.calcMixedColor(new PureHSBColor[]
                    { new PureHSBColor(0.0f, 1.0f), new PureHSBColor(1f / 6f, 1.0f),
                            new PureHSBColor(2f / 3f, 1.0f) }, new float[]
                    { 1f, 0.25f, 0.25f });
        assertEquals(50.0f, color.getHueDegree(), 1e-5f);
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixThreePureColorsFullQuarterHalfIntensityHalfBrightness()
    {
        PureHSBColor color =
                MixColors.calcMixedColor(new PureHSBColor[]
                    { new PureHSBColor(0.0f, 1.0f), new PureHSBColor(1f / 6f, 0.5f),
                            new PureHSBColor(2f / 3f, 1.0f) }, new float[]
                    { 1f, 0.5f, 0.25f });
        assertEquals(50.0f, color.getHueDegree(), 1e-5f);
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixFourPureColorsFullQuarterHalfIntensityHalfBrightness()
    {
        PureHSBColor color =
                MixColors.calcMixedColor(new PureHSBColor[]
                    { new PureHSBColor(1f / 3f, 1.0f), new PureHSBColor(0f, 0.5f),
                            new PureHSBColor(2f / 3f, 0.5f), new PureHSBColor(15f / 18f, 0.25f) },
                        new float[]
                            { 0.9f, 0.1f, 0.1f, 0.1f });
        assertEquals(124.39f, color.getHueDegree(), 5e-4f);
        assertEquals(0.9f, color.getBrightness());
    }

    @Test
    public void testMixTwoColorsFullIntensityDifferentSaturation()
    {
        HSBColor color = MixColors.calcMixedColor(new HSBColor[]
            { new HSBColor(0.0f, 1.0f, 1.0f), new HSBColor(0.5f, 0.5f, 1.0f) }, new float[]
            { 1f, 1f });
        assertEquals(90.0f, color.getHueDegree());
        assertEquals(0.75f, color.getSaturation());
        assertEquals(1.0f, color.getBrightness());
    }

    @Test
    public void testMixFourColorsFullQuarterHalfIntensityHalfBrightness()
    {
        HSBColor color =
                MixColors.calcMixedColor(new HSBColor[]
                    { new HSBColor(1f / 3f, 1.0f), new HSBColor(0f, 0.5f, 0.5f),
                            new HSBColor(2f / 3f, 0.5f, 0.5f),
                            new HSBColor(15f / 18f, 0.25f, 0.25f) }, new float[]
                    { 1f, 0.1f, 0.1f, 0.1f });
        System.out.println(color);
        assertEquals(124f, color.getHueDegree());
        assertEquals(0.9389f, color.getSaturation(), 1e-4f);
        assertEquals(1f, color.getBrightness());
    }

}
