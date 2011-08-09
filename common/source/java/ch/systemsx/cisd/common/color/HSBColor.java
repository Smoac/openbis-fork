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
 * A color object in the HSB (Hue, Saturation, Brightness) color space. 
 * 
 * @author Bernd Rinn
 */
public class HSBColor
{
    private final float hue;

    private final float saturation;

    private final float brightness;

    /**
     * Constructs an HSBColor from the given {@link java.awt.Color}. 
     */
    public static HSBColor createFromRGBColor(Color color)
    {
        float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return new HSBColor(hsv[0], hsv[1], hsv[2]);
    }

    /**
     * Constructor for a pure bright color (saturation = 1, brightness = 1).
     * 
     * @param hue The color's hue.
     */
    public HSBColor(float hue)
    {
        this(hue, 1.0f, 1.0f);
    }

    /**
     * Constructor for a pure color (saturation = 1).
     * 
     * @param hue The color's hue.
     * @param brightness The color's brightness.
     */
    public HSBColor(float hue, float brightness)
    {
        this(hue, 1.0f, brightness);
    }

    /**
     * Constructor for an arbitrary color.
     * 
     * @param hue The color's hue.
     * @param saturation The color's saturation.
     * @param brightness The color's brightness.
     */
    public HSBColor(float hue, float saturation, float brightness)
    {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    /**
     * Returns the color's hue.
     * 
     * @return The hue normalized to 1; in the range [0,1)
     */
    public float getHue()
    {
        return hue;
    }

    /**
     * Returns the color's hue.
     * 
     * @return The hue in degree; in the range [0,360)
     */
    public float getHueDegree()
    {
        return hue * 360f;
    }

    /**
     * Returns the color's saturation.
     * 
     * @return The saturation; in the range [0,1]
     */
    public float getSaturation()
    {
        return saturation;
    }

    /**
     * Returns the color's brightness.
     * 
     * @return The brightness; in the range [0,1]
     */
    public float getBrightness()
    {
        return brightness;
    }

    /**
     * Returns <code>true</code>, if the color is <i>pure</i>, that is: has a saturation of 1.0.
     */
    public boolean isPure()
    {
        return saturation == 1.0f;
    }

    /**
     * Creates a pure color with the same hue and brightness as this color.
     * 
     * @return The pure color.
     */
    public HSBColor createPureColor()
    {
        return new HSBColor(hue, brightness);
    }

    /**
     * Creates a pure bright color with the same hue as this color.
     * 
     * @return The pure bright color.
     */
    public HSBColor createPureBrightColor()
    {
        return new HSBColor(hue);
    }

    /**
     * Returns an {@link java.awt.Color} from this color.
     */
    public Color getColor()
    {
        return Color.getHSBColor(hue, saturation, brightness);
    }

    @Override
    public String toString()
    {
        return "HSBColor [hue=" + hue + ", saturation=" + saturation + ", brightness=" + brightness
                + "]";
    }
}
