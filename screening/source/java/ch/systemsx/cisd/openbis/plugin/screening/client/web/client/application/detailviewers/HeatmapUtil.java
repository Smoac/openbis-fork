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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomasz Pylak
 */
public class HeatmapUtil
{
    // --- DTOs

    static class Color
    {
        private final String hexColor;

        public Color(String hexColor)
        {
            this.hexColor = hexColor;
        }

        public String getHexColor()
        {
            return hexColor;
        }
    }

    static class HeatmapScaleRange
    {
        String label;

        Color color;
    }

    // ---

    static interface IHeatmapRenderer<T>
    {
        Color getColor(T value);

        List<HeatmapScaleRange> calculateScale();
    }

    static class NumberHeatmapRenderer implements IHeatmapRenderer<Float>
    {

        public NumberHeatmapRenderer(float min, float max)
        {

        }

        public Color getColor(Float value)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public List<HeatmapScaleRange> calculateScale()
        {
            // TODO Auto-generated method stub
            return null;
        }

    }

    @SuppressWarnings("unused")
    static class StringHeatmapRenderer implements IHeatmapRenderer<String>
    {
        private static final List<String> DEFAULT_COLORS = Arrays.asList("#67001F", "#B2182B",
                "#D6604D", "#F4A582", "#FDDBC7", "#F7F7F7", "#D1E5F0", "#92C5DE", "#4393C3",
                "#2166AC", "#053061");

        private final Set<String> values;

        private final List<Color> scaleColors;

        public StringHeatmapRenderer(Set<String> values)
        {
            this(values, asColors(DEFAULT_COLORS));
        }

        private static List<Color> asColors(List<String> defaultColors)
        {
            List<Color> colors = new ArrayList<Color>();
            for (String color : DEFAULT_COLORS)
            {
                colors.add(new Color(color));
            }
            return colors;
        }

        public StringHeatmapRenderer(Set<String> values, List<Color> scaleColors)
        {
            this.values = values;
            this.scaleColors = scaleColors;
        }

        public Color getColor(String value)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public List<HeatmapScaleRange> calculateScale()
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    static class ScaleDrawer
    {
        Widget draw(List<HeatmapScaleRange> ranges, int height)
        {
            // TODO 2010--, Tomasz Pylak:
            return null;
        }
    }

}
