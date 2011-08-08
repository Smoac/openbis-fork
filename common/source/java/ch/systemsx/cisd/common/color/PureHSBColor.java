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
 * A pure color object in the HSB (Hue, Saturation, Brightness) color space.
 * <p>
 * A pure color is defined by having saturation = 1.0. 
 * 
 * @author Bernd Rinn
 */
public class PureHSBColor extends HSBColor
{
    /**
     * Constructs a PureHSBColor from the given {@link java.awt.Color}. 
     * 
     * @throw {@link IllegalArgumentException} if <var>color</var> is not pure.
     */
    public static PureHSBColor createFromRGBColor(Color color) throws IllegalArgumentException
    {
        float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        if (hsv[1] != 1.0f)
        {
            throw new IllegalArgumentException("Not a pure color: " + color);
        }
        return new PureHSBColor(hsv[0], hsv[2]);
    }

    public PureHSBColor(float hue, float brightness)
    {
        super(hue, 1.0f, brightness);
    }

    @Override
    public boolean isPure()
    {
        return true;
    }

}
