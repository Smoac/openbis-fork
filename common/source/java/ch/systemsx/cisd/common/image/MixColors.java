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

package ch.systemsx.cisd.common.image;

import java.awt.Color;
import java.awt.image.BufferedImage;

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
        assert colors.length == intensities.length : "number of colors and intensities do not match";

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
        assert colors.length == intensities.length : "number of colors and intensities do not match";

        float red = 0;
        float green = 0;
        float blue = 0;
        for (int i = 0; i < colors.length; ++i)
        {
            // Effective color components are proportional to the intensity.
            float intensity = intensities[i];
            Color color = colors[i];
            final float r = intensity * color.getRed();
            final float g = intensity * color.getGreen();
            final float b = intensity * color.getBlue();
            // All colors contribute linearly.
            red += r;
            green += g;
            blue += b;

            // This is an interesting alternative:
            // red = Math.max(red, r);
            // green = Math.max(green, g);
            // blue = Math.max(blue, b);

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

    private static interface ColorMergingAlgorithm
    {
        Color merge(Color[] colors, float[] intensities);
    }

    /**
     * Calculate a new image by mixing the given gray-scale </var>images</var>.
     * 
     * @param images The images to merge.
     * @param quadratic If <code>true</code>, use a quadratic (weighted) additive color mixture,
     *            otherwise use a linear (unweighted) additive color mixture.
     * @param saturationEnhancementFactor If > 0, perform a saturation enhancement step with the
     *            given factor.
     * @return Returns the mixed image.
     */
    public static BufferedImage mixImages(BufferedImage[] images, Color[] colors,
            boolean quadratic, float saturationEnhancementFactor)
    {
        assert colors.length == images.length : "number of colors and images do not match";

        ColorMergingAlgorithm mergeColorsAlgorithm =
                createColorMergingAlgorithm(quadratic, saturationEnhancementFactor);
        for (int i = 0; i < images.length; ++i)
        {
            if (images[i].getColorModel().getNumColorComponents() != 1
                    || images[i].getColorModel().getComponentSize(0) != 8)
            {
                throw new IllegalArgumentException(
                        "mixImages() only works on 8-bit gray scale images.");
            }
        }
        int width = images[0].getWidth();
        int height = images[0].getHeight();
        final BufferedImage mixed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        final float[] intensities = new float[images.length];
        for (int y = 0; y < images[0].getHeight(); ++y)
        {
            for (int x = 0; x < images[0].getWidth(); ++x)
            {
                for (int i = 0; i < images.length; ++i)
                {
                    intensities[i] = images[i].getRaster().getSampleFloat(x, y, 0) / 255f;
                }
                Color mixColor = mergeColorsAlgorithm.merge(colors, intensities);
                mixed.setRGB(x, y, mixColor.getRGB());
            }
        }
        return mixed;
    }

    private static Color saturate(float saturationEnhancementFactor, Color mixColor)
    {
        final float[] hsb =
                Color.RGBtoHSB(mixColor.getRed(), mixColor.getGreen(), mixColor.getBlue(), null);
        return Color
                .getHSBColor(hsb[0], Math.min(1f, saturationEnhancementFactor * hsb[1]), hsb[2]);
    }

    private static ColorMergingAlgorithm createColorMergingAlgorithm(boolean quadratic,
            final float saturationEnhancementFactor)
    {
        if (quadratic)
        {
            return new ColorMergingAlgorithm()
                {
                    public Color merge(Color[] colors, float[] intensities)
                    {
                        Color color = calcMixedColorQuadratic(colors, intensities);
                        if (saturationEnhancementFactor > 0)
                        {
                            color = saturate(saturationEnhancementFactor, color);
                        }
                        return color;
                    }
                };
        } else
        {
            return new ColorMergingAlgorithm()
                {
                    public Color merge(Color[] colors, float[] intensities)
                    {
                        Color color = calcMixedColorLinear(colors, intensities);
                        if (saturationEnhancementFactor > 0)
                        {
                            color = saturate(saturationEnhancementFactor, color);
                        }
                        return color;
                    }
                };
        }
    }

}
