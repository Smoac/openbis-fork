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

import java.awt.Color;

/**
 * A class for calculating a mixed color from a set of pure colors of different relative
 * intensities.
 * <p>
 * It uses an additive (physiological) color mixture, optionally weighted.
 * 
 * @author Bernd Rinn
 */
public class MixColors
{

    private static final int MAX_COMPONENT_VALUE = 255;
    
    private static final float MAX_COMPONENT_VALUE_FLOAT = MAX_COMPONENT_VALUE;

    private static int getMaxComponent(int r, int g, int b)
    {
        int cmax = (r > g) ? r : g;
        if (b > cmax)
        {
            cmax = b;
        }
        return cmax;
    }

    private static float getMaxComponent(float r, float g, float b)
    {
        float cmax = (r > g) ? r : g;
        if (b > cmax)
        {
            cmax = b;
        }
        return cmax;
    }

    // It is handy to assign to variables in this case, so suppress the warning.
    @SuppressWarnings("all")
    private static Color getColor(float red, float green, float blue)
    {
        final float max = getMaxComponent(red, green, blue);
        if (max > MAX_COMPONENT_VALUE_FLOAT)
        {
            // Scale down as the color exceeds the full intensity range.
            final float normFactor = MAX_COMPONENT_VALUE_FLOAT / max;
            red *= normFactor;
            green *= normFactor;
            blue *= normFactor;
        }
        return new Color(Math.round(red), Math.round(green), Math.round(blue));
    }

    // It is handy to assign to variables in this case, so suppress the warning.
    @SuppressWarnings("all")
    private static Color getColor(float red, float green, float blue, int brightness)
    {
        final float max = getMaxComponent(red, green, blue);
        // Normalize the brightness. 
        final float normFactor = Math.min(MAX_COMPONENT_VALUE_FLOAT, brightness) / max;
        red *= normFactor;
        green *= normFactor;
        blue *= normFactor;
        return new Color(Math.round(red), Math.round(green), Math.round(blue));
    }

    // It is handy to assign to variables in this case, so suppress the warning.
    @SuppressWarnings("all")
    private static Color getColor(int red, int green, int blue)
    {
        final int max = getMaxComponent(red, green, blue);
        if (max > MAX_COMPONENT_VALUE)
        {
            // Scale down as the color exceeds the full intensity range.
            final float normFactor = MAX_COMPONENT_VALUE_FLOAT / max;
            red = Math.round(red * normFactor);
            green = Math.round(green * normFactor);
            blue = Math.round(blue * normFactor);
        }
        return new Color(red, green, blue);
    }

    // It is handy to assign to variables in this case, so suppress the warning.
    @SuppressWarnings("all")
    private static Color getColor(int red, int green, int blue, int brightness)
    {
        final int max = getMaxComponent(red, green, blue);
        // Normalize the brightness. 
        final float normFactor = Math.min(MAX_COMPONENT_VALUE_FLOAT, brightness) / max;
        red = Math.round(red * normFactor);
        green = Math.round(green * normFactor);
        blue = Math.round(blue * normFactor);
        return new Color(red, green, blue);
    }

    /**
     * Calculates a mixed color from given <var>colors</var> quadratically additive.
     * 
     * @param colors The colors to mix.
     * @param intensities The intensities of each of the colors.
     * @return The mixed color.
     */
    public static Color calcMixedColorQuadratic(Color[] colors, float[] intensities)
    {
        assert colors.length == intensities.length;

        float red = 0f;
        float green = 0f;
        float blue = 0f;
        int redLin = 0;
        int greenLin = 0;
        int blueLin = 0;
        for (int i = 0; i < colors.length; ++i)
        {
            // Effective color components are proportional to the intensity.
            final float r = intensities[i] * colors[i].getRed();
            final float g = intensities[i] * colors[i].getGreen();
            final float b = intensities[i] * colors[i].getBlue();
            // We need the linear value as well for brightness normalization.
            redLin += r;
            greenLin += g;
            blueLin += b;
            // The weight is proportional to the brightness, normalization is done afterwards.
            final float weight = getMaxComponent(r, g, b);
            // Brighter color contribute stronger than darker colors.
            red += weight * r;
            green += weight * g;
            blue += weight * b;
        }
        return getColor(red, green, blue, getMaxComponent(redLin, greenLin, blueLin));
    }

    /**
     * Calculates a mixed color from given <var>colors</var> quadratically additive.
     * 
     * @param colors The colors to mix.
     * @return The mixed color.
     */
    public static Color calcMixedColorQuadratic(Color[] colors)
    {
        int red = 0;
        int green = 0;
        int blue = 0;
        int redLin = 0;
        int greenLin = 0;
        int blueLin = 0;
        for (int i = 0; i < colors.length; ++i)
        {
            final int r = colors[i].getRed();
            final int g = colors[i].getGreen();
            final int b = colors[i].getBlue();
            // We need the linear value as well for brightness normalization.
            redLin += r;
            greenLin += g;
            blueLin += b;
            // The weight is proportional to the brightness, normalization is done afterwards.
            final int weight = getMaxComponent(r, g, b);
            // Brighter color contribute stronger than darker colors.
            red += weight * r;
            green += weight * g;
            blue += weight * b;
        }
        return getColor(red, green, blue, getMaxComponent(redLin, greenLin, blueLin));
    }

    /**
     * Calculates a mixed color from given <var>colors</var> linearly additive.
     * 
     * @param colors The colors to mix.
     * @param intensities The intensities of each of the colors.
     * @return The mixed color.
     */
    public static Color calcMixedColorLinear(Color[] colors, float[] intensities)
    {
        assert colors.length == intensities.length;

        float red = 0;
        float green = 0;
        float blue = 0;
        for (int i = 0; i < colors.length; ++i)
        {
            // Effective color components are proportional to the intensity.
            final float r = intensities[i] * colors[i].getRed();
            final float g = intensities[i] * colors[i].getGreen();
            final float b = intensities[i] * colors[i].getBlue();
            // All colors contribute linearly.
            red += r;
            green += g;
            blue += b;
        }
        return getColor(red, green, blue);
    }

    /**
     * Calculates a mixed color from given <var>colors</var> linearly additive.
     * 
     * @param colors The colors to mix.
     * @return The mixed color.
     */
    public static Color calcMixedColorLinear(Color[] colors)
    {
        int red = 0;
        int green = 0;
        int blue = 0;
        for (int i = 0; i < colors.length; ++i)
        {
            final int r = colors[i].getRed();
            final int g = colors[i].getGreen();
            final int b = colors[i].getBlue();
            // All colors contribute linearly.
            red += r;
            green += g;
            blue += b;
        }
        return getColor(red, green, blue);
    }

}
