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

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author Tomasz Pylak
 */
public class HeatmapUtil
{
    // --- DTOs

    class Color
    {
        String hexColor;
    }

    class HeatmapScaleRange
    {
        String label;

        Color color;
    }

    // 0x67001F; 0xB2182B; 0xD6604D; 0xF4A582; 0xFDDBC7; 0xF7F7F7; 0xD1E5F0; 0x92C5DE; 0x4393C3;
    // 0x2166AC; 0x053061;

    // ---

    interface IHeatmapRenderer<T>
    {
        Color getColor(T value);

        List<HeatmapScaleRange> calculateScale();
    }

    class NumberHeatmapRenderer implements IHeatmapRenderer<Float>
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

    class StringHeatmapRenderer implements IHeatmapRenderer<String>
    {

        public StringHeatmapRenderer(List<String> values)
        {

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

    class ScaleDrawer
    {
        Widget draw(List<HeatmapScaleRange> ranges, int height)
        {
            // TODO 2010--, Tomasz Pylak:
            return null;
        }
    }

}
