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


/**
 * A class for calculating a mixed color from a set of pure colors of different relative
 * intensities.
 * <p>
 * It uses an additive (physiological) color mixture.
 * 
 * @author Bernd Rinn
 */
public class MixColors
{

    private static float[] calcWeights(HSBColor[] colors, float[] intensities)
    {
        final float[] effectiveIntensities = new float[intensities.length];
        float sum = 0.0f;
        for (int i = 0; i < intensities.length; ++i)
        {
            effectiveIntensities[i] = intensities[i] * colors[i].getBrightness();
            sum += effectiveIntensities[i];
        }
        final float[] weights = new float[intensities.length];
        for (int i = 0; i < intensities.length; ++i)
        {
            weights[i] = effectiveIntensities[i] / sum;
        }
        return weights;
    }

    /**
     * Calculates a mixed color from given pure <var>colors</var>.
     * 
     * @param colors The colors to mix.
     * @param intensities The intensities of each color. Has to be the same length as
     *            <var>colors</var>.
     * @return The mixed (pure) color.
     */
    public static PureHSBColor calcMixedColor(PureHSBColor[] colors, float[] intensities)
    {
        assert colors.length == intensities.length;

        final float[] weights = calcWeights(colors, intensities);

        float hue = 0.0f;
        float brightness = 0.0f;
        for (int i = 0; i < weights.length; ++i)
        {
            hue += weights[i] * colors[i].getHue();
            brightness = Math.max(brightness, intensities[i] * colors[i].getBrightness());
        }

        return new PureHSBColor(hue, brightness);
    }

    /**
     * Calculates a mixed color from given <var>colors</var>.
     * 
     * @param colors The colors to mix.
     * @param intensities The intensities of each color. Has to be the same length as
     *            <var>colors</var>.
     * @return The mixed color.
     */
    public static HSBColor calcMixedColor(HSBColor[] colors, float[] intensities)
    {
        assert colors.length == intensities.length;

        final float[] weights = calcWeights(colors, intensities);
        
        float hue = 0.0f;
        float saturation = 0.0f;
        float brightness = 0.0f;
        for (int i = 0; i < weights.length; ++i)
        {
            hue += weights[i] * colors[i].getHue();
            saturation += weights[i] * colors[i].getSaturation();
            brightness = Math.max(brightness, intensities[i] * colors[i].getBrightness());
        }

        return new HSBColor(hue, saturation, brightness);
    }

}
