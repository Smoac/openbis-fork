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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.Color;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers.heatmaps.dto.HeatmapScaleElement;

public class HeatmapScaleFactoryDemo
{
    static public void showScalesDemo()
    {
        List<HeatmapScaleElement> elements = new ArrayList<HeatmapScaleElement>();
        for (String s : Arrays.asList("#67001F", "#B2182B", "#D6604D", "#F4A582", "#FDDBC7",
                "#F7F7F7", "#D1E5F0", "#92C5DE", "#4393C3", "#2166AC", "#053061"))
        {
            elements.add(new HeatmapScaleElement(s, new Color(s)));
        }
        Dialog dialog = new Dialog();
        Widget w1 = HeatmapScaleFactory.create(null, elements);
        Widget w2 = HeatmapScaleFactory.create("First", elements);
        NumberHeatmapRenderer numberHeatmapRenderer = new NumberHeatmapRenderer(1, 22);
        Widget w5 =
                HeatmapScaleFactory.create(numberHeatmapRenderer.tryGetFirstLabel(),
                        numberHeatmapRenderer.calculateScale());
        dialog.add(WidgetUtils.inRow(w1, w2, w5));
        dialog.show();
    }
}
